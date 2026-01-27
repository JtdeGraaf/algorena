package com.algorena.security.security;


import com.algorena.users.domain.Language;

public record SimpleUserPrincipal(
        String id,
        String email,
        String name,
        Language language
) {
}
