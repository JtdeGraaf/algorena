import { useState, useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import { Button } from '@/components/ui/button';
import { Select } from '@/components/ui/select';
import { TerminalTable, TerminalTableRow, TerminalTableCell } from '@/components/ui/terminal-table';
import { useAuth } from '@/features/auth/AuthContext';
import { useMatches } from '@/features/matches/useMatches';
import { useBots } from '@/features/bots/useBots';
import { CreateMatchDialog } from '@/features/matches/CreateMatchDialog';
import { MatchDetailsDialog } from '@/features/matches/MatchDetailsDialog';
import { Swords, Plus, Loader2, Filter, Eye, Clock, PlayCircle, CheckCircle, XCircle } from 'lucide-react';
import type { MatchDto } from '@/api/generated';
import { cn } from '@/lib/utils';

type StatusFilter = 'all' | 'active' | 'finished';

export function MatchesPage() {
  const { t } = useTranslation();
  const { isAuthenticated, login } = useAuth();

  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [selectedMatch, setSelectedMatch] = useState<MatchDto | null>(null);
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('all');
  const [botFilter, setBotFilter] = useState<string>('all');

  const { data: matches, isLoading, error } = useMatches();
  const { data: botsPage } = useBots();

  const myBots = botsPage?.content || [];

  const filteredMatches = useMemo(() => {
    if (!matches) return [];

    return matches.filter(match => {
      // Status filter
      if (statusFilter === 'active') {
        if (match.status !== 'CREATED' && match.status !== 'IN_PROGRESS') return false;
      } else if (statusFilter === 'finished') {
        if (match.status !== 'FINISHED' && match.status !== 'ABORTED') return false;
      }

      // Bot filter
      if (botFilter !== 'all') {
        const botId = parseInt(botFilter);
        const hasBot = match.participants?.some(p => p.botId === botId);
        if (!hasBot) return false;
      }

      return true;
    });
  }, [matches, statusFilter, botFilter]);

  const getStatusIcon = (status?: string) => {
    switch (status) {
      case 'CREATED':
        return <Clock className="h-3.5 w-3.5 text-yellow-500" />;
      case 'IN_PROGRESS':
        return <PlayCircle className="h-3.5 w-3.5 text-blue-500" />;
      case 'FINISHED':
        return <CheckCircle className="h-3.5 w-3.5 text-emerald-500" />;
      case 'ABORTED':
        return <XCircle className="h-3.5 w-3.5 text-red-500" />;
      default:
        return null;
    }
  };

  const getStatusColor = (status?: string) => {
    switch (status) {
      case 'CREATED':
        return 'text-yellow-500';
      case 'IN_PROGRESS':
        return 'text-blue-500';
      case 'FINISHED':
        return 'text-emerald-500';
      case 'ABORTED':
        return 'text-red-500';
      default:
        return 'text-zinc-500';
    }
  };

  const formatDate = (dateString?: string) => {
    if (!dateString) return '—';
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return 'just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffHours < 24) return `${diffHours}h ago`;
    if (diffDays < 7) return `${diffDays}d ago`;

    return date.toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric'
    });
  };

  if (!isAuthenticated) {
    return (
      <div className="flex flex-col items-center justify-center py-16 text-center">
        <Swords className="h-16 w-16 text-zinc-600" />
        <h2 className="mt-4 text-xl font-semibold">{t('errors.unauthorized')}</h2>
        <p className="mt-2 text-zinc-400">Login to view and create matches</p>
        <Button onClick={login} className="mt-6">
          {t('nav.login')}
        </Button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="font-mono text-2xl font-bold text-emerald-500">$ ./matches --history</h1>
          <p className="mt-1 font-mono text-sm text-zinc-500"># Challenge bots and track results</p>
        </div>
        <Button onClick={() => setCreateDialogOpen(true)} className="gap-2 font-mono">
          <Plus className="h-4 w-4" />
          new match
        </Button>
      </div>

      {/* Filters */}
      <div className="flex flex-wrap items-center gap-4 font-mono text-sm">
        <div className="flex items-center gap-2">
          <Filter className="h-4 w-4 text-zinc-500" />
          <span className="text-zinc-400">--filter:</span>
        </div>
        <Select
          value={statusFilter}
          onChange={(e: React.ChangeEvent<HTMLSelectElement>) => setStatusFilter(e.target.value as StatusFilter)}
          className="w-40"
        >
          <option value="all">all</option>
          <option value="active">active</option>
          <option value="finished">finished</option>
        </Select>
        <Select
          value={botFilter}
          onChange={(e: React.ChangeEvent<HTMLSelectElement>) => setBotFilter(e.target.value)}
          className="w-48"
        >
          <option value="all">all bots</option>
          {myBots.map(bot => (
            <option key={bot.id} value={bot.id?.toString()}>
              {bot.name}
            </option>
          ))}
        </Select>
      </div>

      {isLoading ? (
        <div className="flex justify-center py-16">
          <Loader2 className="h-8 w-8 animate-spin text-zinc-500" />
        </div>
      ) : error ? (
        <div className="flex flex-col items-center justify-center py-16 text-center">
          <p className="font-mono text-red-400">ERROR: Failed to load matches</p>
          <p className="mt-1 font-mono text-sm text-zinc-500">{(error as Error).message}</p>
        </div>
      ) : filteredMatches.length === 0 ? (
        <div className="flex flex-col items-center justify-center rounded-lg border border-dashed border-zinc-800 py-16 text-center">
          <Swords className="h-12 w-12 text-zinc-600" />
          <h3 className="mt-4 font-mono text-lg font-semibold">No matches found</h3>
          <p className="mt-1 font-mono text-sm text-zinc-500">
            $ create a match to start competing
          </p>
          <Button onClick={() => setCreateDialogOpen(true)} className="mt-6 gap-2 font-mono">
            <Plus className="h-4 w-4" />
            new match
          </Button>
        </div>
      ) : (
        <TerminalTable
          title="matches.log"
          headers={['status', 'matchup', 'game', 'result', 'time', 'actions']}
        >
          {filteredMatches.map(match => {
            const participants = match.participants || [];
            const player1 = participants.find(p => p.playerIndex === 0);
            const player2 = participants.find(p => p.playerIndex === 1);

            const winner = match.status === 'FINISHED'
              ? participants.find(p => (p.score ?? 0) > 0.5)
              : null;

            const isDraw = match.status === 'FINISHED' && !winner;

            return (
              <TerminalTableRow key={match.id} onClick={() => setSelectedMatch(match)}>
                <TerminalTableCell>
                  <div className="flex items-center gap-2">
                    {getStatusIcon(match.status)}
                    <span className={cn('text-xs uppercase', getStatusColor(match.status))}>
                      {match.status === 'IN_PROGRESS' ? 'LIVE' : match.status}
                    </span>
                  </div>
                </TerminalTableCell>
                <TerminalTableCell className="font-semibold">
                  <div className="flex items-center gap-2">
                    <span className={cn(
                      winner?.botId === player1?.botId && 'text-emerald-500'
                    )}>
                      {player1?.botName || '???'}
                    </span>
                    <span className="text-zinc-600">vs</span>
                    <span className={cn(
                      winner?.botId === player2?.botId && 'text-emerald-500'
                    )}>
                      {player2?.botName || '???'}
                    </span>
                  </div>
                </TerminalTableCell>
                <TerminalTableCell>
                  <span className="rounded bg-zinc-800 px-2 py-0.5 text-xs text-zinc-400">
                    {match.game}
                  </span>
                </TerminalTableCell>
                <TerminalTableCell>
                  {match.status === 'FINISHED' ? (
                    isDraw ? (
                      <span className="text-zinc-400">DRAW</span>
                    ) : (
                      <div className="flex items-center gap-2 text-xs">
                        <span className="text-emerald-500">{player1?.score ?? 0}</span>
                        <span className="text-zinc-600">—</span>
                        <span className="text-emerald-500">{player2?.score ?? 0}</span>
                      </div>
                    )
                  ) : (
                    <span className="text-zinc-600">—</span>
                  )}
                </TerminalTableCell>
                <TerminalTableCell className="text-zinc-500">
                  {formatDate(match.finishedAt || match.startedAt)}
                </TerminalTableCell>
                <TerminalTableCell>
                  <Button
                    variant="ghost"
                    size="icon"
                    className="h-7 w-7"
                    onClick={(e) => {
                      e.stopPropagation();
                      setSelectedMatch(match);
                    }}
                    title="View details"
                  >
                    <Eye className="h-3.5 w-3.5" />
                  </Button>
                </TerminalTableCell>
              </TerminalTableRow>
            );
          })}
        </TerminalTable>
      )}

      <CreateMatchDialog
        open={createDialogOpen}
        onOpenChange={setCreateDialogOpen}
      />

      <MatchDetailsDialog
        match={selectedMatch}
        open={!!selectedMatch}
        onOpenChange={(open) => !open && setSelectedMatch(null)}
      />
    </div>
  );
}
