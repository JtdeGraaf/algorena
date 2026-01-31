import { useTranslation } from 'react-i18next';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Swords, Clock, CheckCircle, XCircle, PlayCircle, Eye } from 'lucide-react';
import type { MatchDto } from '@/api/generated';
import { cn } from '@/lib/utils';

interface MatchCardProps {
  match: MatchDto;
  onView: (match: MatchDto) => void;
}

export function MatchCard({ match, onView }: MatchCardProps) {
  const { t } = useTranslation();

  const getStatusIcon = () => {
    switch (match.status) {
      case 'CREATED':
        return <Clock className="h-4 w-4 text-yellow-500" />;
      case 'IN_PROGRESS':
        return <PlayCircle className="h-4 w-4 text-blue-500" />;
      case 'FINISHED':
        return <CheckCircle className="h-4 w-4 text-emerald-500" />;
      case 'ABORTED':
        return <XCircle className="h-4 w-4 text-red-500" />;
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

  const getWinner = () => {
    if (match.status !== 'FINISHED') return null;
    if (!player1 || !player2) return null;
    if ((player1.score ?? 0) > (player2.score ?? 0)) return player1;
    if ((player2.score ?? 0) > (player1.score ?? 0)) return player2;
    return null; // Draw
  };

  const winner = getWinner();
  const isDraw = match.status === 'FINISHED' && !winner;

  return (
    <Card className="overflow-hidden transition-colors hover:border-zinc-700">
      <CardHeader className="pb-3">
        <div className="flex items-start justify-between">
          <div className="flex items-center gap-2">
            <Swords className="h-5 w-5 text-emerald-500" />
            <CardTitle className="text-base">
              {player1?.botName || 'Unknown'} vs {player2?.botName || 'Unknown'}
            </CardTitle>
          </div>
          <div className={cn('flex items-center gap-1.5 rounded-full px-2 py-0.5 text-xs font-medium', getStatusColor())}>
            {getStatusIcon()}
            {t(`matches.status.${match.status?.toLowerCase() || 'created'}`)}
          </div>
        </div>
      </CardHeader>
      <CardContent>
        <div className="space-y-3">
          {/* Score display for finished matches */}
          {match.status === 'FINISHED' && (
            <div className="flex items-center justify-center gap-4 rounded-lg bg-zinc-900/50 py-3">
              <div className={cn('text-center', winner?.botId === player1?.botId && 'text-emerald-500')}>
                <div className="text-2xl font-bold">{player1?.score ?? 0}</div>
                <div className="text-xs text-zinc-500">{player1?.botName}</div>
              </div>
              <div className="text-zinc-600">—</div>
              <div className={cn('text-center', winner?.botId === player2?.botId && 'text-emerald-500')}>
                <div className="text-2xl font-bold">{player2?.score ?? 0}</div>
                <div className="text-xs text-zinc-500">{player2?.botName}</div>
              </div>
            </div>
          )}

          {isDraw && (
            <div className="text-center text-sm text-zinc-400">Draw</div>
          )}

          {/* Match info */}
          <div className="flex items-center justify-between text-sm text-zinc-400">
            <span className="font-mono text-xs uppercase">{match.game}</span>
            <span>
              {match.status === 'FINISHED' ? 'Finished' : 'Started'} {formatDate(match.finishedAt || match.startedAt)}
            </span>
          </div>

          {/* View button */}
          <Button
            variant="outline"
            size="sm"
            className="w-full gap-2"
            onClick={() => onView(match)}
          >
            <Eye className="h-4 w-4" />
            View Details
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}

