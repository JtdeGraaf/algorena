package com.algorena.bots.application;

import com.algorena.bots.data.BotApiKeyRepository;
import com.algorena.bots.data.BotRepository;
import com.algorena.bots.domain.Bot;
import com.algorena.bots.domain.BotApiKey;
import com.algorena.bots.domain.Game;
import com.algorena.bots.dto.*;
import com.algorena.common.exception.DataNotFoundException;
import com.algorena.common.exception.ForbiddenException;
import com.algorena.test.config.AbstractIntegrationTest;
import com.algorena.users.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BotServiceImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private BotService botService;

    @Autowired
    private BotRepository botRepository;

    @Autowired
    private BotApiKeyRepository apiKeyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void createBot_shouldCreateBotForCurrentUser() {
        // Given
        CreateBotRequest request = new CreateBotRequest(
                "Test Bot",
                "Test Description",
                Game.CHESS
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
        CreateBotRequest request1 = new CreateBotRequest("Bot 1", null, Game.CHESS);
        CreateBotRequest request2 = new CreateBotRequest("Bot 2", "Description", Game.CHESS);

        BotDTO bot1 = botService.createBot(request1);
        BotDTO bot2 = botService.createBot(request2);

        // Create a different user with a bot
        User otherUser = createTestUser("otheruser", "other@algorena.dev");
        Bot otherUserBot = Bot.builder()
                .userId(otherUser.getId())
                .name("Other User Bot")
                .game(Game.CHESS)
                .active(true)
                .build();
        botRepository.save(otherUserBot);

        // When
        List<BotDTO> userBots = botService.getUserBots();

        // Then
        assertThat(userBots).hasSize(2);
        assertThat(userBots.stream().map(BotDTO::name))
                .containsExactlyInAnyOrder("Bot 1", "Bot 2");
        assertThat(userBots.stream().map(BotDTO::id))
                .containsExactlyInAnyOrder(bot1.id(), bot2.id());
    }

    @Test
    void getBotById_shouldReturnBotForCurrentUser() {
        // Given
        CreateBotRequest request = new CreateBotRequest("Test Bot", "Description", Game.CHESS);
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
                .active(true)
                .build();
        Bot savedBot = botRepository.save(otherUserBot);

        // When & Then
        assertThatThrownBy(() -> botService.getBotById(savedBot.getId()))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessage("Bot not found");
    }

    @Test
    void deleteBot_shouldDeleteBotForCurrentUser() {
        // Given
        CreateBotRequest request = new CreateBotRequest("Test Bot", null, Game.CHESS);
        BotDTO createdBot = botService.createBot(request);

        // When
        botService.deleteBot(createdBot.id());

        // Then
        assertThat(botRepository.findById(createdBot.id())).isEmpty();
    }

    @Test
    void deleteBot_shouldThrowExceptionForOtherUserBot() {
        // Given - create bot for different user
        User otherUser = createTestUser("otheruser", "other@algorena.dev");
        Bot otherUserBot = Bot.builder()
                .userId(otherUser.getId())
                .name("Other Bot")
                .game(Game.CHESS)
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

    @Test
    void createApiKey_shouldCreateApiKeyForBot() {
        // Given
        CreateBotRequest botRequest = new CreateBotRequest("Test Bot", null, Game.CHESS);
        BotDTO bot = botService.createBot(botRequest);
        CreateApiKeyRequest apiKeyRequest = new CreateApiKeyRequest("API Key 1");

        // When
        CreateApiKeyResponse response = botService.createApiKey(bot.id(), apiKeyRequest);

        // Then
        assertThat(response.apiKey()).isNotNull();
        assertThat(response.apiKey().name()).isEqualTo("API Key 1");
        assertThat(response.apiKey().keyPrefix()).isNotBlank();
        assertThat(response.apiKey().keyPrefix()).startsWith("alg_");
        assertThat(response.apiKey().revoked()).isFalse();
        assertThat(response.apiKey().created()).isNotNull();
        assertThat(response.plainTextKey()).isNotBlank();
        assertThat(response.plainTextKey()).startsWith("alg_");
        assertThat(response.plainTextKey().length()).isGreaterThan(20);

        // Verify API key was saved
        List<BotApiKey> savedKeys = apiKeyRepository.findByBotId(bot.id());
        assertThat(savedKeys).hasSize(1);
        assertThat(savedKeys.getFirst().getName()).isEqualTo("API Key 1");
        assertThat(savedKeys.getFirst().getKeyHash()).isNotBlank();
        assertThat(passwordEncoder.matches(response.plainTextKey(), savedKeys.getFirst().getKeyHash())).isTrue();
    }

    @Test
    void createApiKey_shouldWorkWithoutName() {
        // Given
        CreateBotRequest botRequest = new CreateBotRequest("Test Bot", null, Game.CHESS);
        BotDTO bot = botService.createBot(botRequest);
        CreateApiKeyRequest apiKeyRequest = new CreateApiKeyRequest(null);

        // When
        CreateApiKeyResponse response = botService.createApiKey(bot.id(), apiKeyRequest);

        // Then
        assertThat(response.apiKey()).isNotNull();
        assertThat(response.apiKey().name()).isNull();
        assertThat(response.plainTextKey()).startsWith("alg_");
    }

    @Test
    void getBotApiKeys_shouldReturnKeysForBot() {
        // Given
        CreateBotRequest botRequest = new CreateBotRequest("Test Bot", null, Game.CHESS);
        BotDTO bot = botService.createBot(botRequest);

        CreateApiKeyRequest request1 = new CreateApiKeyRequest("Key 1");
        CreateApiKeyRequest request2 = new CreateApiKeyRequest("Key 2");

        botService.createApiKey(bot.id(), request1);
        botService.createApiKey(bot.id(), request2);

        // When
        List<ApiKeyDTO> apiKeys = botService.getBotApiKeys(bot.id());

        // Then
        assertThat(apiKeys).hasSize(2);
        assertThat(apiKeys.stream().map(ApiKeyDTO::name))
                .containsExactlyInAnyOrder("Key 1", "Key 2");
        assertThat(apiKeys).allMatch(key -> !key.revoked());
        assertThat(apiKeys).allMatch(key -> key.keyPrefix().startsWith("alg_"));
    }

    @Test
    void revokeApiKey_shouldRevokeApiKey() {
        // Given
        CreateBotRequest botRequest = new CreateBotRequest("Test Bot", null, Game.CHESS);
        BotDTO bot = botService.createBot(botRequest);
        CreateApiKeyRequest apiKeyRequest = new CreateApiKeyRequest("Test Key");
        CreateApiKeyResponse createdKey = botService.createApiKey(bot.id(), apiKeyRequest);

        // When
        botService.revokeApiKey(bot.id(), createdKey.apiKey().id());

        // Then
        List<BotApiKey> keys = apiKeyRepository.findByBotId(bot.id());
        assertThat(keys).hasSize(1);
        assertThat(keys.getFirst().isRevoked()).isTrue();

        // Verify through service as well
        List<ApiKeyDTO> apiKeys = botService.getBotApiKeys(bot.id());
        assertThat(apiKeys.getFirst().revoked()).isTrue();
    }

    @Test
    void revokeApiKey_shouldThrowExceptionForWrongBot() {
        // Given - bot for current user
        CreateBotRequest botRequest = new CreateBotRequest("Test Bot", null, Game.CHESS);
        BotDTO bot = botService.createBot(botRequest);
        CreateApiKeyRequest apiKeyRequest = new CreateApiKeyRequest("Test Key");
        CreateApiKeyResponse createdKey = botService.createApiKey(bot.id(), apiKeyRequest);

        // Create another bot
        CreateBotRequest botRequest2 = new CreateBotRequest("Test Bot 2", null, Game.CHESS);
        BotDTO bot2 = botService.createBot(botRequest2);

        // When & Then - trying to revoke key using wrong bot ID
        assertThatThrownBy(() -> botService.revokeApiKey(bot2.id(), createdKey.apiKey().id()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("API key does not belong to this bot");
    }

    @Test
    void revokeApiKey_shouldThrowExceptionForNonExistentKey() {
        // Given
        CreateBotRequest botRequest = new CreateBotRequest("Test Bot", null, Game.CHESS);
        BotDTO bot = botService.createBot(botRequest);

        // When & Then
        assertThatThrownBy(() -> botService.revokeApiKey(bot.id(), 99999L))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessage("API key not found");
    }

    @Test
    void validateApiKey_shouldReturnBotForValidKey() {
        // Given
        CreateBotRequest botRequest = new CreateBotRequest("Test Bot", null, Game.CHESS);
        BotDTO bot = botService.createBot(botRequest);
        CreateApiKeyRequest apiKeyRequest = new CreateApiKeyRequest("Test Key");
        CreateApiKeyResponse createdKey = botService.createApiKey(bot.id(), apiKeyRequest);

        // When
        Bot validatedBot = botService.validateApiKey(createdKey.plainTextKey());

        // Then
        assertThat(validatedBot.getId()).isEqualTo(bot.id());
        assertThat(validatedBot.getName()).isEqualTo("Test Bot");
        assertThat(validatedBot.getUserId()).isEqualTo(testUser.getId());
        assertThat(validatedBot.isActive()).isTrue();
    }

    @Test
    void validateApiKey_shouldThrowExceptionForInvalidKey() {
        // When & Then
        assertThatThrownBy(() -> botService.validateApiKey("alg_invalid_key_that_does_not_exist"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Invalid API key");
    }

    @Test
    void validateApiKey_shouldThrowExceptionForWrongPrefix() {
        // When & Then
        assertThatThrownBy(() -> botService.validateApiKey("wrong_prefix_key"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Invalid API key format");
    }

    @Test
    void validateApiKey_shouldThrowExceptionForRevokedKey() {
        // Given
        CreateBotRequest botRequest = new CreateBotRequest("Test Bot", null, Game.CHESS);
        BotDTO bot = botService.createBot(botRequest);
        CreateApiKeyRequest apiKeyRequest = new CreateApiKeyRequest("Test Key");
        CreateApiKeyResponse createdKey = botService.createApiKey(bot.id(), apiKeyRequest);

        // Revoke the key
        botService.revokeApiKey(bot.id(), createdKey.apiKey().id());

        // When & Then
        assertThatThrownBy(() -> botService.validateApiKey(createdKey.plainTextKey()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Invalid API key");
    }

    @Test
    void validateApiKey_shouldThrowExceptionForInactiveBot() {
        // Given
        CreateBotRequest botRequest = new CreateBotRequest("Test Bot", null, Game.CHESS);
        BotDTO bot = botService.createBot(botRequest);
        CreateApiKeyRequest apiKeyRequest = new CreateApiKeyRequest("Test Key");
        CreateApiKeyResponse createdKey = botService.createApiKey(bot.id(), apiKeyRequest);

        // Deactivate the bot using domain method
        Bot botEntity = botRepository.findById(bot.id()).orElseThrow();
        botEntity.deactivate();
        botRepository.save(botEntity);

        // When & Then
        assertThatThrownBy(() -> botService.validateApiKey(createdKey.plainTextKey()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Bot is not active");
    }

    @Test
    void validateApiKey_shouldWorkMultipleTimes() {
        // Given
        CreateBotRequest botRequest = new CreateBotRequest("Test Bot", null, Game.CHESS);
        BotDTO bot = botService.createBot(botRequest);
        CreateApiKeyRequest apiKeyRequest = new CreateApiKeyRequest("Test Key");
        CreateApiKeyResponse createdKey = botService.createApiKey(bot.id(), apiKeyRequest);

        // When - validate multiple times
        Bot validatedBot1 = botService.validateApiKey(createdKey.plainTextKey());
        Bot validatedBot2 = botService.validateApiKey(createdKey.plainTextKey());
        Bot validatedBot3 = botService.validateApiKey(createdKey.plainTextKey());

        // Then
        assertThat(validatedBot1.getId()).isEqualTo(bot.id());
        assertThat(validatedBot2.getId()).isEqualTo(bot.id());
        assertThat(validatedBot3.getId()).isEqualTo(bot.id());
    }

    @Test
    void integrationTest_completeWorkflow() {
        // Given
        CreateBotRequest botRequest = new CreateBotRequest("Chess Bot", "My chess bot", Game.CHESS);

        // When - Create bot
        BotDTO bot = botService.createBot(botRequest);

        // Then - Verify bot creation
        assertThat(bot.name()).isEqualTo("Chess Bot");
        assertThat(bot.description()).isEqualTo("My chess bot");
        assertThat(bot.game()).isEqualTo(Game.CHESS);
        assertThat(bot.active()).isTrue();

        // When - Get user bots
        List<BotDTO> userBots = botService.getUserBots();

        // Then - Verify bot is in user's bots
        assertThat(userBots).hasSize(1);
        assertThat(userBots.getFirst().id()).isEqualTo(bot.id());

        // When - Create multiple API keys
        CreateApiKeyRequest apiKeyRequest1 = new CreateApiKeyRequest("Main API Key");
        CreateApiKeyRequest apiKeyRequest2 = new CreateApiKeyRequest("Backup API Key");
        CreateApiKeyResponse apiKeyResponse1 = botService.createApiKey(bot.id(), apiKeyRequest1);
        CreateApiKeyResponse apiKeyResponse2 = botService.createApiKey(bot.id(), apiKeyRequest2);

        // Then - Verify API key creation
        assertThat(apiKeyResponse1.apiKey().name()).isEqualTo("Main API Key");
        assertThat(apiKeyResponse1.plainTextKey()).startsWith("alg_");
        assertThat(apiKeyResponse2.apiKey().name()).isEqualTo("Backup API Key");

        // When - Get API keys
        List<ApiKeyDTO> apiKeys = botService.getBotApiKeys(bot.id());

        // Then - Verify API keys are listed
        assertThat(apiKeys).hasSize(2);
        assertThat(apiKeys.stream().map(ApiKeyDTO::name))
                .containsExactlyInAnyOrder("Main API Key", "Backup API Key");

        // When - Validate API key
        Bot validatedBot = botService.validateApiKey(apiKeyResponse1.plainTextKey());

        // Then - Verify validation returns correct bot
        assertThat(validatedBot.getId()).isEqualTo(bot.id());
        assertThat(validatedBot.getName()).isEqualTo("Chess Bot");

        // When - Revoke one key
        botService.revokeApiKey(bot.id(), apiKeyResponse1.apiKey().id());

        // Then - First key should be revoked, second should still work
        assertThatThrownBy(() -> botService.validateApiKey(apiKeyResponse1.plainTextKey()))
                .isInstanceOf(ForbiddenException.class);
        Bot validatedBot2 = botService.validateApiKey(apiKeyResponse2.plainTextKey());
        assertThat(validatedBot2.getId()).isEqualTo(bot.id());

        // When - Delete bot
        botService.deleteBot(bot.id());

        // Then - Verify bot and all keys are deleted (cascade)
        List<BotDTO> finalBots = botService.getUserBots();
        assertThat(finalBots).isEmpty();
        assertThat(botRepository.findById(bot.id())).isEmpty();
        assertThat(apiKeyRepository.findByBotId(bot.id())).isEmpty();
    }
}

