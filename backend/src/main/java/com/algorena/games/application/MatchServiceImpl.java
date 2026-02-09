package com.algorena.games.application;

import com.algorena.bots.data.BotRepository;
import com.algorena.bots.domain.Bot;
import com.algorena.bots.domain.Game;
import com.algorena.common.exception.BadRequestException;
import com.algorena.common.exception.DataNotFoundException;
import com.algorena.common.exception.ForbiddenException;
import com.algorena.games.chess.data.ChessGameStateRepository;
import com.algorena.games.chess.domain.ChessGameState;
import com.algorena.games.chess.domain.ChessMatchMove;
import com.algorena.games.chess.engine.ChessGameEngine;
import com.algorena.games.connect4.domain.Connect4GameState;
import com.algorena.games.connect4.domain.Connect4MatchMove;
import com.algorena.games.connect4.engine.Connect4GameEngine;
import com.algorena.games.data.MatchMoveRepository;
import com.algorena.games.data.MatchRepository;
import com.algorena.games.domain.AbstractMatchMove;
import com.algorena.games.domain.Match;
import com.algorena.games.domain.MatchParticipant;
import com.algorena.games.domain.MatchStatus;
import com.algorena.games.dto.*;
import com.algorena.games.engine.GameEngine;
import com.algorena.games.engine.GameEngineFactory;
import com.algorena.security.CurrentUser;
import lombok.RequiredArgsConstructor;
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
    private final com.algorena.games.connect4.data.Connect4GameStateRepository connect4GameStateRepository;
    private final GameEngineFactory gameEngineFactory;
    private final BotRepository botRepository;
    private final CurrentUser currentUser;
    private final MatchExecutorService matchExecutorService;

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
        initializeGameState(match);

        // Build DTO while still in transaction (to access lazy-loaded collections)
        return toMatchDTO(match);
    }

    private void initializeGameState(Match match) {
        switch (match.getGame()) {
            case CHESS -> {
                GameEngine<ChessGameState, String> engine = gameEngineFactory.getEngine(Game.CHESS);
                ChessGameState initialState = engine.startNewGame();
                initialState.assignMatch(match);
                chessGameStateRepository.save(initialState);
            }
            case CONNECT_FOUR -> {
                GameEngine<Connect4GameState, Integer> engine = gameEngineFactory.getEngine(Game.CONNECT_FOUR);
                Connect4GameState initialState = engine.startNewGame();
                initialState.assignMatch(match);
                connect4GameStateRepository.save(initialState);
            }
            default -> throw new UnsupportedOperationException("Game not supported: " + match.getGame());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public MatchDTO getMatch(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new DataNotFoundException("Match not found"));
        return toMatchDTO(match);
    }

    private MatchDTO toMatchDTO(Match match) {
        GameStateDTO stateDTO = null;
        if (match.getGame() == Game.CHESS) {
            ChessGameState state = chessGameStateRepository.findByMatchId(match.getId())
                    .orElse(null);
            if (state != null) {
                stateDTO = new ChessGameStateDTO(
                        state.getFen(),
                        state.getPgn(),
                        state.getHalfMoveClock(),
                        state.getFullMoveNumber()
                );
            }
        } else if (match.getGame() == Game.CONNECT_FOUR) {
            Connect4GameState state = connect4GameStateRepository.findByMatchId(match.getId())
                    .orElse(null);
            if (state != null) {
                stateDTO = new Connect4GameStateDTO(
                        state.getBoard(),
                        state.getLastMoveColumn()
                );
            }
        }

        return new MatchDTO(
                match.getId(),
                match.getGame(),
                match.getStatus(),
                match.getStartedAt(),
                match.getFinishedAt(),
                match.getParticipants().stream()
                        .map(this::toMatchParticipantDTO)
                        .toList(),
                stateDTO
        );
    }

    private MatchParticipantDTO toMatchParticipantDTO(MatchParticipant participant) {
        return new MatchParticipantDTO(
                participant.getId(),
                participant.getBot().getId(),
                participant.getBot().getName(),
                participant.getPlayerIndex(),
                participant.getScore()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatchMoveDTO> getMatchMoves(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new DataNotFoundException("Match not found"));

        return matchMoveRepository.findByMatchIdOrderByCreatedAsc(matchId).stream()
                .map(this::toMatchMoveDTO)
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

    private MatchMoveDTO toMatchMoveDTO(AbstractMatchMove move) {
        String from = null;
        String to = null;
        String promotion = null;

        if (move instanceof ChessMatchMove chessMove) {
            from = chessMove.getFromSquare();
            to = chessMove.getToSquare();
            promotion = chessMove.getPromotionPiece();
        } else if (move instanceof Connect4MatchMove connect4Move) {
             to = String.valueOf(connect4Move.getColumnIndex());
        }

        return new MatchMoveDTO(
                move.getId(),
                move.getPlayerIndex(),
                move.getMoveNotation(),
                move.getCreated(),
                from,
                to,
                promotion
        );
    }
}
