package com.algorena.games.application;

import com.algorena.games.dto.CreateMatchRequest;
import com.algorena.games.dto.MakeMoveRequest;
import com.algorena.games.dto.MatchDTO;
import com.algorena.games.dto.MatchMoveDTO;

import java.util.List;
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

    /**
     * Retrieves the history of moves for a match.
     *
     * @param matchId The UUID of the match.
     * @return List of MatchMoveDTO.
     */
    List<MatchMoveDTO> getMatchMoves(UUID matchId);

    /**
     * Retrieves matches played by a specific bot.
     *
     * @param botId The ID of the bot.
     * @return List of MatchDTO.
     */
    List<MatchDTO> getMatchesForBot(Long botId);

    /**
     * Retrieves all matches for the current user's bots.
     *
     * @return List of MatchDTO.
     */
    List<MatchDTO> getCurrentUserMatches();

    /**
     * Retrieves recent matches across the platform.
     *
     * @param limit Maximum number of matches to return.
     * @return List of MatchDTO.
     */
    List<MatchDTO> getRecentMatches(int limit);

    /**
     * Aborts an in-progress match.
     *
     * @param matchId The UUID of the match to abort.
     */
    void abortMatch(UUID matchId);

    /**
     * Retrieves all legal moves for the current state of the match.
     * Currently only supported for Chess.
     *
     * @param matchId The UUID of the match.
     * @return List of legal moves in UCI notation (e.g., "e2e4").
     */
    List<String> getLegalMoves(UUID matchId);
}
