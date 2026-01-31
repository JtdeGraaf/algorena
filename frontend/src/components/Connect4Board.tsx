import { useState, useCallback } from 'react';
import { cn } from '@/lib/utils';

interface Connect4BoardProps {
  board: string; // 42-char string, '0'=Empty, '1'=P1, '2'=P2
  className?: string;
  size?: 'sm' | 'md' | 'lg' | 'xl' | 'full';
  interactive?: boolean;
  legalMoves?: string[]; // List of available column indices (e.g. "0", "1", "6")
  onMove?: (column: string) => void;
  onInvalidMove?: (reason?: string) => void;
}

const ROWS = 6;
const COLS = 7;

export function Connect4Board({
  board,
  className,
  size = 'md',
  interactive = false,
  legalMoves,
  onMove,
  onInvalidMove
}: Connect4BoardProps) {
  const [hoveredColumn, setHoveredColumn] = useState<number | null>(null);

  const sizeClasses = {
    sm: 'w-48 h-40',
    md: 'w-64 h-56',
    lg: 'w-80 h-72',
    xl: 'w-[28rem] h-[24rem]',
    full: 'w-full max-w-[32rem] aspect-[7/6]',
  };

  const getPieceColor = (char: string) => {
    switch (char) {
      case '1': return 'bg-red-500 shadow-[inset_0_2px_4px_rgba(0,0,0,0.3)] border-red-700';
      case '2': return 'bg-yellow-400 shadow-[inset_0_2px_4px_rgba(0,0,0,0.3)] border-yellow-600';
      default: return 'bg-zinc-900 shadow-[inset_0_2px_4px_rgba(0,0,0,0.5)]';
    }
  };

  const handleColumnClick = useCallback((colIndex: number) => {
    if (!interactive || !onMove) return;

    if (legalMoves && !legalMoves.includes(colIndex.toString())) {
      onInvalidMove?.("This column is full");
      return;
    }

    onMove(colIndex.toString());
  }, [interactive, onMove, legalMoves, onInvalidMove]);

  // Create grid
  const grid = [];
  // Row 5 is top, Row 0 is bottom in engine logic.
  // Visual rendering needs Row 5 at top.
  for (let r = ROWS - 1; r >= 0; r--) {
    for (let c = 0; c < COLS; c++) {
      const index = r * COLS + c;
      const char = board.charAt(index) || '0';
      grid.push({ r, c, char });
    }
  }

  return (
    <div className={cn('relative rounded-lg bg-blue-700 p-2 shadow-xl', sizeClasses[size], className)}>
      <div 
        className="grid h-full w-full grid-cols-7 grid-rows-6 gap-1 sm:gap-2"
        onMouseLeave={() => setHoveredColumn(null)}
      >
        {grid.map(({ r, c, char }) => {
          const isLegal = legalMoves?.includes(c.toString());
          const isHovered = hoveredColumn === c;
          
          return (
            <div
              key={`${r}-${c}`}
              className="relative flex items-center justify-center"
              onMouseEnter={() => setHoveredColumn(c)}
              onClick={() => handleColumnClick(c)}
            >
              {/* Slot Hole */}
              <div className={cn(
                "h-full w-full rounded-full border-2 sm:border-4 transition-colors duration-200",
                getPieceColor(char),
                // Highlight valid move on hover (only on empty top slots visually, or just column highlight)
                interactive && isHovered && isLegal && char === '0' && "ring-2 ring-white/50",
                interactive && isLegal ? "cursor-pointer" : "cursor-default"
              )} />
              
              {/* Column Highlight Overlay */}
              {interactive && isHovered && (
                <div className={cn(
                  "absolute inset-0 z-10 rounded-full",
                  isLegal ? "bg-white/10" : "bg-red-500/20 cursor-not-allowed"
                )} />
              )}
            </div>
          );
        })}
      </div>
      
      {/* Base */}
      <div className="absolute -bottom-4 left-0 right-0 h-4 rounded-b-xl bg-blue-800 shadow-lg" />
      <div className="absolute -bottom-4 left-4 right-4 h-4 translate-y-1 rounded-b-lg bg-blue-900/50 blur-sm" />
    </div>
  );
}
