package com.algorena.users.domain;

import com.algorena.common.exception.InternalServerException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Supported OAuth2 authentication providers.
 * Each provider defines how to extract user attributes from OAuth2 responses.
 */
@AllArgsConstructor
public enum Provider {
    GOOGLE("sub", List.of("sub"), List.of("email"), List.of("name", "given_name"), List.of("picture")),
    GITHUB("id", List.of("id"), List.of("email"), List.of("name", "login"), List.of("avatar_url")),
    DISCORD("id", List.of("id"), List.of("email"), List.of("global_name", "username"), List.of("avatar"));

    @Getter
    private final String nameAttributeKey;
    private final List<String> idKeys;
    private final List<String> emailKeys;
    private final List<String> nameKeys;
    private final List<String> imageKeys;

    /**
     * Converts OAuth2 registration ID to Provider enum.
     *
     * @param registrationId the OAuth2 client registration ID
     * @return the corresponding Provider
     * @throws InternalServerException if provider is unknown
     */
    public static Provider fromRegistrationId(@Nullable String registrationId) {
        if (registrationId == null) {
            throw new InternalServerException("OAuth2 registration ID cannot be null");
        }
        return switch (registrationId.toLowerCase()) {
            case "google" -> GOOGLE;
            case "github" -> GITHUB;
            case "discord" -> DISCORD;
            default -> throw new InternalServerException("Unknown OAuth2 provider: " + registrationId);
        };
    }

    /**
     * Extracts the provider-specific user ID from OAuth2 attributes.
     *
     * @param attributes the OAuth2 user attributes
     * @return the provider ID, or null if not found
     */
    public @Nullable String extractProviderId(Map<String, Object> attributes) {
        return extractFirstMatch(attributes, idKeys);
    }

    /**
     * Extracts the user's email from OAuth2 attributes.
     *
     * @param attributes the OAuth2 user attributes
     * @return the email, or null if not found
     */
    public @Nullable String extractEmail(Map<String, Object> attributes) {
        return extractFirstMatch(attributes, emailKeys);
    }

    /**
     * Extracts the user's display name from OAuth2 attributes.
     *
     * @param attributes the OAuth2 user attributes
     * @return the name, or null if not found
     */
    public @Nullable String extractName(Map<String, Object> attributes) {
        return extractFirstMatch(attributes, nameKeys);
    }

    /**
     * Extracts the user's profile image URL from OAuth2 attributes.
     * For Discord, constructs the full avatar URL from the avatar hash.
     *
     * @param attributes the OAuth2 user attributes
     * @return the image URL, or null if not found
     */
    public @Nullable String extractImageUrl(Map<String, Object> attributes) {
        String value = extractFirstMatch(attributes, imageKeys);
        if (value == null) {
            return null;
        }
        // Discord returns an avatar hash, not a full URL
        if (this == DISCORD && !value.startsWith("http")) {
            String userId = extractProviderId(attributes);
            if (userId != null) {
                return "https://cdn.discordapp.com/avatars/" + userId + "/" + value + ".png";
            }
        }
        return value;
    }

    private @Nullable String extractFirstMatch(Map<String, Object> attributes, List<String> keys) {
        for (String key : keys) {
            Object value = attributes.get(key);
            if (value != null) {
                return String.valueOf(value);
            }
        }
        return null;
    }
}
