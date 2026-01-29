package com.algorena.games.application;

import com.algorena.games.dto.CreateMatchRequest;
import com.algorena.games.dto.MakeMoveRequest;
import com.algorena.games.dto.MatchDTO;

import java.util.UUID;

public interface MatchService {
    /**
     * Creates a new match between two bots for a specific game.
     *
     * @param request The request containing bot IDs and game type.
     * @return The created Match DTO.
     */
    MatchDTO createMatch(CreateMatchRequest request);

    /**
     * Applies a move to an ongoing match.
     *
     * @param matchId The UUID of the match.
     * @param request The request containing bot ID and move notation.
     */
    void makeMove(UUID matchId, MakeMoveRequest request);

    /**
     * Retrieves a match by its ID.
     *
     * @param matchId The UUID of the match.
     * @return The Match DTO.
     */
    MatchDTO getMatch(UUID matchId);
}
