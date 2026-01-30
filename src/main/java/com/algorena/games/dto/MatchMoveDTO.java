package com.algorena.games.dto;

import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.algorena.common.config.SuppressedWarnings.NULL_AWAY_INIT;

@SuppressWarnings(NULL_AWAY_INIT)
public record MatchMoveDTO(
        @Nullable UUID id,
        int playerIndex,
        String moveNotation,
        LocalDateTime created,
        @Nullable String fromSquare,
        @Nullable String toSquare,
        @Nullable String promotionPiece
) {}
