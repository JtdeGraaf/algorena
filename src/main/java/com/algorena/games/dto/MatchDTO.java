package com.algorena.games.dto;

import com.algorena.bots.domain.Game;
import com.algorena.games.domain.MatchStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import static com.algorena.common.config.SuppressedWarnings.NULL_AWAY_INIT;

@SuppressWarnings(NULL_AWAY_INIT)
public record MatchDTO(
        UUID id,
        Game game,
        MatchStatus status,
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        List<MatchParticipantDTO> participants,
        @Nullable GameStateDTO state
) {
}
