package com.algorena.users.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import org.jspecify.annotations.Nullable;

import static com.algorena.common.config.SuppressedWarnings.NULL_AWAY_INIT;

/**
 * Value object representing OAuth2 provider identity.
 * Encapsulates the provider type and provider-specific user ID.
 * Immutable once created - changes require creating a new instance.
 */
@Embeddable
@Immutable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuppressWarnings(NULL_AWAY_INIT)
public class OAuthIdentity {
    @Enumerated(EnumType.STRING)
    @Column(name = "provider")
    private Provider provider;

    @Nullable
    @Column(name = "provider_id")
    private String providerId;
}
