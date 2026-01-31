package com.algorena.games.connect4.domain;

import com.algorena.games.domain.AbstractMatchMove;
import com.algorena.games.domain.Match;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "connect4_match_moves")
@Getter
@NoArgsConstructor
public class Connect4MatchMove extends AbstractMatchMove {

    @Column(name = "column_index", nullable = false)
    private int columnIndex;

    @Builder
    public Connect4MatchMove(Match match, int playerIndex, String moveNotation, int columnIndex) {
        super(null, match, playerIndex, moveNotation);
        this.columnIndex = columnIndex;
    }
}
