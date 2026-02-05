import { Chess } from 'chess.js';
import type { MatchMoveDto } from '@/api/generated';
import type { GameReplayEngine, GamePosition } from '../types';

/**
 * Chess-specific game position with FEN state.
 */
export interface ChessPosition extends GamePosition<string, MatchMoveDto> {
  state: string; // FEN string
  move: MatchMoveDto | null;
}

const INITIAL_FEN = 'rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1';

/**
 * Replay engine for chess games.
 * Uses chess.js to calculate position history from moves.
 */
class ChessReplayEngine implements GameReplayEngine<ChessPosition> {
  getInitialPosition(): ChessPosition {
    return { state: INITIAL_FEN, move: null };
  }

  calculatePositions(moves: MatchMoveDto[]): ChessPosition[] {
    const chess = new Chess();
    const positions: ChessPosition[] = [
      { state: chess.fen(), move: null }
    ];

    for (const move of moves) {
      try {
        if (move.fromSquare && move.toSquare) {
          chess.move({
            from: move.fromSquare.toLowerCase(),
            to: move.toSquare.toLowerCase(),
            promotion: move.promotionPiece?.toLowerCase() as 'q' | 'r' | 'b' | 'n' | undefined,
          });
        } else if (move.moveNotation) {
          // Fallback to SAN notation
          chess.move(move.moveNotation.toLowerCase());
        }
        positions.push({ state: chess.fen(), move });
      } catch (e) {
        console.error('Failed to apply chess move:', move, e);
      }
    }

    return positions;
  }
}

export const chessReplayEngine = new ChessReplayEngine();
