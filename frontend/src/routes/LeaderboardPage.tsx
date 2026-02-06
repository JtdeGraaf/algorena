import { useState } from 'react';
import { Trophy, Users, Bot, ChevronLeft, ChevronRight, TrendingUp, TrendingDown, AlertCircle } from 'lucide-react';
import { useBotLeaderboard, useUserLeaderboard } from '@/features/leaderboard/useLeaderboard';
import { TerminalTable, TerminalTableRow, TerminalTableCell } from '@/components/ui/terminal-table';
import { cn } from '@/lib/utils';
import type { BotDto, BotLeaderboardEntryDTO, UserLeaderboardEntryDTO } from '@/api/generated';

type Game = NonNullable<BotDto['game']>;
type LeaderboardTab = 'bots' | 'users';

export function LeaderboardPage() {
  const [activeTab, setActiveTab] = useState<LeaderboardTab>('bots');
  const [game, setGame] = useState<Game>('CHESS');
  const [page, setPage] = useState(0);
  const pageSize = 50;

  const {
    data: botLeaderboard,
    isLoading: isLoadingBots,
    error: botError
  } = useBotLeaderboard(game, page, pageSize);

  const {
    data: userLeaderboard,
    isLoading: isLoadingUsers,
    error: userError
  } = useUserLeaderboard(game, page, pageSize);

  const isLoading = activeTab === 'bots' ? isLoadingBots : isLoadingUsers;
  const error = activeTab === 'bots' ? botError : userError;
  const currentData = activeTab === 'bots' ? botLeaderboard : userLeaderboard;
  const totalPages = currentData?.totalPages || 0;
  const totalElements = currentData?.totalElements || 0;

  const handleTabChange = (tab: LeaderboardTab) => {
    setActiveTab(tab);
    setPage(0);
  };

  const handleGameChange = (newGame: Game) => {
    setGame(newGame);
    setPage(0);
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="font-mono text-2xl font-bold text-primary">
          $ ./leaderboard --show
        </h1>
        <p className="mt-1 font-mono text-sm text-text-muted">
          # Top competitors ranked by ELO rating
        </p>
      </div>

      {/* Controls Bar */}
      <div className="flex flex-wrap items-center gap-4">
        {/* Tab Selector */}
        <div className="flex gap-2 rounded-lg border border-border bg-surface/30 p-1">
          <button
            onClick={() => handleTabChange('bots')}
            className={cn(
              'flex items-center gap-2 rounded px-4 py-2 font-mono text-sm font-medium transition-all',
              activeTab === 'bots'
                ? 'bg-primary/10 text-primary shadow-sm'
                : 'text-text-muted hover:bg-surface/50 hover:text-text-primary'
            )}
          >
            <Bot className="h-4 w-4" />
            <span>top_bots</span>
          </button>
          <button
            onClick={() => handleTabChange('users')}
            className={cn(
              'flex items-center gap-2 rounded px-4 py-2 font-mono text-sm font-medium transition-all',
              activeTab === 'users'
                ? 'bg-primary/10 text-primary shadow-sm'
                : 'text-text-muted hover:bg-surface/50 hover:text-text-primary'
            )}
          >
            <Users className="h-4 w-4" />
            <span>top_users</span>
          </button>
        </div>

        {/* Game Filter */}
        <div className="flex items-center gap-2 font-mono text-sm">
          <span className="text-text-secondary">--game:</span>
          <select
            value={game}
            onChange={(e) => handleGameChange(e.target.value as Game)}
            className="rounded border border-border bg-surface/50 px-3 py-2 text-text-primary transition-colors hover:border-primary/50 focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary"
          >
            <option value="CHESS">chess</option>
            <option value="CONNECT_FOUR">connect4</option>
          </select>
        </div>

        {/* Stats Summary */}
        <div className="ml-auto flex items-center gap-2 font-mono text-xs text-text-muted">
          <span>{totalElements} entries</span>
          <span className="text-surface-muted">|</span>
          <span>page {page + 1}/{Math.max(1, totalPages)}</span>
        </div>
      </div>

      {/* Leaderboard Table */}
      {isLoading ? (
        <div className="flex items-center justify-center rounded-lg border border-border bg-surface/30 py-20">
          <div className="text-center">
            <div className="inline-block h-8 w-8 animate-spin rounded-full border-2 border-primary border-t-transparent" />
            <p className="mt-4 font-mono text-sm text-text-muted">loading rankings...</p>
          </div>
        </div>
      ) : error ? (
        <div className="flex flex-col items-center justify-center rounded-lg border border-error/20 bg-error/5 py-16 text-center">
          <AlertCircle className="h-12 w-12 text-error" />
          <h3 className="mt-4 font-mono text-lg font-semibold text-error">Failed to load leaderboard</h3>
          <p className="mt-1 font-mono text-sm text-text-muted">
            {error instanceof Error ? error.message : 'An unexpected error occurred'}
          </p>
        </div>
      ) : activeTab === 'bots' ? (
        <BotLeaderboardTable data={botLeaderboard?.content || []} />
      ) : (
        <UserLeaderboardTable data={userLeaderboard?.content || []} />
      )}

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex items-center justify-between rounded-lg border border-border bg-surface/30 px-4 py-3">
          <button
            onClick={() => setPage((p) => Math.max(0, p - 1))}
            disabled={page === 0}
            className={cn(
              'flex items-center gap-2 rounded px-3 py-1.5 font-mono text-sm transition-all',
              page === 0
                ? 'cursor-not-allowed text-surface-muted'
                : 'text-text-primary hover:bg-surface/50 hover:text-primary'
            )}
          >
            <ChevronLeft className="h-4 w-4" />
            <span>prev</span>
          </button>

          <div className="flex items-center gap-2 font-mono text-sm">
            {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
              let pageNum;
              if (totalPages <= 5) {
                pageNum = i;
              } else if (page < 3) {
                pageNum = i;
              } else if (page > totalPages - 4) {
                pageNum = totalPages - 5 + i;
              } else {
                pageNum = page - 2 + i;
              }

              return (
                <button
                  key={pageNum}
                  onClick={() => setPage(pageNum)}
                  className={cn(
                    'h-8 w-8 rounded transition-all',
                    page === pageNum
                      ? 'bg-primary text-surface font-semibold'
                      : 'text-text-muted hover:bg-surface/50 hover:text-text-primary'
                  )}
                >
                  {pageNum + 1}
                </button>
              );
            })}
          </div>

          <button
            onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
            disabled={page >= totalPages - 1}
            className={cn(
              'flex items-center gap-2 rounded px-3 py-1.5 font-mono text-sm transition-all',
              page >= totalPages - 1
                ? 'cursor-not-allowed text-surface-muted'
                : 'text-text-primary hover:bg-surface/50 hover:text-primary'
            )}
          >
            <span>next</span>
            <ChevronRight className="h-4 w-4" />
          </button>
        </div>
      )}
    </div>
  );
}

