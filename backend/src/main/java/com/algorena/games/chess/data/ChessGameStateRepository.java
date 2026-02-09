package com.algorena.games.chess.data;

import com.algorena.games.chess.domain.ChessGameState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChessGameStateRepository extends JpaRepository<ChessGameState, Long> {
    Optional<ChessGameState> findByMatchId(Long matchId);
}
