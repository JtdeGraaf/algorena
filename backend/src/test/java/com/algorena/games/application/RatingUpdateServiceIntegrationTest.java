package com.algorena.games.application;

import com.algorena.bots.domain.Bot;
import com.algorena.bots.domain.Game;
import com.algorena.games.data.MatchRepository;
import com.algorena.games.data.RatingHistoryRepository;
import com.algorena.games.domain.*;
import com.algorena.test.config.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RatingUpdateServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private RatingUpdateService ratingUpdateService;

    @Autowired
    private RatingHistoryRepository ratingHistoryRepository;

    @Autowired
    private MatchRepository matchRepository;

    private Bot bot1;
    private Bot bot2;

    @BeforeEach
    void setUp() {
        bot1 = createTestBot(testUser, "Bot1", Game.CHESS, "http://localhost:8000/bot1");
        bot2 = createTestBot(testUser, "Bot2", Game.CHESS, "http://localhost:8000/bot2");
    }

    @Test
    void shouldCreateInitialRatingsForNewBots() {
        // Given: A finished match with two bots that have no ratings yet
        Match match = createFinishedMatch(bot1, bot2, 1.0, 0.0);

        // When: Update ratings
        ratingUpdateService.updateRatingsAfterMatch(match);

        // Then: Both bots should have ratings created
        var rating1 = botRatingRepository.findByBotAndGameAndLeaderboard(bot1, Game.CHESS, null);
        var rating2 = botRatingRepository.findByBotAndGameAndLeaderboard(bot2, Game.CHESS, null);

        assertThat(rating1).isPresent();
        assertThat(rating2).isPresent();

        // Bot1 won, so rating should be higher than default
        assertThat(rating1.get().getEloRating()).isGreaterThan(1200);
        // Bot2 lost, so rating should be lower than default
        assertThat(rating2.get().getEloRating()).isLessThan(1200);

        // Match stats should be recorded
        assertThat(rating1.get().getMatchesPlayed()).isEqualTo(1);
        assertThat(rating1.get().getWins()).isEqualTo(1);
        assertThat(rating1.get().getLosses()).isEqualTo(0);

        assertThat(rating2.get().getMatchesPlayed()).isEqualTo(1);
        assertThat(rating2.get().getWins()).isEqualTo(0);
        assertThat(rating2.get().getLosses()).isEqualTo(1);
    }

    @Test
    void shouldUpdateExistingRatings() {
        // Given: Bots with existing ratings
        createTestBotRating(bot1, Game.CHESS, 1400, 5, 3, 2, 0);
        createTestBotRating(bot2, Game.CHESS, 1300, 5, 2, 3, 0);

        // When: Bot2 wins
        Match match = createFinishedMatch(bot1, bot2, 0.0, 1.0);
        ratingUpdateService.updateRatingsAfterMatch(match);

        // Then: Ratings are updated
        var rating1 = botRatingRepository.findByBotAndGameAndLeaderboard(bot1, Game.CHESS, null).orElseThrow();
        var rating2 = botRatingRepository.findByBotAndGameAndLeaderboard(bot2, Game.CHESS, null).orElseThrow();

        // Bot1 lost, rating decreased
        assertThat(rating1.getEloRating()).isLessThan(1400);
        assertThat(rating1.getMatchesPlayed()).isEqualTo(6);
        assertThat(rating1.getLosses()).isEqualTo(3);

        // Bot2 won, rating increased (and increased more as underdog)
        assertThat(rating2.getEloRating()).isGreaterThan(1300);
        assertThat(rating2.getMatchesPlayed()).isEqualTo(6);
        assertThat(rating2.getWins()).isEqualTo(3);
    }

    @Test
    void shouldHandleDraw() {
        // Given: Bots with existing ratings
        createTestBotRating(bot1, Game.CHESS, 1400, 10, 5, 5, 0);
        createTestBotRating(bot2, Game.CHESS, 1400, 10, 5, 5, 0);

        // When: Draw
        Match match = createFinishedMatch(bot1, bot2, 0.5, 0.5);
        ratingUpdateService.updateRatingsAfterMatch(match);

        // Then: Both bots have draws recorded, ratings stay similar
        var rating1 = botRatingRepository.findByBotAndGameAndLeaderboard(bot1, Game.CHESS, null).orElseThrow();
        var rating2 = botRatingRepository.findByBotAndGameAndLeaderboard(bot2, Game.CHESS, null).orElseThrow();

        assertThat(rating1.getDraws()).isEqualTo(1);
        assertThat(rating2.getDraws()).isEqualTo(1);
        assertThat(rating1.getMatchesPlayed()).isEqualTo(11);
        assertThat(rating2.getMatchesPlayed()).isEqualTo(11);

        // Ratings should be unchanged for equal opponents drawing
        assertThat(rating1.getEloRating()).isEqualTo(1400);
        assertThat(rating2.getEloRating()).isEqualTo(1400);
    }

    @Test
    void shouldSaveRatingHistory() {
        // Given: Bots with existing ratings
        createTestBotRating(bot1, Game.CHESS, 1400, 5, 3, 2, 0);
        createTestBotRating(bot2, Game.CHESS, 1300, 5, 2, 3, 0);

        // When: Match completes
        Match match = createFinishedMatch(bot1, bot2, 1.0, 0.0);
        ratingUpdateService.updateRatingsAfterMatch(match);

        // Then: Rating history is saved for both bots
        List<RatingHistory> history = ratingHistoryRepository.findAll();
        assertThat(history).hasSize(2);

        // Check bot1's history
        RatingHistory bot1History = history.stream()
            .filter(h -> h.getBotRating().getBot().getId().equals(bot1.getId()))
            .findFirst()
            .orElseThrow();

        assertThat(bot1History.getOldRating()).isEqualTo(1400);
        assertThat(bot1History.getNewRating()).isGreaterThan(1400);
        assertThat(bot1History.getRatingChange()).isGreaterThan(0);
        assertThat(bot1History.getOpponentRating()).isEqualTo(1300);
        assertThat(bot1History.getOpponentBot().getId()).isEqualTo(bot2.getId());
        assertThat(bot1History.getMatchResult()).isEqualTo(MatchResult.WIN);

        // Check bot2's history
        RatingHistory bot2History = history.stream()
            .filter(h -> h.getBotRating().getBot().getId().equals(bot2.getId()))
            .findFirst()
            .orElseThrow();

        assertThat(bot2History.getOldRating()).isEqualTo(1300);
        assertThat(bot2History.getNewRating()).isLessThan(1300);
        assertThat(bot2History.getRatingChange()).isLessThan(0);
        assertThat(bot2History.getMatchResult()).isEqualTo(MatchResult.LOSS);
    }

    @Test
    void shouldNotUpdateRatingsForNonFinishedMatch() {
        // Given: An in-progress match
        Match match = createInProgressMatch(bot1, bot2);

        // When: Try to update ratings
        ratingUpdateService.updateRatingsAfterMatch(match);

        // Then: No ratings are created
        var rating1 = botRatingRepository.findByBotAndGameAndLeaderboard(bot1, Game.CHESS, null);
        var rating2 = botRatingRepository.findByBotAndGameAndLeaderboard(bot2, Game.CHESS, null);

        assertThat(rating1).isEmpty();
        assertThat(rating2).isEmpty();
    }

    /**
     * Helper: Create a finished match with specific scores.
     */
    private Match createFinishedMatch(Bot bot1, Bot bot2, double score1, double score2) {
        Match match = Match.builder()
            .game(Game.CHESS)
            .status(MatchStatus.FINISHED)
            .build();

        MatchParticipant p1 = MatchParticipant.builder()
            .match(match)
            .bot(bot1)
            .playerIndex(0)
            .build();
        p1.recordScore(score1);
        match.addParticipant(p1);

        MatchParticipant p2 = MatchParticipant.builder()
            .match(match)
            .bot(bot2)
            .playerIndex(1)
            .build();
        p2.recordScore(score2);
        match.addParticipant(p2);

        return matchRepository.save(match);
    }

    /**
     * Helper: Create an in-progress match.
     */
    private Match createInProgressMatch(Bot bot1, Bot bot2) {
        Match match = Match.builder()
            .game(Game.CHESS)
            .status(MatchStatus.IN_PROGRESS)
            .build();

        MatchParticipant p1 = MatchParticipant.builder()
            .match(match)
            .bot(bot1)
            .playerIndex(0)
            .build();
        match.addParticipant(p1);

        MatchParticipant p2 = MatchParticipant.builder()
            .match(match)
            .bot(bot2)
            .playerIndex(1)
            .build();
        match.addParticipant(p2);

        return matchRepository.save(match);
    }
}
