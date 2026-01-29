package com.algorena.games.chess.domain;

import com.algorena.games.domain.AbstractMatchMove;
import com.algorena.games.domain.Match;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

import static com.algorena.common.config.SuppressedWarnings.NULL_AWAY_INIT;

@Entity
@Table(name = "chess_match_moves")
@Getter
@NoArgsConstructor
public class ChessMatchMove extends AbstractMatchMove {

    @Column(name = "from_square", length = 2)
    @SuppressWarnings(NULL_AWAY_INIT)
    private String fromSquare;

    @Column(name = "to_square", length = 2)
    @SuppressWarnings(NULL_AWAY_INIT)
    private String toSquare;

    @Nullable
    @Column(name = "promotion_piece", length = 10)
    private String promotionPiece;

    @Builder
    public ChessMatchMove(Match match, int playerIndex, String moveNotation, String fromSquare, String toSquare, @Nullable String promotionPiece) {
        super(null, match, playerIndex, moveNotation);
        this.fromSquare = fromSquare;
        this.toSquare = toSquare;
        this.promotionPiece = promotionPiece;
    }
}
