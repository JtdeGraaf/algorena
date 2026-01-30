package com.algorena.bots.dto;

import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

public record CreateApiKeyRequest(
        @Nullable
        @Size(max = 50, message = "Name must not exceed 50 characters")
        String name
) {
}

