package com.algorena.users.domain;

import com.algorena.common.exception.InternalServerException;
import org.jspecify.annotations.Nullable;

public enum Provider {
    GOOGLE;

    public static Provider fromOAuth2Provider(@Nullable String provider) {
        return switch (provider) {
            case "google" -> GOOGLE;
            case null, default -> throw new InternalServerException("Unknown OAuth2 provider: " + provider);
        };
    }


}
