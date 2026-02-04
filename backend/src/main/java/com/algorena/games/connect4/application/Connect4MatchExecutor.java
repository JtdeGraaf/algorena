package com.algorena.games.connect4.application;

import com.algorena.bots.domain.Bot;
import com.algorena.bots.domain.Game;
import com.algorena.games.application.BotClientService;
import com.algorena.games.application.GameMatchExecutor;
import com.algorena.games.connect4.data.Connect4GameStateRepository;
import com.algorena.games.connect4.domain.Connect4GameState;
import com.algorena.games.connect4.domain.Connect4MatchMove;
import com.algorena.games.connect4.engine.Connect4GameEngine;
import com.algorena.games.data.MatchMoveRepository;
import com.algorena.games.domain.Match;
import com.algorena.games.domain.MatchParticipant;
import com.algorena.games.dto.BotMoveRequest;
import com.algorena.games.dto.BotMoveResponse;
import com.algorena.games.dto.Connect4GameStateDTO;
import com.algorena.games.engine.GameResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Connect4-specific implementation of the match executor.
 * Handles all Connect4 game logic including board state management,
 * column-based moves, and win condition checking.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class Connect4MatchExecutor implements GameMatchExecutor {

    private static final char EMPTY_CELL = '0';

    private final Connect4GameStateRepository gameStateRepository;
    private final MatchMoveRepository matchMoveRepository;
    private final Connect4GameEngine gameEngine;
    private final BotClientService botClientService;

    @Override
    public Game getGameType() {
        return Game.CONNECT_FOUR;
    }

    @Override
    public @Nullable GameResult executeSingleMove(Match match) {
        Connect4GameState state = gameStateRepository.findByMatchId(match.getId())
                .orElseThrow(() -> new IllegalStateException("Connect4 game state not found for match: " + match.getId()));

        // Check if game is already over
        GameResult result = gameEngine.checkResult(state);
        if (result != null) {
            return result;
        }

        // Determine current player and get their bot
        int currentPlayerIndex = getCurrentPlayerIndex(state);
        MatchParticipant currentParticipant = getParticipantByIndex(match, currentPlayerIndex);
        Bot bot = currentParticipant.getBot();

        // Get legal moves (available columns)
        List<String> legalMoves = gameEngine.getLegalMoves(state).stream()
                .map(String::valueOf)
                .toList();

        // Build and send request to bot
        BotMoveRequest request = buildMoveRequest(match, state, currentPlayerIndex, legalMoves);
        BotMoveResponse response = botClientService.requestMove(bot, request);
        String moveString = response.move().trim();

        log.debug("Connect4 bot {} responded with column: {}", bot.getName(), moveString);

        // Parse and validate move
        int columnIndex = parseColumnIndex(moveString);

        // Apply move
        Connect4GameState newState = gameEngine.applyMove(state, columnIndex, currentPlayerIndex);

        // Update persisted state
        state.updateBoardState(newState.getBoard(), newState.getLastMoveColumn());
        gameStateRepository.save(state);

        // Record the move
        recordMove(match, currentPlayerIndex, moveString, columnIndex);

        // Check for game end after move
        return gameEngine.checkResult(state);
    }

    @Override
    public int getCurrentPlayerIndex(Match match) {
        Connect4GameState state = gameStateRepository.findByMatchId(match.getId())
                .orElseThrow(() -> new IllegalStateException("Connect4 game state not found for match: " + match.getId()));
        return getCurrentPlayerIndex(state);
    }

    private int getCurrentPlayerIndex(Connect4GameState state) {
        // Player 1 moves on even counts (0, 2, 4...), Player 2 on odd counts (1, 3, 5...)
        long movesPlayed = state.getBoard().chars().filter(c -> c != EMPTY_CELL).count();
        return (int) (movesPlayed % 2);
    }

    private MatchParticipant getParticipantByIndex(Match match, int playerIndex) {
        return match.getParticipants().stream()
                .filter(p -> p.getPlayerIndex() == playerIndex)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Participant not found for index " + playerIndex));
    }

    private BotMoveRequest buildMoveRequest(Match match, Connect4GameState state, int playerIndex, List<String> legalMoves) {
        Connect4GameStateDTO stateDTO = new Connect4GameStateDTO(
                state.getBoard(),
                state.getLastMoveColumn()
        );
        return new BotMoveRequest(match.getId(), Game.CONNECT_FOUR, playerIndex, stateDTO, legalMoves);
    }

    private int parseColumnIndex(String moveString) {
        try {
            int columnIndex = Integer.parseInt(moveString);
            if (columnIndex < 0 || columnIndex > 6) {
                throw new IllegalArgumentException("Column index must be between 0 and 6, got: " + columnIndex);
            }
            return columnIndex;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid move format: must be a column index (0-6), got: " + moveString);
        }
    }

    private void recordMove(Match match, int playerIndex, String moveNotation, int columnIndex) {
        Connect4MatchMove matchMove = Connect4MatchMove.builder()
                .match(match)
                .playerIndex(playerIndex)
                .moveNotation(moveNotation)
                .columnIndex(columnIndex)
                .build();

        matchMoveRepository.save(matchMove);
    }
}
