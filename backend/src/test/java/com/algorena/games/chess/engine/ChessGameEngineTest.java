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
        assertThatThrownBy(() -> engine.applyMove(state, "e7e5", 0)); // Black move attempted by White (0)
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

    @Test
    void applyMove_ShouldRejectCorruptKingMove() {
        ChessGameState state = new ChessGameState();
        // FEN from the issue report where King tries to move 3 squares to capture King
        state.updateBoardState("rnb1kbnr/p1P2ppp/8/1p2K3/8/8/PPP2PPP/RNB2BNR b kq - 0 7", 0, 7);
        
        assertThatThrownBy(() -> engine.applyMove(state, "e8e5", 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Illegal move");
    }
}
