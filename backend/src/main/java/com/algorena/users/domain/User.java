package com.algorena.users.domain;

import com.algorena.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * User aggregate root representing an authenticated user in the system.
 * Follows DDD patterns - state changes are made through explicit business methods.
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PACKAGE)
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private OAuthIdentity oauthIdentity;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "username", unique = true, nullable = false, length = 20)
    private String username;

    @Nullable
    @Column(name = "name")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "language", nullable = false, length = 2)
    private Language language;

    @Nullable
    @Column(name = "image_url")
    private String imageUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    @Builder.Default
    private Set<Role> roles = new HashSet<>(Set.of(Role.USER));

    /**
     * Creates a new user from OAuth2 authentication.
     *
     * @param userInfo the extracted OAuth2 user information
     * @param username the unique username for this user
     * @return the new User entity (not yet persisted)
     */
    public static User createFromOAuth2(OAuth2UserInfo userInfo, String username) {
        return User.builder()
                .oauthIdentity(new OAuthIdentity(userInfo.provider(), userInfo.providerId()))
                .email(userInfo.email())
                .username(username)
                .name(userInfo.name())
                .language(Language.EN)
                .imageUrl(userInfo.imageUrl())
                .roles(new HashSet<>(Set.of(Role.USER)))
                .build();
    }

    /**
     * Updates user profile from OAuth2 login.
     * Called on each login to sync the latest data from the OAuth2 provider.
     *
     * @param userInfo the extracted OAuth2 user information
     */
    public void updateFromOAuth2(OAuth2UserInfo userInfo) {
        this.email = userInfo.email();
        if (userInfo.name() != null) {
            this.name = userInfo.name();
        }
        if (userInfo.imageUrl() != null) {
            this.imageUrl = userInfo.imageUrl();
        }
        // Link provider if not already set (for email-matched users)
        if (this.oauthIdentity == null) {
            this.oauthIdentity = new OAuthIdentity(userInfo.provider(), userInfo.providerId());
        }
    }

    /**
     * Updates the user's profile information.
     *
     * @param newUsername the new username (nullable, won't update if null)
     * @param newName     the new display name (nullable, won't update if null)
     */
    public void updateProfile(@Nullable String newUsername, @Nullable String newName) {
        if (newUsername != null) {
            this.username = newUsername;
        }
        if (newName != null) {
            this.name = newName;
        }
    }

    /**
     * Sets the username. Used during initial OAuth2 registration
     * when a generated username needs to be assigned.
     *
     * @param username the username to set
     */
    public void assignUsername(String username) {
        this.username = username;
    }

    /**
     * Checks if the user has a username assigned.
     *
     * @return true if username is set and not empty
     */
    public boolean hasUsername() {
        return this.username != null && !this.username.isEmpty();
    }
}
