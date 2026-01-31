import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Button } from '@/components/ui/button';
import { useAuth } from '@/features/auth/AuthContext';
import { useBots } from '@/features/bots/useBots';
import { BotCard } from '@/features/bots/BotCard';
import { CreateBotDialog } from '@/features/bots/CreateBotDialog';
import { EditBotDialog } from '@/features/bots/EditBotDialog';
import { DeleteBotDialog } from '@/features/bots/DeleteBotDialog';
import { Bot, Plus, Loader2 } from 'lucide-react';
import type { BotDto } from '@/api/generated';

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

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">{t('bots.title')}</h1>
          <p className="text-zinc-400">Create and manage your battle bots</p>
        </div>
        <Button onClick={() => setCreateDialogOpen(true)} className="gap-2">
          <Plus className="h-4 w-4" />
          {t('bots.createBot')}
        </Button>
      </div>

      {isLoading ? (
        <div className="flex justify-center py-16">
          <Loader2 className="h-8 w-8 animate-spin text-zinc-500" />
        </div>
      ) : error ? (
        <div className="flex flex-col items-center justify-center py-16 text-center">
          <p className="text-red-400">Failed to load bots</p>
          <p className="mt-1 text-sm text-zinc-500">{(error as Error).message}</p>
        </div>
      ) : bots.length === 0 ? (
        <div className="flex flex-col items-center justify-center rounded-lg border border-dashed border-zinc-800 py-16 text-center">
          <Bot className="h-12 w-12 text-zinc-600" />
          <h3 className="mt-4 text-lg font-semibold">{t('bots.noBots')}</h3>
          <p className="mt-1 text-sm text-zinc-500">
            Create your first bot to start competing in the arena.
          </p>
          <Button onClick={() => setCreateDialogOpen(true)} className="mt-6 gap-2">
            <Plus className="h-4 w-4" />
            {t('bots.createBot')}
          </Button>
        </div>
      ) : (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {bots.map((bot) => (
            <BotCard
              key={bot.id}
              bot={bot}
              onEdit={setEditBot}
              onDelete={setDeleteBot}
            />
          ))}
        </div>
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
