package com.algorena.games.controllers;

import com.algorena.bots.domain.Game;
import com.algorena.games.application.LeaderboardService;
import com.algorena.games.dto.BotLeaderboardEntryDTO;
import com.algorena.test.config.AbstractIntegrationTest;
import com.algorena.users.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;

class LeaderboardServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private LeaderboardService leaderboardService;

    private User testUser2;

    @BeforeEach
    void setUp() {
        // Create second test user (testUser already created in AbstractIntegrationTest)
        testUser2 = createTestUser("user2", "user2@test.com");

        // Create test bots and ratings using helper methods
        var testBot1 = createTestBot(testUser, "Bot1", Game.CHESS, "http://localhost:8000/bot1");
        var testBot2 = createTestBot(testUser, "Bot2", Game.CHESS, "http://localhost:8000/bot2");
        var testBot3 = createTestBot(testUser2, "Bot3", Game.CHESS, "http://localhost:8000/bot3");

        createTestBotRating(testBot1, Game.CHESS, 1500, 10, 7, 2, 1);
        createTestBotRating(testBot2, Game.CHESS, 1300, 5, 2, 3, 0);
        createTestBotRating(testBot3, Game.CHESS, 1400, 8, 5, 3, 0);
    }

    @Test
    void shouldGetBotLeaderboard() {
        Page<BotLeaderboardEntryDTO> leaderboard = leaderboardService.getBotLeaderboard(
                Game.CHESS,
                PageRequest.of(0, 10)
        );

        assertThat(leaderboard.getContent()).hasSize(3);
        assertThat(leaderboard.getContent().get(0).rank()).isEqualTo(1);
        assertThat(leaderboard.getContent().get(0).eloRating()).isEqualTo(1500);
        assertThat(leaderboard.getContent().get(0).botName()).isEqualTo("Bot1");
        assertThat(leaderboard.getContent().get(1).rank()).isEqualTo(2);
        assertThat(leaderboard.getContent().get(1).eloRating()).isEqualTo(1400);
        assertThat(leaderboard.getContent().get(2).rank()).isEqualTo(3);
        assertThat(leaderboard.getContent().get(2).eloRating()).isEqualTo(1300);
    }

    @Test
    void shouldGetBotRanking() {
        // Get the first bot (highest rated)
        Page<BotLeaderboardEntryDTO> leaderboard = leaderboardService.getBotLeaderboard(
                Game.CHESS,
                PageRequest.of(0, 1)
        );
        Long topBotId = leaderboard.getContent().get(0).botId();

        BotLeaderboardEntryDTO ranking = leaderboardService.getBotRanking(topBotId, Game.CHESS);

        assertThat(ranking.rank()).isEqualTo(1);
        assertThat(ranking.botId()).isEqualTo(topBotId);
        assertThat(ranking.eloRating()).isEqualTo(1500);
        assertThat(ranking.matchesPlayed()).isEqualTo(10);
        assertThat(ranking.wins()).isEqualTo(7);
        assertThat(ranking.losses()).isEqualTo(2);
        assertThat(ranking.draws()).isEqualTo(1);
    }

    @Test
    void shouldPaginateBotLeaderboard() {
        Page<BotLeaderboardEntryDTO> leaderboard = leaderboardService.getBotLeaderboard(
                Game.CHESS,
                PageRequest.of(0, 2)
        );

        assertThat(leaderboard.getContent()).hasSize(2);
        assertThat(leaderboard.getContent().get(0).eloRating()).isEqualTo(1500);
        assertThat(leaderboard.getContent().get(1).eloRating()).isEqualTo(1400);
        assertThat(leaderboard.getTotalElements()).isEqualTo(3);
    }
}
