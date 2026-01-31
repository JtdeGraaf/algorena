import { useState, useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { useCreateMatch, useAllBots } from './useMatches';
import { useBots } from '@/features/bots/useBots';
import { Loader2, Search, Swords, Bot } from 'lucide-react';
import type { BotDto } from '@/api/generated';
import { cn } from '@/lib/utils';

interface CreateMatchDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

type GameType = 'CHESS';

const GAMES: { id: GameType; name: string; description: string }[] = [
  { id: 'CHESS', name: 'Chess', description: 'Classic chess game' },
];

export function CreateMatchDialog({ open, onOpenChange }: CreateMatchDialogProps) {
  const { t } = useTranslation();
  const [step, setStep] = useState<'game' | 'myBot' | 'opponent'>('game');
  const [selectedGame, setSelectedGame] = useState<GameType>('CHESS');
  const [selectedMyBot, setSelectedMyBot] = useState<BotDto | null>(null);
  const [selectedOpponent, setSelectedOpponent] = useState<BotDto | null>(null);
  const [searchQuery, setSearchQuery] = useState('');

  const { data: myBotsPage, isLoading: myBotsLoading } = useBots();
  const { data: allBotsPage, isLoading: allBotsLoading } = useAllBots({ game: selectedGame });
  const createMatch = useCreateMatch();

  const myBots = useMemo(() => {
    return (myBotsPage?.content || []).filter(bot => bot.game === selectedGame && bot.active);
  }, [myBotsPage, selectedGame]);

  const opponentBots = useMemo(() => {
    const allBots = allBotsPage?.content || [];
    const filtered = allBots.filter(bot => {
      // Exclude the selected bot
      if (selectedMyBot && bot.id === selectedMyBot.id) return false;
      // Filter by search query
      if (searchQuery && !bot.name?.toLowerCase().includes(searchQuery.toLowerCase())) return false;
      return true;
    });
    return filtered;
  }, [allBotsPage, selectedMyBot, searchQuery]);

  const handleCreateMatch = async () => {
    if (!selectedMyBot?.id || !selectedOpponent?.id) return;

    try {
      await createMatch.mutateAsync({
        bot1Id: selectedMyBot.id,
        bot2Id: selectedOpponent.id,
        game: selectedGame,
      });
      handleClose();
    } catch (error) {
      console.error('Failed to create match:', error);
    }
  };

  const handleClose = () => {
    setStep('game');
    setSelectedGame('CHESS');
    setSelectedMyBot(null);
    setSelectedOpponent(null);
    setSearchQuery('');
    onOpenChange(false);
  };

  const handleBack = () => {
    if (step === 'opponent') {
      setSelectedOpponent(null);
      setSearchQuery('');
      setStep('myBot');
    } else if (step === 'myBot') {
      setSelectedMyBot(null);
      setStep('game');
    }
  };

  const handleNext = () => {
    if (step === 'game') {
      setStep('myBot');
    } else if (step === 'myBot' && selectedMyBot) {
      setStep('opponent');
    }
  };

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent className="max-w-xl">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <Swords className="h-5 w-5 text-emerald-500" />
            {t('matches.createMatch')}
          </DialogTitle>
          <DialogDescription>
            {step === 'game' && 'Select a game to play'}
            {step === 'myBot' && 'Select your bot to compete'}
            {step === 'opponent' && 'Select an opponent bot to challenge'}
          </DialogDescription>
        </DialogHeader>

        <div className="py-4">
          {/* Step indicator */}
          <div className="mb-6 flex items-center justify-center gap-2">
            {['game', 'myBot', 'opponent'].map((s, i) => (
              <div key={s} className="flex items-center">
                <div
                  className={cn(
                    'flex h-8 w-8 items-center justify-center rounded-full text-sm font-medium',
                    step === s
                      ? 'bg-emerald-500 text-white'
                      : i < ['game', 'myBot', 'opponent'].indexOf(step)
                      ? 'bg-emerald-500/20 text-emerald-500'
                      : 'bg-zinc-800 text-zinc-500'
                  )}
                >
                  {i + 1}
                </div>
                {i < 2 && (
                  <div
                    className={cn(
                      'h-0.5 w-8',
                      i < ['game', 'myBot', 'opponent'].indexOf(step)
                        ? 'bg-emerald-500'
                        : 'bg-zinc-800'
                    )}
                  />
                )}
              </div>
            ))}
          </div>

          {/* Step: Game Selection */}
          {step === 'game' && (
            <div className="space-y-3">
              {GAMES.map((game) => (
                <button
                  key={game.id}
                  onClick={() => setSelectedGame(game.id)}
                  className={cn(
                    'w-full rounded-lg border p-4 text-left transition-colors',
                    selectedGame === game.id
                      ? 'border-emerald-500 bg-emerald-500/10'
                      : 'border-zinc-800 bg-zinc-900/50 hover:border-zinc-700'
                  )}
                >
                  <div className="font-medium">{game.name}</div>
                  <div className="text-sm text-zinc-400">{game.description}</div>
                </button>
              ))}
            </div>
          )}

          {/* Step: My Bot Selection */}
          {step === 'myBot' && (
            <div className="space-y-3">
              {myBotsLoading ? (
                <div className="flex justify-center py-8">
                  <Loader2 className="h-6 w-6 animate-spin text-zinc-500" />
                </div>
              ) : myBots.length === 0 ? (
                <div className="py-8 text-center">
                  <Bot className="mx-auto h-12 w-12 text-zinc-600" />
                  <p className="mt-2 text-zinc-400">No active bots for {selectedGame}</p>
                  <p className="text-sm text-zinc-500">Create a bot first to start a match</p>
                </div>
              ) : (
                <div className="max-h-64 space-y-2 overflow-y-auto">
                  {myBots.map((bot) => (
                    <button
                      key={bot.id}
                      onClick={() => setSelectedMyBot(bot)}
                      className={cn(
                        'w-full rounded-lg border p-3 text-left transition-colors',
                        selectedMyBot?.id === bot.id
                          ? 'border-emerald-500 bg-emerald-500/10'
                          : 'border-zinc-800 bg-zinc-900/50 hover:border-zinc-700'
                      )}
                    >
                      <div className="flex items-center justify-between">
                        <div>
                          <div className="font-medium">{bot.name}</div>
                          <div className="text-sm text-zinc-400">{bot.description || 'No description'}</div>
                        </div>
                        <Bot className="h-5 w-5 text-zinc-500" />
                      </div>
                    </button>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* Step: Opponent Selection */}
          {step === 'opponent' && (
            <div className="space-y-3">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-zinc-500" />
                <Input
                  value={searchQuery}
                  onChange={(e: React.ChangeEvent<HTMLInputElement>) => setSearchQuery(e.target.value)}
                  placeholder="Search bots..."
                  className="pl-10"
                />
              </div>
              {allBotsLoading ? (
                <div className="flex justify-center py-8">
                  <Loader2 className="h-6 w-6 animate-spin text-zinc-500" />
                </div>
              ) : opponentBots.length === 0 ? (
                <div className="py-8 text-center">
                  <Bot className="mx-auto h-12 w-12 text-zinc-600" />
                  <p className="mt-2 text-zinc-400">No bots found</p>
                  <p className="text-sm text-zinc-500">Try a different search</p>
                </div>
              ) : (
                <div className="max-h-64 space-y-2 overflow-y-auto">
                  {opponentBots.map((bot) => (
                    <button
                      key={bot.id}
                      onClick={() => setSelectedOpponent(bot)}
                      className={cn(
                        'w-full rounded-lg border p-3 text-left transition-colors',
                        selectedOpponent?.id === bot.id
                          ? 'border-emerald-500 bg-emerald-500/10'
                          : 'border-zinc-800 bg-zinc-900/50 hover:border-zinc-700'
                      )}
                    >
                      <div className="flex items-center justify-between">
                        <div>
                          <div className="font-medium">{bot.name}</div>
                          <div className="text-sm text-zinc-400">{bot.description || 'No description'}</div>
                        </div>
                        <Bot className="h-5 w-5 text-zinc-500" />
                      </div>
                    </button>
                  ))}
                </div>
              )}
            </div>
          )}
        </div>

        <DialogFooter>
          {step !== 'game' && (
            <Button type="button" variant="outline" onClick={handleBack}>
              {t('common.back')}
            </Button>
          )}
          {step === 'game' && (
            <Button type="button" variant="outline" onClick={handleClose}>
              {t('common.cancel')}
            </Button>
          )}
          {step !== 'opponent' ? (
            <Button
              onClick={handleNext}
              disabled={step === 'myBot' && !selectedMyBot}
            >
              {t('common.next')}
            </Button>
          ) : (
            <Button
              onClick={handleCreateMatch}
              disabled={!selectedOpponent || createMatch.isPending}
            >
              {createMatch.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              Start Match
            </Button>
          )}
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

