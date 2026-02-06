package com.algorena.games.application;

import com.algorena.games.domain.BotRating;
import org.springframework.stereotype.Service;

/**
 * Implementation of ELO rating calculation service.
 */
@Service
public class EloServiceImpl implements EloService {

    private static final int DEFAULT_RATING = 1200;
    private static final int K_FACTOR_NEW = 32;         // For bots with < 30 matches
    private static final int K_FACTOR_ESTABLISHED = 16; // For bots with >= 30 matches
    private static final int MATCHES_THRESHOLD = 30;

    @Override
    public EloUpdateResult calculateNewRatings(
        BotRating bot1Rating,
        BotRating bot2Rating,
        double bot1Score
    ) {
        int kFactor1 = getKFactor(bot1Rating);
        int kFactor2 = getKFactor(bot2Rating);

        double expected1 = calculateExpectedScore(
            bot1Rating.getEloRating(),
            bot2Rating.getEloRating()
        );
        double expected2 = 1.0 - expected1;

        double bot2Score = 1.0 - bot1Score;

        int newRating1 = bot1Rating.getEloRating() +
            (int) Math.round(kFactor1 * (bot1Score - expected1));
        int newRating2 = bot2Rating.getEloRating() +
            (int) Math.round(kFactor2 * (bot2Score - expected2));

        return new EloUpdateResult(newRating1, newRating2);
    }

    /**
     * Calculate expected score (probability of winning) for a player.
     * Uses standard ELO formula: 1 / (1 + 10^((opponentRating - yourRating) / 400))
     *
     * @param rating1 First player's rating
     * @param rating2 Second player's rating (opponent)
     * @return Expected score for first player (0.0 to 1.0)
     */
    private double calculateExpectedScore(int rating1, int rating2) {
        return 1.0 / (1.0 + Math.pow(10.0, (rating2 - rating1) / 400.0));
    }

    /**
     * Get K-factor based on bot's experience.
     * New bots (< 30 matches) have higher K-factor for faster rating adjustment.
     *
     * @param rating Bot's current rating
     * @return K-factor value
     */
    private int getKFactor(BotRating rating) {
        return rating.getMatchesPlayed() < MATCHES_THRESHOLD
            ? K_FACTOR_NEW
            : K_FACTOR_ESTABLISHED;
    }
}
