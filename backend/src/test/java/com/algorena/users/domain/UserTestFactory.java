package com.algorena.users.domain;

import java.util.HashSet;
import java.util.Set;

/**
 * Factory for creating User entities in tests.
 * Package-private access to User's builder is available since we're in the same package.
 */
public final class UserTestFactory {

    private UserTestFactory() {
        // utility class
    }

    /**
     * Creates a test user with the given username and email.
     *
     * @param username   the username
     * @param email      the email
     * @param provider   the OAuth provider
     * @param providerId the provider-specific user ID
     * @return a new User entity (not yet persisted)
     */
    public static User create(
            String username,
            String email,
            Provider provider,
            String providerId
    ) {
        return User.builder()
                .username(username)
                .email(email)
                .language(Language.EN)
                .provider(provider)
                .providerId(providerId)
                .roles(new HashSet<>(Set.of(Role.USER)))
                .build();
    }
}
