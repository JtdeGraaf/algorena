package com.algorena.bots.mapper;

import com.algorena.bots.domain.Bot;
import com.algorena.bots.dto.BotDTO;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting Bot entities to DTOs.
 */
@Component
public class BotMapper {

    /**
     * Converts a Bot to DTO with all fields including sensitive data (API key).
     * Use for owner viewing their own bot.
     */
    public BotDTO toPrivateDTO(Bot bot) {
        return new BotDTO(
                bot.getId(),
                bot.getName(),
                bot.getDescription(),
                bot.getGame(),
                bot.isActive(),
                bot.getEndpoint(),
                bot.getApiKey(),
                bot.getCreated(),
                bot.getLastUpdated()
        );
    }

    /**
     * Converts a Bot to DTO without sensitive data (API key hidden).
     * Use for public bot lists.
     */
    public BotDTO toPublicDTO(Bot bot) {
        return new BotDTO(
                bot.getId(),
                bot.getName(),
                bot.getDescription(),
                bot.getGame(),
                bot.isActive(),
                null,
                null, // Don't expose API key in public lists
                bot.getCreated(),
                bot.getLastUpdated()
        );
    }

    /**
     * Converts a Bot to DTO based on visibility.
     *
     * @param bot       the bot entity
     * @param isPrivate true to include sensitive fields, false for public view
     */
    public BotDTO toDTO(Bot bot, boolean isPrivate) {
        return isPrivate ? toPrivateDTO(bot) : toPublicDTO(bot);
    }
}
