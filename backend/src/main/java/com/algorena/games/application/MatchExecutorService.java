package com.algorena.games.application;

import com.algorena.bots.domain.Game;
import com.algorena.common.exception.BotCommunicationException;
import com.algorena.games.data.MatchRepository;
import com.algorena.games.domain.Match;
import com.algorena.games.domain.MatchParticipant;
import com.algorena.games.domain.MatchStatus;
import com.algorena.games.engine.GameResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service responsible for orchestrating match execution asynchronously.
 * <p>
 * This service manages the game loop for matches, delegating game-specific logic
 * to {@link GameMatchExecutor} implementations. It handles:
 * <ul>
 *   <li>Asynchronous match execution via a thread pool</li>
 *   <li>Turn-by-turn game progression</li>
 *   <li>Error handling (bot timeouts, invalid moves)</li>
 *   <li>Match completion and scoring</li>
 * </ul>
 */
@Service
@Slf4j
public class MatchExecutorService {

    private final MatchRepository matchRepository;
    private final RatingUpdateService ratingUpdateService;
    private final Map<Game, GameMatchExecutor> executorsByGame;

    @Value("${algorena.match.max-moves-per-game:500}")
    private int maxMovesPerGame;

    public MatchExecutorService(
        MatchRepository matchRepository,
        RatingUpdateService ratingUpdateService,
        List<GameMatchExecutor> executors
    ) {
        this.matchRepository = matchRepository;
        this.ratingUpdateService = ratingUpdateService;
        this.executorsByGame = executors.stream()
                .collect(Collectors.toMap(GameMatchExecutor::getGameType, Function.identity()));
    }

    /**
     * Executes a match asynchronously in a background thread.
     * <p>
     * This method returns immediately and the match runs in the "matchExecutor" thread pool.
     * The match progresses through turns until completion, timeout, or error.
     *
     * @param matchId the ID of the match to execute
     * @return a CompletableFuture that completes when the match finishes
     */
    @Async("matchExecutor")
    public CompletableFuture<Void> executeMatch(Long matchId) {
        log.info("Starting execution of match {}", matchId);

        try {
            runMatchLoop(matchId);
        } catch (Exception e) {
            log.error("Unexpected error during match {} execution", matchId, e);
            abortMatchOnError(matchId);
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Main game loop that processes moves until the match ends.
     * Runs within a transaction and delegates to game-specific executors.
     */
    @Transactional
    protected void runMatchLoop(Long matchId) {
        Match match = fetchMatch(matchId);

        if (match.getStatus() != MatchStatus.IN_PROGRESS) {
            log.warn("Match {} is not in progress (status: {}), skipping execution", matchId, match.getStatus());
            return;
        }

        GameMatchExecutor executor = getExecutorForGame(match.getGame());
        int moveCount = 0;

        while (moveCount < maxMovesPerGame) {
            // Refresh match state to check for external changes (e.g., abort)
            match = fetchMatch(matchId);
            if (match.getStatus() != MatchStatus.IN_PROGRESS) {
                log.info("Match {} is no longer in progress, stopping execution", matchId);
                break;
            }

            try {
                GameResult result = executor.executeSingleMove(match);
                if (result != null) {
                    finishMatch(match, result);
                    break;
                }
                moveCount++;
            } catch (BotCommunicationException e) {
                handleBotCommunicationError(match, executor, e);
                break;
            } catch (IllegalArgumentException e) {
                handleInvalidMove(match, executor, e);
                break;
            }
        }

        if (moveCount >= maxMovesPerGame && match.getStatus() == MatchStatus.IN_PROGRESS) {
            log.warn("Match {} exceeded maximum moves ({}), ending as draw", matchId, maxMovesPerGame);
            endMatchAsDraw(match);
        }

        log.info("Match {} execution completed with status {}", matchId, match.getStatus());
    }

    private Match fetchMatch(Long matchId) {
        return matchRepository.findByIdWithParticipants(matchId)
                .orElseThrow(() -> new IllegalStateException("Match not found: " + matchId));
    }

    private GameMatchExecutor getExecutorForGame(Game game) {
        GameMatchExecutor executor = executorsByGame.get(game);
        if (executor == null) {
            throw new UnsupportedOperationException("No executor registered for game: " + game);
        }
        return executor;
    }

    /**
     * Handles bot communication failures (timeout, connection error).
     * The opponent wins by forfeit.
     */
    private void handleBotCommunicationError(Match match, GameMatchExecutor executor, BotCommunicationException e) {
        log.warn("Bot communication error in match {}: {} (reason: {})",
                match.getId(), e.getMessage(), e.getReason());

        int failedPlayerIndex = executor.getCurrentPlayerIndex(match);
        forfeitMatch(match, failedPlayerIndex, e.getReason());
    }

    /**
     * Handles invalid moves returned by bots.
     * The opponent wins by forfeit.
     */
    private void handleInvalidMove(Match match, GameMatchExecutor executor, Exception e) {
        log.warn("Invalid move in match {}: {}", match.getId(), e.getMessage());

        int failedPlayerIndex = executor.getCurrentPlayerIndex(match);
        forfeitMatch(match, failedPlayerIndex, "INVALID_MOVE");
    }

    private void forfeitMatch(Match match, int forfeitingPlayerIndex, String reason) {
        int winnerIndex = 1 - forfeitingPlayerIndex;
        match.forfeit(reason);

        for (MatchParticipant p : match.getParticipants()) {
            p.recordScore(p.getPlayerIndex() == winnerIndex ? 1.0 : 0.0);
        }

        matchRepository.save(match);

        // Update bot ELO ratings after forfeit
        ratingUpdateService.updateRatingsAfterMatch(match);
    }

    private void finishMatch(Match match, GameResult result) {
        match.finish();

        for (MatchParticipant p : match.getParticipants()) {
            Double score = result.scores().getScore(p.getPlayerIndex());
            p.recordScore(score);
        }

        matchRepository.save(match);

        // Update bot ELO ratings after match completes
        ratingUpdateService.updateRatingsAfterMatch(match);
    }

    private void endMatchAsDraw(Match match) {
        match.finish();

        for (MatchParticipant p : match.getParticipants()) {
            p.recordScore(0.5);
        }

        matchRepository.save(match);

        // Update bot ELO ratings after draw
        ratingUpdateService.updateRatingsAfterMatch(match);
    }

    @Transactional
    protected void abortMatchOnError(Long matchId) {
        matchRepository.findById(matchId).ifPresent(match -> {
            if (match.getStatus() == MatchStatus.IN_PROGRESS) {
                log.warn("Aborting match {} due to unexpected error", matchId);
                match.abort();
                matchRepository.save(match);
            }
        });
    }
}
