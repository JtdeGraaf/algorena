package com.algorena.users.domain;

import com.algorena.common.exception.InternalServerException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OAuth2UserInfoTest {

    @Test
    void fromOAuth2Attributes_extractsGoogleAttributes() {
        Map<String, Object> attrs = Map.of(
                "sub", "12345",
                "email", "user@gmail.com",
                "name", "John Doe",
                "picture", "https://example.com/photo.jpg"
        );

        OAuth2UserInfo info = OAuth2UserInfo.fromOAuth2Attributes("google", attrs);

        assertThat(info.provider()).isEqualTo(Provider.GOOGLE);
        assertThat(info.providerId()).isEqualTo("12345");
        assertThat(info.email()).isEqualTo("user@gmail.com");
        assertThat(info.name()).isEqualTo("John Doe");
        assertThat(info.imageUrl()).isEqualTo("https://example.com/photo.jpg");
    }

    @Test
    void fromOAuth2Attributes_throwsWhenEmailMissing() {
        Map<String, Object> attrs = Map.of("sub", "12345");

        assertThatThrownBy(() -> OAuth2UserInfo.fromOAuth2Attributes("google", attrs))
                .isInstanceOf(InternalServerException.class)
                .hasMessageContaining("Email is required");
    }

    @Test
    void fromOAuth2Attributes_throwsWhenAttributesNull() {
        assertThatThrownBy(() -> OAuth2UserInfo.fromOAuth2Attributes("google", null))
                .isInstanceOf(InternalServerException.class);
    }
}
