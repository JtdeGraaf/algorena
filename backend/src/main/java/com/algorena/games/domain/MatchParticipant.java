package com.algorena.games.domain;

import com.algorena.bots.domain.Bot;
import com.algorena.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "match_participants")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchParticipant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bot_id", nullable = false)
    private Bot bot;

    @Column(name = "player_index", nullable = false)
    private int playerIndex; // 0 for White/Player1, 1 for Black/Player2

    @Column(name = "score")
    private Double score; // 1.0 for win, 0.5 for draw, 0.0 for loss

    public void recordScore(Double score) {
        this.score = score;
    }
}
