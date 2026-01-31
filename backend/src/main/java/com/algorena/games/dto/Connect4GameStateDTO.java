package com.algorena.games.dto;

import org.jspecify.annotations.Nullable;

public record Connect4GameStateDTO(
        String board,
        @Nullable Integer lastMoveColumn
) implements GameStateDTO {
}
