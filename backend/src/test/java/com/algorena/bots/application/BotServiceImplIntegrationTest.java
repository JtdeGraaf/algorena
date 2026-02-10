package com.algorena.bots.application;

import com.algorena.bots.data.BotRepository;
import com.algorena.bots.domain.Bot;
import com.algorena.bots.domain.Game;
import com.algorena.bots.dto.BotDTO;
import com.algorena.bots.dto.CreateBotRequest;
import com.algorena.common.exception.DataNotFoundException;
import com.algorena.test.config.AbstractIntegrationTest;
import com.algorena.users.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BotServiceImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private BotService botService;

    @Autowired
    private BotRepository botRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void createBot_shouldCreateBotForCurrentUser() {
        // Given
        CreateBotRequest request = new CreateBotRequest(
                "Test Bot",
                "Test Description",
                Game.CHESS,
                "http://localhost:8080/bot-endpoint",
                "secret-api"
        );

        // When
        BotDTO result = botService.createBot(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Test Bot");
        assertThat(result.description()).isEqualTo("Test Description");
        assertThat(result.game()).isEqualTo(Game.CHESS);
        assertThat(result.active()).isTrue();
        assertThat(result.id()).isNotNull();
        assertThat(result.created()).isNotNull();
        assertThat(result.lastUpdated()).isNotNull();

        // Verify bot was saved to database
        Bot savedBot = botRepository.findById(result.id()).orElseThrow();
        assertThat(savedBot.getUserId()).isEqualTo(testUser.getId());
        assertThat(savedBot.getName()).isEqualTo("Test Bot");
        assertThat(savedBot.getDescription()).isEqualTo("Test Description");
        assertThat(savedBot.getGame()).isEqualTo(Game.CHESS);
        assertThat(savedBot.isActive()).isTrue();
    }

    @Test
    void getUserBots_shouldReturnOnlyBotsForCurrentUser() {
        // Given - create bots for test user
        CreateBotRequest request1 = new CreateBotRequest("Bot 1", null, Game.CHESS, "http://localhost:8080/bot-endpoint", "secret-api");
        CreateBotRequest request2 = new CreateBotRequest("Bot 2", "Description", Game.CHESS, "http://localhost:8080/bot-endpoint", "secret-api");

        BotDTO bot1 = botService.createBot(request1);
        BotDTO bot2 = botService.createBot(request2);

        // Create a different user with a bot
        User otherUser = createTestUser("otheruser", "other@algorena.dev");
        Bot otherUserBot = Bot.builder()
                .userId(otherUser.getId())
                .name("Other User Bot")
                .game(Game.CHESS)
                .endpoint("http://localhost:8080/other-user-bot")
                .active(true)
                .build();
        botRepository.save(otherUserBot);

        // When
        var userBots = botService.getBots(Pageable.unpaged(), testUser.getId(), null, null, null);

        // Then
        assertThat(userBots.getContent()).hasSize(2);
        assertThat(userBots.getContent().stream().map(BotDTO::name))
                .containsExactlyInAnyOrder("Bot 1", "Bot 2");
        assertThat(userBots.getContent().stream().map(BotDTO::id))
                .containsExactlyInAnyOrder(bot1.id(), bot2.id());
    }

    @Test
    void getBotById_shouldReturnBotForCurrentUser() {
        // Given
        CreateBotRequest request = new CreateBotRequest("Test Bot", "Description", Game.CHESS, "http://localhost:8080/bot-endpoint", "secret-api");
        BotDTO createdBot = botService.createBot(request);

        // When
        BotDTO result = botService.getBotById(createdBot.id());

        // Then
        assertThat(result.id()).isEqualTo(createdBot.id());
        assertThat(result.name()).isEqualTo("Test Bot");
        assertThat(result.description()).isEqualTo("Description");
        assertThat(result.game()).isEqualTo(Game.CHESS);
        assertThat(result.active()).isTrue();
    }

    @Test
    void getBotById_shouldThrowExceptionForOtherUserBot() {
        // Given - create bot for different user
        User otherUser = createTestUser("otheruser", "other@algorena.dev");
        Bot otherUserBot = Bot.builder()
                .userId(otherUser.getId())
                .name("Other Bot")
                .game(Game.CHESS)
                .endpoint("http://localhost:8080/other-user-bot")
                .active(true)
                .build();
        Bot savedBot = botRepository.save(otherUserBot);

        // When & Then
        assertThatThrownBy(() -> botService.getBotById(savedBot.getId()))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessage("Bot not found");
    }

    @Test
    void deleteBot_shouldSoftDeleteBotForCurrentUser() {
        // Given
        CreateBotRequest request = new CreateBotRequest("Test Bot", null, Game.CHESS, "http://localhost:8080/bot-endpoint", "secret-api");
        BotDTO createdBot = botService.createBot(request);

        // When
        botService.deleteBot(createdBot.id());

        // Then - bot is soft deleted (marked as deleted but still in DB)
        Bot deletedBot = botRepository.findById(createdBot.id()).orElseThrow();
        assertThat(deletedBot.isDeleted()).isTrue();
        assertThat(deletedBot.isActive()).isFalse();

        // And - bot is not returned by normal queries
        assertThat(botRepository.findByIdAndUserIdAndDeletedFalse(createdBot.id(), testUser.getId())).isEmpty();
    }

    @Test
    void deleteBot_shouldThrowExceptionForOtherUserBot() {
        // Given - create bot for different user
        User otherUser = createTestUser("otheruser", "other@algorena.dev");
        Bot otherUserBot = Bot.builder()
                .userId(otherUser.getId())
                .name("Other Bot")
                .game(Game.CHESS)
                .endpoint("http://localhost:8080/other-user-bot")
                .active(true)
                .build();
        Bot savedBot = botRepository.save(otherUserBot);

        // When & Then
        assertThatThrownBy(() -> botService.deleteBot(savedBot.getId()))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessage("Bot not found");

        // Bot should still exist
        assertThat(botRepository.findById(savedBot.getId())).isPresent();
    }

}

