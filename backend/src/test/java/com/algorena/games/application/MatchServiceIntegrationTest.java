package com.algorena.games.application;

import com.algorena.bots.data.BotRepository;
import com.algorena.bots.domain.Bot;
import com.algorena.bots.domain.Game;
import com.algorena.games.chess.data.ChessGameStateRepository;
import com.algorena.games.chess.domain.ChessGameState;
import com.algorena.games.data.MatchRepository;
import com.algorena.games.domain.MatchStatus;
import com.algorena.games.dto.CreateMatchRequest;
import com.algorena.games.dto.MatchDTO;
import com.algorena.games.dto.MatchParticipantDTO;
import com.algorena.test.config.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the MatchService focusing on match creation and query operations.
 * Match execution tests are covered in {@link MatchExecutorIntegrationTest}.
 */
class MatchServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MatchService matchService;

    @Autowired
    private BotRepository botRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private ChessGameStateRepository chessGameStateRepository;

    // Mock the executor to prevent async execution during these tests
    @MockitoBean
    private MatchExecutorService matchExecutorService;

    private Bot botWhite;
    private Bot botBlack;

    @BeforeEach
    void setUpBots() {
        botWhite = botRepository.save(Bot.builder()
                .userId(testUser.getId())
                .name("WhiteBot")
                .game(Game.CHESS)
                .endpoint("http://localhost:8081/white-bot")
                .active(true)
                .build());

        botBlack = botRepository.save(Bot.builder()
                .userId(testUser.getId())
                .name("BlackBot")
                .game(Game.CHESS)
                .endpoint("http://localhost:8081/black-bot")
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

        // Check Game State using Repository directly
        ChessGameState state = chessGameStateRepository.findByMatchId(match.id()).orElseThrow();
        assertThat(state.getFen()).isEqualTo("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    }

    @Test
    @Transactional
    void getMatch_ShouldReturnMatchWithDetails() {
        CreateMatchRequest request = new CreateMatchRequest(botWhite.getId(), botBlack.getId(), Game.CHESS);
        MatchDTO created = matchService.createMatch(request);

        MatchDTO retrieved = matchService.getMatch(created.id());

        assertThat(retrieved.id()).isEqualTo(created.id());
        assertThat(retrieved.game()).isEqualTo(Game.CHESS);
        assertThat(retrieved.status()).isEqualTo(MatchStatus.IN_PROGRESS);
        assertThat(retrieved.participants()).hasSize(2);
        assertThat(retrieved.state()).isNotNull();
    }

    @Test
    @Transactional
    void getLegalMoves_ShouldReturnInitialChessMoves() {
        CreateMatchRequest request = new CreateMatchRequest(botWhite.getId(), botBlack.getId(), Game.CHESS);
        MatchDTO match = matchService.createMatch(request);

        var legalMoves = matchService.getLegalMoves(match.id());

        // Initial position has 20 legal moves (16 pawn moves + 4 knight moves)
        assertThat(legalMoves).hasSize(20);
        assertThat(legalMoves).contains("e2e4", "d2d4", "g1f3", "b1c3");
    }

    @Test
    @Transactional
    void getCurrentUserMatches_ShouldReturnMatchesForUserBots() {
        CreateMatchRequest request = new CreateMatchRequest(botWhite.getId(), botBlack.getId(), Game.CHESS);
        matchService.createMatch(request);
        matchService.createMatch(request);

        var matches = matchService.getCurrentUserMatches();

        assertThat(matches).hasSize(2);
    }

    @Test
    @Transactional
    void getMatchesForBot_ShouldReturnMatchesContainingBot() {
        CreateMatchRequest request = new CreateMatchRequest(botWhite.getId(), botBlack.getId(), Game.CHESS);
        matchService.createMatch(request);

        var matches = matchService.getMatchesForBot(botWhite.getId());

        assertThat(matches).hasSize(1);
        assertThat(matches.getFirst().participants())
                .anyMatch(p -> p.botId().equals(botWhite.getId()));
    }

    @Test
    @Transactional
    void abortMatch_ShouldChangeStatusToAborted() {
        CreateMatchRequest request = new CreateMatchRequest(botWhite.getId(), botBlack.getId(), Game.CHESS);
        MatchDTO match = matchService.createMatch(request);

        matchService.abortMatch(match.id());

        MatchDTO aborted = matchService.getMatch(match.id());
        assertThat(aborted.status()).isEqualTo(MatchStatus.ABORTED);
        assertThat(aborted.finishedAt()).isNotNull();
    }
}
