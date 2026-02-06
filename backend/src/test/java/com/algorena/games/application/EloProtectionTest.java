package com.algorena.games.application;

import com.algorena.bots.domain.Bot;
import com.algorena.bots.domain.Game;
import com.algorena.common.exception.BadRequestException;
import com.algorena.games.dto.CreateMatchRequest;
import com.algorena.test.config.AbstractIntegrationTest;
import com.algorena.users.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for ELO protection rules.
 */
class EloProtectionTest extends AbstractIntegrationTest {

    @Autowired
    private MatchService matchService;

    @Autowired
    private RatingUpdateService ratingUpdateService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User user1;
    private User user2;
    private Bot user1Bot1;
    private Bot user1Bot2;
    private Bot user2Bot1;

    @BeforeEach
    void setUp() {
        // Create two users
        user1 = testUser; // From AbstractIntegrationTest
        user2 = createTestUser("user2", "user2@test.com");

        // Create bots for each user
        user1Bot1 = createTestBot(user1, "User1-Bot1", Game.CHESS, "http://localhost:8001");
        user1Bot2 = createTestBot(user1, "User1-Bot2", Game.CHESS, "http://localhost:8002");
        user2Bot1 = createTestBot(user2, "User2-Bot1", Game.CHESS, "http://localhost:8003");
    }

    @Test
    void shouldAllowMatchCreationBetweenSameUserBots() {
        // Given: A request to create a match between same user's bots
        CreateMatchRequest request = new CreateMatchRequest(
            user1Bot1.getId(),
            user1Bot2.getId(),
            Game.CHESS
        );

        // When: Creating and finishing the match
        var matchDTO = matchService.createMatch(request);

        // Simulate a finished match by creating one directly
        var finishedMatch = createFinishedMatchDirectly(user1Bot1, user1Bot2, 1.0, 0.0);
        ratingUpdateService.updateRatingsAfterMatch(finishedMatch);

        // Then: Match should be created successfully
        assertThat(matchDTO).isNotNull();
        assertThat(matchDTO.status()).isNotNull();

        // And: ELO ratings should NOT be created
        var rating1 = botRatingRepository.findByBotAndGameAndLeaderboard(user1Bot1, Game.CHESS, null);
        var rating2 = botRatingRepository.findByBotAndGameAndLeaderboard(user1Bot2, Game.CHESS, null);

        assertThat(rating1).isEmpty();
        assertThat(rating2).isEmpty();
    }

    @Test
    void shouldAllowMatchCreationBetweenDifferentUserBots() {
        // Given: A request to create a match between different users' bots
        CreateMatchRequest request = new CreateMatchRequest(
            user1Bot1.getId(),
            user2Bot1.getId(),
            Game.CHESS
        );

        // When: Creating the match
        var match = matchService.createMatch(request);

        // Then: Match should be created successfully
        assertThat(match).isNotNull();
        assertThat(match.status()).isNotNull();
    }

    @Test
    void shouldSkipRatingUpdatesForSameUserBots() {
        // Given: A match between same user's bots (created directly, bypassing validation)
        var match = createFinishedMatchDirectly(user1Bot1, user1Bot2, 1.0, 0.0);

        // When: Attempting to update ratings
        ratingUpdateService.updateRatingsAfterMatch(match);

        // Then: No ratings should be created
        var rating1 = botRatingRepository.findByBotAndGameAndLeaderboard(user1Bot1, Game.CHESS, null);
        var rating2 = botRatingRepository.findByBotAndGameAndLeaderboard(user1Bot2, Game.CHESS, null);

        assertThat(rating1).isEmpty();
        assertThat(rating2).isEmpty();
    }

    @Test
    void shouldUpdateRatingsForDifferentUserBots() {
        // Given: A match between different users' bots
        var match = createFinishedMatchDirectly(user1Bot1, user2Bot1, 1.0, 0.0);

        // When: Updating ratings
        ratingUpdateService.updateRatingsAfterMatch(match);

        // Then: Ratings should be created and updated
        var rating1 = botRatingRepository.findByBotAndGameAndLeaderboard(user1Bot1, Game.CHESS, null);
        var rating2 = botRatingRepository.findByBotAndGameAndLeaderboard(user2Bot1, Game.CHESS, null);

        assertThat(rating1).isPresent();
        assertThat(rating2).isPresent();
        assertThat(rating1.get().getMatchesPlayed()).isEqualTo(1);
        assertThat(rating2.get().getMatchesPlayed()).isEqualTo(1);
    }

