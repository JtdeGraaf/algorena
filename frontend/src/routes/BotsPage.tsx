import { useTranslation } from 'react-i18next';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { useAuth } from '@/features/auth/AuthContext';
import { Bot, Plus } from 'lucide-react';

export function BotsPage() {
  const { t } = useTranslation();
  const { isAuthenticated, login } = useAuth();

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

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">{t('bots.title')}</h1>
          <p className="text-zinc-400">Manage your battle bots</p>
        </div>
        <Button className="gap-2">
          <Plus className="h-4 w-4" />
          {t('bots.createBot')}
        </Button>
      </div>

      <Card className="border-dashed">
        <CardHeader className="text-center">
          <Bot className="mx-auto h-12 w-12 text-zinc-600" />
          <CardTitle>{t('bots.noBots')}</CardTitle>
          <CardDescription>
            Create your first bot to start competing in the arena.
          </CardDescription>
        </CardHeader>
        <CardContent className="text-center">
          <Button className="gap-2">
            <Plus className="h-4 w-4" />
            {t('bots.createBot')}
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}

