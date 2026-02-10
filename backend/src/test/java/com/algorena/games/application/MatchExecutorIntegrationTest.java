package com.algorena.games.application;

import com.algorena.bots.data.BotRepository;
import com.algorena.bots.domain.Bot;
import com.algorena.bots.domain.Game;
import com.algorena.common.exception.BotCommunicationException;
import com.algorena.games.chess.data.ChessGameStateRepository;
import com.algorena.games.chess.domain.ChessGameState;
import com.algorena.games.connect4.data.Connect4GameStateRepository;
import com.algorena.games.connect4.domain.Connect4GameState;
import com.algorena.games.data.MatchMoveRepository;
import com.algorena.games.data.MatchRepository;
import com.algorena.games.domain.Match;
import com.algorena.games.domain.MatchParticipant;
import com.algorena.games.domain.MatchStatus;
import com.algorena.games.dto.BotMoveResponse;
import com.algorena.games.dto.MatchDTO;
import com.algorena.games.engine.GameEngineFactory;
import com.algorena.test.config.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Integration tests for the match execution flow using the push model.
 * Tests the complete end-to-end flow of creating a match and having bots play via mocked HTTP endpoints.
 * <p>
 * Note: These tests mock the BotClientService and call the executor synchronously to avoid
 * transaction isolation issues with async execution. Matches are created directly via repositories
 * to avoid triggering async execution from the service layer.
 */
class MatchExecutorIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MatchService matchService;

    @Autowired
    private MatchExecutorService matchExecutorService;

    @Autowired
    private BotRepository botRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private ChessGameStateRepository chessGameStateRepository;

    @Autowired
    private Connect4GameStateRepository connect4GameStateRepository;

    @Autowired
    private MatchMoveRepository matchMoveRepository;

    @Autowired
    private GameEngineFactory gameEngineFactory;

    @MockitoBean
    private BotClientService botClientService;

    private Bot chessBot1;
    private Bot chessBot2;
    private Bot connect4Bot1;
    private Bot connect4Bot2;

    @BeforeEach
    void setUpBots() {
        // Create chess bots
        chessBot1 = botRepository.save(Bot.builder()
                .userId(testUser.getId())
                .name("ChessBot1")
                .game(Game.CHESS)
                .endpoint("http://localhost:8081/chess-bot-1")
                .active(true)
                .build());

        chessBot2 = botRepository.save(Bot.builder()
                .userId(testUser.getId())
                .name("ChessBot2")
                .game(Game.CHESS)
                .endpoint("http://localhost:8081/chess-bot-2")
                .active(true)
                .build());

        // Create Connect4 bots
        connect4Bot1 = botRepository.save(Bot.builder()
                .userId(testUser.getId())
                .name("Connect4Bot1")
                .game(Game.CONNECT_FOUR)
                .endpoint("http://localhost:8081/connect4-bot-1")
                .active(true)
                .build());

        connect4Bot2 = botRepository.save(Bot.builder()
                .userId(testUser.getId())
                .name("Connect4Bot2")
                .game(Game.CONNECT_FOUR)
                .endpoint("http://localhost:8081/connect4-bot-2")
                .active(true)
                .build());
    }

    /**
     * Creates a match directly via repositories without triggering async execution.
     * This allows tests to set up mocks before running the match synchronously.
     */
    private Long createMatchDirectly(Bot bot1, Bot bot2, Game game) {
        Match match = Match.builder()
                .game(game)
                .status(MatchStatus.IN_PROGRESS)
                .build();
        match.start();

        MatchParticipant p1 = MatchParticipant.builder()
                .match(match)
                .bot(bot1)
                .playerIndex(0)
                .build();

        MatchParticipant p2 = MatchParticipant.builder()
                .match(match)
                .bot(bot2)
                .playerIndex(1)
                .build();

        match.addParticipant(p1);
        match.addParticipant(p2);

        match = matchRepository.save(match);

        // Initialize game state
        if (game == Game.CHESS) {
            var engine = gameEngineFactory.<ChessGameState, String>getEngine(Game.CHESS);
            ChessGameState state = engine.startNewGame();
            state.assignMatch(match);
            chessGameStateRepository.save(state);
        } else if (game == Game.CONNECT_FOUR) {
            var engine = gameEngineFactory.<Connect4GameState, Integer>getEngine(Game.CONNECT_FOUR);
            Connect4GameState state = engine.startNewGame();
            state.assignMatch(match);
            connect4GameStateRepository.save(state);
        }

        return match.getId();
    }

    @Test
    void chessMatch_FoolsMate_ShouldCompleteWithBlackWinning() {
        // Setup: Mock bot responses for Fool's Mate
        // 1. f3 e5 2. g4 Qh4#
        when(botClientService.requestMove(any(), any()))
                .thenReturn(new BotMoveResponse("f2f3"))  // White: f3
                .thenReturn(new BotMoveResponse("e7e5"))  // Black: e5
                .thenReturn(new BotMoveResponse("g2g4"))  // White: g4
                .thenReturn(new BotMoveResponse("d8h4")); // Black: Qh4#

        // Create match directly
        Long matchId = createMatchDirectly(chessBot1, chessBot2, Game.CHESS);

        // Execute match synchronously
        matchExecutorService.runMatchLoop(matchId);

        // Verify match result
        MatchDTO finishedMatch = matchService.getMatch(matchId);
        assertThat(finishedMatch.status()).isEqualTo(MatchStatus.FINISHED);

        // White (player 0) loses, Black (player 1) wins
        var white = finishedMatch.participants().stream()
                .filter(p -> p.playerIndex() == 0).findFirst().orElseThrow();
        var black = finishedMatch.participants().stream()
                .filter(p -> p.playerIndex() == 1).findFirst().orElseThrow();

        assertThat(white.score()).isEqualTo(0.0);
        assertThat(black.score()).isEqualTo(1.0);

        // Verify moves were recorded
        var moves = matchMoveRepository.findByMatchIdOrderByCreatedAsc(matchId);
        assertThat(moves).hasSize(4);
    }

    @Test
    void chessMatch_ScholarsMate_ShouldCompleteWithWhiteWinning() {
        // Setup: Mock bot responses for Scholar's Mate
        // 1. e4 e5 2. Bc4 Nc6 3. Qh5 Nf6 4. Qxf7#
        when(botClientService.requestMove(any(), any()))
                .thenReturn(new BotMoveResponse("e2e4"))  // White: e4
                .thenReturn(new BotMoveResponse("e7e5"))  // Black: e5
                .thenReturn(new BotMoveResponse("f1c4"))  // White: Bc4
                .thenReturn(new BotMoveResponse("b8c6"))  // Black: Nc6
                .thenReturn(new BotMoveResponse("d1h5"))  // White: Qh5
                .thenReturn(new BotMoveResponse("g8f6"))  // Black: Nf6
                .thenReturn(new BotMoveResponse("h5f7")); // White: Qxf7#

        Long matchId = createMatchDirectly(chessBot1, chessBot2, Game.CHESS);

        matchExecutorService.runMatchLoop(matchId);

        MatchDTO finishedMatch = matchService.getMatch(matchId);
        assertThat(finishedMatch.status()).isEqualTo(MatchStatus.FINISHED);

        var white = finishedMatch.participants().stream()
                .filter(p -> p.playerIndex() == 0).findFirst().orElseThrow();
        var black = finishedMatch.participants().stream()
                .filter(p -> p.playerIndex() == 1).findFirst().orElseThrow();

        assertThat(white.score()).isEqualTo(1.0);
        assertThat(black.score()).isEqualTo(0.0);
    }

    @Test
    void connect4Match_VerticalWin_ShouldCompleteWithPlayer1Winning() {
        // Setup: Player 1 stacks 4 in column 0
        // P1: 0, P2: 1, P1: 0, P2: 1, P1: 0, P2: 1, P1: 0 (win)
        when(botClientService.requestMove(any(), any()))
                .thenReturn(new BotMoveResponse("0"))  // P1: col 0
                .thenReturn(new BotMoveResponse("1"))  // P2: col 1
                .thenReturn(new BotMoveResponse("0"))  // P1: col 0
                .thenReturn(new BotMoveResponse("1"))  // P2: col 1
                .thenReturn(new BotMoveResponse("0"))  // P1: col 0
                .thenReturn(new BotMoveResponse("1"))  // P2: col 1
                .thenReturn(new BotMoveResponse("0")); // P1: col 0 - WIN

        Long matchId = createMatchDirectly(connect4Bot1, connect4Bot2, Game.CONNECT_FOUR);

        matchExecutorService.runMatchLoop(matchId);

        MatchDTO finishedMatch = matchService.getMatch(matchId);
        assertThat(finishedMatch.status()).isEqualTo(MatchStatus.FINISHED);

        var player1 = finishedMatch.participants().stream()
                .filter(p -> p.playerIndex() == 0).findFirst().orElseThrow();
        var player2 = finishedMatch.participants().stream()
                .filter(p -> p.playerIndex() == 1).findFirst().orElseThrow();

        assertThat(player1.score()).isEqualTo(1.0);
        assertThat(player2.score()).isEqualTo(0.0);

        var moves = matchMoveRepository.findByMatchIdOrderByCreatedAsc(matchId);
        assertThat(moves).hasSize(7);
    }

    @Test
    void connect4Match_HorizontalWin_ShouldCompleteWithPlayer2Winning() {
        // Setup: Player 2 gets 4 in a row horizontally
        // P1: 0, P2: 1, P1: 0, P2: 2, P1: 0, P2: 3, P1: 6, P2: 4 (win)
        when(botClientService.requestMove(any(), any()))
                .thenReturn(new BotMoveResponse("0"))  // P1: col 0
                .thenReturn(new BotMoveResponse("1"))  // P2: col 1
                .thenReturn(new BotMoveResponse("0"))  // P1: col 0
                .thenReturn(new BotMoveResponse("2"))  // P2: col 2
                .thenReturn(new BotMoveResponse("0"))  // P1: col 0
                .thenReturn(new BotMoveResponse("3"))  // P2: col 3
                .thenReturn(new BotMoveResponse("6"))  // P1: col 6
                .thenReturn(new BotMoveResponse("4")); // P2: col 4 - WIN

        Long matchId = createMatchDirectly(connect4Bot1, connect4Bot2, Game.CONNECT_FOUR);

        matchExecutorService.runMatchLoop(matchId);

        MatchDTO finishedMatch = matchService.getMatch(matchId);
        assertThat(finishedMatch.status()).isEqualTo(MatchStatus.FINISHED);

        var player1 = finishedMatch.participants().stream()
                .filter(p -> p.playerIndex() == 0).findFirst().orElseThrow();
        var player2 = finishedMatch.participants().stream()
                .filter(p -> p.playerIndex() == 1).findFirst().orElseThrow();

        assertThat(player1.score()).isEqualTo(0.0);
        assertThat(player2.score()).isEqualTo(1.0);
    }

    @Test
    void match_BotReturnsInvalidMove_ShouldForfeitToOpponent() {
        // Setup: First bot returns an invalid move
        when(botClientService.requestMove(any(), any()))
                .thenReturn(new BotMoveResponse("invalid_move"));

        Long matchId = createMatchDirectly(chessBot1, chessBot2, Game.CHESS);

        matchExecutorService.runMatchLoop(matchId);

        Match finishedMatch = matchRepository.findByIdWithParticipants(matchId).orElseThrow();
        assertThat(finishedMatch.getStatus()).isEqualTo(MatchStatus.FORFEITED);
        assertThat(finishedMatch.getForfeitReason()).isEqualTo("INVALID_MOVE");

        // Player 0 (white) made invalid move, so player 1 (black) wins
        MatchParticipant white = finishedMatch.getParticipants().stream()
                .filter(p -> p.getPlayerIndex() == 0).findFirst().orElseThrow();
        MatchParticipant black = finishedMatch.getParticipants().stream()
                .filter(p -> p.getPlayerIndex() == 1).findFirst().orElseThrow();

        assertThat(white.getScore()).isEqualTo(0.0);
        assertThat(black.getScore()).isEqualTo(1.0);
    }

    @Test
    void match_BotTimeout_ShouldForfeitToOpponent() {
        // Setup: Bot throws timeout exception
        when(botClientService.requestMove(any(), any()))
                .thenThrow(new BotCommunicationException("Bot timed out", "TIMEOUT"));

        Long matchId = createMatchDirectly(chessBot1, chessBot2, Game.CHESS);

        matchExecutorService.runMatchLoop(matchId);

        Match finishedMatch = matchRepository.findByIdWithParticipants(matchId).orElseThrow();
        assertThat(finishedMatch.getStatus()).isEqualTo(MatchStatus.FORFEITED);
        assertThat(finishedMatch.getForfeitReason()).isEqualTo("TIMEOUT");

        MatchParticipant white = finishedMatch.getParticipants().stream()
                .filter(p -> p.getPlayerIndex() == 0).findFirst().orElseThrow();
        MatchParticipant black = finishedMatch.getParticipants().stream()
                .filter(p -> p.getPlayerIndex() == 1).findFirst().orElseThrow();

        assertThat(white.getScore()).isEqualTo(0.0);
        assertThat(black.getScore()).isEqualTo(1.0);
    }

    @Test
    void match_BotConnectionError_ShouldForfeitToOpponent() {
        // Setup: Bot throws connection error
        when(botClientService.requestMove(any(), any()))
                .thenThrow(new BotCommunicationException("Connection refused", "CONNECTION_ERROR"));

        Long matchId = createMatchDirectly(connect4Bot1, connect4Bot2, Game.CONNECT_FOUR);

        matchExecutorService.runMatchLoop(matchId);

        Match finishedMatch = matchRepository.findByIdWithParticipants(matchId).orElseThrow();
        assertThat(finishedMatch.getStatus()).isEqualTo(MatchStatus.FORFEITED);
        assertThat(finishedMatch.getForfeitReason()).isEqualTo("CONNECTION_ERROR");

        MatchParticipant player1 = finishedMatch.getParticipants().stream()
                .filter(p -> p.getPlayerIndex() == 0).findFirst().orElseThrow();
        MatchParticipant player2 = finishedMatch.getParticipants().stream()
                .filter(p -> p.getPlayerIndex() == 1).findFirst().orElseThrow();

        assertThat(player1.getScore()).isEqualTo(0.0);
        assertThat(player2.getScore()).isEqualTo(1.0);
    }
}
