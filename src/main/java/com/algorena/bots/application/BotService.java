package com.algorena.bots.application;

import com.algorena.bots.domain.Bot;
import com.algorena.bots.dto.*;

import java.util.List;

public interface BotService {
    BotDTO createBot(CreateBotRequest request);

    List<BotDTO> getUserBots();

    BotDTO getBotById(Long botId);

    void deleteBot(Long botId);

    CreateApiKeyResponse createApiKey(Long botId, CreateApiKeyRequest request);

    List<ApiKeyDTO> getBotApiKeys(Long botId);

    void revokeApiKey(Long botId, Long apiKeyId);

    Bot validateApiKey(String apiKey);
}

