import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Select } from '@/components/ui/select';
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
  const [game, setGame] = useState<'CHESS' | 'CONNECT_FOUR'>('CHESS');
  const [endpoint, setEndpoint] = useState('');
  const [apiKey, setApiKey] = useState('');
  const createBot = useCreateBot();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await createBot.mutateAsync({
        name,
        description: description || undefined,
        game,
        endpoint,
        apiKey: apiKey || undefined,
      });
      setName('');
      setDescription('');
      setGame('CHESS');
      setEndpoint('');
      setApiKey('');
      onOpenChange(false);
    } catch (error) {
      console.error('Failed to create bot:', error);
    }
  };

  const handleClose = () => {
    setName('');
    setDescription('');
    setGame('CHESS');
    setEndpoint('');
    setApiKey('');
    onOpenChange(false);
  };

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{t('bots.createBot')}</DialogTitle>
          <DialogDescription>
            Create a new bot to compete in the arena. Your bot must expose an HTTP endpoint that accepts move requests.
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
            <Label htmlFor="game">{t('bots.game')}</Label>
            <Select
              id="game"
              value={game}
              onChange={(e: React.ChangeEvent<HTMLSelectElement>) => setGame(e.target.value as 'CHESS' | 'CONNECT_FOUR')}
              className="bg-surface border-border-hover"
            >
              <option value="CHESS">Chess</option>
              <option value="CONNECT_FOUR">Connect 4</option>
            </Select>
            <p className="text-xs text-text-muted">More games coming soon!</p>
          </div>
          <div className="space-y-2">
            <Label htmlFor="endpoint">Endpoint URL *</Label>
            <Input
              id="endpoint"
              type="url"
              value={endpoint}
              onChange={(e: React.ChangeEvent<HTMLInputElement>) => setEndpoint(e.target.value)}
              placeholder="https://your-bot.example.com/move"
              required
              maxLength={500}
            />
            <p className="text-xs text-text-muted">
              The URL where Algorena will send move requests to your bot.
            </p>
          </div>
          <div className="space-y-2">
            <Label htmlFor="apiKey">API Key (optional)</Label>
            <Input
              id="apiKey"
              type="password"
              value={apiKey}
              onChange={(e: React.ChangeEvent<HTMLInputElement>) => setApiKey(e.target.value)}
              placeholder="Your bot's secret key"
              maxLength={255}
            />
            <p className="text-xs text-text-muted">
              Sent in X-Algorena-API-Key header so your bot can verify requests are from Algorena.
            </p>
          </div>
          <DialogFooter className="pt-4">
            <Button type="button" variant="outline" onClick={handleClose}>
              {t('common.cancel')}
            </Button>
            <Button type="submit" disabled={createBot.isPending || !name.trim() || !endpoint.trim()}>
              {createBot.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              {t('common.create')}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
