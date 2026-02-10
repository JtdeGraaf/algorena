package com.algorena.games.mapper;

import com.algorena.games.chess.domain.ChessGameState;
import com.algorena.games.chess.domain.ChessMatchMove;
import com.algorena.games.connect4.domain.Connect4GameState;
import com.algorena.games.connect4.domain.Connect4MatchMove;
import com.algorena.games.domain.AbstractGameState;
import com.algorena.games.domain.AbstractMatchMove;
import com.algorena.games.domain.Match;
import com.algorena.games.domain.MatchParticipant;
import com.algorena.games.dto.*;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting Match-related entities to DTOs.
 * Pure mapping only - no repository access.
 */
@Component
public class MatchMapper {

    /**
     * Converts a Match entity to MatchDTO.
     *
     * @param match the match entity
     * @param gameState the current game state (can be null)
     */
    public MatchDTO toDTO(Match match, @Nullable AbstractGameState gameState) {
        GameStateDTO stateDTO = toGameStateDTO(gameState);

        return new MatchDTO(
                match.getId(),
                match.getGame(),
                match.getStatus(),
                match.getStartedAt(),
                match.getFinishedAt(),
                match.getParticipants().stream()
                        .map(this::toParticipantDTO)
                        .toList(),
                stateDTO
        );
    }

    /**
     * Converts a MatchParticipant entity to MatchParticipantDTO.
     */
    public MatchParticipantDTO toParticipantDTO(MatchParticipant participant) {
        return new MatchParticipantDTO(
                participant.getId(),
                participant.getBot().getId(),
                participant.getBot().getName(),
                participant.getPlayerIndex(),
                participant.getScore()
        );
    }

    /**
     * Converts an AbstractMatchMove entity to MatchMoveDTO.
     * Handles game-specific move types (Chess, Connect4).
     */
    public MatchMoveDTO toMoveDTO(AbstractMatchMove move) {
        String from = null;
        String to = null;
        String promotion = null;

        if (move instanceof ChessMatchMove chessMove) {
            from = chessMove.getFromSquare();
            to = chessMove.getToSquare();
            promotion = chessMove.getPromotionPiece();
        } else if (move instanceof Connect4MatchMove connect4Move) {
            to = String.valueOf(connect4Move.getColumnIndex());
        }

        return new MatchMoveDTO(
                move.getId(),
                move.getPlayerIndex(),
                move.getMoveNotation(),
                move.getCreated(),
                from,
                to,
                promotion
        );
    }

    /**
     * Converts a game state entity to the appropriate DTO.
     */
    private @Nullable GameStateDTO toGameStateDTO(@Nullable AbstractGameState gameState) {
        if (gameState == null) {
            return null;
        }

        if (gameState instanceof ChessGameState chessState) {
            return new ChessGameStateDTO(
                    chessState.getFen(),
                    chessState.getPgn(),
                    chessState.getHalfMoveClock(),
                    chessState.getFullMoveNumber()
            );
        } else if (gameState instanceof Connect4GameState connect4State) {
            return new Connect4GameStateDTO(
                    connect4State.getBoard(),
                    connect4State.getLastMoveColumn()
            );
        }

        return null;
    }
}
