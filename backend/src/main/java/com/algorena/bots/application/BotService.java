package com.algorena.bots.application;

import com.algorena.bots.domain.Game;
import com.algorena.bots.dto.*;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BotService {
    BotDTO createBot(CreateBotRequest request);

    Page<BotDTO> getBots(Pageable pageable, @Nullable Long userId, @Nullable String name, @Nullable Game game, @Nullable Boolean active);

    BotDTO getBotById(Long botId);

    BotDTO updateBot(Long botId, UpdateBotRequest request);

    void deleteBot(Long botId);

    BotStatsDTO getBotStats(Long botId);
}
