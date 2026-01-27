package com.algorena.users.application;


import com.algorena.common.exception.InternalServerException;
import com.algorena.users.data.UserRepository;
import com.algorena.users.domain.Language;
import com.algorena.users.domain.Provider;
import com.algorena.users.domain.Role;
import com.algorena.users.domain.User;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    private UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = delegate.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        User user = this.processOAuth2User(registrationId, oauth2User.getAttributes());
        Set<SimpleGrantedAuthority> authorities = buildAuthorities(user);
        return new DefaultOAuth2User(
                authorities,
                oauth2User.getAttributes(),
                determineNameAttributeKey(oauth2User.getAttributes())
        );
    }

    // TODO: Refactor this method to reduce complexity obviously, AI slopped this one
    @Transactional
    public User processOAuth2User(String registrationId, @Nullable Map<String, Object> attributes) {
        if (attributes == null) throw new InternalServerException("Could not find attributes for OAuth2 user");
        // Determine provider user id (sub, id, user_id)
        String providerId = null;
        if (attributes.containsKey("sub")) providerId = String.valueOf(attributes.get("sub"));
        else if (attributes.containsKey("id")) providerId = String.valueOf(attributes.get("id"));
        else if (attributes.containsKey("user_id")) providerId = String.valueOf(attributes.get("user_id"));
        String email = attributes.containsKey("email") ? (String) attributes.get("email") : null;
        if (email == null) throw new InternalServerException("Could not find email for OAuth2 user");
        Optional<User> optUser = Optional.empty();
        if (registrationId != null && providerId != null) {
            optUser = userRepository.findByProviderAndProviderId(Provider.fromOAuth2Provider(registrationId), providerId);
        }
        if (optUser.isEmpty()) {
            optUser = userRepository.findByEmail(email);
        }
        User user;
        if (optUser.isPresent()) {
            user = optUser.get();
            user.setEmail(email);
            if (attributes.containsKey("name")) user.setName((String) attributes.get("name"));
            if (attributes.containsKey("picture")) user.setImageUrl((String) attributes.get("picture"));
            if (user.getProvider() == null && registrationId != null)
                user.setProvider(Provider.fromOAuth2Provider(registrationId));
            if (user.getProviderId() == null && providerId != null) user.setProviderId(providerId);
            // Ensure existing user has a username
            if (user.getUsername() == null || user.getUsername().isEmpty()) {
                user.setUsername(generateUniqueUsername(email, providerId));
            }
        } else {
            String name = null;
            if (attributes.containsKey("name")) name = (String) attributes.get("name");
            else if (attributes.containsKey("given_name")) name = (String) attributes.get("given_name");
            else if (attributes.containsKey("first_name")) name = (String) attributes.get("first_name");
            String imageUrl = null;
            if (attributes.containsKey("picture")) imageUrl = (String) attributes.get("picture");
            else if (attributes.containsKey("avatar")) imageUrl = (String) attributes.get("avatar");
            // Generate unique username for new user
            String username = generateUniqueUsername(email, providerId);
            user = User.builder()
                    .provider(Provider.fromOAuth2Provider(registrationId))
                    .providerId(providerId)
                    .email(email)
                    .username(username)
                    .name(name)
                    .language(Language.EN)
                    .imageUrl(imageUrl)
                    .roles(Set.of(Role.USER))
                    .created(LocalDateTime.now())
                    .lastUpdated(LocalDateTime.now())
                    .build();
        }
        return userRepository.save(user);
    }

    /**
     * Generates a unique username based on email or provider ID.
     * Format: derived from email prefix or user{hash}.
     * If collision occurs, appends incremental suffix.
     *
     * @param email      the user's email (may be null)
     * @param providerId the OAuth provider ID (may be null)
     * @return a unique username
     */
    private String generateUniqueUsername(@Nullable String email, @Nullable String providerId) {
        String baseUsername;
        if (email != null && !email.isEmpty()) {
            // Extract username part from email (before @)
            String emailPrefix = email.split("@")[0];
            // Clean up: remove special characters, limit length
            baseUsername = emailPrefix.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        } else if (providerId != null) {
            // Use provider ID as base
            baseUsername = "user" + Math.abs(providerId.hashCode());
        } else {
            // Fallback to timestamp-based username
            baseUsername = "user" + System.currentTimeMillis();
        }
        // Ensure minimum length and maximum length
        if (baseUsername.length() < 3) {
            baseUsername = "user" + baseUsername;
        }
        if (baseUsername.length() > 20) {
            baseUsername = baseUsername.substring(0, 20);
        }
        // Check if username exists and append suffix if needed
        String username = baseUsername;
        int suffix = 1;
        while (userRepository.findByUsername(username).isPresent()) {
            String suffixStr = String.valueOf(suffix);
            int maxBaseLength = 20 - suffixStr.length();
            username = baseUsername.substring(0, Math.min(baseUsername.length(), maxBaseLength)) + suffixStr;
            suffix++;
        }
        return username;
    }

    /**
     * Builds Spring Security authorities from user roles.
     *
     * @param user the user entity
     * @return set of granted authorities
     */
    private Set<SimpleGrantedAuthority> buildAuthorities(User user) {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getAuthority()))
                .collect(Collectors.toSet());
    }


    /**
     * Determines the OAuth2 attribute key for the user's name.
     *
     * @param attributes OAuth2 user attributes
     * @return the key to use for name attribute
     */
    private String determineNameAttributeKey(Map<String, Object> attributes) {
        if (attributes.containsKey("sub")) return "sub";
        if (attributes.containsKey("id")) return "id";
        return "name";
    }
}
