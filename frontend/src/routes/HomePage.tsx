import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { useAuth } from '@/features/auth/AuthContext';
import { Bot, Swords, Trophy, Code2 } from 'lucide-react';

export function HomePage() {
  const { t } = useTranslation();
  const { isAuthenticated, login } = useAuth();

  const features = [
    {
      icon: Bot,
      title: 'Build Your Bot',
      description: 'Write your bot in any language. Use our API to receive game state and submit moves.',
    },
    {
      icon: Swords,
      title: 'Battle Others',
      description: 'Challenge other bots to matches and climb the leaderboard.',
    },
    {
      icon: Trophy,
      title: 'Compete & Learn',
      description: 'Analyze your matches, improve your algorithm, and become the best.',
    },
    {
      icon: Code2,
      title: 'Developer First',
      description: 'Clean APIs, detailed documentation, and instant feedback.',
    },
  ];

  return (
    <div className="space-y-16">
      <section className="py-12 text-center">
        <div className="mx-auto max-w-3xl space-y-6">
          <h1 className="font-mono text-4xl font-bold tracking-tight text-text-primary sm:text-5xl md:text-6xl">
            <span className="text-primary">{'>'}</span> {t('home.title')}
          </h1>
          <p className="text-lg text-text-secondary sm:text-xl">
            {t('home.subtitle')}
          </p>
          <div className="flex flex-wrap justify-center gap-4 pt-4">
            {isAuthenticated ? (
              <Link to="/bots">
                <Button size="lg">{t('home.getStarted')}</Button>
              </Link>
            ) : (
              <Button size="lg" onClick={login}>
                {t('home.getStarted')}
              </Button>
            )}
            <Link to="/docs">
              <Button variant="outline" size="lg">
                {t('home.viewDocs')}
              </Button>
            </Link>
          </div>
        </div>
      </section>

      <section className="grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
        {features.map((feature) => (
          <Card key={feature.title} className="border-border bg-surface/50">
            <CardHeader>
              <feature.icon className="h-10 w-10 text-primary" />
              <CardTitle className="text-lg">{feature.title}</CardTitle>
            </CardHeader>
            <CardContent>
              <CardDescription>{feature.description}</CardDescription>
            </CardContent>
          </Card>
        ))}
      </section>

      <section className="mx-auto max-w-3xl">
        <Card className="overflow-hidden border-border bg-surface/50">
          <CardHeader>
            <CardTitle className="font-mono text-primary">Quick Start</CardTitle>
            <CardDescription>Get your bot running in minutes</CardDescription>
          </CardHeader>
          <CardContent>
            <pre className="overflow-x-auto rounded-lg bg-background p-4 text-sm">
              <code className="text-text-primary">
{`# 1. Create a bot and get your API key
# 2. Connect to a match

import requests

API_KEY = "your-api-key"
BASE_URL = "http://localhost:8080/api/v1"

# Get match state
match = requests.get(
    f"{BASE_URL}/matches/{match_id}",
    headers={"Authorization": f"Bearer {API_KEY}"}
).json()

# Make a move
requests.post(
    f"{BASE_URL}/matches/{match_id}/move",
    headers={"Authorization": f"Bearer {API_KEY}"},
    json={"botId": bot_id, "move": "e2e4"}
)`}
              </code>
            </pre>
          </CardContent>
        </Card>
      </section>
    </div>
  );
}

