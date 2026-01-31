import { useState, useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import { Button } from '@/components/ui/button';
import { Select } from '@/components/ui/select';
import { useAuth } from '@/features/auth/AuthContext';
import { useMatches } from '@/features/matches/useMatches';
import { useBots } from '@/features/bots/useBots';
import { MatchCard } from '@/features/matches/MatchCard';
import { CreateMatchDialog } from '@/features/matches/CreateMatchDialog';
import { MatchDetailsDialog } from '@/features/matches/MatchDetailsDialog';
import { Swords, Plus, Loader2, Filter } from 'lucide-react';
import type { MatchDto } from '@/api/generated';

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

  // Group matches by status
  const activeMatches = filteredMatches.filter(m => m.status === 'CREATED' || m.status === 'IN_PROGRESS');
  const finishedMatches = filteredMatches.filter(m => m.status === 'FINISHED' || m.status === 'ABORTED');

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
          <h1 className="text-2xl font-bold">{t('matches.title')}</h1>
          <p className="text-zinc-400">Challenge other bots and track your matches</p>
        </div>
        <Button onClick={() => setCreateDialogOpen(true)} className="gap-2">
          <Plus className="h-4 w-4" />
          {t('matches.createMatch')}
        </Button>
      </div>

      {/* Filters */}
      <div className="flex flex-wrap items-center gap-4">
        <div className="flex items-center gap-2">
          <Filter className="h-4 w-4 text-zinc-500" />
          <span className="text-sm text-zinc-400">Filters:</span>
        </div>
        <Select
          value={statusFilter}
          onChange={(e: React.ChangeEvent<HTMLSelectElement>) => setStatusFilter(e.target.value as StatusFilter)}
        >
          <option value="all">All Statuses</option>
          <option value="active">Active</option>
          <option value="finished">Finished</option>
        </Select>
        <Select
          value={botFilter}
          onChange={(e: React.ChangeEvent<HTMLSelectElement>) => setBotFilter(e.target.value)}
        >
          <option value="all">All Bots</option>
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
          <p className="text-red-400">Failed to load matches</p>
          <p className="mt-1 text-sm text-zinc-500">{(error as Error).message}</p>
        </div>
      ) : filteredMatches.length === 0 ? (
        <div className="flex flex-col items-center justify-center rounded-lg border border-dashed border-zinc-800 py-16 text-center">
          <Swords className="h-12 w-12 text-zinc-600" />
          <h3 className="mt-4 text-lg font-semibold">{t('matches.noMatches')}</h3>
          <p className="mt-1 text-sm text-zinc-500">
            Create a match to start competing!
          </p>
          <Button onClick={() => setCreateDialogOpen(true)} className="mt-6 gap-2">
            <Plus className="h-4 w-4" />
            {t('matches.createMatch')}
          </Button>
        </div>
      ) : (
        <div className="space-y-8">
          {/* Active Matches */}
          {activeMatches.length > 0 && (
            <div className="space-y-4">
              <h2 className="flex items-center gap-2 text-lg font-semibold">
                <div className="h-2 w-2 rounded-full bg-blue-500" />
                Active Matches ({activeMatches.length})
              </h2>
              <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                {activeMatches.map(match => (
                  <MatchCard
                    key={match.id}
                    match={match}
                    onView={setSelectedMatch}
                  />
                ))}
              </div>
            </div>
          )}

          {/* Finished Matches */}
          {finishedMatches.length > 0 && (
            <div className="space-y-4">
              <h2 className="flex items-center gap-2 text-lg font-semibold">
                <div className="h-2 w-2 rounded-full bg-zinc-500" />
                Completed Matches ({finishedMatches.length})
              </h2>
              <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                {finishedMatches.map(match => (
                  <MatchCard
                    key={match.id}
                    match={match}
                    onView={setSelectedMatch}
                  />
                ))}
              </div>
            </div>
          )}
        </div>
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
