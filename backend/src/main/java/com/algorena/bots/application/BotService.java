package com.algorena.bots.application;

import com.algorena.bots.domain.Bot;
import com.algorena.bots.domain.Game;
import com.algorena.bots.dto.*;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BotService {
    BotDTO createBot(CreateBotRequest request);

    Page<BotDTO> getBots(Pageable pageable, @Nullable Long userId, @Nullable String name, @Nullable Game game, @Nullable Boolean active);

    BotDTO getBotById(Long botId);

    BotDTO updateBot(Long botId, UpdateBotRequest request);

    void deleteBot(Long botId);

    CreateApiKeyResponse createApiKey(Long botId, CreateApiKeyRequest request);

    List<ApiKeyDTO> getBotApiKeys(Long botId);

    void revokeApiKey(Long botId, Long apiKeyId);

    Bot validateApiKey(String apiKey);


    BotStatsDTO getBotStats(Long botId);
}

