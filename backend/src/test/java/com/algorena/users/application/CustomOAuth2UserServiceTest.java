package com.algorena.users.application;

import com.algorena.users.data.UserRepository;
import com.algorena.users.domain.Provider;
import com.algorena.users.domain.User;
import com.algorena.users.domain.UserTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private CustomOAuth2UserService service;

    @BeforeEach
    void setUp() {
        service = new CustomOAuth2UserService(userRepository);
    }

    @Test
    void processOAuth2User_createsNewUser_whenNoExistingUser() {
        // Given
        Map<String, Object> attrs = Map.of(
                "sub", "google-123",
                "email", "john@example.com",
                "name", "John Doe",
                "picture", "https://example.com/photo.jpg"
        );
        when(userRepository.findByProviderAndProviderId(any(), any())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        User result = service.processOAuth2User("google", attrs);

        // Then
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getProvider()).isEqualTo(Provider.GOOGLE);
        assertThat(result.getProviderId()).isEqualTo("google-123");
        assertThat(result.getUsername()).isEqualTo("john");
    }

    @Test
    void processOAuth2User_findsExistingUser_byProviderAndProviderId() {
        // Given
        Map<String, Object> attrs = Map.of(
                "sub", "google-123",
                "email", "john@example.com",
                "name", "John Updated"
        );
        User existingUser = UserTestFactory.create("john", "john@example.com", Provider.GOOGLE, "google-123");

        when(userRepository.findByProviderAndProviderId(Provider.GOOGLE, "google-123"))
                .thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        User result = service.processOAuth2User("google", attrs);

        // Then
        assertThat(result.getName()).isEqualTo("John Updated");
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void processOAuth2User_linksAccount_whenFoundByEmail() {
        // Given
        Map<String, Object> attrs = Map.of(
                "id", 12345,
                "email", "john@example.com",
                "login", "johndoe"
        );
        User existingUser = UserTestFactory.create("john", "john@example.com", Provider.GOOGLE, "google-123");

        when(userRepository.findByProviderAndProviderId(Provider.GITHUB, "12345"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        User result = service.processOAuth2User("github", attrs);

        // Then - existing user is returned and updated
        assertThat(result.getUsername()).isEqualTo("john");
        assertThat(result.getProvider()).isEqualTo(Provider.GOOGLE); // Original provider preserved
    }

    @Test
    void processOAuth2User_generatesUniqueUsername_whenCollision() {
        // Given
        Map<String, Object> attrs = Map.of(
                "sub", "google-456",
                "email", "john@example.com"
        );
        User existingJohn = UserTestFactory.create("john", "other@example.com", Provider.GOOGLE, "google-123");

        when(userRepository.findByProviderAndProviderId(any(), any())).thenReturn(Optional.empty());
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(existingJohn));
        when(userRepository.findByUsername("john1")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        User result = service.processOAuth2User("google", attrs);

        // Then
        assertThat(result.getUsername()).isEqualTo("john1");
    }

    @Test
    void processOAuth2User_normalizesUsername() {
        // Given
        Map<String, Object> attrs = Map.of(
                "sub", "google-123",
                "email", "John.Doe+test@example.com"
        );
        when(userRepository.findByProviderAndProviderId(any(), any())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        User result = service.processOAuth2User("google", attrs);

        // Then - special chars removed, lowercased
        assertThat(result.getUsername()).isEqualTo("johndoetest");
    }

    @Test
    void processOAuth2User_truncatesLongUsername() {
        // Given
        Map<String, Object> attrs = Map.of(
                "sub", "google-123",
                "email", "verylongemailaddressname@example.com"
        );
        when(userRepository.findByProviderAndProviderId(any(), any())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        User result = service.processOAuth2User("google", attrs);

        // Then - truncated to 20 chars
        assertThat(result.getUsername()).hasSize(20);
        assertThat(result.getUsername()).isEqualTo("verylongemailaddress");
    }
}
