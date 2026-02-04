package com.algorena.games.application;

import com.algorena.bots.domain.Bot;
import com.algorena.common.exception.BotCommunicationException;
import com.algorena.games.dto.BotMoveRequest;
import com.algorena.games.dto.BotMoveResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Service for communicating with bot endpoints to request moves.
 */
@Service
@Slf4j
public class BotClientService {

    private static final String API_KEY_HEADER = "X-Algorena-API-Key";
    private static final String MATCH_ID_HEADER = "X-Algorena-Match-ID";
    private final RestClient restClient;

    public BotClientService(RestClient botRestClient) {
        this.restClient = botRestClient;
    }

    /**
     * Requests a move from a bot by calling its endpoint.
     *
     * @param bot     The bot to request a move from
     * @param request The move request containing game state and legal moves
     * @return The bot's response containing the move
     * @throws BotCommunicationException if communication fails
     */
    public BotMoveResponse requestMove(Bot bot, BotMoveRequest request) {
        log.debug("Requesting move from bot {} at endpoint {}", bot.getName(), bot.getEndpoint());

        try {
            RestClient.RequestBodySpec requestSpec = restClient.post()
                    .uri(bot.getEndpoint())
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(MATCH_ID_HEADER, request.matchId().toString());

            // Add API key header if the bot has one configured
            if (bot.getApiKey() != null && !bot.getApiKey().isBlank()) {
                requestSpec.header(API_KEY_HEADER, bot.getApiKey());
            }

            BotMoveResponse response = requestSpec
                    .body(request)
                    .retrieve()
                    .body(BotMoveResponse.class);

            if (response == null || response.move() == null || response.move().isBlank()) {
                throw new BotCommunicationException(
                        "Bot returned empty or invalid response",
                        "INVALID_RESPONSE"
                );
            }

            log.debug("Bot {} responded with move: {}", bot.getName(), response.move());
            return response;

        } catch (ResourceAccessException e) {
            // Timeout or connection error
            log.warn("Timeout or connection error when calling bot {}: {}", bot.getName(), e.getMessage());
            throw new BotCommunicationException(
                    "Bot endpoint timed out or connection failed: " + e.getMessage(),
                    "TIMEOUT",
                    e
            );
        } catch (RestClientException e) {
            // Other REST client errors (4xx, 5xx responses, etc.)
            log.warn("Error response from bot {}: {}", bot.getName(), e.getMessage());
            throw new BotCommunicationException(
                    "Bot endpoint returned error: " + e.getMessage(),
                    "CONNECTION_ERROR",
                    e
            );
        }
    }
}