    @Test
    void shouldSkipRatingUpdatesForRematchWithinCooldown() {
        // Given: First match between bots (updates ratings)
        var match1 = createFinishedMatchDirectly(user1Bot1, user2Bot1, 1.0, 0.0);
        ratingUpdateService.updateRatingsAfterMatch(match1);

        var rating1AfterFirst = botRatingRepository.findByBotAndGameAndLeaderboard(user1Bot1, Game.CHESS, null).orElseThrow();
        var rating2AfterFirst = botRatingRepository.findByBotAndGameAndLeaderboard(user2Bot1, Game.CHESS, null).orElseThrow();
        int elo1AfterFirst = rating1AfterFirst.getEloRating();
        int elo2AfterFirst = rating2AfterFirst.getEloRating();

        // When: Second match between same bots immediately after
        var match2 = createFinishedMatchDirectly(user1Bot1, user2Bot1, 1.0, 0.0);
        ratingUpdateService.updateRatingsAfterMatch(match2);

        // Then: Ratings should NOT change (still only 1 match counted)
        var rating1AfterSecond = botRatingRepository.findByBotAndGameAndLeaderboard(user1Bot1, Game.CHESS, null).orElseThrow();
        var rating2AfterSecond = botRatingRepository.findByBotAndGameAndLeaderboard(user2Bot1, Game.CHESS, null).orElseThrow();

        assertThat(rating1AfterSecond.getEloRating()).isEqualTo(elo1AfterFirst);
        assertThat(rating2AfterSecond.getEloRating()).isEqualTo(elo2AfterFirst);
        assertThat(rating1AfterSecond.getMatchesPlayed()).isEqualTo(1); // Still only 1
        assertThat(rating2AfterSecond.getMatchesPlayed()).isEqualTo(1); // Still only 1
    }

    @Test
    void shouldAllowRatingUpdatesAfterCooldownExpires() {
        // Given: First match (in the past, beyond cooldown)
        var match1 = createFinishedMatchInPast(user1Bot1, user2Bot1, 1.0, 0.0, 2); // 2 hours ago
        ratingUpdateService.updateRatingsAfterMatch(match1);

        var rating1AfterFirst = botRatingRepository.findByBotAndGameAndLeaderboard(user1Bot1, Game.CHESS, null).orElseThrow();
        int elo1AfterFirst = rating1AfterFirst.getEloRating();

        // When: Second match now (cooldown expired)
        var match2 = createFinishedMatchDirectly(user1Bot1, user2Bot1, 1.0, 0.0);
        ratingUpdateService.updateRatingsAfterMatch(match2);

        // Then: Ratings SHOULD change (cooldown expired)
        var rating1AfterSecond = botRatingRepository.findByBotAndGameAndLeaderboard(user1Bot1, Game.CHESS, null).orElseThrow();

        assertThat(rating1AfterSecond.getEloRating()).isNotEqualTo(elo1AfterFirst);
        assertThat(rating1AfterSecond.getMatchesPlayed()).isEqualTo(2); // Both matches counted
    }

    /**
     * Helper to create a finished match directly (bypassing service validation).
     * Used for testing defense-in-depth scenarios.
     */
    private com.algorena.games.domain.Match createFinishedMatchDirectly(Bot bot1, Bot bot2, double score1, double score2) {
        var match = com.algorena.games.domain.Match.builder()
            .game(Game.CHESS)
            .status(com.algorena.games.domain.MatchStatus.FINISHED)
            .build();
        match.finish(); // Sets finishedAt timestamp

        var p1 = com.algorena.games.domain.MatchParticipant.builder()
            .match(match)
            .bot(bot1)
            .playerIndex(0)
            .build();
        p1.recordScore(score1);
        match.addParticipant(p1);

        var p2 = com.algorena.games.domain.MatchParticipant.builder()
            .match(match)
            .bot(bot2)
            .playerIndex(1)
            .build();
        p2.recordScore(score2);
        match.addParticipant(p2);

        return matchRepository.save(match);
    }

    /**
     * Helper to create a finished match in the past (for cooldown testing).
     */
    private com.algorena.games.domain.Match createFinishedMatchInPast(Bot bot1, Bot bot2, double score1, double score2, int hoursAgo) {
        var match = com.algorena.games.domain.Match.builder()
            .game(Game.CHESS)
            .status(com.algorena.games.domain.MatchStatus.FINISHED)
            .build();

        var p1 = com.algorena.games.domain.MatchParticipant.builder()
            .match(match)
            .bot(bot1)
            .playerIndex(0)
            .build();
        p1.recordScore(score1);
        match.addParticipant(p1);

        var p2 = com.algorena.games.domain.MatchParticipant.builder()
            .match(match)
            .bot(bot2)
            .playerIndex(1)
            .build();
        p2.recordScore(score2);
        match.addParticipant(p2);

        match = matchRepository.save(match);

        // Manually set finishedAt in the past using JdbcTemplate
        java.time.LocalDateTime finishedAt = java.time.LocalDateTime.now().minusHours(hoursAgo);
        jdbcTemplate.update(
            "UPDATE matches SET finished_at = ? WHERE id = ?::uuid",
            finishedAt,
            match.getId().toString()
        );

        // Clear and reload to get fresh data with updated timestamp, eagerly fetching participants
        matchRepository.flush();
        return matchRepository.findByIdWithParticipants(match.getId()).orElseThrow();
    }
}
