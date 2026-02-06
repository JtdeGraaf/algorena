package com.algorena.games.data;

import com.algorena.bots.domain.Bot;
import com.algorena.bots.domain.Game;
import com.algorena.games.domain.BotRating;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for bot ELO ratings.
 */
@Repository
public interface BotRatingRepository extends JpaRepository<BotRating, Long> {

    /**
     * Find bot's rating for a specific game and leaderboard.
     * For global leaderboard, pass leaderboardId = null.
     */
    @Query("""
        SELECT br FROM BotRating br
        WHERE br.bot = :bot
        AND br.game = :game
        AND (:leaderboardId IS NULL AND br.leaderboardId IS NULL
             OR br.leaderboardId = :leaderboardId)
    """)
    Optional<BotRating> findByBotAndGameAndLeaderboard(
        Bot bot,
        Game game,
        @Nullable Long leaderboardId
    );

    /**
     * Find all bot ratings for a game and leaderboard, ordered by ELO descending.
     * For global leaderboard, pass leaderboardId = null.
     */
    @Query("""
        SELECT br FROM BotRating br
        WHERE br.game = :game
        AND (:leaderboardId IS NULL AND br.leaderboardId IS NULL
             OR br.leaderboardId = :leaderboardId)
        ORDER BY br.eloRating DESC
    """)
    Page<BotRating> findByGameAndLeaderboardOrderByEloRatingDesc(
        Game game,
        @Nullable Long leaderboardId,
        Pageable pageable
    );

    /**
     * Count bots with higher ELO than the given rating.
     * Used to calculate rank.
     */
    @Query("""
        SELECT COUNT(br) FROM BotRating br
        WHERE br.game = :game
        AND (:leaderboardId IS NULL AND br.leaderboardId IS NULL
             OR br.leaderboardId = :leaderboardId)
        AND br.eloRating > :rating
    """)
    long countByGameAndLeaderboardAndEloRatingGreaterThan(
        Game game,
        @Nullable Long leaderboardId,
        Integer rating
    );

    /**
     * Find bot's rating by bot ID, game, and leaderboard.
     */
    @Query("""
        SELECT br FROM BotRating br
        WHERE br.bot.id = :botId
        AND br.game = :game
        AND (:leaderboardId IS NULL AND br.leaderboardId IS NULL
             OR br.leaderboardId = :leaderboardId)
    """)
    Optional<BotRating> findByBotIdAndGameAndLeaderboard(
        Long botId,
        Game game,
        @Nullable Long leaderboardId
    );
}
