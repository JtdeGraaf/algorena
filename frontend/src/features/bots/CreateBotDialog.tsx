import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { useCreateBot } from './useBots';
import { Loader2 } from 'lucide-react';

interface CreateBotDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function CreateBotDialog({ open, onOpenChange }: CreateBotDialogProps) {
  const { t } = useTranslation();
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const createBot = useCreateBot();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await createBot.mutateAsync({
        name,
        description: description || undefined,
        game: 'CHESS',
      });
      setName('');
      setDescription('');
      onOpenChange(false);
    } catch (error) {
      console.error('Failed to create bot:', error);
    }
  };

  const handleClose = () => {
    setName('');
    setDescription('');
    onOpenChange(false);
  };

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{t('bots.createBot')}</DialogTitle>
          <DialogDescription>
            Create a new bot to compete in the arena. You'll get an API key to connect your bot.
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4 py-4">
          <div className="space-y-2">
            <Label htmlFor="name">{t('bots.name')} *</Label>
            <Input
              id="name"
              value={name}
              onChange={(e: React.ChangeEvent<HTMLInputElement>) => setName(e.target.value)}
              placeholder="My Awesome Bot"
              required
              minLength={3}
              maxLength={50}
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="description">{t('bots.description')}</Label>
            <Textarea
              id="description"
              value={description}
              onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => setDescription(e.target.value)}
              placeholder="A brief description of your bot's strategy..."
              rows={3}
              maxLength={500}
            />
          </div>
          <div className="space-y-2">
            <Label>{t('bots.game')}</Label>
            <Input value="Chess" disabled className="bg-zinc-800" />
            <p className="text-xs text-zinc-500">More games coming soon!</p>
          </div>
          <DialogFooter className="pt-4">
            <Button type="button" variant="outline" onClick={handleClose}>
              {t('common.cancel')}
            </Button>
            <Button type="submit" disabled={createBot.isPending || !name.trim()}>
              {createBot.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              {t('common.create')}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

