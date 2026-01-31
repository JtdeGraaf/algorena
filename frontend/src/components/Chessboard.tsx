import { useMemo } from 'react';
import { cn } from '@/lib/utils';

interface ChessboardProps {
  fen: string;
  className?: string;
  size?: 'sm' | 'md' | 'lg';
  highlightSquares?: { from?: string; to?: string };
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

function squareToCoords(square: string): { row: number; col: number } | null {
  if (!square || square.length !== 2) return null;
  const file = square[0].toLowerCase();
  const rank = square[1];
  const col = FILES.indexOf(file);
  const row = RANKS.indexOf(rank);
  if (col === -1 || row === -1) return null;
  return { row, col };
}

export function Chessboard({ fen, className, size = 'md', highlightSquares }: ChessboardProps) {
  const board = useMemo(() => parseFen(fen), [fen]);

  const sizeClasses = {
    sm: 'w-48',
    md: 'w-64',
    lg: 'w-96',
  };

  const pieceSizeClasses = {
    sm: 'text-xl',
    md: 'text-2xl',
    lg: 'text-4xl',
  };

  const fromCoords = highlightSquares?.from ? squareToCoords(highlightSquares.from) : null;
  const toCoords = highlightSquares?.to ? squareToCoords(highlightSquares.to) : null;

  return (
    <div className={cn('aspect-square', sizeClasses[size], className)}>
      <div className="grid h-full w-full grid-cols-8 grid-rows-8 overflow-hidden rounded-md border border-zinc-700">
        {board.map((row, rowIndex) =>
          row.map((piece, colIndex) => {
            const isLight = (rowIndex + colIndex) % 2 === 0;
            const isFromSquare = fromCoords?.row === rowIndex && fromCoords?.col === colIndex;
            const isToSquare = toCoords?.row === rowIndex && toCoords?.col === colIndex;
            const isHighlighted = isFromSquare || isToSquare;

            return (
              <div
                key={`${rowIndex}-${colIndex}`}
                className={cn(
                  'relative flex items-center justify-center',
                  isLight ? 'bg-zinc-300' : 'bg-zinc-600',
                  isHighlighted && 'ring-2 ring-inset ring-emerald-500',
                  isFromSquare && 'bg-emerald-400/50',
                  isToSquare && 'bg-emerald-500/50'
                )}
              >
                {piece && (
                  <span
                    className={cn(
                      pieceSizeClasses[size],
                      'select-none leading-none',
                      piece === piece.toUpperCase() ? 'text-white drop-shadow-[0_1px_1px_rgba(0,0,0,0.8)]' : 'text-zinc-900'
                    )}
                  >
                    {PIECE_UNICODE[piece]}
                  </span>
                )}
                {/* Coordinates */}
                {colIndex === 0 && (
                  <span className={cn(
                    'absolute left-0.5 top-0.5 text-[8px] font-medium',
                    isLight ? 'text-zinc-600' : 'text-zinc-300'
                  )}>
                    {RANKS[rowIndex]}
                  </span>
                )}
                {rowIndex === 7 && (
                  <span className={cn(
                    'absolute bottom-0.5 right-0.5 text-[8px] font-medium',
                    isLight ? 'text-zinc-600' : 'text-zinc-300'
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

