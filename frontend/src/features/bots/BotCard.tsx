import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { useBotStats } from './useBots';
import { Edit2, Trash2, Key, Trophy, Target, Minus, TrendingUp } from 'lucide-react';
import type { BotDto } from '@/api/generated';
import { ApiKeysDialog } from './ApiKeysDialog';

interface BotCardProps {
  bot: BotDto;
  onEdit: (bot: BotDto) => void;
  onDelete: (bot: BotDto) => void;
}

export function BotCard({ bot, onEdit, onDelete }: BotCardProps) {
  const { t } = useTranslation();
  const [apiKeysOpen, setApiKeysOpen] = useState(false);
  const { data: stats } = useBotStats(bot.id!);

  const formatDate = (dateString?: string) => {
    if (!dateString) return '—';
    return new Date(dateString).toLocaleDateString();
  };

  return (
    <>
      <Card className="relative overflow-hidden">
        {/* Active status indicator */}
        <div
          className={`absolute left-0 top-0 h-full w-1 ${
            bot.active ? 'bg-emerald-500' : 'bg-zinc-600'
          }`}
        />

        <CardHeader className="pb-3">
          <div className="flex items-start justify-between">
            <div className="space-y-1">
              <CardTitle className="flex items-center gap-2 text-lg">
                {bot.name}
                <span
                  className={`inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium ${
                    bot.active
                      ? 'bg-emerald-500/10 text-emerald-500'
                      : 'bg-zinc-700 text-zinc-400'
                  }`}
                >
                  {bot.active ? t('bots.active') : t('bots.inactive')}
                </span>
              </CardTitle>
              <CardDescription className="line-clamp-2">
                {bot.description || 'No description'}
              </CardDescription>
            </div>
            <div className="flex gap-1">
              <Button
                variant="ghost"
                size="icon"
                onClick={() => setApiKeysOpen(true)}
                title={t('bots.apiKeys')}
              >
                <Key className="h-4 w-4" />
              </Button>
              <Button
                variant="ghost"
                size="icon"
                onClick={() => onEdit(bot)}
                title={t('common.edit')}
              >
                <Edit2 className="h-4 w-4" />
              </Button>
              <Button
                variant="ghost"
                size="icon"
                onClick={() => onDelete(bot)}
                title={t('common.delete')}
                className="text-red-400 hover:text-red-300"
              >
                <Trash2 className="h-4 w-4" />
              </Button>
            </div>
          </div>
        </CardHeader>

        <CardContent>
          <div className="space-y-3">
            {/* Game badge */}
            <div className="flex items-center gap-2 text-sm text-zinc-400">
              <span className="font-mono text-xs uppercase tracking-wider">{bot.game}</span>
              <span className="text-zinc-600">•</span>
              <span>Created {formatDate(bot.created)}</span>
            </div>

            {/* Stats */}
            {stats && (
              <div className="grid grid-cols-4 gap-2 rounded-lg bg-zinc-900/50 p-3">
                <div className="text-center">
                  <div className="flex items-center justify-center gap-1 text-emerald-500">
                    <Trophy className="h-3 w-3" />
                    <span className="text-lg font-bold">{stats.wins ?? 0}</span>
                  </div>
                  <div className="text-xs text-zinc-500">{t('bots.wins')}</div>
                </div>
                <div className="text-center">
                  <div className="flex items-center justify-center gap-1 text-red-400">
                    <Target className="h-3 w-3" />
                    <span className="text-lg font-bold">{stats.losses ?? 0}</span>
                  </div>
                  <div className="text-xs text-zinc-500">{t('bots.losses')}</div>
                </div>
                <div className="text-center">
                  <div className="flex items-center justify-center gap-1 text-zinc-400">
                    <Minus className="h-3 w-3" />
                    <span className="text-lg font-bold">{stats.draws ?? 0}</span>
                  </div>
                  <div className="text-xs text-zinc-500">{t('bots.draws')}</div>
                </div>
                <div className="text-center">
                  <div className="flex items-center justify-center gap-1 text-blue-400">
                    <TrendingUp className="h-3 w-3" />
                    <span className="text-lg font-bold">
                      {stats.winRate != null ? `${Math.round(stats.winRate * 100)}%` : '—'}
                    </span>
                  </div>
                  <div className="text-xs text-zinc-500">{t('bots.winRate')}</div>
                </div>
              </div>
            )}
          </div>
        </CardContent>
      </Card>

      <ApiKeysDialog
        bot={bot}
        open={apiKeysOpen}
        onOpenChange={setApiKeysOpen}
      />
    </>
  );
}

