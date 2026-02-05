import { Chessboard } from '@/components/Chessboard';
import type { GameDetailsProps } from '../types';

/**
 * Chess-specific match details component.
 * Displays the current chess board position.
 */
export function ChessMatchDetails({ match, isFullscreen }: GameDetailsProps) {
  if (!match.state || !('fen' in match.state)) {
    return null;
  }

  return (
    <Chessboard
      fen={match.state.fen as string}
      size={isFullscreen ? 'xl' : 'lg'}
    />
  );
}
