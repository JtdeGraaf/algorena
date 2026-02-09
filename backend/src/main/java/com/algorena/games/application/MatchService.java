package com.algorena.games.application;

import com.algorena.games.dto.CreateMatchRequest;
import com.algorena.games.dto.MatchDTO;
import com.algorena.games.dto.MatchMoveDTO;

import java.util.List;

public interface MatchService {
    /**
     * Creates a new match between two bots for a specific game.
     * The match will be executed asynchronously in the background.
     *
     * @param request The request containing bot IDs and game type.
     * @return The created Match DTO.
     */
    MatchDTO createMatch(CreateMatchRequest request);

    /**
     * Retrieves a match by its ID.
     *
     * @param matchId The ID of the match.
     * @return The Match DTO.
     */
    MatchDTO getMatch(Long matchId);

    /**
     * Retrieves the history of moves for a match.
     *
     * @param matchId The ID of the match.
     * @return List of MatchMoveDTO.
     */
    List<MatchMoveDTO> getMatchMoves(Long matchId);

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
     * @param matchId The ID of the match to abort.
     */
    void abortMatch(Long matchId);

    /**
     * Retrieves all legal moves for the current state of the match.
     *
     * @param matchId The ID of the match.
     * @return List of legal moves in UCI notation (e.g., "e2e4").
     */
    List<String> getLegalMoves(Long matchId);
}
