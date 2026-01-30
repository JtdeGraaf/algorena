import { useTranslation } from 'react-i18next';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { BookOpen } from 'lucide-react';

export function DocsPage() {
  const { t } = useTranslation();

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">{t('nav.docs')}</h1>
        <p className="text-zinc-400">Learn how to build and deploy your bot</p>
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <BookOpen className="h-5 w-5 text-emerald-500" />
              Getting Started
            </CardTitle>
            <CardDescription>
              Quick introduction to building your first bot
            </CardDescription>
          </CardHeader>
          <CardContent className="prose prose-invert prose-sm max-w-none">
            <ol className="list-decimal space-y-2 pl-4 text-zinc-300">
              <li>Create an account using Google OAuth</li>
              <li>Navigate to "My Bots" and create a new bot</li>
              <li>Generate an API key for your bot</li>
              <li>Use the API to connect your bot to matches</li>
              <li>Submit moves and compete!</li>
            </ol>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <BookOpen className="h-5 w-5 text-emerald-500" />
              API Reference
            </CardTitle>
            <CardDescription>
              Endpoints and data formats
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div>
              <h4 className="font-mono text-sm font-semibold text-emerald-500">Base URL</h4>
              <code className="text-sm text-zinc-400">http://localhost:8080/api/v1</code>
            </div>
            <div>
              <h4 className="font-mono text-sm font-semibold text-emerald-500">Authentication</h4>
              <p className="text-sm text-zinc-400">
                Use Bearer token authentication with your bot's API key.
              </p>
            </div>
            <div>
              <h4 className="font-mono text-sm font-semibold text-emerald-500">OpenAPI Spec</h4>
              <a
                href="http://localhost:8080/swagger-ui.html"
                target="_blank"
                rel="noopener noreferrer"
                className="text-sm text-emerald-500 hover:underline"
              >
                View Swagger UI →
              </a>
            </div>
          </CardContent>
        </Card>

        <Card className="md:col-span-2">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <BookOpen className="h-5 w-5 text-emerald-500" />
              Chess Game Rules
            </CardTitle>
            <CardDescription>
              How chess matches work in Algorena
            </CardDescription>
          </CardHeader>
          <CardContent className="prose prose-invert prose-sm max-w-none">
            <ul className="list-disc space-y-2 pl-4 text-zinc-300">
              <li>Standard chess rules apply</li>
              <li>Moves are submitted in UCI format (e.g., "e2e4", "e7e8q" for promotion)</li>
              <li>Game state is provided as FEN and PGN</li>
              <li>Invalid moves result in an error response</li>
              <li>Timeouts and disconnections result in forfeits</li>
            </ul>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

