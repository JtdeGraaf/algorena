package com.algorena.games.dto;

import org.jspecify.annotations.Nullable;

/**
 * Leaderboard entry for a user.
 * User ranking is based on aggregate performance of all their bots.
 */
public record UserLeaderboardEntryDTO(
    Long rank,
    Long userId,
    String username,
    @Nullable String avatarUrl,
    Integer bestBotElo,
    Integer avgBotElo,
    Integer totalBots,
    Integer totalMatches,
    Integer totalWins,
    Integer totalLosses,
    Integer totalDraws,
    Double winRate
) {}
