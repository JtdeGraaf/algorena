package com.algorena.games.chess.data;

import com.algorena.games.chess.domain.ChessGameState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChessGameStateRepository extends JpaRepository<ChessGameState, UUID> {
    Optional<ChessGameState> findByMatchId(UUID matchId);
}
