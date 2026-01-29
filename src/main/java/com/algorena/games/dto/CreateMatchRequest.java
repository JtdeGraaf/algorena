package com.algorena.games.dto;

import com.algorena.bots.domain.Game;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import static com.algorena.common.config.SuppressedWarnings.NULL_AWAY_INIT;

@SuppressWarnings(NULL_AWAY_INIT)
public record CreateMatchRequest(
    Long bot1Id,
    Long bot2Id,
    Game game
) {}
