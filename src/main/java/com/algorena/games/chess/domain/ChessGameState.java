package com.algorena.games.chess.domain;

import com.algorena.games.domain.AbstractGameState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import static com.algorena.common.config.SuppressedWarnings.NULL_AWAY_INIT;

@Entity
@Table(name = "chess_game_states")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChessGameState extends AbstractGameState {

    @Column(name = "fen", nullable = false)
    @SuppressWarnings(NULL_AWAY_INIT)
    private String fen;

    @Column(name = "pgn", columnDefinition = "TEXT")
    @SuppressWarnings(NULL_AWAY_INIT)
    private String pgn;

    @Column(name = "half_move_clock")
    private int halfMoveClock;

    @Column(name = "full_move_number")
    private int fullMoveNumber;

    public void updateBoardState(String fen, int halfMoveClock, int fullMoveNumber) {
        this.fen = fen;
        this.halfMoveClock = halfMoveClock;
        this.fullMoveNumber = fullMoveNumber;
    }
}
