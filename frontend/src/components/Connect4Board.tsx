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
      case '1': return 'bg-emerald-500 border-2 border-emerald-400 shadow-[0_0_10px_rgba(16,185,129,0.3)]';
      case '2': return 'bg-red-500 border-2 border-red-400 shadow-[0_0_10px_rgba(239,68,68,0.3)]';
      default: return 'bg-zinc-900 border-2 border-zinc-800';
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
    <div className={cn('relative', sizeClasses[size], className)}>
      {/* Terminal-style header */}
      <div className="mb-2 flex items-center gap-2 rounded-t border border-b-0 border-zinc-700 bg-zinc-900 px-3 py-1.5">
        <div className="flex gap-1.5">
          <div className="h-2 w-2 rounded-full bg-red-500/60" />
          <div className="h-2 w-2 rounded-full bg-yellow-500/60" />
          <div className="h-2 w-2 rounded-full bg-emerald-500/60" />
        </div>
        <div className="font-mono text-xs text-zinc-500">connect4.game</div>
      </div>

      {/* Game board */}
      <div className="rounded-b border border-zinc-700 bg-zinc-950 p-3 shadow-xl">
        {/* Column numbers */}
        <div className="mb-2 grid grid-cols-7 gap-1 px-1 font-mono text-xs text-zinc-600">
          {Array.from({ length: 7 }, (_, i) => (
            <div key={i} className="text-center">{i}</div>
          ))}
        </div>

        <div
          className="grid h-full w-full grid-cols-7 grid-rows-6 gap-1.5 rounded bg-zinc-900/50 p-2"
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
                {/* Slot */}
                <div className={cn(
                  "h-full w-full rounded-full transition-all duration-200",
                  getPieceColor(char),
                  // Glow effect for pieces
                  char !== '0' && "animate-pulse-slow",
                  // Highlight valid move on hover
                  interactive && isHovered && isLegal && "ring-2 ring-emerald-500/50",
                  interactive && isLegal ? "cursor-pointer hover:scale-105" : "",
                  !isLegal && interactive && isHovered && "ring-2 ring-red-500/50"
                )} />

                {/* Column indicator on hover */}
                {interactive && isHovered && r === ROWS - 1 && (
                  <div className={cn(
                    "absolute -top-6 text-xs font-mono",
                    isLegal ? "text-emerald-500" : "text-red-500"
                  )}>
                    ▼
                  </div>
                )}
              </div>
            );
          })}
        </div>

        {/* Status indicator */}
        <div className="mt-2 flex items-center justify-between px-1 font-mono text-xs">
          <div className="flex items-center gap-2">
            <div className="flex items-center gap-1">
              <div className="h-2 w-2 rounded-full bg-emerald-500" />
              <span className="text-zinc-500">P1</span>
            </div>
            <div className="flex items-center gap-1">
              <div className="h-2 w-2 rounded-full bg-red-500" />
              <span className="text-zinc-500">P2</span>
            </div>
          </div>
          {interactive && (
            <span className="text-zinc-600">
              {legalMoves && legalMoves.length > 0 ? 'Your turn' : 'Waiting...'}
            </span>
          )}
        </div>
      </div>
    </div>
  );
}
