package com.algorena.games.connect4.engine;

import com.algorena.games.connect4.domain.Connect4GameState;
import com.algorena.games.engine.GameResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Connect4GameEngineTest {

    private Connect4GameEngine engine;

    @BeforeEach
    void setUp() {
        engine = new Connect4GameEngine();
    }

    @Test
    void startNewGame_ShouldReturnEmptyBoard() {
        Connect4GameState state = engine.startNewGame();
        assertThat(state.getBoard()).hasSize(42);
        assertThat(state.getBoard()).containsOnlyDigits().containsOnlyOnce("0".repeat(42));
    }

    @Test
    void applyMove_ShouldPlacePieceInCorrectRow() {
        Connect4GameState state = engine.startNewGame();
        
        // P1 moves in column 0
        state = engine.applyMove(state, 0, 0);
        
        // Bottom left should be '1' (Player 1)
        assertThat(state.getBoard().charAt(0)).isEqualTo('1');
        
        // P2 moves in column 0
        state = engine.applyMove(state, 0, 1);
        
        // One row up should be '2' (Player 2) - Index is 0 + 7 = 7
        assertThat(state.getBoard().charAt(7)).isEqualTo('2');
    }

    @Test
    void applyMove_ShouldThrowOnFullColumn() {
        Connect4GameState state = engine.startNewGame();
        
        // Fill column 0 (6 rows)
        for (int i = 0; i < 6; i++) {
            state = engine.applyMove(state, 0, i % 2);
        }
        
        Connect4GameState fullState = state;
        
        // Try to place one more piece
        assertThatThrownBy(() -> engine.applyMove(fullState, 0, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Column 0 is full");
    }

    @Test
    void applyMove_ShouldThrowOnWrongTurn() {
        Connect4GameState state = engine.startNewGame();
        // P1 moves
        state = engine.applyMove(state, 3, 0);
        
        // P1 tries to move again (should be P2)
        Connect4GameState p1State = state;
        assertThatThrownBy(() -> engine.applyMove(p1State, 3, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("It is not player 0's turn");
    }

    @Test
    void checkResult_ShouldDetectHorizontalWin() {
        Connect4GameState state = engine.startNewGame();
        
        // P1 builds horizontal row in bottom row (cols 0, 1, 2, 3)
        // P2 dumps in col 6
        state = engine.applyMove(state, 0, 0); // P1
        state = engine.applyMove(state, 6, 1); // P2
        state = engine.applyMove(state, 1, 0); // P1
        state = engine.applyMove(state, 6, 1); // P2
        state = engine.applyMove(state, 2, 0); // P1
        state = engine.applyMove(state, 6, 1); // P2
        state = engine.applyMove(state, 3, 0); // P1 - WIN

        GameResult result = engine.checkResult(state);
        assertThat(result).isNotNull();
        if (result != null) {
            assertThat(result.scores().getScore(0)).isEqualTo(1.0); // P1 Wins
        }
    }

    @Test
    void checkResult_ShouldDetectVerticalWin() {
        Connect4GameState state = engine.startNewGame();
        
        // P1 builds vertical column in col 0
        // P2 dumps in col 1
        state = engine.applyMove(state, 0, 0); // P1
        state = engine.applyMove(state, 1, 1); // P2
        state = engine.applyMove(state, 0, 0); // P1
        state = engine.applyMove(state, 1, 1); // P2
        state = engine.applyMove(state, 0, 0); // P1
        state = engine.applyMove(state, 1, 1); // P2
        state = engine.applyMove(state, 0, 0); // P1 - WIN

        GameResult result = engine.checkResult(state);
        assertThat(result).isNotNull();
        if (result != null) {
            assertThat(result.scores().getScore(0)).isEqualTo(1.0);
        }
    }

    @Test
    void checkResult_ShouldDetectDiagonalWin() {
        Connect4GameState state = engine.startNewGame();
        
        /*
          Setup for / diagonal win:
          . . . . . .
          . . . 1 . .
          . . 1 2 . .
          . 1 2 1 . .
          1 2 1 2 . .
        */
        
        state = engine.applyMove(state, 0, 0); // P1 (0,0)
        state = engine.applyMove(state, 1, 1); // P2 (0,1)
        state = engine.applyMove(state, 2, 0); // P1 (0,2)
        state = engine.applyMove(state, 3, 1); // P2 (0,3)
        
        state = engine.applyMove(state, 1, 0); // P1 (1,1)
        state = engine.applyMove(state, 2, 1); // P2 (1,2)
        state = engine.applyMove(state, 3, 0); // P1 (1,3)
        state = engine.applyMove(state, 4, 1); // P2 (0,4) - waste
        
        state = engine.applyMove(state, 2, 0); // P1 (2,2)
        state = engine.applyMove(state, 3, 1); // P2 (2,3)
        
        state = engine.applyMove(state, 3, 0); // P1 (3,3) - WIN

        GameResult result = engine.checkResult(state);
        assertThat(result).isNotNull();
        if (result != null) {
            assertThat(result.scores().getScore(0)).isEqualTo(1.0);
        }
    }

    @Test
    void getLegalMoves_ShouldReturnAvailableColumns() {
        Connect4GameState state = engine.startNewGame();
        
        // Fill column 0 completely
        for (int i = 0; i < 6; i++) {
            state = engine.applyMove(state, 0, i % 2);
        }

        List<Integer> legalMoves = engine.getLegalMoves(state);
        
        // Should contain 1-6, but NOT 0
        assertThat(legalMoves).contains(1, 2, 3, 4, 5, 6);
        assertThat(legalMoves).doesNotContain(0);
    }
}
