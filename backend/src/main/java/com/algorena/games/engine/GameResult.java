package com.algorena.games.engine;

import java.util.Map;

/**
 * Represents the final outcome of a completed game.
 */
public record GameResult(MatchScores scores) {

    public static GameResult winner(int winnerIndex, int loserIndex) {
        return new GameResult(MatchScores.of(Map.of(winnerIndex, 1.0, loserIndex, 0.0)));
    }

    public static GameResult draw() {
        return new GameResult(MatchScores.of(Map.of(0, 0.5, 1, 0.5)));
    }
}
