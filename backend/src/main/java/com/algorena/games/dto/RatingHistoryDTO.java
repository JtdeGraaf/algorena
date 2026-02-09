package com.algorena.games.dto;

import com.algorena.games.domain.MatchResult;

import java.time.LocalDateTime;

/**
 * Historical rating change entry for a bot.
 */
public record RatingHistoryDTO(
    Long matchId,
    Integer oldRating,
    Integer newRating,
    Integer ratingChange,
    Integer opponentRating,
    Long opponentBotId,
    String opponentBotName,
    MatchResult result,
    LocalDateTime playedAt
) {}
