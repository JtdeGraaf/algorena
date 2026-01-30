package com.algorena.games.engine;

import org.jspecify.annotations.Nullable;

public interface GameEngine<S, M> {
    /**
     * Initializes a new game state.
     *
     * @return The initial state of the game.
     */
    S startNewGame();

    /**
     * Applies a move to the current state and returns the new state.
     *
     * @param state        The current game state.
     * @param move         The move to apply.
     * @param playerIndex  The index of the player making the move.
     * @return The updated game state.
     * @throws IllegalArgumentException if the move is illegal or it is not the player's turn.
     */
    S applyMove(S state, M move, int playerIndex);

    /**
     * Checks if the game has ended and returns the result.
     *
     * @param state The current game state.
     * @return The result of the game if it has ended, or null if it is still ongoing.
     */
    @Nullable
    GameResult checkResult(S state);
}
