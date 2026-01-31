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
import com.algorena.games.engine.GameResult;
import com.algorena.security.CurrentUser;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

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

    @Override
    @Transactional
    public MatchDTO createMatch(CreateMatchRequest request) {
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
                .playerIndex(0) // White
                .build();

        MatchParticipant p2 = MatchParticipant.builder()
                .match(match)
                .bot(bot2)
                .playerIndex(1) // Black
                .build();

        match.addParticipant(p1);
        match.addParticipant(p2);

        matchRepository.save(match);

        // Initialize Game State
        initializeGameState(match);

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
                // Explicitly save the relationship or ensure match is persisted first (it is saved above)
                connect4GameStateRepository.save(initialState);
            }
            default -> throw new UnsupportedOperationException("Game not supported: " + match.getGame());
        }
    }

    @Override
    @Transactional
    public void makeMove(UUID matchId, MakeMoveRequest request) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new DataNotFoundException("Match not found"));

        Bot bot = botRepository.findById(request.botId())
                .orElseThrow(() -> new DataNotFoundException("Bot not found: " + request.botId()));

        if (!bot.getUserId().equals(currentUser.id())) {
            throw new ForbiddenException("You do not own this bot");
        }

        if (match.getStatus() != MatchStatus.IN_PROGRESS) {
            throw new BadRequestException("Match is not in progress");
        }

        MatchParticipant participant = match.getParticipants().stream()
                .filter(p -> p.getBot().getId().equals(bot.getId()))
                .findFirst()
                .orElseThrow(() -> new ForbiddenException("Bot is not a participant in this match"));

        switch (match.getGame()) {
            case CHESS -> handleChessMove(match, participant, request.move());
            case CONNECT_FOUR -> handleConnect4Move(match, participant, request.move());
            default -> throw new UnsupportedOperationException("Game not supported");
        }
    }

    private void handleConnect4Move(Match match, MatchParticipant participant, String moveString) {
        Connect4GameState state = connect4GameStateRepository.findByMatchId(match.getId())
                .orElseThrow(() -> new DataNotFoundException("Game state not found"));

        GameEngine<Connect4GameState, Integer> engine = gameEngineFactory.getEngine(Game.CONNECT_FOUR);

        int columnIndex;
        try {
            columnIndex = Integer.parseInt(moveString);
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid move format: must be a column index (0-6)");
        }

        // Apply Move
        Connect4GameState newState = engine.applyMove(state, columnIndex, participant.getPlayerIndex());

        // Update State
        state.updateBoardState(newState.getBoard(), newState.getLastMoveColumn());
        connect4GameStateRepository.save(state);

        // Record Move
        Connect4MatchMove matchMove = Connect4MatchMove.builder()
                .match(match)
                .playerIndex(participant.getPlayerIndex())
                .moveNotation(moveString)
                .columnIndex(columnIndex)
                .build();
        matchMoveRepository.save(matchMove);

        // Check Result
        GameResult result = engine.checkResult(state);
        if (result != null) {
            finishMatch(match, result);
        }
    }

    private void handleChessMove(Match match, MatchParticipant participant, String moveNotation) {
        ChessGameState state = chessGameStateRepository.findByMatchId(match.getId())
                .orElseThrow(() -> new DataNotFoundException("Game state not found"));

        GameEngine<ChessGameState, String> engine = gameEngineFactory.getEngine(Game.CHESS);

        // Apply Move (Engine validates turn and rules)
        ChessGameState newState = engine.applyMove(state, moveNotation, participant.getPlayerIndex());

        // Update State
        state.updateBoardState(newState.getFen(), newState.getHalfMoveClock(), newState.getFullMoveNumber());

        chessGameStateRepository.save(state);

        // Record Move
        // Parse move details
        Move move = new Move(moveNotation, Side.WHITE);
        String promotion = move.getPromotion().equals(com.github.bhlangonijr.chesslib.Piece.NONE) ? null : move.getPromotion().value();

        ChessMatchMove matchMove = ChessMatchMove.builder()
                .match(match)
                .playerIndex(participant.getPlayerIndex())
                .moveNotation(moveNotation)
                .fromSquare(move.getFrom().value())
                .toSquare(move.getTo().value())
                .promotionPiece(promotion)
                .build();
        matchMoveRepository.save(matchMove);

        // Check Result
        GameResult result = engine.checkResult(state);
        if (result != null) {
            finishMatch(match, result);
        }
    }

    private void finishMatch(Match match, GameResult result) {
        match.finish();

        for (MatchParticipant p : match.getParticipants()) {
            // Default to 0.0 if the player is not in the scores map (e.g. forfeit/loss)
            Double score = result.scores().getScore(p.getPlayerIndex());
            p.recordScore(score);
        }

        matchRepository.save(match);
    }

    @Override
    @Transactional(readOnly = true)
    public MatchDTO getMatch(UUID matchId) {
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
    public List<MatchMoveDTO> getMatchMoves(UUID matchId) {
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
        // Use optimized repository method instead of loading all matches
        return matchRepository.findByUserIdOrderByCreatedDesc(currentUser.id()).stream()
                .map(this::toMatchDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatchDTO> getRecentMatches(int limit) {
        // Use optimized repository method with Pageable for efficient limiting
        return matchRepository.findRecentMatches(PageRequest.of(0, limit)).stream()
                .map(this::toMatchDTO)
                .toList();
    }

    @Override
    @Transactional
    public void abortMatch(UUID matchId) {
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
    public List<String> getLegalMoves(UUID matchId) {
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
             // For Connect 4, we might not populate from/to squares in the same way,
             // or we could map "column" to "to" if frontend expects it.
             // For now, leaving as null, relying on moveNotation ("3")
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
