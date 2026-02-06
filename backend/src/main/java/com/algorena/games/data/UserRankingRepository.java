package com.algorena.games.data;

import com.algorena.bots.domain.Game;
import com.algorena.games.domain.UserRanking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for user rankings (materialized view).
 */
@Repository
public interface UserRankingRepository extends JpaRepository<UserRanking, UserRanking.UserRankingId> {

    /**
     * Find all user rankings for a game, ordered by best bot ELO descending.
     */
    @Query("""
        SELECT ur FROM UserRanking ur
        WHERE ur.game = :game
        ORDER BY ur.bestBotElo DESC
    """)
    Page<UserRanking> findByGameOrderByBestBotEloDesc(
        Game game,
        Pageable pageable
    );

    /**
     * Find user's ranking for a specific game.
     */
    Optional<UserRanking> findByUserIdAndGame(Long userId, Game game);

    /**
     * Count users with higher best bot ELO than the given rating.
     * Used to calculate rank.
     */
    long countByGameAndBestBotEloGreaterThan(Game game, Integer bestBotElo);

    /**
     * Refresh the materialized view.
     * Should be called after matches complete.
     */
    @Modifying
    @Query(value = "REFRESH MATERIALIZED VIEW CONCURRENTLY user_rankings", nativeQuery = true)
    void refresh();
}
