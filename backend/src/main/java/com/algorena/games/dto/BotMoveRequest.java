package com.algorena.games.dto;

import com.algorena.bots.domain.Game;

import java.util.List;

/**
 * Request payload sent to bot endpoints to request a move.
 */
public record BotMoveRequest(
        Long matchId,
        Game game,
        int playerIndex,
        GameStateDTO gameState,
        List<String> legalMoves
) {
}
