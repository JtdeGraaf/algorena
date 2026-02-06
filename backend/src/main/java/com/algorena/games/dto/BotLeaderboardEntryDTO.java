package com.algorena.games.dto;

import org.jspecify.annotations.Nullable;

/**
 * Leaderboard entry for a bot.
 */
public record BotLeaderboardEntryDTO(
    Long rank,
    Long botId,
    String botName,
    Long ownerId,
    String ownerName,
    @Nullable String ownerAvatarUrl,
    Integer eloRating,
    Integer matchesPlayed,
    Integer wins,
    Integer losses,
    Integer draws,
    Double winRate
) {}
