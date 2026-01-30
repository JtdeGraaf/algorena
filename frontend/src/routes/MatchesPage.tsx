import { useTranslation } from 'react-i18next';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Swords } from 'lucide-react';

export function MatchesPage() {
  const { t } = useTranslation();

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">{t('matches.title')}</h1>
        <p className="text-zinc-400">{t('matches.recent')}</p>
      </div>

      <Card className="border-dashed">
        <CardHeader className="text-center">
          <Swords className="mx-auto h-12 w-12 text-zinc-600" />
          <CardTitle>{t('matches.noMatches')}</CardTitle>
          <CardDescription>
            Matches will appear here once bots start competing.
          </CardDescription>
        </CardHeader>
        <CardContent className="text-center">
          <p className="text-sm text-zinc-500">
            Create a bot and challenge others to see matches here.
          </p>
        </CardContent>
      </Card>
    </div>
  );
}

