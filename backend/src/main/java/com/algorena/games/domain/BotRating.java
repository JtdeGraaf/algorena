package com.algorena.games.domain;

import com.algorena.bots.domain.Bot;
import com.algorena.bots.domain.Game;
import com.algorena.common.config.SuppressedWarnings;
import com.algorena.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "bot_ratings")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings(SuppressedWarnings.NULL_AWAY_INIT)
public class BotRating extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bot_id", nullable = false)
    private Bot bot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Game game;

    @Column(name = "leaderboard_id")
    private @Nullable Long leaderboardId; // NULL = global, future: private leaderboard ID

    @Column(name = "elo_rating", nullable = false)
    private Integer eloRating = 1200;

    @Column(name = "matches_played", nullable = false)
    private Integer matchesPlayed = 0;

    @Column(nullable = false)
    private Integer wins = 0;

    @Column(nullable = false)
    private Integer losses = 0;

    @Column(nullable = false)
    private Integer draws = 0;

    public BotRating(Bot bot, Game game, @Nullable Long leaderboardId) {
        this.bot = bot;
        this.game = game;
        this.leaderboardId = leaderboardId;
        this.eloRating = 1200;
        this.matchesPlayed = 0;
        this.wins = 0;
        this.losses = 0;
        this.draws = 0;
    }

    /**
     * Updates ELO rating after a match.
     *
     * @param newRating the new ELO rating to set
     */
    public void updateRating(int newRating) {
        this.eloRating = newRating;
    }

    /**
     * Increments matches played and updates win/loss/draw stats based on result.
     */
    public void recordMatchResult(MatchResult result) {
        this.matchesPlayed++;
        switch (result) {
            case WIN -> this.wins++;
            case LOSS -> this.losses++;
            case DRAW -> this.draws++;
        }
    }

    /**
     * Calculated win rate (0.0 to 1.0).
     */
    public Double getWinRate() {
        return matchesPlayed > 0 ? (double) wins / matchesPlayed : 0.0;
    }
}
