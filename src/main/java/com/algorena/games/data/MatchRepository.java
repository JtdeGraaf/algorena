package com.algorena.games.data;

import com.algorena.games.domain.Match;
import com.algorena.games.domain.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MatchRepository extends JpaRepository<Match, UUID> {
    List<Match> findByParticipants_Bot_Id(Long botId);
    List<Match> findByParticipants_Bot_IdAndStatus(Long botId, MatchStatus status);
}
