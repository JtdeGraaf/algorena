import { useTranslation } from 'react-i18next';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { useMatchMoves, useAbortMatch } from './useMatches';
import { Loader2, Swords, Clock, CheckCircle, XCircle, PlayCircle, StopCircle } from 'lucide-react';
import type { MatchDto } from '@/api/generated';
import { cn } from '@/lib/utils';

interface MatchDetailsDialogProps {
  match: MatchDto | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function MatchDetailsDialog({ match, open, onOpenChange }: MatchDetailsDialogProps) {
  const { t } = useTranslation();
  const { data: moves, isLoading: movesLoading } = useMatchMoves(match?.id || '');
  const abortMatch = useAbortMatch();

  if (!match) return null;

  const getStatusIcon = () => {
    switch (match.status) {
      case 'CREATED':
        return <Clock className="h-5 w-5 text-yellow-500" />;
      case 'IN_PROGRESS':
        return <PlayCircle className="h-5 w-5 text-blue-500" />;
      case 'FINISHED':
        return <CheckCircle className="h-5 w-5 text-emerald-500" />;
      case 'ABORTED':
        return <XCircle className="h-5 w-5 text-red-500" />;
      default:
        return null;
    }
  };

  const getStatusColor = () => {
    switch (match.status) {
      case 'CREATED':
        return 'bg-yellow-500/10 text-yellow-500';
      case 'IN_PROGRESS':
        return 'bg-blue-500/10 text-blue-500';
      case 'FINISHED':
        return 'bg-emerald-500/10 text-emerald-500';
      case 'ABORTED':
        return 'bg-red-500/10 text-red-500';
      default:
        return 'bg-zinc-500/10 text-zinc-500';
    }
  };

  const formatDate = (dateString?: string) => {
    if (!dateString) return '—';
    return new Date(dateString).toLocaleString();
  };

  const participants = match.participants || [];
  const player1 = participants.find(p => p.playerIndex === 0);
  const player2 = participants.find(p => p.playerIndex === 1);

  const canAbort = match.status === 'CREATED' || match.status === 'IN_PROGRESS';

  const handleAbort = async () => {
    if (!match.id) return;
    try {
      await abortMatch.mutateAsync(match.id);
      onOpenChange(false);
    } catch (error) {
      console.error('Failed to abort match:', error);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <Swords className="h-5 w-5 text-emerald-500" />
            Match Details
          </DialogTitle>
          <DialogDescription>
            {player1?.botName} vs {player2?.botName}
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-6 py-4">
          {/* Status and info */}
          <div className="flex items-center justify-between">
            <div className={cn('flex items-center gap-2 rounded-full px-3 py-1 text-sm font-medium', getStatusColor())}>
              {getStatusIcon()}
              {t(`matches.status.${match.status?.toLowerCase() || 'created'}`)}
            </div>
            <div className="text-sm text-zinc-400">
              <span className="font-mono text-xs uppercase">{match.game}</span>
            </div>
          </div>

          {/* Participants */}
          <div className="grid grid-cols-2 gap-4">
            <div className="rounded-lg border border-zinc-800 bg-zinc-900/50 p-4">
              <div className="text-sm text-zinc-500">Player 1 (White)</div>
              <div className="mt-1 font-medium">{player1?.botName || 'Unknown'}</div>
              {match.status === 'FINISHED' && (
                <div className="mt-2 text-2xl font-bold text-emerald-500">{player1?.score ?? 0}</div>
              )}
            </div>
            <div className="rounded-lg border border-zinc-800 bg-zinc-900/50 p-4">
              <div className="text-sm text-zinc-500">Player 2 (Black)</div>
              <div className="mt-1 font-medium">{player2?.botName || 'Unknown'}</div>
              {match.status === 'FINISHED' && (
                <div className="mt-2 text-2xl font-bold text-emerald-500">{player2?.score ?? 0}</div>
              )}
            </div>
          </div>

          {/* Timestamps */}
          <div className="grid grid-cols-2 gap-4 text-sm">
            <div>
              <span className="text-zinc-500">Started:</span>
              <span className="ml-2">{formatDate(match.startedAt)}</span>
            </div>
            {match.finishedAt && (
              <div>
                <span className="text-zinc-500">Finished:</span>
                <span className="ml-2">{formatDate(match.finishedAt)}</span>
              </div>
            )}
          </div>

          {/* Chess state */}
          {match.state && match.game === 'CHESS' && (
            <div className="space-y-2">
              <h4 className="font-medium">Game State</h4>
              <div className="rounded-lg bg-zinc-900 p-3">
                <div className="text-sm text-zinc-400">
                  <span className="font-medium">FEN:</span>
                  <code className="ml-2 break-all text-xs text-zinc-300">{match.state.fen}</code>
                </div>
                {match.state.pgn && (
                  <div className="mt-2 text-sm text-zinc-400">
                    <span className="font-medium">PGN:</span>
                    <pre className="mt-1 overflow-x-auto whitespace-pre-wrap text-xs text-zinc-300">{match.state.pgn}</pre>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* Moves list */}
          <div className="space-y-2">
            <h4 className="font-medium">{t('matches.moves')} ({moves?.length || 0})</h4>
            {movesLoading ? (
              <div className="flex justify-center py-4">
                <Loader2 className="h-5 w-5 animate-spin text-zinc-500" />
              </div>
            ) : moves && moves.length > 0 ? (
              <div className="max-h-48 overflow-y-auto rounded-lg bg-zinc-900 p-3">
                <div className="grid grid-cols-2 gap-1 text-sm">
                  {moves.map((move, index) => (
                    <div
                      key={move.id || index}
                      className={cn(
                        'flex items-center gap-2 rounded px-2 py-1',
                        move.playerIndex === 0 ? 'bg-zinc-800/50' : ''
                      )}
                    >
                      <span className="text-zinc-500">{Math.floor(index / 2) + 1}.</span>
                      <span className="font-mono">{move.moveNotation}</span>
                    </div>
                  ))}
                </div>
              </div>
            ) : (
              <p className="text-sm text-zinc-500">No moves yet</p>
            )}
          </div>

          {/* Actions */}
          {canAbort && (
            <div className="flex justify-end border-t border-zinc-800 pt-4">
              <Button
                variant="destructive"
                onClick={handleAbort}
                disabled={abortMatch.isPending}
                className="gap-2"
              >
                {abortMatch.isPending ? (
                  <Loader2 className="h-4 w-4 animate-spin" />
                ) : (
                  <StopCircle className="h-4 w-4" />
                )}
                Abort Match
              </Button>
            </div>
          )}
        </div>
      </DialogContent>
    </Dialog>
  );
}

