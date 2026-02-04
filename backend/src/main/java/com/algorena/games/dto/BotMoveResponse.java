package com.algorena.games.dto;

/**
 * Response payload from bot endpoints containing the move to make.
 */
public record BotMoveResponse(
        String move
) {
}
