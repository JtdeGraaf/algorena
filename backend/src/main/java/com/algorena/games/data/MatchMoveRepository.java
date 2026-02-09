package com.algorena.games.data;

import com.algorena.games.domain.AbstractMatchMove;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchMoveRepository extends JpaRepository<AbstractMatchMove, Long> {
    List<AbstractMatchMove> findByMatchIdOrderByCreatedAsc(Long matchId);
}
