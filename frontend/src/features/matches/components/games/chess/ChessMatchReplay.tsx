import { Chessboard } from '@/components/Chessboard';
import { cn } from '@/lib/utils';
import type { GameReplayProps } from '../types';
import type { ChessPosition } from './chessReplayEngine';

/**
 * Chess-specific match replay component.
 * Displays the chess board at the current position with move highlighting.
 */
export function ChessMatchReplay({
  match,
  moves,
  positions,
  currentMoveIndex,
  onMoveSelect
}: GameReplayProps<ChessPosition>) {
  const participants = match.participants || [];
  const player1 = participants.find(p => p.playerIndex === 0);
  const player2 = participants.find(p => p.playerIndex === 1);

  const currentPosition = positions[currentMoveIndex + 1] || positions[0];
  const currentMove = currentMoveIndex >= 0 ? moves[currentMoveIndex] : null;

  return (
    <div className="flex gap-6 py-4">
      {/* Chessboard */}
      <div className="flex flex-col items-center gap-4 flex-1">
        <Chessboard
          fen={currentPosition.state}
          size="xl"
          highlightSquares={
            currentMove
              ? { from: currentMove.fromSquare?.toLowerCase(), to: currentMove.toSquare?.toLowerCase() }
              : undefined
          }
        />
      </div>

      {/* Move list and info */}
      <div className="w-64 space-y-3">
        <h4 className="font-medium">Move History</h4>

        {/* Current position info */}
        <div className="rounded-lg bg-surface p-3 text-sm">
          <div className="text-text-secondary">
            <span className="font-medium">FEN:</span>
            <code className="ml-2 break-all text-xs text-text-primary">{currentPosition.state}</code>
          </div>
          {currentMove && (
            <div className="mt-2 text-text-primary">
              <span className="font-medium text-text-secondary">Current move:</span>{' '}
              <span className="font-mono">{currentMove.moveNotation}</span>
              <span className="ml-2 text-text-muted">
                ({currentMove.fromSquare} → {currentMove.toSquare})
              </span>
            </div>
          )}
        </div>

        {/* Scrollable move list */}
        <div className="max-h-72 overflow-y-auto rounded-lg border border-border bg-background p-2">
          <div className="grid grid-cols-2 gap-1 text-sm">
            {moves.map((move, index) => {
              const moveNum = Math.floor(index / 2) + 1;
              const isWhite = move.playerIndex === 0;
              const isCurrentMove = index === currentMoveIndex;

              return (
                <button
                  key={move.id || index}
                  onClick={() => onMoveSelect(index)}
                  className={cn(
                    'flex items-center gap-2 rounded px-2 py-1 text-left transition-colors',
                    isCurrentMove
                      ? 'bg-primary/20 text-primary-active'
                      : 'hover:bg-surface-elevated'
                  )}
                >
                  <span className="w-6 text-text-muted">{isWhite ? `${moveNum}.` : ''}</span>
                  <span className="font-mono">{move.moveNotation}</span>
                </button>
              );
            })}
          </div>
        </div>

        {/* Match result */}
        {match.status === 'FINISHED' && (
          <div className="rounded-lg border border-border bg-surface/50 p-3">
            <div className="text-sm text-text-secondary">Result</div>
            <div className="mt-1 flex items-center gap-4">
              <div className={cn(
                'font-medium',
                (player1?.score ?? 0) > (player2?.score ?? 0) && 'text-primary'
              )}>
                {player1?.botName}: {player1?.score}
              </div>
              <span className="text-surface-muted">—</span>
              <div className={cn(
                'font-medium',
                (player2?.score ?? 0) > (player1?.score ?? 0) && 'text-primary'
              )}>
                {player2?.botName}: {player2?.score}
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
