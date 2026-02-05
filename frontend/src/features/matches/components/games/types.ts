import type { MatchDto, MatchMoveDto } from '@/api/generated';

/**
 * Base interface for game position state.
 * Generic type parameters allow game-specific state representation.
 */
export interface GamePosition<TState = unknown, TMove = unknown> {
  state: TState;
  move: TMove | null;
}

/**
 * Props for game-specific match details components.
 */
export interface GameDetailsProps {
  match: MatchDto;
  moves: MatchMoveDto[] | undefined;
  isFullscreen: boolean;
}

/**
 * Props for game-specific match replay components.
 */
export interface GameReplayProps<TPosition extends GamePosition = GamePosition> {
  match: MatchDto;
  moves: MatchMoveDto[];
  positions: TPosition[];
  currentMoveIndex: number;
  onMoveSelect: (index: number) => void;
}

/**
 * Interface for game-specific replay engines.
 * Each game type implements this to calculate position history from moves.
 */
export interface GameReplayEngine<TPosition extends GamePosition = GamePosition> {
  /**
   * Returns the initial game position before any moves.
   */
  getInitialPosition(): TPosition;

  /**
   * Calculates all positions from a sequence of moves.
   * Returns array where index 0 is initial position, index i+1 is position after move i.
   */
  calculatePositions(moves: MatchMoveDto[]): TPosition[];
}
