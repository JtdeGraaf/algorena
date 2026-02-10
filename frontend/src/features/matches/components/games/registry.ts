import type React from 'react';
import type { GameDetailsProps, GameReplayProps, GameReplayEngine, GamePosition } from './types';
import { ChessMatchDetails } from './chess/ChessMatchDetails';
import { ChessMatchReplay } from './chess/ChessMatchReplay';
import { chessReplayEngine } from './chess/chessReplayEngine';
import { Connect4MatchDetails } from './connect4/Connect4MatchDetails';
import { Connect4MatchReplay } from './connect4/Connect4MatchReplay';
import { connect4ReplayEngine } from './connect4/connect4ReplayEngine';

/**
 * Registry entry for a game type containing its UI components.
 */
interface GameComponents {
  DetailsComponent: React.ComponentType<GameDetailsProps>;
  ReplayComponent: React.ComponentType<GameReplayProps<GamePosition>>;
}

/**
 * Component registry mapping game type names to their implementations.
 */
const gameRegistry: Record<string, GameComponents> = {
  CHESS: {
    DetailsComponent: ChessMatchDetails,
    ReplayComponent: ChessMatchReplay as React.ComponentType<GameReplayProps<GamePosition>>
  },
  CONNECT_FOUR: {
    DetailsComponent: Connect4MatchDetails,
    ReplayComponent: Connect4MatchReplay as React.ComponentType<GameReplayProps<GamePosition>>
  }
};

/**
 * Engine registry mapping game type names to their replay engines.
 */
const engineRegistry: Record<string, GameReplayEngine<GamePosition>> = {
  CHESS: chessReplayEngine,
  CONNECT_FOUR: connect4ReplayEngine
};

/**
 * Retrieves the UI components for a game type.
 * @throws Error if game type is not registered
 */
export function getGameComponents(gameType: string): GameComponents {
  const components = gameRegistry[gameType];
  if (!components) {
    throw new Error(`No components registered for game type: ${gameType}`);
  }
  return components;
}

/**
 * Retrieves the replay engine for a game type.
 * @throws Error if game type is not registered
 */
export function getReplayEngine<TPosition extends GamePosition = GamePosition>(
  gameType: string
): GameReplayEngine<TPosition> {
  const engine = engineRegistry[gameType];
  if (!engine) {
    throw new Error(`No replay engine registered for game type: ${gameType}`);
  }
  return engine as GameReplayEngine<TPosition>;
}

/**
 * Checks if a game type is registered and supports replay.
 */
export function isGameRegistered(gameType: string): boolean {
  return gameType in gameRegistry && gameType in engineRegistry;
}