// Bot Leaderboard Table Component
function BotLeaderboardTable({ data }: { data: BotLeaderboardEntryDTO[] }) {
  if (data.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center rounded-lg border border-dashed border-border py-16 text-center">
        <Bot className="h-12 w-12 text-surface-muted" />
        <p className="mt-4 font-mono text-sm text-text-muted">no rankings yet</p>
        <p className="mt-1 font-mono text-xs text-text-secondary">compete in matches to appear here</p>
      </div>
    );
  }

  return (
    <TerminalTable
      title="bot_rankings.dat"
      headers={['rank', 'bot', 'owner', 'elo', 'matches', 'record', 'win%']}
    >
      {data.map((entry) => (
        <TerminalTableRow key={entry.botId} className="group hover:bg-primary/5">
          <TerminalTableCell>
            <div className="flex items-center gap-2">
              {entry.rank === 1 && <Trophy className="h-4 w-4 text-yellow-500" />}
              {entry.rank === 2 && <Trophy className="h-4 w-4 text-gray-400" />}
              {entry.rank === 3 && <Trophy className="h-4 w-4 text-amber-600" />}
              <span className={cn(
                'font-semibold',
                entry.rank <= 3 ? 'text-primary' : 'text-text-muted'
              )}>
                #{entry.rank}
              </span>
            </div>
          </TerminalTableCell>
          <TerminalTableCell>
            <div className="flex items-center gap-2">
              <Bot className="h-3.5 w-3.5 text-primary/70" />
              <span className="font-medium text-text-primary">{entry.botName}</span>
            </div>
          </TerminalTableCell>
          <TerminalTableCell>
            <span className="text-text-muted">{entry.ownerName}</span>
          </TerminalTableCell>
          <TerminalTableCell>
            <span className="font-mono font-semibold text-primary">
              {entry.eloRating}
            </span>
          </TerminalTableCell>
          <TerminalTableCell>
            <span className="text-text-secondary">{entry.matchesPlayed}</span>
          </TerminalTableCell>
          <TerminalTableCell>
            <div className="flex items-center gap-2 text-xs">
              <span className="text-primary">{entry.wins}W</span>
              <span className="text-error">{entry.losses}L</span>
              <span className="text-text-muted">{entry.draws}D</span>
            </div>
          </TerminalTableCell>
          <TerminalTableCell>
            <div className="flex items-center gap-1.5">
              <span className={cn(
                'font-medium',
                entry.winRate >= 0.6 ? 'text-primary' :
                entry.winRate >= 0.4 ? 'text-text-primary' :
                'text-text-muted'
              )}>
                {(entry.winRate * 100).toFixed(1)}%
              </span>
              {entry.winRate >= 0.6 && <TrendingUp className="h-3 w-3 text-primary" />}
              {entry.winRate < 0.4 && <TrendingDown className="h-3 w-3 text-error" />}
            </div>
          </TerminalTableCell>
        </TerminalTableRow>
      ))}
    </TerminalTable>
  );
}

