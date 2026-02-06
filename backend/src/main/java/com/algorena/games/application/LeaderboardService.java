package com.algorena.games.application;

import com.algorena.bots.domain.Game;
import com.algorena.games.dto.BotLeaderboardEntryDTO;
import com.algorena.games.dto.RatingHistoryDTO;
import com.algorena.games.dto.UserLeaderboardEntryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service for querying leaderboard rankings.
 * Supports both bot-level and user-level leaderboards.
 */
public interface LeaderboardService {

    /**
     * Get bot leaderboard for a game.
     * Bots are ranked by their individual ELO ratings.
     *
     * @param game     the game type
     * @param pageable pagination parameters
     * @return page of bot leaderboard entries
     */
    Page<BotLeaderboardEntryDTO> getBotLeaderboard(Game game, Pageable pageable);

    /**
     * Get a specific bot's ranking.
     *
     * @param botId the bot ID
     * @param game  the game type
     * @return bot leaderboard entry with rank
     */
    BotLeaderboardEntryDTO getBotRanking(Long botId, Game game);

    /**
     * Get user leaderboard for a game.
     * Users are ranked by their best bot's ELO rating.
     *
     * @param game     the game type
     * @param pageable pagination parameters
     * @return page of user leaderboard entries
     */
    Page<UserLeaderboardEntryDTO> getUserLeaderboard(Game game, Pageable pageable);

    /**
     * Get a specific user's ranking.
     *
     * @param userId the user ID
     * @param game   the game type
     * @return user leaderboard entry with rank
     */
    UserLeaderboardEntryDTO getUserRanking(Long userId, Game game);

    /**
     * Get rating history for a bot.
     * Returns recent rating changes in descending chronological order.
     *
     * @param botId the bot ID
     * @param game  the game type
     * @param limit maximum number of history entries
     * @return list of rating history entries
     */
    List<RatingHistoryDTO> getBotRatingHistory(Long botId, Game game, int limit);
}
