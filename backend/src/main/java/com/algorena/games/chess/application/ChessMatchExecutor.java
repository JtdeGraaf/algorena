package com.algorena.games.chess.application;

import com.algorena.bots.domain.Bot;
import com.algorena.bots.domain.Game;
import com.algorena.games.application.BotClientService;
import com.algorena.games.application.GameMatchExecutor;
import com.algorena.games.chess.data.ChessGameStateRepository;
import com.algorena.games.chess.domain.ChessGameState;
import com.algorena.games.chess.domain.ChessMatchMove;
import com.algorena.games.chess.engine.ChessGameEngine;
import com.algorena.games.data.MatchMoveRepository;
import com.algorena.games.domain.Match;
import com.algorena.games.domain.MatchParticipant;
import com.algorena.games.dto.BotMoveRequest;
import com.algorena.games.dto.BotMoveResponse;
import com.algorena.games.dto.ChessGameStateDTO;
import com.algorena.games.engine.GameResult;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Chess-specific implementation of the match executor.
 * Handles all chess game logic including FEN parsing, move validation,
 * and interaction with the chesslib library.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ChessMatchExecutor implements GameMatchExecutor {

    private final ChessGameStateRepository gameStateRepository;
    private final MatchMoveRepository matchMoveRepository;
    private final ChessGameEngine gameEngine;
    private final BotClientService botClientService;

    @Override
    public Game getGameType() {
        return Game.CHESS;
    }

    @Override
    public @Nullable GameResult executeSingleMove(Match match) {
        ChessGameState state = gameStateRepository.findByMatchId(match.getId())
                .orElseThrow(() -> new IllegalStateException("Chess game state not found for match: " + match.getId()));

        // Check if game is already over
        GameResult result = gameEngine.checkResult(state);
        if (result != null) {
            return result;
        }

        // Determine current player and get their bot
        int currentPlayerIndex = getCurrentPlayerIndex(state);
        MatchParticipant currentParticipant = getParticipantByIndex(match, currentPlayerIndex);
        Bot bot = currentParticipant.getBot();

        // Get legal moves for the request
        List<String> legalMoves = gameEngine.getLegalMoves(state);

        // Build and send request to bot
        BotMoveRequest request = buildMoveRequest(match, state, currentPlayerIndex, legalMoves);
        BotMoveResponse response = botClientService.requestMove(bot, request);
        String moveNotation = response.move().trim();

        log.debug("Chess bot {} responded with move: {}", bot.getName(), moveNotation);

        // Validate and apply move
        ChessGameState newState = gameEngine.applyMove(state, moveNotation, currentPlayerIndex);

        // Update persisted state
        state.updateBoardState(newState.getFen(), newState.getHalfMoveClock(), newState.getFullMoveNumber());
        gameStateRepository.save(state);

        // Record the move
        recordMove(match, currentPlayerIndex, moveNotation);

        // Check for game end after move
        return gameEngine.checkResult(state);
    }

    @Override
    public int getCurrentPlayerIndex(Match match) {
        ChessGameState state = gameStateRepository.findByMatchId(match.getId())
                .orElseThrow(() -> new IllegalStateException("Chess game state not found for match: " + match.getId()));
        return getCurrentPlayerIndex(state);
    }

    private int getCurrentPlayerIndex(ChessGameState state) {
        // FEN format: "pieces activeColor castling enPassant halfmove fullmove"
        // activeColor is 'w' for white (player 0) or 'b' for black (player 1)
        String fen = state.getFen();
        String[] parts = fen.split(" ");
        return parts[1].equals("w") ? 0 : 1;
    }

    private MatchParticipant getParticipantByIndex(Match match, int playerIndex) {
        return match.getParticipants().stream()
                .filter(p -> p.getPlayerIndex() == playerIndex)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Participant not found for index " + playerIndex));
    }

    private BotMoveRequest buildMoveRequest(Match match, ChessGameState state, int playerIndex, List<String> legalMoves) {
        ChessGameStateDTO stateDTO = new ChessGameStateDTO(
                state.getFen(),
                state.getPgn(),
                state.getHalfMoveClock(),
                state.getFullMoveNumber()
        );
        return new BotMoveRequest(match.getId(), Game.CHESS, playerIndex, stateDTO, legalMoves);
    }

    private void recordMove(Match match, int playerIndex, String moveNotation) {
        Move move = new Move(moveNotation, Side.WHITE);
        String promotion = move.getPromotion().equals(Piece.NONE) ? null : move.getPromotion().value();

        ChessMatchMove matchMove = ChessMatchMove.builder()
                .match(match)
                .playerIndex(playerIndex)
                .moveNotation(moveNotation)
                .fromSquare(move.getFrom().value())
                .toSquare(move.getTo().value())
                .promotionPiece(promotion)
                .build();

        matchMoveRepository.save(matchMove);
    }
}
