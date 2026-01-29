package com.algorena.games.engine;

import java.util.Collections;
import java.util.Map;

/**
 * Wrapper for match scores to avoid ambiguous Map<Integer, Double> usage.
 * Maps player index to their score (e.g. 1.0 for win, 0.5 for draw, 0.0 for loss).
 */
public record MatchScores(Map<Integer, Double> values) {
    
    public MatchScores {
        values = Collections.unmodifiableMap(values);
    }

    public double getScore(int playerIndex) {
        return values.getOrDefault(playerIndex, 0.0);
    }
    
    public static MatchScores of(Map<Integer, Double> values) {
        return new MatchScores(values);
    }
}
