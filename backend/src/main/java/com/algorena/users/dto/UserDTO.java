package com.algorena.users.dto;

import com.algorena.users.domain.Provider;
import org.jspecify.annotations.Nullable;

public record UserDTO(
        Long id,
        String username,
        @Nullable
        String name,
        Provider provider,
        @Nullable
        String providerId
) {
}

