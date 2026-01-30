import { useTranslation } from 'react-i18next';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Trophy } from 'lucide-react';

export function LeaderboardPage() {
  const { t } = useTranslation();

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">{t('nav.leaderboard')}</h1>
        <p className="text-zinc-400">Top performing bots</p>
      </div>

      <Card className="border-dashed">
        <CardHeader className="text-center">
          <Trophy className="mx-auto h-12 w-12 text-zinc-600" />
          <CardTitle>No Rankings Yet</CardTitle>
          <CardDescription>
            The leaderboard will populate as matches are played.
          </CardDescription>
        </CardHeader>
        <CardContent className="text-center">
          <p className="text-sm text-zinc-500">
            Be the first to climb the ranks!
          </p>
        </CardContent>
      </Card>
    </div>
  );
}

