package com.algorena.bots.dto;

import com.algorena.bots.domain.Game;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

public record BotDTO(
        Long id,
        String name,
        @Nullable String description,
        Game game,
        boolean active,
        LocalDateTime created,
        LocalDateTime lastUpdated
) {
}
