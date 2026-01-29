package com.algorena.games.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import static com.algorena.common.config.SuppressedWarnings.NULL_AWAY_INIT;

@SuppressWarnings(NULL_AWAY_INIT)
public record MakeMoveRequest(
    Long botId,
    String move
) {}
