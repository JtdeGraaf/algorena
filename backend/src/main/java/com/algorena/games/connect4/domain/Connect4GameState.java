package com.algorena.games.connect4.domain;

import com.algorena.games.domain.AbstractGameState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.jspecify.annotations.Nullable;

import static com.algorena.common.config.SuppressedWarnings.NULL_AWAY_INIT;

@Entity
@Table(name = "connect4_game_states")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Connect4GameState extends AbstractGameState {

    /**
     * Represents the 6x7 board as a 42-character string.
     * Index 0 is bottom-left (row 0, col 0), index 6 is bottom-right (row 0, col 6).
     * Index 7 is row 1, col 0, and so on.
     * Characters: '0' = Empty, '1' = Player 1 (Red), '2' = Player 2 (Yellow)
     */
    @Column(name = "board", length = 42, nullable = false)
    @SuppressWarnings(NULL_AWAY_INIT)
    private String board;

    @Column(name = "last_move_column")
    private @Nullable Integer lastMoveColumn;

    public void updateBoardState(String board, @Nullable Integer lastMoveColumn) {
        this.board = board;
        this.lastMoveColumn = lastMoveColumn;
    }
}
