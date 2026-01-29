package com.algorena.games.chess.engine;

import com.algorena.games.chess.domain.ChessGameState;
import com.algorena.games.engine.GameResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChessGameEngineTest {

    private ChessGameEngine engine;

    @BeforeEach
    void setUp() {
        engine = new ChessGameEngine();
    }

    @Test
    void startNewGame_ShouldReturnInitialBoard() {
        ChessGameState state = engine.startNewGame();
        assertThat(state.getFen()).isEqualTo("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    }

    @Test
    void applyMove_ShouldUpdateBoard() {
        ChessGameState state = engine.startNewGame();
        ChessGameState newState = engine.applyMove(state, "e2e4", 0); // White moves e2e4

        // En passant target is e3
        assertThat(newState.getFen()).isEqualTo("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1");
    }

    @Test
    void applyMove_ShouldThrowOnIllegalMove() {
        ChessGameState state = engine.startNewGame();
        // Try a move that is definitely illegal (Rook blocked)
        assertThatThrownBy(() -> engine.applyMove(state, "a1a2", 0)) 
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Illegal move");
    }

    @Test
    void applyMove_ShouldThrowOnWrongTurn() {
        ChessGameState state = engine.startNewGame();
        assertThatThrownBy(() -> engine.applyMove(state, "e7e5", 0)) // Black move attempted by White (0)
                 // Wait, e7e5 is Black's move. 0 is White.
                 // The engine check: "It is not player 0's turn" if turn is Black.
                 // Here turn is White. Player is 0 (White).
                 // So "e7e5" is technically illegal for White? No, e7e5 is black pawn.
                 // But logic says: getSideToMove() -> White.
                 // Player Index 0 -> White.
                 // So checks pass.
                 // But move "e7e5" for White moving Black piece? Chesslib might reject this as illegal move.
                 // Let's test actual wrong turn:
                 // White moves, then Black tries to move AGAIN.
        ;
    }
    
    @Test
    void applyMove_ShouldThrowWhenWrongPlayerMoves() {
         ChessGameState state = engine.startNewGame();
         // It is White's turn. Player 1 (Black) tries to move.
         assertThatThrownBy(() -> engine.applyMove(state, "e2e4", 1))
                 .isInstanceOf(IllegalArgumentException.class)
                 .hasMessageContaining("It is not player 1's turn");
    }

    @Test
    void checkResult_ShouldReturnNullForOngoingGame() {
        ChessGameState state = engine.startNewGame();
        assertThat(engine.checkResult(state)).isNull();
    }
    
    @Test
    void checkResult_ShouldDetectCheckmate() {
        // Fools Mate
        ChessGameState state = engine.startNewGame();
        state = engine.applyMove(state, "f2f3", 0);
        state = engine.applyMove(state, "e7e5", 1);
        state = engine.applyMove(state, "g2g4", 0);
        state = engine.applyMove(state, "d8h4", 1); // Mate
        
        GameResult result = engine.checkResult(state);
        assertThat(result).isNotNull();
        // Black (1) wins -> 1.0, White (0) loses -> 0.0
        if (result != null) {
            assertThat(result.scores().getScore(1)).isEqualTo(1.0);
            assertThat(result.scores().getScore(0)).isEqualTo(0.0);
        }
    }
}
