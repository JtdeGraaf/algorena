package com.algorena.games.application;

import com.algorena.games.domain.BotRating;

/**
 * Service for calculating ELO rating changes.
 * Uses standard ELO algorithm with variable K-factor based on experience.
 */
public interface EloService {

    /**
     * Calculate new ELO ratings after a match.
     *
     * @param bot1Rating First bot's rating
     * @param bot2Rating Second bot's rating
     * @param bot1Score Bot1's score (1.0 = win, 0.5 = draw, 0.0 = loss)
     * @return New ratings for both bots
     */
    EloUpdateResult calculateNewRatings(
        BotRating bot1Rating,
        BotRating bot2Rating,
        double bot1Score
    );

    /**
     * Result of ELO calculation for both players.
     */
    record EloUpdateResult(int newRating1, int newRating2) {}
}
