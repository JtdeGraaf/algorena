package com.algorena.games.application;

import com.algorena.bots.domain.Bot;
import com.algorena.bots.domain.Game;
import com.algorena.common.exception.BadRequestException;
import com.algorena.common.exception.DataNotFoundException;
import com.algorena.common.exception.ForbiddenException;
import com.algorena.games.chess.domain.ChessMatchMove;
import com.algorena.games.chess.data.ChessGameStateRepository;
import com.algorena.games.data.MatchMoveRepository;
import com.algorena.games.data.MatchRepository;
import com.algorena.games.domain.*;
import com.algorena.games.chess.domain.ChessGameState;
import com.algorena.games.engine.GameEngine;
import com.algorena.games.engine.GameEngineFactory;
import com.algorena.games.engine.GameResult;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final MatchMoveRepository matchMoveRepository;
    private final ChessGameStateRepository chessGameStateRepository;
    private final GameEngineFactory gameEngineFactory;

    /**
     * Creates a new match between two bots for a specific game.
     *
     * @param bot1 The first bot (Player 0 / White).
     * @param bot2 The second bot (Player 1 / Black).
     * @param game The game type (e.g. CHESS).
     * @return The created Match entity.
     * @throws BadRequestException if the bots do not play the specified game.
     */
    @Transactional
    public Match createMatch(Bot bot1, Bot bot2, Game game) {
        if (bot1.getGame() != game || bot2.getGame() != game) {
            throw new BadRequestException("Both bots must play " + game);
        }

        Match match = Match.builder()
                .game(game)
                .status(MatchStatus.IN_PROGRESS)
                .build();
        match.start();

        MatchParticipant p1 = MatchParticipant.builder()
                .match(match)
                .bot(bot1)
                .playerIndex(0) // White
                .build();

        MatchParticipant p2 = MatchParticipant.builder()
                .match(match)
                .bot(bot2)
                .playerIndex(1) // Black
                .build();

        match.addParticipant(p1);
        match.addParticipant(p2);

        matchRepository.save(match);

        // Initialize Game State
        initializeGameState(match);

        return match;
    }

    private void initializeGameState(Match match) {
        if (match.getGame() == Game.CHESS) {
            GameEngine<ChessGameState, String> engine = gameEngineFactory.getEngine(Game.CHESS);
            ChessGameState initialState = engine.startNewGame();
            initialState.assignMatch(match);
            chessGameStateRepository.save(initialState);
        } else {
            throw new UnsupportedOperationException("Game not supported: " + match.getGame());
        }
    }

    /**
     * Applies a move to an ongoing match.
     *
     * @param matchId      The UUID of the match.
     * @param bot          The bot making the move.
     * @param moveNotation The move in standard notation (e.g., "e2e4" for UCI Chess).
     * @throws DataNotFoundException if the match or bot is not found.
     * @throws BadRequestException   if the match is not in progress.
     * @throws ForbiddenException    if the bot is not a participant in the match.
     */
    @Transactional
    public void makeMove(UUID matchId, Bot bot, String moveNotation) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new DataNotFoundException("Match not found"));

        if (match.getStatus() != MatchStatus.IN_PROGRESS) {
            throw new BadRequestException("Match is not in progress");
        }

        MatchParticipant participant = match.getParticipants().stream()
                .filter(p -> p.getBot().getId().equals(bot.getId()))
                .findFirst()
                .orElseThrow(() -> new ForbiddenException("Bot is not a participant in this match"));

        if (match.getGame() == Game.CHESS) {
            handleChessMove(match, participant, moveNotation);
        } else {
            throw new UnsupportedOperationException("Game not supported");
        }
    }

    private void handleChessMove(Match match, MatchParticipant participant, String moveNotation) {
        ChessGameState state = chessGameStateRepository.findByMatchId(match.getId())
                .orElseThrow(() -> new DataNotFoundException("Game state not found"));

        GameEngine<ChessGameState, String> engine = gameEngineFactory.getEngine(Game.CHESS);

        // Apply Move (Engine validates turn and rules)
        ChessGameState newState = engine.applyMove(state, moveNotation, participant.getPlayerIndex());

        // Update State
        state.updateBoardState(newState.getFen(), newState.getHalfMoveClock(), newState.getFullMoveNumber());
        // PGN handling omitted for brevity, can append moveNotation
        
        chessGameStateRepository.save(state);

        // Record Move
        // Parse move details
        Move move = new Move(moveNotation, Side.WHITE);
        String promotion = move.getPromotion().equals(com.github.bhlangonijr.chesslib.Piece.NONE) ? null : move.getPromotion().value();

        ChessMatchMove matchMove = ChessMatchMove.builder()
                .match(match)
                .playerIndex(participant.getPlayerIndex())
                .moveNotation(moveNotation)
                .fromSquare(move.getFrom().value())
                .toSquare(move.getTo().value())
                .promotionPiece(promotion)
                .build();
        matchMoveRepository.save(matchMove);

        // Check Result
        GameResult result = engine.checkResult(state);
        if (result != null) {
            finishMatch(match, result);
        }
    }

    private void finishMatch(Match match, GameResult result) {
        match.finish();

        for (MatchParticipant p : match.getParticipants()) {
             // Default to 0.0 if the player is not in the scores map (e.g. forfeit/loss)
             Double score = result.scores().getScore(p.getPlayerIndex());
             p.recordScore(score);
        }
        
        matchRepository.save(match);
    }
    
    /**
     * Retrieves a match by its ID.
     *
     * @param matchId The UUID of the match.
     * @return The Match entity.
     * @throws DataNotFoundException if the match is not found.
     */
    public Match getMatch(UUID matchId) {
         return matchRepository.findById(matchId)
                .orElseThrow(() -> new DataNotFoundException("Match not found"));
    }
}
