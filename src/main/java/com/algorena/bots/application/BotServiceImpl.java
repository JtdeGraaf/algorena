package com.algorena.bots.application;

import com.algorena.bots.data.BotApiKeyRepository;
import com.algorena.bots.data.BotRepository;
import com.algorena.bots.domain.Bot;
import com.algorena.bots.domain.BotApiKey;
import com.algorena.bots.dto.*;
import com.algorena.common.exception.DataNotFoundException;
import com.algorena.common.exception.ForbiddenException;
import com.algorena.common.exception.InternalServerException;
import com.algorena.security.CurrentUser;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

@Service
@AllArgsConstructor
public class BotServiceImpl implements BotService {

    private static final String API_KEY_PREFIX = "alg_";
    private static final int API_KEY_LENGTH = 32;

    private final BotRepository botRepository;
    private final BotApiKeyRepository apiKeyRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUser currentUser;

    @Override
    @Transactional
    public BotDTO createBot(CreateBotRequest request) {
        Bot bot = Bot.builder()
                .userId(currentUser.id())
                .name(request.name())
                .description(request.description())
                .game(request.game())
                .active(true)
                .build();

        bot = botRepository.save(bot);
        return toDTO(bot);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BotDTO> getUserBots() {
        return botRepository.findByUserId(currentUser.id()).stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BotDTO getBotById(Long botId) {
        Bot bot = botRepository.findByIdAndUserId(botId, currentUser.id())
                .orElseThrow(() -> new DataNotFoundException("Bot not found"));
        return toDTO(bot);
    }

    @Override
    @Transactional
    public void deleteBot(Long botId) {
        Bot bot = botRepository.findByIdAndUserId(botId, currentUser.id())
                .orElseThrow(() -> new DataNotFoundException("Bot not found"));
        botRepository.delete(bot);
    }

    @Override
    @Transactional
    public CreateApiKeyResponse createApiKey(Long botId, CreateApiKeyRequest request) {
        Bot bot = botRepository.findByIdAndUserId(botId, currentUser.id())
                .orElseThrow(() -> new DataNotFoundException("Bot not found"));

        // Generate random API key
        String plainTextKey = generateApiKey();
        String fullKey = API_KEY_PREFIX + plainTextKey;

        // Hash the key for storage
        String keyHash = passwordEncoder.encode(fullKey);
        if (keyHash == null) {
            throw new InternalServerException("Failed to hash API key");
        }

        // Extract prefix for display
        String keyPrefix = fullKey.substring(0, Math.min(10, fullKey.length()));

        BotApiKey apiKey = BotApiKey.builder()
                .botId(bot.getId())
                .keyHash(keyHash)
                .keyPrefix(keyPrefix)
                .name(request.name())
                .revoked(false)
                .build();

        apiKey = apiKeyRepository.save(apiKey);

        return new CreateApiKeyResponse(
                toApiKeyDTO(apiKey),
                fullKey
        );
    }

    @Override
    public List<ApiKeyDTO> getBotApiKeys(Long botId) {
        Bot bot = botRepository.findByIdAndUserId(botId, currentUser.id())
                .orElseThrow(() -> new DataNotFoundException("Bot not found"));

        return apiKeyRepository.findByBotId(bot.getId()).stream()
                .map(this::toApiKeyDTO)
                .toList();
    }

    @Override
    public void revokeApiKey(Long botId, Long apiKeyId) {
        Bot bot = botRepository.findByIdAndUserId(botId, currentUser.id())
                .orElseThrow(() -> new DataNotFoundException("Bot not found"));

        BotApiKey apiKey = apiKeyRepository.findById(apiKeyId)
                .orElseThrow(() -> new DataNotFoundException("API key not found"));

        if (!apiKey.getBotId().equals(bot.getId())) {
            throw new ForbiddenException("API key does not belong to this bot");
        }

        apiKey.revoke();
        apiKeyRepository.save(apiKey);
    }

    @Override
    @Transactional(readOnly = true)
    public Bot validateApiKey(String apiKey) {
        if (!apiKey.startsWith(API_KEY_PREFIX)) {
            throw new ForbiddenException("Invalid API key format");
        }

        // Find all non-revoked keys and check against them
        List<BotApiKey> allKeys = apiKeyRepository.findAll().stream()
                .filter(key -> !key.isRevoked())
                .toList();

        for (BotApiKey key : allKeys) {
            if (passwordEncoder.matches(apiKey, key.getKeyHash())) {
                Bot bot = botRepository.findById(key.getBotId())
                        .orElseThrow(() -> new DataNotFoundException("Bot not found"));

                if (!bot.isActive()) {
                    throw new ForbiddenException("Bot is not active");
                }

                // Update last used timestamp
                key.markAsUsed();
                apiKeyRepository.save(key);

                return bot;
            }
        }

        throw new ForbiddenException("Invalid API key");
    }

    private String generateApiKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[API_KEY_LENGTH];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private BotDTO toDTO(Bot bot) {
        return new BotDTO(
                bot.getId(),
                bot.getName(),
                bot.getDescription(),
                bot.getGame(),
                bot.isActive(),
                bot.getCreated(),
                bot.getLastUpdated()
        );
    }

    private ApiKeyDTO toApiKeyDTO(BotApiKey apiKey) {
        return new ApiKeyDTO(
                apiKey.getId(),
                apiKey.getName(),
                apiKey.getKeyPrefix(),
                apiKey.getLastUsed(),
                apiKey.getExpiresAt(),
                apiKey.isRevoked(),
                apiKey.getCreated()
        );
    }
}

