package com.algorena.bots.application;

import com.algorena.bots.data.BotRepository;
import com.algorena.bots.domain.Bot;
import com.algorena.bots.domain.Game;
import com.algorena.bots.dto.*;
import com.algorena.bots.mapper.BotMapper;
import com.algorena.common.exception.DataNotFoundException;
import com.algorena.games.data.MatchRepository;
import com.algorena.games.domain.Match;
import com.algorena.games.domain.MatchParticipant;
import com.algorena.games.domain.MatchStatus;
import com.algorena.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BotServiceImpl implements BotService {

    private final BotRepository botRepository;
    private final MatchRepository matchRepository;
    private final CurrentUser currentUser;
    private final BotMapper botMapper;

    @Override
    @Transactional
    public BotDTO createBot(CreateBotRequest request) {
        Bot bot = Bot.builder()
                .userId(currentUser.id())
                .name(request.name())
                .description(request.description())
                .game(request.game())
                .endpoint(request.endpoint())
                .apiKey(request.apiKey())
                .active(true)
                .build();

        bot = botRepository.save(bot);
        return botMapper.toPrivateDTO(bot);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<BotDTO> getBots(Pageable pageable, @Nullable Long userId, @Nullable String name, @Nullable Game game, @Nullable Boolean active) {
        // If filtering by current user's ID, include API keys (private view)
        boolean isOwnBots = userId != null && userId.equals(currentUser.id());

        return botRepository.findByFilters(userId, name, game, active, pageable)
                .map(bot -> botMapper.toDTO(bot, isOwnBots));
    }

    @Override
    @Transactional(readOnly = true)
    public BotDTO getBotById(Long botId) {
        Bot bot = botRepository.findByIdAndUserIdAndDeletedFalse(botId, currentUser.id())
                .orElseThrow(() -> new DataNotFoundException("Bot not found"));
        return botMapper.toPrivateDTO(bot);
    }

    @Override
    @Transactional
    public BotDTO updateBot(Long botId, UpdateBotRequest request) {
        Bot bot = botRepository.findByIdAndUserIdAndDeletedFalse(botId, currentUser.id())
                .orElseThrow(() -> new DataNotFoundException("Bot not found"));

        bot.updateDetails(request.name(), request.description());
        bot.updateEndpoint(request.endpoint(), request.apiKey());

        if (request.active() != null) {
            if (request.active()) {
                bot.activate();
            } else {
                bot.deactivate();
            }
        }

        bot = botRepository.save(bot);
        return botMapper.toPrivateDTO(bot);
    }

    /**
     * Soft-deletes a bot by marking it as deleted.
     * Preserves the bot record to maintain referential integrity with match history.
     * Deleted bots are automatically deactivated and excluded from queries.
     */
    @Override
    @Transactional
    public void deleteBot(Long botId) {
        Bot bot = botRepository.findByIdAndUserIdAndDeletedFalse(botId, currentUser.id())
                .orElseThrow(() -> new DataNotFoundException("Bot not found"));
        bot.markAsDeleted();
        botRepository.save(bot);
    }

    @Override
    @Transactional(readOnly = true)
    public BotStatsDTO getBotStats(Long botId) {
        Bot bot = botRepository.findById(botId)
                .orElseThrow(() -> new DataNotFoundException("Bot not found"));

        // Get all finished matches for this bot
        List<Match> matches = matchRepository.findByParticipants_Bot_IdAndStatus(botId, MatchStatus.FINISHED);

        int totalMatches = matches.size();
        int wins = 0;
        int losses = 0;
        int draws = 0;

        for (Match match : matches) {
            MatchParticipant participant = match.getParticipants().stream()
                    .filter(p -> p.getBot().getId().equals(botId))
                    .findFirst()
                    .orElse(null);

            if (participant == null || participant.getScore() == null) {
                continue;
            }

            double score = participant.getScore();
            if (score == 1.0) {
                wins++;
            } else if (score == 0.5) {
                draws++;
            } else if (score == 0.0) {
                losses++;
            }
        }

        double winRate = totalMatches > 0 ? (wins + 0.5 * draws) / totalMatches : 0.0;

        return new BotStatsDTO(
                bot.getId(),
                bot.getName(),
                totalMatches,
                wins,
                losses,
                draws,
                winRate
        );
    }
}
