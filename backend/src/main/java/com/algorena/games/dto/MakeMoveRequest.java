package com.algorena.games.dto;

public record MakeMoveRequest(
        Long botId,
        String move
) {
}