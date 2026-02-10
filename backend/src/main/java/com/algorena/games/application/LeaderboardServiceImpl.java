package com.algorena.games.application;

import com.algorena.bots.domain.Game;
import com.algorena.common.exception.DataNotFoundException;
import com.algorena.games.data.BotRatingRepository;
import com.algorena.games.data.RatingHistoryRepository;
import com.algorena.games.data.UserRankingRepository;
import com.algorena.games.domain.BotRating;
import com.algorena.games.domain.UserRanking;
import com.algorena.games.dto.BotLeaderboardEntryDTO;
import com.algorena.games.dto.RatingHistoryDTO;
import com.algorena.games.dto.UserLeaderboardEntryDTO;
import com.algorena.games.mapper.LeaderboardMapper;
import com.algorena.users.data.UserRepository;
import com.algorena.users.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementation of leaderboard service.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LeaderboardServiceImpl implements LeaderboardService {

    private final BotRatingRepository botRatingRepository;
    private final UserRankingRepository userRankingRepository;
    private final RatingHistoryRepository ratingHistoryRepository;
    private final UserRepository userRepository;
    private final LeaderboardMapper leaderboardMapper;

    // ===== Bot Leaderboard =====

    @Override
    public Page<BotLeaderboardEntryDTO> getBotLeaderboard(Game game, Pageable pageable) {
        Page<BotRating> ratings = botRatingRepository
            .findByGameAndLeaderboardOrderByEloRatingDesc(game, null, pageable);

        // Collect all user IDs from the ratings
        Set<Long> userIds = ratings.getContent().stream()
            .map(r -> r.getBot().getUserId())
            .collect(Collectors.toSet());

        // Batch fetch all users
        Map<Long, User> usersById = userRepository.findAllById(userIds).stream()
            .collect(Collectors.toMap(User::getId, Function.identity()));

        // Calculate rank based on page position
        long startRank = pageable.getOffset() + 1;

        return ratings.map(rating -> {
            User owner = usersById.get(rating.getBot().getUserId());
            if (owner == null) {
                throw new DataNotFoundException("User not found: " + rating.getBot().getUserId());
            }
            // Calculate rank based on position in page
            long rank = startRank + ratings.getContent().indexOf(rating);
            return leaderboardMapper.toBotLeaderboardEntryDTO(rating, owner, rank);
        });
    }

    @Override
    public BotLeaderboardEntryDTO getBotRanking(Long botId, Game game) {
        BotRating rating = botRatingRepository
            .findByBotIdAndGameAndLeaderboard(botId, game, null)
            .orElseThrow(() -> new DataNotFoundException("Bot rating not found for bot " + botId + " in game " + game));

        User owner = userRepository.findById(rating.getBot().getUserId())
            .orElseThrow(() -> new DataNotFoundException("User not found: " + rating.getBot().getUserId()));

        long rank = botRatingRepository
            .countByGameAndLeaderboardAndEloRatingGreaterThan(game, null, rating.getEloRating()) + 1;

        return leaderboardMapper.toBotLeaderboardEntryDTO(rating, owner, rank);
    }

    // ===== User Leaderboard =====

    @Override
    public Page<UserLeaderboardEntryDTO> getUserLeaderboard(Game game, Pageable pageable) {
        Page<UserRanking> rankings = userRankingRepository
            .findByGameOrderByBestBotEloDesc(game, pageable);

        // Calculate rank based on page position
        long startRank = pageable.getOffset() + 1;

        return rankings.map(ranking -> {
            long rank = startRank + rankings.getContent().indexOf(ranking);
            return leaderboardMapper.toUserLeaderboardEntryDTO(ranking, rank);
        });
    }

    @Override
    public UserLeaderboardEntryDTO getUserRanking(Long userId, Game game) {
        UserRanking ranking = userRankingRepository
            .findByUserIdAndGame(userId, game)
            .orElseThrow(() -> new DataNotFoundException("User ranking not found for user " + userId + " in game " + game));

        long rank = userRankingRepository
            .countByGameAndBestBotEloGreaterThan(game, ranking.getBestBotElo()) + 1;

        return leaderboardMapper.toUserLeaderboardEntryDTO(ranking, rank);
    }

    // ===== Rating History =====

    @Override
    public List<RatingHistoryDTO> getBotRatingHistory(Long botId, Game game, int limit) {
        return ratingHistoryRepository
            .findRecentByBotAndGame(botId, game, PageRequest.of(0, limit))
            .stream()
            .map(leaderboardMapper::toRatingHistoryDTO)
            .toList();
    }
}
