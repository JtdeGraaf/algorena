package com.algorena.users.dto;

import org.jspecify.annotations.Nullable;

public record UserDTO(
        Long id,
        String username,
        @Nullable
        String name
) {
}

