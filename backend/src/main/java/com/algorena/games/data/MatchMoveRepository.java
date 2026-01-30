package com.algorena.games.data;

import com.algorena.games.domain.AbstractMatchMove;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MatchMoveRepository extends JpaRepository<AbstractMatchMove, UUID> {
    List<AbstractMatchMove> findByMatchIdOrderByCreatedAsc(UUID matchId);
}
