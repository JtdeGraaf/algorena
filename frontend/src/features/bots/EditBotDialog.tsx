import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { useUpdateBot } from './useBots';
import { Loader2 } from 'lucide-react';
import type { BotDto } from '@/api/generated';

interface EditBotDialogProps {
  bot: BotDto | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function EditBotDialog({ bot, open, onOpenChange }: EditBotDialogProps) {
  const { t } = useTranslation();
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [active, setActive] = useState(true);
  const [endpoint, setEndpoint] = useState('');
  const [apiKey, setApiKey] = useState('');
  const updateBot = useUpdateBot();

  useEffect(() => {
    if (bot) {
      setName(bot.name || '');
      setDescription(bot.description || '');
      setActive(bot.active ?? true);
      setEndpoint(bot.endpoint || '');
      // API key is not returned from the server for security, so we leave it empty
      // User can enter a new one if they want to change it
      setApiKey('');
    }
  }, [bot]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!bot?.id) return;

    try {
      await updateBot.mutateAsync({
        botId: bot.id,
        data: {
          name,
          description: description || undefined,
          active,
          endpoint,
          apiKey: apiKey || undefined,
        },
      });
      onOpenChange(false);
    } catch (error) {
      console.error('Failed to update bot:', error);
    }
  };

  const handleClose = () => {
    onOpenChange(false);
  };

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{t('common.edit')} Bot</DialogTitle>
          <DialogDescription>
            Update your bot's details.
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4 py-4">
          <div className="space-y-2">
            <Label htmlFor="edit-name">{t('bots.name')} *</Label>
            <Input
              id="edit-name"
              value={name}
              onChange={(e: React.ChangeEvent<HTMLInputElement>) => setName(e.target.value)}
              placeholder="My Awesome Bot"
              required
              minLength={3}
              maxLength={50}
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="edit-description">{t('bots.description')}</Label>
            <Textarea
              id="edit-description"
              value={description}
              onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => setDescription(e.target.value)}
              placeholder="A brief description of your bot's strategy..."
              rows={3}
              maxLength={500}
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="edit-endpoint">Endpoint URL *</Label>
            <Input
              id="edit-endpoint"
              type="url"
              value={endpoint}
              onChange={(e: React.ChangeEvent<HTMLInputElement>) => setEndpoint(e.target.value)}
              placeholder="https://your-bot.example.com/move"
              required
              maxLength={500}
            />
            <p className="text-xs text-zinc-500">
              The URL where Algorena will send move requests to your bot.
            </p>
          </div>
          <div className="space-y-2">
            <Label htmlFor="edit-apiKey">API Key (optional)</Label>
            <Input
              id="edit-apiKey"
              type="password"
              value={apiKey}
              onChange={(e: React.ChangeEvent<HTMLInputElement>) => setApiKey(e.target.value)}
              placeholder="Enter new key to change, or leave empty"
              maxLength={255}
            />
            <p className="text-xs text-zinc-500">
              Sent in X-Algorena-API-Key header. Leave empty to keep the current key.
            </p>
          </div>
          <div className="flex items-center gap-2">
            <input
              type="checkbox"
              id="edit-active"
              checked={active}
              onChange={(e: React.ChangeEvent<HTMLInputElement>) => setActive(e.target.checked)}
              className="h-4 w-4 rounded border-zinc-700 bg-zinc-900 text-emerald-500 focus:ring-emerald-500"
            />
            <Label htmlFor="edit-active">{t('bots.active')}</Label>
          </div>
          <DialogFooter className="pt-4">
            <Button type="button" variant="outline" onClick={handleClose}>
              {t('common.cancel')}
            </Button>
            <Button type="submit" disabled={updateBot.isPending || !name.trim() || !endpoint.trim()}>
              {updateBot.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              {t('common.save')}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
