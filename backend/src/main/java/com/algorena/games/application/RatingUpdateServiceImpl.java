package com.algorena.games.application;

import com.algorena.bots.domain.Game;
import com.algorena.games.data.BotRatingRepository;
import com.algorena.games.data.RatingHistoryRepository;
import com.algorena.games.data.UserRankingRepository;
import com.algorena.games.domain.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of rating update service.
 */
@Service
@Slf4j
@AllArgsConstructor
public class RatingUpdateServiceImpl implements RatingUpdateService {

    private final BotRatingRepository botRatingRepository;
    private final RatingHistoryRepository ratingHistoryRepository;
    private final UserRankingRepository userRankingRepository;
    private final EloService eloService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateRatingsAfterMatch(Match match) {
        if (match.getStatus() != MatchStatus.FINISHED) {
            log.warn("Attempted to update ratings for non-finished match {}", match.getId());
            return;
        }

        List<MatchParticipant> participants = match.getParticipants();
        if (participants.size() != 2) {
            log.warn("Match {} does not have exactly 2 participants, skipping rating update", match.getId());
            return;
        }

        try {
            updateGlobalRatings(match, participants);
            refreshUserRankingsAsync();

            log.info("Successfully updated ratings for match {}", match.getId());
        } catch (Exception e) {
            log.error("Failed to update ratings for match {}", match.getId(), e);
            // Don't propagate exception - rating updates should not fail match completion
        }
    }

    private void updateGlobalRatings(Match match, List<MatchParticipant> participants) {
        MatchParticipant p1 = participants.get(0);
        MatchParticipant p2 = participants.get(1);

        // Get or create ratings for both bots (leaderboardId = null for global)
        BotRating rating1 = getOrCreateBotRating(p1.getBot(), match.getGame(), null);
        BotRating rating2 = getOrCreateBotRating(p2.getBot(), match.getGame(), null);

        // Calculate new ratings
        EloService.EloUpdateResult result = eloService.calculateNewRatings(
            rating1,
            rating2,
            p1.getScore()
        );

        // Save rating history before updating
        saveRatingHistory(rating1, match, result.newRating1(), rating2.getEloRating(), p2.getBot(), getMatchResult(p1.getScore()));
        saveRatingHistory(rating2, match, result.newRating2(), rating1.getEloRating(), p1.getBot(), getMatchResult(p2.getScore()));

        // Update ratings and stats
        rating1.updateRating(result.newRating1());
        rating1.recordMatchResult(getMatchResult(p1.getScore()));

        rating2.updateRating(result.newRating2());
        rating2.recordMatchResult(getMatchResult(p2.getScore()));

        botRatingRepository.saveAll(List.of(rating1, rating2));
    }

    private BotRating getOrCreateBotRating(com.algorena.bots.domain.Bot bot, Game game, @Nullable Long leaderboardId) {
        return botRatingRepository
            .findByBotAndGameAndLeaderboard(bot, game, leaderboardId)
            .orElseGet(() -> {
                BotRating newRating = new BotRating(bot, game, leaderboardId);
                return botRatingRepository.save(newRating);
            });
    }

    private void saveRatingHistory(
        BotRating botRating,
        Match match,
        int newRating,
        int opponentRating,
        com.algorena.bots.domain.Bot opponentBot,
        MatchResult matchResult
    ) {
        RatingHistory history = new RatingHistory(
            botRating,
            match,
            botRating.getEloRating(),
            newRating,
            opponentRating,
            opponentBot,
            matchResult
        );

        ratingHistoryRepository.save(history);
    }

    private MatchResult getMatchResult(double score) {
        if (score == 1.0) {
            return MatchResult.WIN;
        } else if (score == 0.5) {
            return MatchResult.DRAW;
        } else {
            return MatchResult.LOSS;
        }
    }

    /**
     * Refresh user rankings materialized view asynchronously.
     * This can be slow on large databases, so run it in the background.
     */
    @Async
    @Transactional
    protected void refreshUserRankingsAsync() {
        try {
            userRankingRepository.refresh();
            log.debug("User rankings materialized view refreshed");
        } catch (Exception e) {
            log.error("Failed to refresh user rankings materialized view", e);
        }
    }
}
