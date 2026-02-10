package com.algorena.games.application;

import com.algorena.bots.data.BotRepository;
import com.algorena.bots.domain.Bot;
import com.algorena.bots.domain.Game;
import com.algorena.common.exception.BadRequestException;
import com.algorena.common.exception.DataNotFoundException;
import com.algorena.common.exception.ForbiddenException;
import com.algorena.games.chess.data.ChessGameStateRepository;
import com.algorena.games.chess.domain.ChessGameState;
import com.algorena.games.chess.engine.ChessGameEngine;
import com.algorena.games.connect4.data.Connect4GameStateRepository;
import com.algorena.games.connect4.domain.Connect4GameState;
import com.algorena.games.connect4.engine.Connect4GameEngine;
import com.algorena.games.data.MatchMoveRepository;
import com.algorena.games.data.MatchRepository;
import com.algorena.games.domain.AbstractGameState;
import com.algorena.games.domain.Match;
import com.algorena.games.domain.MatchParticipant;
import com.algorena.games.domain.MatchStatus;
import com.algorena.games.dto.CreateMatchRequest;
import com.algorena.games.dto.MatchDTO;
import com.algorena.games.dto.MatchMoveDTO;
import com.algorena.games.engine.GameEngine;
import com.algorena.games.engine.GameEngineFactory;
import com.algorena.games.mapper.MatchMapper;
import com.algorena.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchServiceImpl implements MatchService {

    private final MatchRepository matchRepository;
    private final MatchMoveRepository matchMoveRepository;
    private final ChessGameStateRepository chessGameStateRepository;
    private final Connect4GameStateRepository connect4GameStateRepository;
    private final GameEngineFactory gameEngineFactory;
    private final BotRepository botRepository;
    private final CurrentUser currentUser;
    private final MatchExecutorService matchExecutorService;
    private final MatchMapper matchMapper;

    @Override
    public MatchDTO createMatch(CreateMatchRequest request) {
        MatchDTO matchDTO = createMatchInTransaction(request);

        // Start async match execution after transaction commits
        matchExecutorService.executeMatch(matchDTO.id());

        return matchDTO;
    }

    @Transactional
    protected MatchDTO createMatchInTransaction(CreateMatchRequest request) {
        Bot bot1 = botRepository.findById(request.bot1Id())
                .orElseThrow(() -> new DataNotFoundException("Bot not found: " + request.bot1Id()));
        Bot bot2 = botRepository.findById(request.bot2Id())
                .orElseThrow(() -> new DataNotFoundException("Bot not found: " + request.bot2Id()));

        if (bot1.getGame() != request.game() || bot2.getGame() != request.game()) {
            throw new BadRequestException("Both bots must play " + request.game());
        }

        Match match = Match.builder()
                .game(request.game())
                .status(MatchStatus.IN_PROGRESS)
                .build();
        match.start();

        MatchParticipant p1 = MatchParticipant.builder()
                .match(match)
                .bot(bot1)
                .playerIndex(0) // White / Player 1
                .build();

        MatchParticipant p2 = MatchParticipant.builder()
                .match(match)
                .bot(bot2)
                .playerIndex(1) // Black / Player 2
                .build();

        match.addParticipant(p1);
        match.addParticipant(p2);

        matchRepository.save(match);

        // Initialize Game State
        AbstractGameState gameState = initializeGameState(match);

        // Build DTO while still in transaction (to access lazy-loaded collections)
        return matchMapper.toDTO(match, gameState);
    }

    /**
     * Initializes the game state for a match and returns it.
     */
    private AbstractGameState initializeGameState(Match match) {
        return switch (match.getGame()) {
            case CHESS -> {
                GameEngine<ChessGameState, String> engine = gameEngineFactory.getEngine(Game.CHESS);
                ChessGameState initialState = engine.startNewGame();
                initialState.assignMatch(match);
                yield chessGameStateRepository.save(initialState);
            }
            case CONNECT_FOUR -> {
                GameEngine<Connect4GameState, Integer> engine = gameEngineFactory.getEngine(Game.CONNECT_FOUR);
                Connect4GameState initialState = engine.startNewGame();
                initialState.assignMatch(match);
                yield connect4GameStateRepository.save(initialState);
            }
        };
    }

    @Override
    @Transactional(readOnly = true)
    public MatchDTO getMatch(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new DataNotFoundException("Match not found"));
        AbstractGameState gameState = getGameState(match);
        return matchMapper.toDTO(match, gameState);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatchMoveDTO> getMatchMoves(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new DataNotFoundException("Match not found"));

        return matchMoveRepository.findByMatchIdOrderByCreatedAsc(matchId).stream()
                .map(matchMapper::toMoveDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatchDTO> getMatchesForBot(Long botId) {
        return matchRepository.findByParticipants_Bot_Id(botId).stream()
                .map(this::toMatchDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatchDTO> getCurrentUserMatches() {
        return matchRepository.findByUserIdOrderByCreatedDesc(currentUser.id()).stream()
                .map(this::toMatchDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatchDTO> getRecentMatches(int limit) {
        return matchRepository.findRecentMatches(PageRequest.of(0, limit)).stream()
                .map(this::toMatchDTO)
                .toList();
    }

    @Override
    @Transactional
    public void abortMatch(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new DataNotFoundException("Match not found"));

        if (match.getStatus() != MatchStatus.IN_PROGRESS) {
            throw new BadRequestException("Only in-progress matches can be aborted");
        }

        // Check if the current user owns any bot in this match
        boolean userOwnsBot = match.getParticipants().stream()
                .anyMatch(p -> p.getBot().getUserId().equals(currentUser.id()));

        if (!userOwnsBot) {
            throw new ForbiddenException("You can only abort matches involving your bots");
        }

        match.abort();
        matchRepository.save(match);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getLegalMoves(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new DataNotFoundException("Match not found"));

        if (match.getStatus() != MatchStatus.IN_PROGRESS) {
            return List.of();
        }

        if (match.getGame() == Game.CHESS) {
            ChessGameState state = chessGameStateRepository.findByMatchId(matchId)
                    .orElseThrow(() -> new DataNotFoundException("Game state not found"));

            GameEngine<ChessGameState, String> engine = gameEngineFactory.getEngine(Game.CHESS);
            if (engine instanceof ChessGameEngine chessEngine) {
                return chessEngine.getLegalMoves(state);
            }
        } else if (match.getGame() == Game.CONNECT_FOUR) {
            Connect4GameState state = connect4GameStateRepository.findByMatchId(matchId)
                    .orElseThrow(() -> new DataNotFoundException("Game state not found"));

            GameEngine<Connect4GameState, Integer> engine = gameEngineFactory.getEngine(Game.CONNECT_FOUR);
            if (engine instanceof Connect4GameEngine connect4Engine) {
                return connect4Engine.getLegalMoves(state).stream()
                        .map(String::valueOf)
                        .toList();
            }
        }

        return List.of();
    }

    /**
     * Helper method to convert Match to DTO with game state lookup.
     */
    private MatchDTO toMatchDTO(Match match) {
        AbstractGameState gameState = getGameState(match);
        return matchMapper.toDTO(match, gameState);
    }

    /**
     * Fetches the game state for a match based on game type.
     */
    private @Nullable AbstractGameState getGameState(Match match) {
        return switch (match.getGame()) {
            case CHESS -> chessGameStateRepository.findByMatchId(match.getId()).orElse(null);
            case CONNECT_FOUR -> connect4GameStateRepository.findByMatchId(match.getId()).orElse(null);
        };
    }
}
