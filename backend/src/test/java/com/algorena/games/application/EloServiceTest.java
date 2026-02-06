package com.algorena.games.application;

import com.algorena.bots.domain.Bot;
import com.algorena.bots.domain.Game;
import com.algorena.games.domain.BotRating;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EloServiceTest {

    private EloService eloService;

    @BeforeEach
    void setUp() {
        eloService = new EloServiceImpl();
    }

    @Test
    void shouldCalculateNewRatingsForWin() {
        // Given: Two bots with equal ratings
        BotRating bot1Rating = createBotRating(1200, 0);
        BotRating bot2Rating = createBotRating(1200, 0);

        // When: Bot1 wins (score = 1.0)
        EloService.EloUpdateResult result = eloService.calculateNewRatings(
            bot1Rating,
            bot2Rating,
            1.0
        );

        // Then: Bot1 gains points, Bot2 loses points
        assertThat(result.newRating1()).isGreaterThan(1200);
        assertThat(result.newRating2()).isLessThan(1200);
        // Total rating should be conserved
        assertThat(result.newRating1() + result.newRating2()).isEqualTo(2400);
    }

    @Test
    void shouldCalculateNewRatingsForDraw() {
        // Given: Two bots with equal ratings
        BotRating bot1Rating = createBotRating(1200, 0);
        BotRating bot2Rating = createBotRating(1200, 0);

        // When: Draw (score = 0.5)
        EloService.EloUpdateResult result = eloService.calculateNewRatings(
            bot1Rating,
            bot2Rating,
            0.5
        );

        // Then: Ratings stay the same for equal bots
        assertThat(result.newRating1()).isEqualTo(1200);
        assertThat(result.newRating2()).isEqualTo(1200);
    }

    @Test
    void shouldGiveBiggerPointsToUnderdog() {
        // Given: Bot1 has lower rating (underdog)
        BotRating bot1Rating = createBotRating(1000, 0);
        BotRating bot2Rating = createBotRating(1400, 0);

        // When: Underdog wins
        EloService.EloUpdateResult result = eloService.calculateNewRatings(
            bot1Rating,
            bot2Rating,
            1.0
        );

        // Then: Underdog gains more points than they would against equal opponent
        int bot1Gain = result.newRating1() - 1000;
        assertThat(bot1Gain).isGreaterThan(16); // More than half the K-factor for new bots
    }

    @Test
    void shouldGiveSmallerPointsToFavorite() {
        // Given: Bot1 has higher rating (favorite)
        BotRating bot1Rating = createBotRating(1400, 0);
        BotRating bot2Rating = createBotRating(1000, 0);

        // When: Favorite wins (as expected)
        EloService.EloUpdateResult result = eloService.calculateNewRatings(
            bot1Rating,
            bot2Rating,
            1.0
        );

        // Then: Favorite gains fewer points than they would against equal opponent
        int bot1Gain = result.newRating1() - 1400;
        assertThat(bot1Gain).isLessThan(16); // Less than half the K-factor for new bots
    }

    @Test
    void shouldUseHigherKFactorForNewBots() {
        // Given: Bot with less than 30 matches (K=32)
        BotRating newBot = createBotRating(1200, 10);
        BotRating experiencedBot = createBotRating(1200, 30);

        // When: Both win against equal opponent
        BotRating opponent1 = createBotRating(1200, 0);
        BotRating opponent2 = createBotRating(1200, 0);

        EloService.EloUpdateResult newBotResult = eloService.calculateNewRatings(
            newBot,
            opponent1,
            1.0
        );

        EloService.EloUpdateResult experiencedBotResult = eloService.calculateNewRatings(
            experiencedBot,
            opponent2,
            1.0
        );

        // Then: New bot gains more points (K=32 vs K=16)
        int newBotGain = newBotResult.newRating1() - 1200;
        int experiencedBotGain = experiencedBotResult.newRating1() - 1200;
        assertThat(newBotGain).isEqualTo(16); // K=32 * 0.5 expected score
        assertThat(experiencedBotGain).isEqualTo(8); // K=16 * 0.5 expected score
    }

    @Test
    void shouldUseLowerKFactorForEstablishedBots() {
        // Given: Bot with 30+ matches (K=16)
        BotRating establishedBot = createBotRating(1200, 30);
        BotRating opponent = createBotRating(1200, 0);

        // When: Bot wins
        EloService.EloUpdateResult result = eloService.calculateNewRatings(
            establishedBot,
            opponent,
            1.0
        );

        // Then: Gains exactly K * (actual - expected) = 16 * (1.0 - 0.5) = 8 points
        assertThat(result.newRating1()).isEqualTo(1208);
    }

    @Test
    void shouldHandleLoss() {
        // Given: Two equal bots
        BotRating bot1Rating = createBotRating(1200, 0);
        BotRating bot2Rating = createBotRating(1200, 0);

        // When: Bot1 loses (score = 0.0)
        EloService.EloUpdateResult result = eloService.calculateNewRatings(
            bot1Rating,
            bot2Rating,
            0.0
        );

        // Then: Bot1 loses points, Bot2 gains points
        assertThat(result.newRating1()).isLessThan(1200);
        assertThat(result.newRating2()).isGreaterThan(1200);
        assertThat(result.newRating1()).isEqualTo(1184); // 1200 - 16
        assertThat(result.newRating2()).isEqualTo(1216); // 1200 + 16
    }

    /**
     * Helper to create a BotRating with specific ELO and matches played.
     */
    private BotRating createBotRating(int eloRating, int matchesPlayed) {
        Bot dummyBot = Bot.builder()
            .userId(1L)
            .name("TestBot")
            .game(Game.CHESS)
            .endpoint("http://test")
            .active(true)
            .build();

        BotRating rating = new BotRating(dummyBot, Game.CHESS, null);
        rating.updateRating(eloRating);

        // Simulate matches to set matchesPlayed count
        for (int i = 0; i < matchesPlayed; i++) {
            rating.recordMatchResult(com.algorena.games.domain.MatchResult.DRAW);
        }

        return rating;
    }
}
