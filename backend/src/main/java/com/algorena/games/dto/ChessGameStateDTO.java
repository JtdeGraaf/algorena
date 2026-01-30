package com.algorena.games.dto;

import static com.algorena.common.config.SuppressedWarnings.NULL_AWAY_INIT;

@SuppressWarnings(NULL_AWAY_INIT)
public record ChessGameStateDTO(
        String fen,
        String pgn,
        int halfMoveClock,
        int fullMoveNumber
) implements GameStateDTO {
}
