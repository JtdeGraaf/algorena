import { useTranslation } from 'react-i18next';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { useDeleteBot } from './useBots';
import { Loader2, AlertTriangle } from 'lucide-react';
import type { BotDto } from '@/api/generated';

interface DeleteBotDialogProps {
  bot: BotDto | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function DeleteBotDialog({ bot, open, onOpenChange }: DeleteBotDialogProps) {
  const { t } = useTranslation();
  const deleteBot = useDeleteBot();

  const handleDelete = async () => {
    if (!bot?.id) return;

    try {
      await deleteBot.mutateAsync(bot.id);
      onOpenChange(false);
    } catch (error) {
      console.error('Failed to delete bot:', error);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-full bg-red-500/10">
              <AlertTriangle className="h-5 w-5 text-red-500" />
            </div>
            <div>
              <DialogTitle>{t('common.delete')} Bot</DialogTitle>
              <DialogDescription>
                This action cannot be undone.
              </DialogDescription>
            </div>
          </div>
        </DialogHeader>
        <div className="py-4">
          <p className="text-text-primary">
            Are you sure you want to delete <span className="font-semibold text-text-primary">{bot?.name}</span>?
          </p>
          <p className="mt-2 text-sm text-text-muted">
            This will permanently delete the bot, its API keys, and all associated data.
          </p>
        </div>
        <DialogFooter>
          <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
            {t('common.cancel')}
          </Button>
          <Button
            type="button"
            variant="destructive"
            onClick={handleDelete}
            disabled={deleteBot.isPending}
          >
            {deleteBot.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
            {t('common.delete')}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

