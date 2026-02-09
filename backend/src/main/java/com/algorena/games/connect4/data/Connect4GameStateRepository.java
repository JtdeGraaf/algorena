package com.algorena.games.connect4.data;

import com.algorena.games.connect4.domain.Connect4GameState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface Connect4GameStateRepository extends JpaRepository<Connect4GameState, Long> {
    Optional<Connect4GameState> findByMatchId(Long matchId);
}
