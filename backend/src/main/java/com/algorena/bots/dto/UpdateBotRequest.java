package com.algorena.bots.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

public record UpdateBotRequest(
        @NotBlank(message = "Bot name is required")
        @Size(max = 50, message = "Bot name must not exceed 50 characters")
        String name,

        @Nullable
        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        @Nullable
        Boolean active
) {
}

