package com.algorena.games.application;

import com.algorena.bots.data.BotRepository;
import com.algorena.bots.domain.Bot;
import com.algorena.common.exception.BadRequestException;
import com.algorena.common.exception.DataNotFoundException;
import com.algorena.common.exception.ForbiddenException;
import com.algorena.games.chess.data.ChessGameStateRepository;
import com.algorena.games.chess.domain.ChessGameState;
import com.algorena.games.chess.domain.ChessMatchMove;
import com.algorena.games.data.MatchMoveRepository;
import com.algorena.games.data.MatchRepository;
import com.algorena.games.domain.Match;
import com.algorena.games.domain.MatchParticipant;
import com.algorena.games.domain.MatchStatus;
import com.algorena.games.dto.CreateMatchRequest;
import com.algorena.games.dto.MakeMoveRequest;
import com.algorena.games.dto.MatchDTO;
import com.algorena.games.dto.MatchParticipantDTO;
import com.algorena.games.engine.GameEngine;
import com.algorena.games.engine.GameEngineFactory;
import com.algorena.games.engine.GameResult;
import com.algorena.security.CurrentUser;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MatchServiceImpl implements MatchService {

    private final MatchRepository matchRepository;
    private final MatchMoveRepository matchMoveRepository;
    private final ChessGameStateRepository chessGameStateRepository;
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
                GameEngine<ChessGameState, String> engine = gameEngineFactory.getEngine(com.algorena.bots.domain.Game.CHESS);
                ChessGameState initialState = engine.startNewGame();
                initialState.assignMatch(match);
                chessGameStateRepository.save(initialState);
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
            default -> throw new UnsupportedOperationException("Game not supported");
        }
    }

    private void handleChessMove(Match match, MatchParticipant participant, String moveNotation) {
        ChessGameState state = chessGameStateRepository.findByMatchId(match.getId())
                .orElseThrow(() -> new DataNotFoundException("Game state not found"));

        GameEngine<ChessGameState, String> engine = gameEngineFactory.getEngine(com.algorena.bots.domain.Game.CHESS);

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
    public MatchDTO getMatch(UUID matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new DataNotFoundException("Match not found"));
        return toMatchDTO(match);
    }

    private MatchDTO toMatchDTO(Match match) {
        return new MatchDTO(
                match.getId(),
                match.getGame(),
                match.getStatus(),
                match.getStartedAt(),
                match.getFinishedAt(),
                match.getParticipants().stream()
                        .map(this::toMatchParticipantDTO)
                        .toList()
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
}
