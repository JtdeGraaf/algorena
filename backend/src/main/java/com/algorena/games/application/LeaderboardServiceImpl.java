package com.algorena.games.application;

import com.algorena.bots.data.BotRepository;
import com.algorena.bots.domain.Game;
import com.algorena.common.exception.DataNotFoundException;
import com.algorena.games.data.BotRatingRepository;
import com.algorena.games.data.RatingHistoryRepository;
import com.algorena.games.data.UserRankingRepository;
import com.algorena.games.domain.BotRating;
import com.algorena.games.domain.RatingHistory;
import com.algorena.games.domain.UserRanking;
import com.algorena.games.dto.BotLeaderboardEntryDTO;
import com.algorena.games.dto.RatingHistoryDTO;
import com.algorena.games.dto.UserLeaderboardEntryDTO;
import com.algorena.users.data.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of leaderboard service.
 */
@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class LeaderboardServiceImpl implements LeaderboardService {

    private final BotRatingRepository botRatingRepository;
    private final UserRankingRepository userRankingRepository;
    private final RatingHistoryRepository ratingHistoryRepository;
    private final BotRepository botRepository;
    private final UserRepository userRepository;

    // ===== Bot Leaderboard =====

    @Override
    public Page<BotLeaderboardEntryDTO> getBotLeaderboard(Game game, Pageable pageable) {
        Page<BotRating> ratings = botRatingRepository
            .findByGameAndLeaderboardOrderByEloRatingDesc(game, null, pageable);

        return ratings.map(this::toBotLeaderboardEntry);
    }

    @Override
    public BotLeaderboardEntryDTO getBotRanking(Long botId, Game game) {
        BotRating rating = botRatingRepository
            .findByBotIdAndGameAndLeaderboard(botId, game, null)
            .orElseThrow(() -> new DataNotFoundException("Bot rating not found for bot " + botId + " in game " + game));

        long rank = botRatingRepository
            .countByGameAndLeaderboardAndEloRatingGreaterThan(game, null, rating.getEloRating()) + 1;

        return toBotLeaderboardEntry(rating, rank);
    }

    private BotLeaderboardEntryDTO toBotLeaderboardEntry(BotRating rating) {
        long rank = botRatingRepository
            .countByGameAndLeaderboardAndEloRatingGreaterThan(
                rating.getGame(),
                null,
                rating.getEloRating()
            ) + 1;

        return toBotLeaderboardEntry(rating, rank);
    }

    private BotLeaderboardEntryDTO toBotLeaderboardEntry(BotRating rating, long rank) {
        var bot = rating.getBot();
        var owner = userRepository.findById(bot.getUserId())
            .orElseThrow(() -> new DataNotFoundException("User not found: " + bot.getUserId()));

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

    // ===== User Leaderboard =====

    @Override
    public Page<UserLeaderboardEntryDTO> getUserLeaderboard(Game game, Pageable pageable) {
        Page<UserRanking> rankings = userRankingRepository
            .findByGameOrderByBestBotEloDesc(game, pageable);

        return rankings.map(this::toUserLeaderboardEntry);
    }

    @Override
    public UserLeaderboardEntryDTO getUserRanking(Long userId, Game game) {
        UserRanking ranking = userRankingRepository
            .findByUserIdAndGame(userId, game)
            .orElseThrow(() -> new DataNotFoundException("User ranking not found for user " + userId + " in game " + game));

        long rank = userRankingRepository
            .countByGameAndBestBotEloGreaterThan(game, ranking.getBestBotElo()) + 1;

        return toUserLeaderboardEntry(ranking, rank);
    }

    private UserLeaderboardEntryDTO toUserLeaderboardEntry(UserRanking ranking) {
        long rank = userRankingRepository
            .countByGameAndBestBotEloGreaterThan(ranking.getGame(), ranking.getBestBotElo()) + 1;

        return toUserLeaderboardEntry(ranking, rank);
    }

    private UserLeaderboardEntryDTO toUserLeaderboardEntry(UserRanking ranking, long rank) {
        var user = ranking.getUser();
        if (user == null) {
            throw new DataNotFoundException("User not found in ranking");
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

    // ===== Rating History =====

    @Override
    public List<RatingHistoryDTO> getBotRatingHistory(Long botId, Game game, int limit) {
        List<RatingHistory> history = ratingHistoryRepository
            .findRecentByBotAndGame(botId, game, PageRequest.of(0, limit));

        return history.stream()
            .map(this::toRatingHistoryDTO)
            .toList();
    }

    private RatingHistoryDTO toRatingHistoryDTO(RatingHistory history) {
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
