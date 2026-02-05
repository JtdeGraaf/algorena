import { Connect4Board } from '@/components/Connect4Board';
import { cn } from '@/lib/utils';
import type { GameReplayProps } from '../types';
import type { Connect4Position } from './connect4ReplayEngine';

/**
 * Connect4-specific match replay component.
 * Displays the Connect4 board at the current position.
 */
export function Connect4MatchReplay({
  match,
  moves,
  positions,
  currentMoveIndex,
  onMoveSelect
}: GameReplayProps<Connect4Position>) {
  const participants = match.participants || [];
  const player1 = participants.find(p => p.playerIndex === 0);
  const player2 = participants.find(p => p.playerIndex === 1);

  const currentPosition = positions[currentMoveIndex + 1] || positions[0];
  const currentMove = currentMoveIndex >= 0 ? moves[currentMoveIndex] : null;

  return (
    <div className="flex gap-6 py-4">
      {/* Connect4 Board */}
      <div className="flex flex-col items-center gap-4 flex-1">
        <Connect4Board
          board={currentPosition.state}
          size="xl"
        />
      </div>

      {/* Move list and info */}
      <div className="w-64 space-y-3">
        <h4 className="font-medium">Move History</h4>

        {/* Current position info */}
        <div className="rounded-lg bg-zinc-900 p-3 text-sm">
          <div className="text-zinc-400">
            <span className="font-medium">Board State:</span>
            <code className="ml-2 block mt-1 break-all text-xs text-zinc-300 font-mono">
              {currentPosition.state}
            </code>
          </div>
          {currentMove && (
            <div className="mt-2 text-zinc-300">
              <span className="font-medium text-zinc-400">Current move:</span>{' '}
              <span className="font-mono">
                {currentMove.playerIndex === 0 ? 'Red' : 'Yellow'} → Column {currentMove.toSquare}
              </span>
            </div>
          )}
        </div>

        {/* Scrollable move list */}
        <div className="max-h-72 overflow-y-auto rounded-lg border border-zinc-800 bg-zinc-950 p-2">
          <div className="space-y-1 text-sm">
            {moves.map((move, index) => {
              const moveNum = index + 1;
              const isPlayer1 = move.playerIndex === 0;
              const isCurrentMove = index === currentMoveIndex;

              return (
                <button
                  key={move.id || index}
                  onClick={() => onMoveSelect(index)}
                  className={cn(
                    'flex w-full items-center gap-2 rounded px-2 py-1 text-left transition-colors',
                    isCurrentMove
                      ? 'bg-emerald-500/20 text-emerald-400'
                      : 'hover:bg-zinc-800'
                  )}
                >
                  <span className="w-8 text-zinc-500">{moveNum}.</span>
                  <span className={cn(
                    'font-medium',
                    isPlayer1 ? 'text-emerald-400' : 'text-red-400'
                  )}>
                    {isPlayer1 ? 'Red' : 'Yellow'}
                  </span>
                  <span className="font-mono">→ Col {move.toSquare}</span>
                </button>
              );
            })}
          </div>
        </div>

        {/* Match result */}
        {match.status === 'FINISHED' && (
          <div className="rounded-lg border border-zinc-800 bg-zinc-900/50 p-3">
            <div className="text-sm text-zinc-400">Result</div>
            <div className="mt-1 flex items-center gap-4">
              <div className={cn(
                'font-medium',
                (player1?.score ?? 0) > (player2?.score ?? 0) && 'text-emerald-500'
              )}>
                {player1?.botName}: {player1?.score}
              </div>
              <span className="text-zinc-600">—</span>
              <div className={cn(
                'font-medium',
                (player2?.score ?? 0) > (player1?.score ?? 0) && 'text-emerald-500'
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
