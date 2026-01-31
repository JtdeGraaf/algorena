package com.algorena.games.chess.engine;

import com.algorena.games.chess.domain.ChessGameState;
import com.algorena.games.engine.GameEngine;
import com.algorena.games.engine.GameResult;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChessGameEngine implements GameEngine<ChessGameState, String> {

    @Override
    public ChessGameState startNewGame() {
        Board board = new Board();
        return createGameState(board);
    }

    @Override
    public ChessGameState applyMove(ChessGameState state, String moveNotation, int playerIndex) {
        Board board = new Board();
        board.loadFromFen(state.getFen());

        // Validate turn
        Side turn = board.getSideToMove();
        Side playerSide = (playerIndex == 0) ? Side.WHITE : Side.BLACK;
        if (turn != playerSide) {
            throw new IllegalArgumentException("It is not player " + playerIndex + "'s turn.");
        }

        Move move = new Move(moveNotation, turn);
        // Use legalMoves() instead of isMoveLegal() as the latter has bugs with some invalid moves (e.g. e8e5 for king)
        if (!board.legalMoves().contains(move)) {
             throw new IllegalArgumentException("Illegal move: " + moveNotation);
        }
        
        board.doMove(move);
        return createGameState(board);
    }

    @Override
    public @Nullable GameResult checkResult(ChessGameState state) {
        Board board = new Board();
        board.loadFromFen(state.getFen());

        if (board.isMated()) {
            // If White is mated (turn is White), Black (index 1) wins.
            // If Black is mated (turn is Black), White (index 0) wins.
            return board.getSideToMove() == Side.WHITE 
                    ? GameResult.winner(1, 0) 
                    : GameResult.winner(0, 1);
        } else if (board.isDraw() || board.isStaleMate()) {
            return GameResult.draw();
        }

        return null; // Game is ongoing
    }

    public List<String> getLegalMoves(ChessGameState state) {
        Board board = new Board();
        board.loadFromFen(state.getFen());
        return board.legalMoves().stream()
                .map(Move::toString)
                .toList();
    }

    private ChessGameState createGameState(Board board) {
        ChessGameState state = new ChessGameState();
        state.updateBoardState(board.getFen(), board.getHalfMoveCounter(), board.getMoveCounter());
        return state;
    }
}
