package com.algorena.games.application;

import com.algorena.bots.data.BotRepository;
import com.algorena.bots.domain.Bot;
import com.algorena.bots.domain.Game;
import com.algorena.games.chess.data.ChessGameStateRepository;
import com.algorena.games.chess.domain.ChessGameState;
import com.algorena.games.chess.domain.ChessMatchMove;
import com.algorena.games.data.MatchMoveRepository;
import com.algorena.games.data.MatchRepository;
import com.algorena.games.domain.MatchStatus;
import com.algorena.games.dto.CreateMatchRequest;
import com.algorena.games.dto.MakeMoveRequest;
import com.algorena.games.dto.MatchDTO;
import com.algorena.games.dto.MatchParticipantDTO;
import com.algorena.test.config.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MatchServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MatchService matchService;

    @Autowired
    private BotRepository botRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private ChessGameStateRepository chessGameStateRepository;

    @Autowired
    private MatchMoveRepository matchMoveRepository;


    private Bot botWhite;
    private Bot botBlack;

    @BeforeEach
    void setUpBots() {
        // testUser is created in super.setupIntegrationTest()
        botWhite = botRepository.save(Bot.builder()
                .userId(testUser.getId())
                .name("WhiteBot")
                .game(Game.CHESS)
                .active(true)
                .build());

        botBlack = botRepository.save(Bot.builder()
                .userId(testUser.getId())
                .name("BlackBot")
                .game(Game.CHESS)
                .active(true)
                .build());
    }

    @Test
    @Transactional
    void createMatch_ShouldInitializeChessMatch() {
        CreateMatchRequest request = new CreateMatchRequest(botWhite.getId(), botBlack.getId(), Game.CHESS);
        MatchDTO match = matchService.createMatch(request);

        assertThat(match.id()).isNotNull();
        assertThat(match.status()).isEqualTo(MatchStatus.IN_PROGRESS);
        assertThat(match.game()).isEqualTo(Game.CHESS);
        assertThat(match.participants()).hasSize(2);

        MatchParticipantDTO p1 = match.participants().get(0);
        assertThat(p1.playerIndex()).isEqualTo(0);
        assertThat(p1.botId()).isEqualTo(botWhite.getId());

        MatchParticipantDTO p2 = match.participants().get(1);
        assertThat(p2.playerIndex()).isEqualTo(1);
        assertThat(p2.botId()).isEqualTo(botBlack.getId());

        // Check Game State using Repository directly to verify side effects
        ChessGameState state = chessGameStateRepository.findByMatchId(match.id()).orElseThrow();
        assertThat(state.getFen()).isEqualTo("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    }

    @Test
    @Transactional
    void makeMove_ShouldUpdateStateAndPersistMove() {
        CreateMatchRequest createRequest = new CreateMatchRequest(botWhite.getId(), botBlack.getId(), Game.CHESS);
        MatchDTO match = matchService.createMatch(createRequest);
        UUID matchId = match.id();

        // White moves e2e4
        MakeMoveRequest moveRequest = new MakeMoveRequest(botWhite.getId(), "e2e4");
        matchService.makeMove(matchId, moveRequest);

        // Verify State Updated
        ChessGameState state = chessGameStateRepository.findByMatchId(matchId).orElseThrow();
        assertThat(state.getFen()).contains("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR");
        assertThat(state.getFullMoveNumber()).isEqualTo(1);

        // Verify Move Persisted
        var moves = matchMoveRepository.findByMatchIdOrderByCreatedAsc(matchId);
        assertThat(moves).hasSize(1);

        var move = moves.get(0);
        assertThat(move).isInstanceOf(ChessMatchMove.class);
        ChessMatchMove chessMove = (ChessMatchMove) move;
        assertThat(chessMove.getMoveNotation()).isEqualTo("e2e4");
        // Chesslib uses uppercase for Square.value()
        assertThat(chessMove.getFromSquare()).isEqualTo("E2");
        assertThat(chessMove.getToSquare()).isEqualTo("E4");
        assertThat(chessMove.getPlayerIndex()).isEqualTo(0);
    }

    @Test
    @Transactional
    void fullGame_FoolsMate_ShouldEndMatchAndAssignScores() {
        CreateMatchRequest createRequest = new CreateMatchRequest(botWhite.getId(), botBlack.getId(), Game.CHESS);
        MatchDTO match = matchService.createMatch(createRequest);
        UUID matchId = match.id();

        // 1. f3 e5
        matchService.makeMove(matchId, new MakeMoveRequest(botWhite.getId(), "f2f3"));
        matchService.makeMove(matchId, new MakeMoveRequest(botBlack.getId(), "e7e5"));

        // 2. g4 Qh4#
        matchService.makeMove(matchId, new MakeMoveRequest(botWhite.getId(), "g2g4"));
        matchService.makeMove(matchId, new MakeMoveRequest(botBlack.getId(), "d8h4"));

        // Verify Match Finished via Service DTO
        MatchDTO finishedMatch = matchService.getMatch(matchId);
        assertThat(finishedMatch.status()).isEqualTo(MatchStatus.FINISHED);
        assertThat(finishedMatch.finishedAt()).isNotNull();

        // Verify Scores
        List<MatchParticipantDTO> participants = finishedMatch.participants();
        MatchParticipantDTO white = participants.stream().filter(p -> p.playerIndex() == 0).findFirst().orElseThrow();
        MatchParticipantDTO black = participants.stream().filter(p -> p.playerIndex() == 1).findFirst().orElseThrow();

        assertThat(white.score()).isEqualTo(0.0); // Loss
        assertThat(black.score()).isEqualTo(1.0); // Win
    }
}
