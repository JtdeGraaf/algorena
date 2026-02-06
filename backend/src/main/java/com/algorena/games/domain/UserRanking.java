package com.algorena.games.domain;

import com.algorena.bots.domain.Game;
import com.algorena.common.config.SuppressedWarnings;
import com.algorena.users.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import org.jspecify.annotations.Nullable;

import java.io.Serializable;

/**
 * Read-only view of user rankings computed from bot ratings.
 * Backed by a materialized view that is refreshed after matches complete.
 */
@Entity
@Table(name = "user_rankings")
@Immutable
@Getter
@IdClass(UserRanking.UserRankingId.class)
@SuppressWarnings(SuppressedWarnings.NULL_AWAY_INIT)
public class UserRanking {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "game", nullable = false)
    private Game game;

    @Column(name = "best_bot_elo", nullable = false)
    private Integer bestBotElo;

    @Column(name = "avg_bot_elo", nullable = false)
    private Integer avgBotElo;

    @Column(name = "total_bots", nullable = false)
    private Integer totalBots;

    @Column(name = "total_matches", nullable = false)
    private Integer totalMatches;

    @Column(name = "total_wins", nullable = false)
    private Integer totalWins;

    @Column(name = "total_losses", nullable = false)
    private Integer totalLosses;

    @Column(name = "total_draws", nullable = false)
    private Integer totalDraws;

    @Column(name = "win_rate", nullable = false)
    private Double winRate;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private @Nullable User user;

    /**
     * Composite key class for UserRanking entity.
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class UserRankingId implements Serializable {
        private Long userId;
        private Game game;
    }
}
