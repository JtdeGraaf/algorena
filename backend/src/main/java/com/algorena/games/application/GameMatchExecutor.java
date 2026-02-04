package com.algorena.games.application;

import com.algorena.bots.domain.Game;
import com.algorena.games.domain.Match;
import com.algorena.games.engine.GameResult;
import org.jspecify.annotations.Nullable;

/**
 * Strategy interface for game-specific match execution logic.
 * Each game type (Chess, Connect4, etc.) has its own implementation that handles
 * the specifics of move execution, state management, and result checking.
 */
public interface GameMatchExecutor {

    /**
     * Returns the game type this executor handles.
     *
     * @return the Game enum value for this executor
     */
    Game getGameType();

    /**
     * Executes a single move in the match by calling the current player's bot endpoint.
     * This method handles:
     * <ul>
     *   <li>Determining whose turn it is</li>
     *   <li>Building and sending the move request to the bot</li>
     *   <li>Validating and applying the bot's response</li>
     *   <li>Recording the move in the database</li>
     * </ul>
     *
     * @param match the match to execute a move for
     * @return the game result if the game has ended, null if the game continues
     * @throws com.algorena.common.exception.BotCommunicationException if the bot fails to respond
     * @throws IllegalArgumentException if the bot returns an invalid move
     */
    @Nullable
    GameResult executeSingleMove(Match match);

    /**
     * Determines the current player index (0 or 1) based on the game state.
     *
     * @param match the match to check
     * @return 0 for player 1 (white/first), 1 for player 2 (black/second)
     */
    int getCurrentPlayerIndex(Match match);
}
