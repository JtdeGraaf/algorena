package com.algorena.games.connect4.engine;

import com.algorena.games.connect4.domain.Connect4GameState;
import com.algorena.games.engine.GameEngine;
import com.algorena.games.engine.GameResult;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class Connect4GameEngine implements GameEngine<Connect4GameState, Integer> {

    public static final int ROWS = 6;
    public static final int COLS = 7;
    private static final char EMPTY = '0';
    private static final char PLAYER_1 = '1';
    private static final char PLAYER_2 = '2';

    @Override
    public Connect4GameState startNewGame() {
        return new Connect4GameState(
                String.join("", Collections.nCopies(ROWS * COLS, String.valueOf(EMPTY))),
                null
        );
    }

    @Override
    public Connect4GameState applyMove(Connect4GameState state, Integer columnIndex, int playerIndex) {
        if (columnIndex < 0 || columnIndex >= COLS) {
            throw new IllegalArgumentException("Column index out of bounds: " + columnIndex);
        }

        // Determine player char
        char playerChar = (playerIndex == 0) ? PLAYER_1 : PLAYER_2;
        
        // Check if it's this player's turn
        // Count pieces to determine whose turn it is. P1 moves on even counts (0, 2, ...), P2 on odd.
        long movesPlayed = state.getBoard().chars().filter(c -> c != EMPTY).count();
        if (movesPlayed % 2 != playerIndex) {
             throw new IllegalArgumentException("It is not player " + playerIndex + "'s turn.");
        }

        StringBuilder board = new StringBuilder(state.getBoard());
        
        // Find the lowest empty row in the column
        int placedRow = -1;
        for (int r = 0; r < ROWS; r++) {
            int index = getIndex(r, columnIndex);
            if (board.charAt(index) == EMPTY) {
                board.setCharAt(index, playerChar);
                placedRow = r;
                break;
            }
        }

        if (placedRow == -1) {
            throw new IllegalArgumentException("Column " + columnIndex + " is full");
        }

        return new Connect4GameState(board.toString(), columnIndex);
    }

    @Override
    public @Nullable GameResult checkResult(Connect4GameState state) {
        String board = state.getBoard();

        // Check for 4 in a row
        // Check horizontal
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c <= COLS - 4; c++) {
                char p = board.charAt(getIndex(r, c));
                if (p != EMPTY &&
                    p == board.charAt(getIndex(r, c + 1)) &&
                    p == board.charAt(getIndex(r, c + 2)) &&
                    p == board.charAt(getIndex(r, c + 3))) {
                    return createWinResult(p);
                }
            }
        }

        // Check vertical
        for (int c = 0; c < COLS; c++) {
            for (int r = 0; r <= ROWS - 4; r++) {
                char p = board.charAt(getIndex(r, c));
                if (p != EMPTY &&
                    p == board.charAt(getIndex(r + 1, c)) &&
                    p == board.charAt(getIndex(r + 2, c)) &&
                    p == board.charAt(getIndex(r + 3, c))) {
                    return createWinResult(p);
                }
            }
        }

        // Check diagonal (bottom-left to top-right)
        for (int r = 0; r <= ROWS - 4; r++) {
            for (int c = 0; c <= COLS - 4; c++) {
                char p = board.charAt(getIndex(r, c));
                if (p != EMPTY &&
                    p == board.charAt(getIndex(r + 1, c + 1)) &&
                    p == board.charAt(getIndex(r + 2, c + 2)) &&
                    p == board.charAt(getIndex(r + 3, c + 3))) {
                    return createWinResult(p);
                }
            }
        }

        // Check diagonal (top-left to bottom-right)
        // Note: r starts at 3 because we go DOWN-right
        for (int r = 3; r < ROWS; r++) {
            for (int c = 0; c <= COLS - 4; c++) {
                char p = board.charAt(getIndex(r, c));
                if (p != EMPTY &&
                    p == board.charAt(getIndex(r - 1, c + 1)) &&
                    p == board.charAt(getIndex(r - 2, c + 2)) &&
                    p == board.charAt(getIndex(r - 3, c + 3))) {
                    return createWinResult(p);
                }
            }
        }

        // Check for draw (full board)
        if (board.indexOf(String.valueOf(EMPTY)) == -1) {
            return GameResult.draw();
        }

        return null;
    }

    public List<Integer> getLegalMoves(Connect4GameState state) {
        List<Integer> legalMoves = new ArrayList<>();
        String board = state.getBoard();
        
        // A move is legal if the top row of the column is empty
        for (int c = 0; c < COLS; c++) {
            // Check the top row (row 5)
            if (board.charAt(getIndex(ROWS - 1, c)) == EMPTY) {
                legalMoves.add(c);
            }
        }
        return legalMoves;
    }

    private int getIndex(int row, int col) {
        return row * COLS + col;
    }

    private GameResult createWinResult(char playerChar) {
        if (playerChar == PLAYER_1) {
            return GameResult.winner(0, 1);
        } else {
            return GameResult.winner(1, 0);
        }
    }
}
