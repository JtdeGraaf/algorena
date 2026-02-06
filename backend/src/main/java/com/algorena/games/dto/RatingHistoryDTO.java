package com.algorena.games.dto;

import com.algorena.games.domain.MatchResult;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Historical rating change entry for a bot.
 */
public record RatingHistoryDTO(
    UUID matchId,
    Integer oldRating,
    Integer newRating,
    Integer ratingChange,
    Integer opponentRating,
    Long opponentBotId,
    String opponentBotName,
    MatchResult result,
    LocalDateTime playedAt
) {}
