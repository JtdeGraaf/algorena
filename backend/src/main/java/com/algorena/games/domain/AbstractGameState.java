package com.algorena.games.domain;

import com.algorena.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;

@Entity
@Table(name = "game_states")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
public abstract class AbstractGameState extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    @SuppressWarnings("NullAway.Init")
    private Match match;

    public void assignMatch(Match match) {
        this.match = match;
    }
}
