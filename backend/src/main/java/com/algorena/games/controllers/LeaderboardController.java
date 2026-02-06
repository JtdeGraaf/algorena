package com.algorena.games.controllers;

import com.algorena.bots.domain.Game;
import com.algorena.games.application.LeaderboardService;
import com.algorena.games.dto.BotLeaderboardEntryDTO;
import com.algorena.games.dto.RatingHistoryDTO;
import com.algorena.games.dto.UserLeaderboardEntryDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for leaderboard endpoints.
 * Provides both bot-level and user-level rankings.
 *
 * <p>Note: These endpoints are public (no authentication required) to allow
 * anyone to view the leaderboards.
 */
@RestController
@RequestMapping("/api/v1/leaderboard")
@AllArgsConstructor
@Tag(name = "Leaderboard", description = "Bot and user leaderboard rankings")
public class LeaderboardController {
    private final LeaderboardService leaderboardService;

    // ===== Bot Leaderboard Endpoints =====

    @GetMapping("/bots")
    @Operation(summary = "Get bot leaderboard", description = "Returns bots ranked by ELO rating for a specific game")
    public ResponseEntity<Page<BotLeaderboardEntryDTO>> getBotLeaderboard(
            @Parameter(description = "Game type", required = true)
            @RequestParam Game game,
            @ParameterObject @PageableDefault(size = 50) Pageable pageable
    ) {
        return ResponseEntity.ok(leaderboardService.getBotLeaderboard(game, pageable));
    }

    @GetMapping("/bots/{botId}")
    @Operation(summary = "Get bot ranking", description = "Returns a specific bot's rank and stats")
    public ResponseEntity<BotLeaderboardEntryDTO> getBotRanking(
            @Parameter(description = "Bot ID", required = true)
            @PathVariable Long botId,
            @Parameter(description = "Game type", required = true)
            @RequestParam Game game
    ) {
        return ResponseEntity.ok(leaderboardService.getBotRanking(botId, game));
    }

    @GetMapping("/bots/{botId}/history")
    @Operation(summary = "Get bot rating history", description = "Returns historical rating changes for a bot")
    public ResponseEntity<List<RatingHistoryDTO>> getBotRatingHistory(
            @Parameter(description = "Bot ID", required = true)
            @PathVariable Long botId,
            @Parameter(description = "Game type", required = true)
            @RequestParam Game game,
            @Parameter(description = "Maximum number of history entries")
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ResponseEntity.ok(leaderboardService.getBotRatingHistory(botId, game, limit));
    }

    // ===== User Leaderboard Endpoints =====

    @GetMapping("/users")
    @Operation(summary = "Get user leaderboard", description = "Returns users ranked by their best bot's ELO rating")
    public ResponseEntity<Page<UserLeaderboardEntryDTO>> getUserLeaderboard(
            @Parameter(description = "Game type", required = true)
            @RequestParam Game game,
            @ParameterObject @PageableDefault(size = 50) Pageable pageable
    ) {
        return ResponseEntity.ok(leaderboardService.getUserLeaderboard(game, pageable));
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Get user ranking", description = "Returns a specific user's rank and aggregate stats")
    public ResponseEntity<UserLeaderboardEntryDTO> getUserRanking(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "Game type", required = true)
            @RequestParam Game game
    ) {
        return ResponseEntity.ok(leaderboardService.getUserRanking(userId, game));
    }
}
