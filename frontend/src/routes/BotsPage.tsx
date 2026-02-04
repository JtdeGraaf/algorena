import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Button } from '@/components/ui/button';
import { TerminalTable, TerminalTableRow, TerminalTableCell } from '@/components/ui/terminal-table';
import { useAuth } from '@/features/auth/AuthContext';
import { useBots } from '@/features/bots/useBots';
import { useBotStats } from '@/features/bots/useBots';
import { CreateBotDialog } from '@/features/bots/CreateBotDialog';
import { EditBotDialog } from '@/features/bots/EditBotDialog';
import { DeleteBotDialog } from '@/features/bots/DeleteBotDialog';
import { Bot, Plus, Loader2, Edit2, Trash2, Circle } from 'lucide-react';
import type { BotDto } from '@/api/generated';
import { cn } from '@/lib/utils';

function BotStatsCell({ botId }: { botId: number }) {
  const { data: stats } = useBotStats(botId);

  if (!stats) {
    return <span className="text-zinc-600">—</span>;
  }

  const winRate = stats.winRate != null ? Math.round(stats.winRate * 100) : 0;

  return (
    <div className="flex items-center gap-3 text-xs">
      <span className="text-emerald-500">{stats.wins || 0}W</span>
      <span className="text-red-400">{stats.losses || 0}L</span>
      <span className="text-zinc-400">{stats.draws || 0}D</span>
      <span className={cn(
        'font-semibold',
        winRate >= 50 ? 'text-emerald-500' : 'text-zinc-500'
      )}>
        {winRate}%
      </span>
    </div>
  );
}

export function BotsPage() {
  const { t } = useTranslation();
  const { isAuthenticated, login } = useAuth();
  const { data: botsPage, isLoading, error } = useBots();

  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [editBot, setEditBot] = useState<BotDto | null>(null);
  const [deleteBot, setDeleteBot] = useState<BotDto | null>(null);

  if (!isAuthenticated) {
    return (
      <div className="flex flex-col items-center justify-center py-16 text-center">
        <Bot className="h-16 w-16 text-zinc-600" />
        <h2 className="mt-4 text-xl font-semibold">{t('errors.unauthorized')}</h2>
        <p className="mt-2 text-zinc-400">Login to manage your bots</p>
        <Button onClick={login} className="mt-6">
          {t('nav.login')}
        </Button>
      </div>
    );
  }

  const bots = botsPage?.content || [];

  const formatDate = (dateString?: string) => {
    if (!dateString) return '—';
    return new Date(dateString).toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: '2-digit'
    });
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="font-mono text-2xl font-bold text-emerald-500">$ ./bots --list</h1>
          <p className="mt-1 font-mono text-sm text-zinc-500"># Manage your battle bots</p>
        </div>
        <Button onClick={() => setCreateDialogOpen(true)} className="gap-2 font-mono">
          <Plus className="h-4 w-4" />
          new bot
        </Button>
      </div>

      {isLoading ? (
        <div className="flex justify-center py-16">
          <Loader2 className="h-8 w-8 animate-spin text-zinc-500" />
        </div>
      ) : error ? (
        <div className="flex flex-col items-center justify-center py-16 text-center">
          <p className="font-mono text-red-400">ERROR: Failed to load bots</p>
          <p className="mt-1 font-mono text-sm text-zinc-500">{(error as Error).message}</p>
        </div>
      ) : bots.length === 0 ? (
        <div className="flex flex-col items-center justify-center rounded-lg border border-dashed border-zinc-800 py-16 text-center">
          <Bot className="h-12 w-12 text-zinc-600" />
          <h3 className="mt-4 font-mono text-lg font-semibold">No bots found</h3>
          <p className="mt-1 font-mono text-sm text-zinc-500">
            $ create your first bot to start competing
          </p>
          <Button onClick={() => setCreateDialogOpen(true)} className="mt-6 gap-2 font-mono">
            <Plus className="h-4 w-4" />
            new bot
          </Button>
        </div>
      ) : (
        <TerminalTable
          title="bots.db"
          headers={['status', 'name', 'game', 'stats', 'created', 'actions']}
        >
          {bots.map((bot) => (
            <TerminalTableRow key={bot.id}>
              <TerminalTableCell>
                <div className="flex items-center gap-2">
                  <Circle
                    className={cn(
                      'h-2 w-2',
                      bot.active ? 'fill-emerald-500 text-emerald-500' : 'fill-zinc-600 text-zinc-600'
                    )}
                  />
                  <span className={cn(
                    'text-xs',
                    bot.active ? 'text-emerald-500' : 'text-zinc-600'
                  )}>
                    {bot.active ? 'ACTIVE' : 'IDLE'}
                  </span>
                </div>
              </TerminalTableCell>
              <TerminalTableCell className="font-semibold text-zinc-100">
                <div>
                  <div>{bot.name}</div>
                  {bot.description && (
                    <div className="mt-0.5 text-xs text-zinc-500">{bot.description}</div>
                  )}
                </div>
              </TerminalTableCell>
              <TerminalTableCell>
                <span className="rounded bg-zinc-800 px-2 py-0.5 text-xs text-zinc-400">
                  {bot.game}
                </span>
              </TerminalTableCell>
              <TerminalTableCell>
                <BotStatsCell botId={bot.id!} />
              </TerminalTableCell>
              <TerminalTableCell className="text-zinc-500">
                {formatDate(bot.created)}
              </TerminalTableCell>
              <TerminalTableCell>
                <div className="flex items-center gap-1">
                  <Button
                    variant="ghost"
                    size="icon"
                    className="h-7 w-7"
                    onClick={() => setEditBot(bot)}
                    title="Edit"
                  >
                    <Edit2 className="h-3.5 w-3.5" />
                  </Button>
                  <Button
                    variant="ghost"
                    size="icon"
                    className="h-7 w-7 text-red-400 hover:text-red-300"
                    onClick={() => setDeleteBot(bot)}
                    title="Delete"
                  >
                    <Trash2 className="h-3.5 w-3.5" />
                  </Button>
                </div>
              </TerminalTableCell>
            </TerminalTableRow>
          ))}
        </TerminalTable>
      )}

      <CreateBotDialog
        open={createDialogOpen}
        onOpenChange={setCreateDialogOpen}
      />

      <EditBotDialog
        bot={editBot}
        open={!!editBot}
        onOpenChange={(open) => !open && setEditBot(null)}
      />

      <DeleteBotDialog
        bot={deleteBot}
        open={!!deleteBot}
        onOpenChange={(open) => !open && setDeleteBot(null)}
      />
    </div>
  );
}
