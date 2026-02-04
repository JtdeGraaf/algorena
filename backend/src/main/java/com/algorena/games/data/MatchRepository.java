package com.algorena.games.data;

import com.algorena.games.domain.Match;
import com.algorena.games.domain.MatchStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MatchRepository extends JpaRepository<Match, UUID> {
    List<Match> findByParticipants_Bot_Id(Long botId);

    List<Match> findByParticipants_Bot_IdAndStatus(Long botId, MatchStatus status);

    // Eagerly fetch match with participants and bots for executor
    @Query("SELECT m FROM Match m " +
            "LEFT JOIN FETCH m.participants p " +
            "LEFT JOIN FETCH p.bot " +
            "WHERE m.id = :id")
    Optional<Match> findByIdWithParticipants(@Param("id") UUID id);

    // Find matches where any bot owned by the user is participating
    @Query("SELECT DISTINCT m FROM Match m " +
            "JOIN m.participants p " +
            "WHERE p.bot.userId = :userId " +
            "ORDER BY m.created DESC")
    List<Match> findByUserIdOrderByCreatedDesc(@Param("userId") Long userId);

    // Get recent matches with limit (using Pageable for efficiency)
    @Query("SELECT m FROM Match m ORDER BY m.created DESC")
    List<Match> findRecentMatches(Pageable pageable);

    // Find matches by multiple bot IDs (for user's bots)
    @Query("SELECT DISTINCT m FROM Match m " +
            "JOIN m.participants p " +
            "WHERE p.bot.id IN :botIds " +
            "ORDER BY m.created DESC")
    List<Match> findByBotIdsOrderByCreatedDesc(@Param("botIds") List<Long> botIds);
}
