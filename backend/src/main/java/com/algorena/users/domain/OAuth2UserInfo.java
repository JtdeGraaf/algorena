package com.algorena.users.domain;

import com.algorena.common.exception.InternalServerException;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * Value object representing extracted OAuth2 user information.
 * Encapsulates the user data obtained from various OAuth2 providers.
 *
 * @param provider   the OAuth2 provider
 * @param providerId the provider-specific user ID
 * @param email      the user's email (required)
 * @param name       the user's display name (optional)
 * @param imageUrl   the user's profile image URL (optional)
 */
public record OAuth2UserInfo(
        Provider provider,
        @Nullable String providerId,
        String email,
        @Nullable String name,
        @Nullable String imageUrl
) {

    /**
     * Creates an OAuth2UserInfo from raw OAuth2 attributes.
     *
     * @param registrationId the OAuth2 client registration ID
     * @param attributes     the raw OAuth2 user attributes
     * @return the extracted user info
     * @throws InternalServerException if attributes are null or email is missing
     */
    public static OAuth2UserInfo fromOAuth2Attributes(
            String registrationId,
            @Nullable Map<String, Object> attributes
    ) {
        if (attributes == null) {
            throw new InternalServerException("OAuth2 attributes cannot be null");
        }

        Provider provider = Provider.fromRegistrationId(registrationId);
        String email = provider.extractEmail(attributes);

        if (email == null || email.isBlank()) {
            throw new InternalServerException("Email is required for OAuth2 authentication");
        }

        return new OAuth2UserInfo(
                provider,
                provider.extractProviderId(attributes),
                email,
                provider.extractName(attributes),
                provider.extractImageUrl(attributes)
        );
    }
}
