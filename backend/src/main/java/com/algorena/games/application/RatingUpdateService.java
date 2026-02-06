package com.algorena.games.application;

import com.algorena.games.domain.Match;

/**
 * Service responsible for updating bot ELO ratings after matches complete.
 * Handles both global leaderboards and (future) private leaderboards.
 */
public interface RatingUpdateService {

    /**
     * Update bot ratings after a match completes.
     * This should be called whenever a match reaches FINISHED status.
     *
     * <p>Runs in a new transaction to ensure rating updates are independent
     * of match execution transaction.
     *
     * @param match the completed match
     */
    void updateRatingsAfterMatch(Match match);
}
