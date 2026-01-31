package com.algorena.games.engine;

import com.algorena.bots.domain.Game;
import com.algorena.games.chess.engine.ChessGameEngine;
import com.algorena.games.connect4.engine.Connect4GameEngine;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class GameEngineFactory {

    private final Map<Game, GameEngine<?, ?>> engines;

    public GameEngineFactory(ChessGameEngine chessGameEngine, Connect4GameEngine connect4GameEngine) {
        this.engines = Map.of(
                Game.CHESS, chessGameEngine,
                Game.CONNECT_FOUR, connect4GameEngine
        );
    }

    @SuppressWarnings("unchecked")
    public <S, M> GameEngine<S, M> getEngine(Game game) {
        return (GameEngine<S, M>) Optional.ofNullable(engines.get(game))
                .orElseThrow(() -> new IllegalArgumentException("No engine found for game: " + game));
    }
}

