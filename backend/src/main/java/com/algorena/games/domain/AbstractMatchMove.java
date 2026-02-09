package com.algorena.games.domain;

import com.algorena.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.jspecify.annotations.Nullable;

import static com.algorena.common.config.SuppressedWarnings.NULL_AWAY_INIT;

@Entity
@Table(name = "match_moves")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@NoArgsConstructor
public abstract class AbstractMatchMove extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Nullable
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    @SuppressWarnings(NULL_AWAY_INIT)
    private Match match;

    @Column(name = "player_index", nullable = false)
    private int playerIndex;

    @Column(name = "move_notation", nullable = false, columnDefinition = "TEXT")
    @SuppressWarnings(NULL_AWAY_INIT)
    private String moveNotation;

    protected AbstractMatchMove(@Nullable Long id, Match match, int playerIndex, String moveNotation) {
        this.id = id;
        this.match = match;
        this.playerIndex = playerIndex;
        this.moveNotation = moveNotation;
    }
}
