import { useMemo, useState, useCallback } from 'react';
import { cn } from '@/lib/utils';

interface ChessboardProps {
  fen: string;
  className?: string;
  size?: 'sm' | 'md' | 'lg' | 'xl' | 'full';
  highlightSquares?: { from?: string; to?: string };
  interactive?: boolean;
  legalMoves?: string[]; // List of UCI moves (e.g., "e2e4", "e7e8q")
  onMove?: (from: string, to: string) => void;
  onInvalidMove?: (reason?: string) => void;
}

const PIECE_UNICODE: Record<string, string> = {
  'K': '♔', 'Q': '♕', 'R': '♖', 'B': '♗', 'N': '♘', 'P': '♙',
  'k': '♚', 'q': '♛', 'r': '♜', 'b': '♝', 'n': '♞', 'p': '♟',
};

const FILES = ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'];
const RANKS = ['8', '7', '6', '5', '4', '3', '2', '1'];

function parseFen(fen: string): (string | null)[][] {
  const board: (string | null)[][] = [];
  const [position] = fen.split(' ');
  const rows = position.split('/');

  for (const row of rows) {
    const boardRow: (string | null)[] = [];
    for (const char of row) {
      if (/\d/.test(char)) {
        // Empty squares
        const count = parseInt(char);
        for (let i = 0; i < count; i++) {
          boardRow.push(null);
        }
      } else {
        boardRow.push(char);
      }
    }
    board.push(boardRow);
  }

  return board;
}

function coordsToSquare(row: number, col: number): string {
  return FILES[col] + RANKS[row];
}

function squareToCoords(square: string): { row: number; col: number } | null {
  if (!square || square.length !== 2) return null;
  const file = square[0].toLowerCase();
  const rank = square[1];
  const col = FILES.indexOf(file);
  const row = RANKS.indexOf(rank);
  if (col === -1 || row === -1) return null;
  return { row, col };
}

function isPieceSameColor(p1: string | null, p2: string | null): boolean {
  if (!p1 || !p2) return false;
  const isP1White = p1 === p1.toUpperCase();
  const isP2White = p2 === p2.toUpperCase();
  return isP1White === isP2White;
}

