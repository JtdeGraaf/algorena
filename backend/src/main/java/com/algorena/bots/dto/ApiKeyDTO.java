package com.algorena.bots.dto;

import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

public record ApiKeyDTO(
        Long id,
        @Nullable String name,
        String keyPrefix,
        @Nullable LocalDateTime lastUsed,
        @Nullable LocalDateTime expiresAt,
        boolean revoked,
        LocalDateTime created
) {
}


