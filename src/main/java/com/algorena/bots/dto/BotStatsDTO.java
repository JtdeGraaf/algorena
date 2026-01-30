package com.algorena.bots.dto;

public record BotStatsDTO(
        Long botId,
        String botName,
        int totalMatches,
        int wins,
        int losses,
        int draws,
        double winRate
) {
}

