package com.algorena.bots.controllers;

import com.algorena.bots.application.BotService;
import com.algorena.bots.domain.Game;
import com.algorena.bots.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bots")
@PreAuthorize("hasRole('USER')")
@AllArgsConstructor
@Tag(name = "Bots", description = "Bot management endpoints")
public class BotController {
    private final BotService botService;

    @PostMapping
    @Operation(summary = "Create a new bot")
    public ResponseEntity<BotDTO> createBot(@Valid @RequestBody CreateBotRequest request) {
        BotDTO bot = botService.createBot(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(bot);
    }

    @GetMapping
    @Operation(summary = "Get all bots with optional pagination and filters")
    public ResponseEntity<Page<BotDTO>> getBots(
            @ParameterObject @PageableDefault(size = 20, sort = "created") Pageable pageable,
            @Parameter(description = "Filter by user ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "Filter by bot name (partial match)") @RequestParam(required = false) String name,
            @Parameter(description = "Filter by game type") @RequestParam(required = false) Game game,
            @Parameter(description = "Filter by active status") @RequestParam(required = false) Boolean active) {
        Page<BotDTO> bots = botService.getBots(pageable, userId, name, game, active);
        return ResponseEntity.ok(bots);
    }


    @GetMapping("/{botId}")
    @Operation(summary = "Get a specific bot by ID")
    public ResponseEntity<BotDTO> getBotById(@PathVariable Long botId) {
        BotDTO bot = botService.getBotById(botId);
        return ResponseEntity.ok(bot);
    }

    @PutMapping("/{botId}")
    @Operation(summary = "Update a bot")
    public ResponseEntity<BotDTO> updateBot(@PathVariable Long botId, @Valid @RequestBody UpdateBotRequest request) {
        BotDTO bot = botService.updateBot(botId, request);
        return ResponseEntity.ok(bot);
    }

    @GetMapping("/{botId}/stats")
    @Operation(summary = "Get bot statistics")
    public ResponseEntity<BotStatsDTO> getBotStats(@PathVariable Long botId) {
        return ResponseEntity.ok(botService.getBotStats(botId));
    }

    @DeleteMapping("/{botId}")
    @Operation(summary = "Delete a bot")
    public ResponseEntity<Void> deleteBot(@PathVariable Long botId) {
        botService.deleteBot(botId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{botId}/api-keys")
    @Operation(summary = "Generate a new API key for a bot")
    public ResponseEntity<CreateApiKeyResponse> createApiKey(@PathVariable Long botId, @Valid @RequestBody CreateApiKeyRequest request) {
        CreateApiKeyResponse response = botService.createApiKey(botId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{botId}/api-keys")
    @Operation(summary = "Get all API keys for a bot")
    public ResponseEntity<List<ApiKeyDTO>> getBotApiKeys(@PathVariable Long botId) {
        List<ApiKeyDTO> apiKeys = botService.getBotApiKeys(botId);
        return ResponseEntity.ok(apiKeys);
    }

    @DeleteMapping("/{botId}/api-keys/{apiKeyId}")
    @Operation(summary = "Revoke an API key")
    public ResponseEntity<Void> revokeApiKey(@PathVariable Long botId, @PathVariable Long apiKeyId) {
        botService.revokeApiKey(botId, apiKeyId);
        return ResponseEntity.noContent().build();
    }
}

