package com.algorena.games.data;

import com.algorena.bots.domain.Game;
import com.algorena.games.domain.RatingHistory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for rating history records.
 */
@Repository
public interface RatingHistoryRepository extends JpaRepository<RatingHistory, Long> {

    /**
     * Find recent rating history for a specific bot and game.
     * Returns up to 'limit' most recent entries.
     */
    @Query("""
        SELECT rh FROM RatingHistory rh
        WHERE rh.botRating.bot.id = :botId
        AND rh.botRating.game = :game
        AND rh.botRating.leaderboardId IS NULL
        ORDER BY rh.created DESC
    """)
    List<RatingHistory> findRecentByBotAndGame(
        Long botId,
        Game game,
        PageRequest pageRequest
    );
}
