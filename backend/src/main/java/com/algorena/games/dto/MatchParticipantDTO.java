package com.algorena.games.dto;

import static com.algorena.common.config.SuppressedWarnings.NULL_AWAY_INIT;

@SuppressWarnings(NULL_AWAY_INIT)
public record MatchParticipantDTO(
    Long id,
    Long botId,
    String botName,
    int playerIndex,
    Double score
) {}
