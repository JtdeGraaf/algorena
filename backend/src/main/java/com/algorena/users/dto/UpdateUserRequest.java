package com.algorena.users.dto;

import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

public record UpdateUserRequest(
        @Nullable
        @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
        String username,

        @Nullable
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name
) {
}
