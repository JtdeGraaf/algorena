package com.algorena.games.controllers;

import com.algorena.bots.data.BotRepository;
import com.algorena.bots.domain.Bot;
import com.algorena.common.exception.DataNotFoundException;
import com.algorena.common.exception.ForbiddenException;
import com.algorena.games.application.MatchService;
import com.algorena.games.domain.Match;
import com.algorena.games.dto.CreateMatchRequest;
import com.algorena.games.dto.MakeMoveRequest;
import com.algorena.security.CurrentUser;
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
    private final BotRepository botRepository;
    private final CurrentUser currentUser;

    @PostMapping
    public ResponseEntity<Match> createMatch(@Valid @RequestBody CreateMatchRequest request) {
        Bot bot1 = botRepository.findById(request.bot1Id())
                .orElseThrow(() -> new DataNotFoundException("Bot 1 not found"));
        Bot bot2 = botRepository.findById(request.bot2Id())
                .orElseThrow(() -> new DataNotFoundException("Bot 2 not found"));

        Match match = matchService.createMatch(bot1, bot2, request.game());
        return ResponseEntity.ok(match);
    }

    @PostMapping("/{matchId}/move")
    public ResponseEntity<Void> makeMove(@PathVariable UUID matchId,
                                         @Valid @RequestBody MakeMoveRequest request) {
        Bot bot = botRepository.findById(request.botId())
                .orElseThrow(() -> new DataNotFoundException("Bot not found"));
        
        if (!bot.getUserId().equals(currentUser.id())) {
            throw new ForbiddenException("You do not own this bot");
        }

        matchService.makeMove(matchId, bot, request.move());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{matchId}")
    public ResponseEntity<Match> getMatch(@PathVariable UUID matchId) {
        return ResponseEntity.ok(matchService.getMatch(matchId));
    }
}
