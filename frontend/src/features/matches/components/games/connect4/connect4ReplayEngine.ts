import type { MatchMoveDto } from '@/api/generated';
import type { GameReplayEngine, GamePosition } from '../types';

/**
 * Connect4-specific game position with board state.
 */
export interface Connect4Position extends GamePosition<string, MatchMoveDto> {
  state: string; // 42-character board string ('0'=empty, '1'=P1, '2'=P2)
  move: MatchMoveDto | null;
}

const ROWS = 6;
const COLS = 7;
const EMPTY_BOARD = '0'.repeat(ROWS * COLS);

/**
 * Replay engine for Connect4 games.
 * Mirrors backend Connect4GameEngine logic for move application.
 */
class Connect4ReplayEngine implements GameReplayEngine<Connect4Position> {
  getInitialPosition(): Connect4Position {
    return { state: EMPTY_BOARD, move: null };
  }

  calculatePositions(moves: MatchMoveDto[]): Connect4Position[] {
    const positions: Connect4Position[] = [
      { state: EMPTY_BOARD, move: null }
    ];

    let currentBoard = EMPTY_BOARD;

    moves.forEach((move, index) => {
      // toSquare contains the column index for Connect4
      const columnIndex = move.toSquare ? parseInt(move.toSquare) : null;

      if (columnIndex === null || isNaN(columnIndex)) {
        console.error('Invalid Connect4 move:', move);
        return;
      }

      // Apply move using backend logic (mirrors Connect4GameEngine.applyMove)
      const playerIndex = index % 2; // P1 on even indices, P2 on odd
      currentBoard = this.applyMove(currentBoard, columnIndex, playerIndex);
      positions.push({ state: currentBoard, move });
    });

    return positions;
  }

  /**
   * Applies a move to the board.
   * Mirrors backend Connect4GameEngine.applyMove logic:
   * - Board is row-major indexed (index = row * COLS + col)
   * - Row 0 is bottom, Row 5 is top
   * - Gravity: find lowest empty row in column
   */
  private applyMove(board: string, columnIndex: number, playerIndex: number): string {
    if (columnIndex < 0 || columnIndex >= COLS) {
      console.error('Column index out of bounds:', columnIndex);
      return board;
    }

    const chars = board.split('');
    const playerChar = playerIndex === 0 ? '1' : '2';

    // Find the lowest empty row in the column (row 0 = bottom)
    for (let row = 0; row < ROWS; row++) {
      const index = row * COLS + columnIndex; // row-major indexing
      if (chars[index] === '0') {
        chars[index] = playerChar;
        break;
      }
    }

    return chars.join('');
  }
}

export const connect4ReplayEngine = new Connect4ReplayEngine();