export function Chessboard({
  fen,
  className,
  size = 'md',
  highlightSquares,
  interactive = false,
  legalMoves,
  onMove,
  onInvalidMove
}: ChessboardProps) {
  const board = useMemo(() => parseFen(fen), [fen]);
  const [selectedSquare, setSelectedSquare] = useState<string | null>(null);

  const sizeClasses = {
    sm: 'w-48',
    md: 'w-64',
    lg: 'w-80',
    xl: 'w-[28rem]',
    full: 'w-full max-w-[32rem]',
  };

  const pieceSizeClasses = {
    sm: 'text-xl',
    md: 'text-3xl',
    lg: 'text-4xl',
    xl: 'text-5xl',
    full: 'text-5xl',
  };

  const coordSizeClasses = {
    sm: 'text-[8px]',
    md: 'text-[10px]',
    lg: 'text-xs',
    xl: 'text-sm',
    full: 'text-sm',
  };

  const fromCoords = highlightSquares?.from ? squareToCoords(highlightSquares.from) : null;
  const toCoords = highlightSquares?.to ? squareToCoords(highlightSquares.to) : null;
  const selectedCoords = selectedSquare ? squareToCoords(selectedSquare) : null;

  // Calculate valid target squares for the selected piece
  const validTargets = useMemo(() => {
    if (!selectedSquare || !legalMoves) return new Set<string>();
    const targets = new Set<string>();
    legalMoves.forEach(move => {
      if (move.startsWith(selectedSquare)) {
        // move is like "e2e4" or "e7e8q". Target is indices 2,3 (length 4 or 5)
        targets.add(move.substring(2, 4));
      }
    });
    return targets;
  }, [selectedSquare, legalMoves]);

  const handleSquareClick = useCallback((row: number, col: number) => {
    if (!interactive || !onMove) return;

    const square = coordsToSquare(row, col);
    const piece = board[row][col];

    if (selectedSquare) {
      if (selectedSquare === square) {
        setSelectedSquare(null);
        return;
      }

      // Check if clicking another own piece (to switch selection)
      const selectedCoords = squareToCoords(selectedSquare);
      if (selectedCoords) {
        const selectedPiece = board[selectedCoords.row][selectedCoords.col];
        if (isPieceSameColor(selectedPiece, piece)) {
          setSelectedSquare(square);
          return;
        }
      }

      // If we have a selected square, try to make a move
      if (validTargets.has(square)) {
        onMove(selectedSquare, square);
        setSelectedSquare(null);
      } else {
        // Invalid move attempt
        if (onInvalidMove) {
          onInvalidMove("This move is illegal (e.g. leaves king in check)");
        }
        setSelectedSquare(null);
      }
    } else if (piece) {
      // Select this square if it has a piece
      setSelectedSquare(square);
    }
  }, [interactive, onMove, selectedSquare, board, validTargets, onInvalidMove]);

  return (
    <div className={cn('aspect-square', sizeClasses[size], className)}>
      <div className="grid h-full w-full grid-cols-8 grid-rows-8 overflow-hidden rounded-lg border border-zinc-600 shadow-xl">
        {board.map((row, rowIndex) =>
          row.map((piece, colIndex) => {
            const isLight = (rowIndex + colIndex) % 2 === 0;
            const isFromSquare = fromCoords?.row === rowIndex && fromCoords?.col === colIndex;
            const isToSquare = toCoords?.row === rowIndex && toCoords?.col === colIndex;
            const isSelected = selectedCoords?.row === rowIndex && selectedCoords?.col === colIndex;
            const squareName = coordsToSquare(rowIndex, colIndex);
            const isValidTarget = validTargets.has(squareName);
            const isHighlighted = isFromSquare || isToSquare;

            return (
              <div
                key={`${rowIndex}-${colIndex}`}
                onClick={() => handleSquareClick(rowIndex, colIndex)}
                className={cn(
                  'relative flex items-center justify-center transition-colors',
                  // Dark theme colors - zinc/slate tones that match the site
                  isLight ? 'bg-zinc-400' : 'bg-zinc-700',
                  // Highlight last move with emerald (site accent color)
                  isFromSquare && 'bg-emerald-600/70',
                  isToSquare && 'bg-emerald-500/80',
                  // Selected square highlight
                  isSelected && 'bg-sky-500/80',
                  // Valid target highlight (green dot or slight tint)
                  isValidTarget && (isLight ? 'bg-emerald-300/50' : 'bg-emerald-800/50'),
                  // Interactive hover effect
                  interactive && 'cursor-pointer',
                  interactive && !isHighlighted && !isSelected && !isValidTarget && 'hover:brightness-125'
                )}
              >
                {/* Add a small dot for valid moves on empty squares */}
                {isValidTarget && !piece && (
                  <div className="absolute h-3 w-3 rounded-full bg-emerald-500/50" />
                )}
                {/* Add rings for capture targets */}
                {isValidTarget && piece && (
                   <div className="absolute h-full w-full rounded-full border-4 border-emerald-500/50" />
                )}
                
                {piece && (
                  <span
                    className={cn(
                      pieceSizeClasses[size],
                      'select-none leading-none transition-transform',
                      interactive && 'hover:scale-110',
                      // White pieces - cream color with dark shadow
                      piece === piece.toUpperCase()
                        ? 'text-zinc-100 drop-shadow-[0_2px_2px_rgba(0,0,0,0.8)]'
                        // Black pieces - dark with subtle light edge
                        : 'text-zinc-900 drop-shadow-[0_1px_1px_rgba(255,255,255,0.2)]'
                    )}
                    style={{
                      textShadow: piece === piece.toUpperCase()
                        ? '0 0 8px rgba(0,0,0,0.5)'
                        : undefined
                    }}
                  >
                    {PIECE_UNICODE[piece]}
                  </span>
                )}
                {/* Coordinates */}
                {colIndex === 0 && (
                  <span className={cn(
                    'absolute left-0.5 top-0.5 font-bold opacity-60',
                    coordSizeClasses[size],
                    isLight ? 'text-zinc-600' : 'text-zinc-400'
                  )}>
                    {RANKS[rowIndex]}
                  </span>
                )}
                {rowIndex === 7 && (
                  <span className={cn(
                    'absolute bottom-0.5 right-1 font-bold opacity-60',
                    coordSizeClasses[size],
                    isLight ? 'text-zinc-600' : 'text-zinc-400'
                  )}>
                    {FILES[colIndex]}
                  </span>
                )}
              </div>
            );
          })
        )}
      </div>
    </div>
  );
}
