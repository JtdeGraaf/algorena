package com.algorena.users.application;

import com.algorena.users.data.UserRepository;
import com.algorena.users.domain.OAuth2UserInfo;
import com.algorena.users.domain.User;
import lombok.AllArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Custom OAuth2 user service that handles user registration and updates
 * for multiple OAuth2 providers (Google, GitHub, Discord).
 */
@Service
@AllArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MAX_USERNAME_LENGTH = 20;

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = delegate.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2UserInfo userInfo = OAuth2UserInfo.fromOAuth2Attributes(
                registrationId,
                oauth2User.getAttributes()
        );

        User user = findOrCreateUser(userInfo);
        Set<SimpleGrantedAuthority> authorities = buildAuthorities(user);

        return new DefaultOAuth2User(
                authorities,
                oauth2User.getAttributes(),
                userInfo.provider().getNameAttributeKey()
        );
    }

    /**
     * Processes OAuth2 user attributes and returns the User entity.
     * Used by OAuth2AuthenticationSuccessHandler to get the persisted user for JWT generation.
     *
     * @param registrationId the OAuth2 client registration ID
     * @param attributes     the raw OAuth2 user attributes
     * @return the persisted User entity
     */
    @Transactional
    public User processOAuth2User(String registrationId, java.util.Map<String, Object> attributes) {
        OAuth2UserInfo userInfo = OAuth2UserInfo.fromOAuth2Attributes(registrationId, attributes);
        return findOrCreateUser(userInfo);
    }

    /**
     * Finds an existing user or creates a new one from OAuth2 info.
     * User lookup priority:
     * 1. By provider + provider ID (exact match)
     * 2. By email (links accounts across providers)
     *
     * @param userInfo the extracted OAuth2 user information
     * @return the existing or newly created user
     */
    private User findOrCreateUser(OAuth2UserInfo userInfo) {
        // First try to find by provider and provider ID
        Optional<User> existingUser = findByProviderInfo(userInfo);

        // Fall back to finding by email (allows linking accounts)
        if (existingUser.isEmpty()) {
            existingUser = userRepository.findByEmail(userInfo.email());
        }

        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            user.updateFromOAuth2(userInfo);
            ensureUsernameExists(user, userInfo);
        } else {
            String username = generateUniqueUsername(userInfo);
            user = User.createFromOAuth2(userInfo, username);
        }

        return userRepository.save(user);
    }

    private Optional<User> findByProviderInfo(OAuth2UserInfo userInfo) {
        if (userInfo.providerId() == null) {
            return Optional.empty();
        }
        return userRepository.findByProviderAndProviderId(
                userInfo.provider(),
                userInfo.providerId()
        );
    }

    private void ensureUsernameExists(User user, OAuth2UserInfo userInfo) {
        if (!user.hasUsername()) {
            String username = generateUniqueUsername(userInfo);
            user.assignUsername(username);
        }
    }

    /**
     * Generates a unique username based on email or provider ID.
     * Appends numeric suffix if collision occurs.
     */
    private String generateUniqueUsername(OAuth2UserInfo userInfo) {
        String baseUsername = deriveBaseUsername(userInfo);
        baseUsername = normalizeUsername(baseUsername);

        String username = baseUsername;
        int suffix = 1;

        while (userRepository.findByUsername(username).isPresent()) {
            String suffixStr = String.valueOf(suffix);
            int maxBaseLength = MAX_USERNAME_LENGTH - suffixStr.length();
            username = truncate(baseUsername, maxBaseLength) + suffixStr;
            suffix++;
        }

        return username;
    }

    private String deriveBaseUsername(OAuth2UserInfo userInfo) {
        String email = userInfo.email();
        if (email != null && !email.isEmpty()) {
            return email.split("@")[0];
        }
        if (userInfo.providerId() != null) {
            return "user" + Math.abs(userInfo.providerId().hashCode());
        }
        return "user" + System.currentTimeMillis();
    }

    private String normalizeUsername(String username) {
        // Remove non-alphanumeric characters and lowercase
        String normalized = username.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();

        // Ensure minimum length
        if (normalized.length() < MIN_USERNAME_LENGTH) {
            normalized = "user" + normalized;
        }

        // Truncate to max length
        return truncate(normalized, MAX_USERNAME_LENGTH);
    }

    private String truncate(String value, int maxLength) {
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }

    private Set<SimpleGrantedAuthority> buildAuthorities(User user) {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getAuthority()))
                .collect(Collectors.toSet());
    }
}
