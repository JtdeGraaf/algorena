package com.algorena.games.controllers;

import com.algorena.games.application.MatchService;
import com.algorena.games.dto.CreateMatchRequest;
import com.algorena.games.dto.MakeMoveRequest;
import com.algorena.games.dto.MatchDTO;
import com.algorena.games.dto.MatchMoveDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @GetMapping
    public ResponseEntity<List<MatchDTO>> getMatches(@RequestParam(required = false) Long botId) {
        if (botId != null) {
            return ResponseEntity.ok(matchService.getMatchesForBot(botId));
        }
        // Return current user's matches if no botId specified
        return ResponseEntity.ok(matchService.getCurrentUserMatches());
    }

    @GetMapping("/recent")
    public ResponseEntity<List<MatchDTO>> getRecentMatches(
            @RequestParam(defaultValue = "50") @Valid @Min(1) @Max(100) int limit) {
        return ResponseEntity.ok(matchService.getRecentMatches(limit));
    }

    @PostMapping
    public ResponseEntity<MatchDTO> createMatch(@Valid @RequestBody CreateMatchRequest request) {
        MatchDTO match = matchService.createMatch(request);
        return ResponseEntity.ok(match);
    }

    @PostMapping("/{matchId}/move")
    public ResponseEntity<Void> makeMove(@PathVariable UUID matchId,
                                         @Valid @RequestBody MakeMoveRequest request) {
        matchService.makeMove(matchId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{matchId}")
    public ResponseEntity<MatchDTO> getMatch(@PathVariable UUID matchId) {
        return ResponseEntity.ok(matchService.getMatch(matchId));
    }

    @GetMapping("/{matchId}/moves")
    public ResponseEntity<List<MatchMoveDTO>> getMatchMoves(@PathVariable UUID matchId) {
        return ResponseEntity.ok(matchService.getMatchMoves(matchId));
    }

    @PostMapping("/{matchId}/abort")
    public ResponseEntity<Void> abortMatch(@PathVariable UUID matchId) {
        matchService.abortMatch(matchId);
        return ResponseEntity.noContent().build();
    }
}
