package com.algorena.games.dto;

import com.algorena.bots.domain.Game;

public record CreateMatchRequest(
        Long bot1Id,
        Long bot2Id,
        Game game
) {
}
