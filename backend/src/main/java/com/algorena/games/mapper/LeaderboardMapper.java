package com.algorena.games.mapper;

import com.algorena.common.exception.DataNotFoundException;
import com.algorena.games.domain.BotRating;
import com.algorena.games.domain.RatingHistory;
import com.algorena.games.domain.UserRanking;
import com.algorena.games.dto.BotLeaderboardEntryDTO;
import com.algorena.games.dto.RatingHistoryDTO;
import com.algorena.games.dto.UserLeaderboardEntryDTO;
import com.algorena.users.domain.User;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting leaderboard-related entities to DTOs.
 * Pure mapping only - no repository access.
 */
@Component
public class LeaderboardMapper {

    // ===== Bot Leaderboard Mappings =====

    /**
     * Converts a BotRating to BotLeaderboardEntryDTO with pre-fetched owner and rank.
     *
     * @param rating the bot rating entity
     * @param owner the bot's owner (user)
     * @param rank the pre-calculated rank position
     */
    public BotLeaderboardEntryDTO toBotLeaderboardEntryDTO(BotRating rating, User owner, long rank) {
        var bot = rating.getBot();

        return new BotLeaderboardEntryDTO(
                rank,
                bot.getId(),
                bot.getName(),
                owner.getId(),
                owner.getUsername(),
                owner.getImageUrl(),
                rating.getEloRating(),
                rating.getMatchesPlayed(),
                rating.getWins(),
                rating.getLosses(),
                rating.getDraws(),
                rating.getWinRate()
        );
    }

    // ===== User Leaderboard Mappings =====

    /**
     * Converts a UserRanking to UserLeaderboardEntryDTO with pre-calculated rank.
     *
     * @param ranking the user ranking entity (must have user loaded)
     * @param rank the pre-calculated rank position
     */
    public UserLeaderboardEntryDTO toUserLeaderboardEntryDTO(UserRanking ranking, long rank) {
        var user = ranking.getUser();
        if (user == null) {
            throw new DataNotFoundException("User not found for ranking with userId: " + ranking.getUserId());
        }

        return new UserLeaderboardEntryDTO(
                rank,
                user.getId(),
                user.getUsername(),
                user.getImageUrl(),
                ranking.getBestBotElo(),
                ranking.getAvgBotElo(),
                ranking.getTotalBots(),
                ranking.getTotalMatches(),
                ranking.getTotalWins(),
                ranking.getTotalLosses(),
                ranking.getTotalDraws(),
                ranking.getWinRate()
        );
    }

    // ===== Rating History Mappings =====

    /**
     * Converts a RatingHistory entity to RatingHistoryDTO.
     */
    public RatingHistoryDTO toRatingHistoryDTO(RatingHistory history) {
        return new RatingHistoryDTO(
                history.getMatch().getId(),
                history.getOldRating(),
                history.getNewRating(),
                history.getRatingChange(),
                history.getOpponentRating(),
                history.getOpponentBot().getId(),
                history.getOpponentBot().getName(),
                history.getMatchResult(),
                history.getCreated()
        );
    }
}
