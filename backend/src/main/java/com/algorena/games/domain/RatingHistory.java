package com.algorena.games.domain;

import com.algorena.bots.domain.Bot;
import com.algorena.common.config.SuppressedWarnings;
import com.algorena.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Historical record of ELO rating changes after a match.
 * Used to track rating trends over time.
 */
@Entity
@Table(name = "rating_history")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings(SuppressedWarnings.NULL_AWAY_INIT)
public class RatingHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bot_rating_id", nullable = false)
    private BotRating botRating;

    @ManyToOne
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @Column(name = "old_rating", nullable = false)
    private Integer oldRating;

    @Column(name = "new_rating", nullable = false)
    private Integer newRating;

    @Column(name = "rating_change", nullable = false)
    private Integer ratingChange;

    @Column(name = "opponent_rating", nullable = false)
    private Integer opponentRating;

    @ManyToOne
    @JoinColumn(name = "opponent_bot_id", nullable = false)
    private Bot opponentBot;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_result", nullable = false)
    private MatchResult matchResult;


    /**
     * Constructor that automatically calculates rating change.
     */
    public RatingHistory(
            BotRating botRating,
            Match match,
            Integer oldRating,
            Integer newRating,
            Integer opponentRating,
            Bot opponentBot,
            MatchResult matchResult
    ) {
        this.botRating = botRating;
        this.match = match;
        this.oldRating = oldRating;
        this.newRating = newRating;
        this.ratingChange = newRating - oldRating;
        this.opponentRating = opponentRating;
        this.opponentBot = opponentBot;
        this.matchResult = matchResult;
    }
}
