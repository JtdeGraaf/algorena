package com.algorena.users.domain;

import com.algorena.common.exception.InternalServerException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProviderTest {

    @Test
    void fromRegistrationId_mapsProvidersCorrectly() {
        assertThat(Provider.fromRegistrationId("google")).isEqualTo(Provider.GOOGLE);
        assertThat(Provider.fromRegistrationId("github")).isEqualTo(Provider.GITHUB);
        assertThat(Provider.fromRegistrationId("discord")).isEqualTo(Provider.DISCORD);
        assertThat(Provider.fromRegistrationId("GOOGLE")).isEqualTo(Provider.GOOGLE);
    }

    @Test
    void fromRegistrationId_throwsOnUnknownProvider() {
        assertThatThrownBy(() -> Provider.fromRegistrationId("unknown"))
                .isInstanceOf(InternalServerException.class);
    }

    @Test
    void google_extractsAttributesCorrectly() {
        Map<String, Object> attrs = Map.of(
                "sub", "12345",
                "email", "user@gmail.com",
                "name", "John Doe",
                "picture", "https://example.com/photo.jpg"
        );

        assertThat(Provider.GOOGLE.extractProviderId(attrs)).isEqualTo("12345");
        assertThat(Provider.GOOGLE.extractEmail(attrs)).isEqualTo("user@gmail.com");
        assertThat(Provider.GOOGLE.extractName(attrs)).isEqualTo("John Doe");
        assertThat(Provider.GOOGLE.extractImageUrl(attrs)).isEqualTo("https://example.com/photo.jpg");
        assertThat(Provider.GOOGLE.getNameAttributeKey()).isEqualTo("sub");
    }

    @Test
    void github_extractsAttributesCorrectly() {
        Map<String, Object> attrs = Map.of(
                "id", 67890,
                "email", "user@github.com",
                "login", "johndoe",
                "avatar_url", "https://avatars.githubusercontent.com/u/67890"
        );

        assertThat(Provider.GITHUB.extractProviderId(attrs)).isEqualTo("67890");
        assertThat(Provider.GITHUB.extractEmail(attrs)).isEqualTo("user@github.com");
        assertThat(Provider.GITHUB.extractName(attrs)).isEqualTo("johndoe");
        assertThat(Provider.GITHUB.extractImageUrl(attrs)).isEqualTo("https://avatars.githubusercontent.com/u/67890");
        assertThat(Provider.GITHUB.getNameAttributeKey()).isEqualTo("id");
    }

    @Test
    void discord_constructsAvatarUrl() {
        Map<String, Object> attrs = Map.of(
                "id", "123456789",
                "email", "user@discord.com",
                "username", "johndoe",
                "avatar", "abc123hash"
        );

        assertThat(Provider.DISCORD.extractImageUrl(attrs))
                .isEqualTo("https://cdn.discordapp.com/avatars/123456789/abc123hash.png");
    }

    @Test
    void extractName_fallsBackToAlternativeKeys() {
        // Google falls back to given_name if name is missing
        Map<String, Object> attrs = Map.of(
                "sub", "12345",
                "given_name", "John"
        );
        assertThat(Provider.GOOGLE.extractName(attrs)).isEqualTo("John");
    }
}
