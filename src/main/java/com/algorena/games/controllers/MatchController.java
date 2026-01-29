package com.algorena.games.controllers;

import com.algorena.games.application.MatchService;
import com.algorena.games.dto.CreateMatchRequest;
import com.algorena.games.dto.MakeMoveRequest;
import com.algorena.games.dto.MatchDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

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
}