// User Leaderboard Table Component
function UserLeaderboardTable({ data }: { data: UserLeaderboardEntryDTO[] }) {
  if (data.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center rounded-lg border border-dashed border-border py-16 text-center">
        <Users className="h-12 w-12 text-surface-muted" />
        <p className="mt-4 font-mono text-sm text-text-muted">no rankings yet</p>
        <p className="mt-1 font-mono text-xs text-text-secondary">compete in matches to appear here</p>
      </div>
    );
  }

  return (
    <TerminalTable
      title="user_rankings.dat"
      headers={['rank', 'user', 'best_elo', 'avg_elo', 'bots', 'matches', 'win%']}
    >
      {data.map((entry) => (
        <TerminalTableRow key={entry.userId} className="group hover:bg-primary/5">
          <TerminalTableCell>
            <div className="flex items-center gap-2">
              {entry.rank === 1 && <Trophy className="h-4 w-4 text-yellow-500" />}
              {entry.rank === 2 && <Trophy className="h-4 w-4 text-gray-400" />}
              {entry.rank === 3 && <Trophy className="h-4 w-4 text-amber-600" />}
              <span className={cn(
                'font-semibold',
                entry.rank <= 3 ? 'text-primary' : 'text-text-muted'
              )}>
                #{entry.rank}
              </span>
            </div>
          </TerminalTableCell>
          <TerminalTableCell>
            <div className="flex items-center gap-2">
              {entry.avatarUrl ? (
                <img
                  src={entry.avatarUrl}
                  alt={entry.username}
                  className="h-5 w-5 rounded-full border border-border"
                />
              ) : (
                <Users className="h-3.5 w-3.5 text-primary/70" />
              )}
              <span className="font-medium text-text-primary">{entry.username}</span>
            </div>
          </TerminalTableCell>
          <TerminalTableCell>
            <span className="font-mono font-semibold text-primary">
              {entry.bestBotElo}
            </span>
          </TerminalTableCell>
          <TerminalTableCell>
            <span className="font-mono text-text-secondary">
              {entry.avgBotElo}
            </span>
          </TerminalTableCell>
          <TerminalTableCell>
            <span className="text-text-muted">{entry.totalBots}</span>
          </TerminalTableCell>
          <TerminalTableCell>
            <span className="text-text-secondary">{entry.totalMatches}</span>
          </TerminalTableCell>
          <TerminalTableCell>
            <div className="flex items-center gap-1.5">
              <span className={cn(
                'font-medium',
                entry.winRate >= 0.6 ? 'text-primary' :
                entry.winRate >= 0.4 ? 'text-text-primary' :
                'text-text-muted'
              )}>
                {(entry.winRate * 100).toFixed(1)}%
              </span>
              {entry.winRate >= 0.6 && <TrendingUp className="h-3 w-3 text-primary" />}
              {entry.winRate < 0.4 && <TrendingDown className="h-3 w-3 text-error" />}
            </div>
          </TerminalTableCell>
        </TerminalTableRow>
      ))}
    </TerminalTable>
  );
}
