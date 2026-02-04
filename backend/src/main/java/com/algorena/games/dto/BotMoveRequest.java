package com.algorena.games.dto;

import com.algorena.bots.domain.Game;

import java.util.List;
import java.util.UUID;

/**
 * Request payload sent to bot endpoints to request a move.
 */
public record BotMoveRequest(
        UUID matchId,
        Game game,
        int playerIndex,
        GameStateDTO gameState,
        List<String> legalMoves
) {
}
