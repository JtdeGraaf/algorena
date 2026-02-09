package com.algorena.games.data;

import com.algorena.bots.domain.Game;
import com.algorena.games.domain.Match;
import com.algorena.games.domain.MatchStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByParticipants_Bot_Id(Long botId);

    List<Match> findByParticipants_Bot_IdAndStatus(Long botId, MatchStatus status);

    // Eagerly fetch match with participants and bots for executor
    @Query("SELECT m FROM Match m " +
            "LEFT JOIN FETCH m.participants p " +
            "LEFT JOIN FETCH p.bot " +
            "WHERE m.id = :id")
    Optional<Match> findByIdWithParticipants(Long id);

    // Find matches where any bot owned by the user is participating
    @Query("SELECT DISTINCT m FROM Match m " +
            "JOIN m.participants p " +
            "WHERE p.bot.userId = :userId " +
            "ORDER BY m.created DESC")
    List<Match> findByUserIdOrderByCreatedDesc(Long userId);

    // Get recent matches with limit (using Pageable for efficiency)
    @Query("SELECT m FROM Match m ORDER BY m.created DESC")
    List<Match> findRecentMatches(Pageable pageable);

    // Find matches by multiple bot IDs (for user's bots)
    @Query("SELECT DISTINCT m FROM Match m " +
            "JOIN m.participants p " +
            "WHERE p.bot.id IN :botIds " +
            "ORDER BY m.created DESC")
    List<Match> findByBotIdsOrderByCreatedDesc(List<Long> botIds);

    /**
     * Check if there's a recent finished match between two specific bots.
     * Used for ELO protection to prevent rematch farming.
     *
     * @param bot1Id         first bot ID
     * @param bot2Id         second bot ID
     * @param game           the game type
     * @param since          only check matches finished after this time
     * @param excludeMatchId exclude this match ID from the check
     * @return true if there's a recent match between these bots
     */
    @Query("""
                SELECT COUNT(m) > 0
                FROM Match m
                JOIN m.participants p1
                JOIN m.participants p2
                WHERE m.game = :game
                AND m.status = 'FINISHED'
                AND m.finishedAt >= :since
                AND m.id != :excludeMatchId
                AND p1.bot.id = :bot1Id
                AND p2.bot.id = :bot2Id
                AND p1.id != p2.id
            """)
    boolean existsRecentMatchBetweenBots(
            Long bot1Id,
            Long bot2Id,
            Game game,
            LocalDateTime since,
            Long excludeMatchId
    );
}
