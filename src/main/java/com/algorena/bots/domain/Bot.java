package com.algorena.bots.domain;

import com.algorena.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "bots")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bot extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Nullable
    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "game", nullable = false)
    private Game game;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public void updateDetails(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Bot name cannot be blank");
        }
        if (name.length() > 50) {
            throw new IllegalArgumentException("Bot name cannot exceed 50 characters");
        }
        if (description != null && description.length() > 500) {
            throw new IllegalArgumentException("Description cannot exceed 500 characters");
        }
        this.name = name;
        this.description = description;
    }
}

