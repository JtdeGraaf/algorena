package com.algorena.users.domain;

import jakarta.persistence.*;
import lombok.*;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider")
    @Enumerated(EnumType.STRING)
    private Provider provider;

    @Nullable
    @Column(name = "provider_id")
    private String providerId;

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

    @Column(name = "created")
    private LocalDateTime created;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

}
