import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useBotApiKeys, useCreateApiKey, useRevokeApiKey } from './useBots';
import { Loader2, Plus, Trash2, Copy, Check, Key, AlertCircle } from 'lucide-react';
import type { BotDto } from '@/api/generated';

interface ApiKeysDialogProps {
  bot: BotDto;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function ApiKeysDialog({ bot, open, onOpenChange }: ApiKeysDialogProps) {
  const { t } = useTranslation();
  const [newKeyName, setNewKeyName] = useState('');
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [newPlainTextKey, setNewPlainTextKey] = useState<string | null>(null);
  const [copiedKey, setCopiedKey] = useState(false);

  const { data: apiKeys, isLoading } = useBotApiKeys(bot.id!);
  const createApiKey = useCreateApiKey();
  const revokeApiKey = useRevokeApiKey();

  const handleCreateKey = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!bot.id) return;

    try {
      const result = await createApiKey.mutateAsync({
        botId: bot.id,
        data: { name: newKeyName || undefined },
      });
      if (result?.plainTextKey) {
        setNewPlainTextKey(result.plainTextKey);
      }
      setNewKeyName('');
      setShowCreateForm(false);
    } catch (error) {
      console.error('Failed to create API key:', error);
    }
  };

  const handleRevokeKey = async (apiKeyId: number) => {
    if (!bot.id) return;

    try {
      await revokeApiKey.mutateAsync({ botId: bot.id, apiKeyId });
    } catch (error) {
      console.error('Failed to revoke API key:', error);
    }
  };

  const handleCopyKey = async (key: string) => {
    await navigator.clipboard.writeText(key);
    setCopiedKey(true);
    setTimeout(() => setCopiedKey(false), 2000);
  };

  const handleClose = () => {
    setNewPlainTextKey(null);
    setShowCreateForm(false);
    setNewKeyName('');
    onOpenChange(false);
  };

  const formatDate = (dateString?: string) => {
    if (!dateString) return '—';
    return new Date(dateString).toLocaleDateString();
  };

  const activeKeys = apiKeys?.filter((key) => !key.revoked) || [];

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent className="max-w-xl">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <Key className="h-5 w-5" />
            API Keys for {bot.name}
          </DialogTitle>
          <DialogDescription>
            Use API keys to authenticate your bot when making moves.
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-4 py-4">
          {/* New key display */}
          {newPlainTextKey && (
            <div className="rounded-lg border border-emerald-500/50 bg-emerald-500/10 p-4">
              <div className="flex items-start gap-3">
                <AlertCircle className="mt-0.5 h-5 w-5 flex-shrink-0 text-emerald-500" />
                <div className="flex-1 space-y-2">
                  <p className="text-sm font-medium text-emerald-400">
                    API Key Created Successfully!
                  </p>
                  <p className="text-xs text-zinc-400">
                    Copy this key now. You won't be able to see it again!
                  </p>
                  <div className="flex items-center gap-2">
                    <code className="flex-1 rounded bg-zinc-900 px-3 py-2 font-mono text-xs text-zinc-300 break-all">
                      {newPlainTextKey}
                    </code>
                    <Button
                      variant="outline"
                      size="icon"
                      onClick={() => handleCopyKey(newPlainTextKey)}
                    >
                      {copiedKey ? (
                        <Check className="h-4 w-4 text-emerald-500" />
                      ) : (
                        <Copy className="h-4 w-4" />
                      )}
                    </Button>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Create new key form */}
          {showCreateForm ? (
            <form onSubmit={handleCreateKey} className="space-y-3 rounded-lg border border-zinc-800 p-4">
              <Label htmlFor="key-name">Key Name (optional)</Label>
              <div className="flex gap-2">
                <Input
                  id="key-name"
                  value={newKeyName}
                  onChange={(e: React.ChangeEvent<HTMLInputElement>) => setNewKeyName(e.target.value)}
                  placeholder="e.g., Production Key"
                />
                <Button type="submit" disabled={createApiKey.isPending}>
                  {createApiKey.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                  Create
                </Button>
                <Button type="button" variant="outline" onClick={() => setShowCreateForm(false)}>
                  Cancel
                </Button>
              </div>
            </form>
          ) : (
            <Button onClick={() => setShowCreateForm(true)} className="w-full gap-2">
              <Plus className="h-4 w-4" />
              {t('bots.generateApiKey')}
            </Button>
          )}

          {/* Existing keys list */}
          <div className="space-y-2">
            <h4 className="text-sm font-medium text-zinc-300">Active Keys ({activeKeys.length})</h4>
            {isLoading ? (
              <div className="flex justify-center py-4">
                <Loader2 className="h-6 w-6 animate-spin text-zinc-500" />
              </div>
            ) : activeKeys.length === 0 ? (
              <p className="py-4 text-center text-sm text-zinc-500">
                No API keys yet. Create one to get started.
              </p>
            ) : (
              <div className="space-y-2">
                {activeKeys.map((key) => (
                  <div
                    key={key.id}
                    className="flex items-center justify-between rounded-lg border border-zinc-800 bg-zinc-900/50 px-4 py-3"
                  >
                    <div className="space-y-1">
                      <div className="flex items-center gap-2">
                        <span className="font-medium text-zinc-200">
                          {key.name || 'Unnamed Key'}
                        </span>
                        <code className="rounded bg-zinc-800 px-2 py-0.5 font-mono text-xs text-zinc-400">
                          {key.keyPrefix}...
                        </code>
                      </div>
                      <div className="text-xs text-zinc-500">
                        Created {formatDate(key.created)}
                        {key.lastUsed && ` • Last used ${formatDate(key.lastUsed)}`}
                      </div>
                    </div>
                    <Button
                      variant="ghost"
                      size="icon"
                      onClick={() => handleRevokeKey(key.id!)}
                      disabled={revokeApiKey.isPending}
                      className="text-red-400 hover:text-red-300"
                      title="Revoke key"
                    >
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}

