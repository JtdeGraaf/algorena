import type { MatchDto, MatchMoveDto } from '@/api/generated';

interface MatchInfoPanelProps {
  match: MatchDto;
  moves: MatchMoveDto[] | undefined;
}

/**
 * Displays match information including participants, scores, and timestamps.
 */
export function MatchInfoPanel({ match, moves }: MatchInfoPanelProps) {
  const participants = match.participants || [];
  const player1 = participants.find(p => p.playerIndex === 0);
  const player2 = participants.find(p => p.playerIndex === 1);

  const formatDate = (dateString?: string) => {
    if (!dateString) return '—';
    return new Date(dateString).toLocaleString();
  };

  const getPlayerLabel = (playerIndex: number) => {
    if (match.game === 'CONNECT_FOUR') {
      return playerIndex === 0 ? 'Player 1 (Red)' : 'Player 2 (Yellow)';
    }
    return playerIndex === 0 ? 'Player 1 (White)' : 'Player 2 (Black)';
  };

  return (
    <div className="w-72 space-y-4">
      {/* Participants */}
      <div className="grid grid-cols-2 gap-3">
        <div className="rounded-lg border border-border bg-surface/50 p-3">
          <div className="text-xs text-text-muted">{getPlayerLabel(0)}</div>
          <div className="mt-1 font-medium">{player1?.botName || 'Unknown'}</div>
          {match.status === 'FINISHED' && (
            <div className="mt-1 text-xl font-bold text-primary">{player1?.score ?? 0}</div>
          )}
        </div>
        <div className="rounded-lg border border-border bg-surface/50 p-3">
          <div className="text-xs text-text-muted">{getPlayerLabel(1)}</div>
          <div className="mt-1 font-medium">{player2?.botName || 'Unknown'}</div>
          {match.status === 'FINISHED' && (
            <div className="mt-1 text-xl font-bold text-primary">{player2?.score ?? 0}</div>
          )}
        </div>
      </div>

      {/* Timestamps */}
      <div className="text-sm">
        <div className="flex justify-between">
          <span className="text-text-muted">Started:</span>
          <span>{formatDate(match.startedAt)}</span>
        </div>
        {match.finishedAt && (
          <div className="flex justify-between mt-1">
            <span className="text-text-muted">Finished:</span>
            <span>{formatDate(match.finishedAt)}</span>
          </div>
        )}
      </div>

      {/* Move count */}
      <div className="text-sm">
        <div className="flex justify-between">
          <span className="text-text-muted">Total Moves:</span>
          <span className="font-medium">{moves?.length || 0}</span>
        </div>
      </div>
    </div>
  );
}
