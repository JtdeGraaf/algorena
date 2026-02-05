import { Connect4Board } from '@/components/Connect4Board';
import type { GameDetailsProps } from '../types';

/**
 * Connect4-specific match details component.
 * Displays the current Connect4 board position.
 */
export function Connect4MatchDetails({ match, isFullscreen }: GameDetailsProps) {
  if (!match.state || !('board' in match.state)) {
    return null;
  }

  return (
    <Connect4Board
      board={match.state.board as string}
      size={isFullscreen ? 'xl' : 'lg'}
    />
  );
}
