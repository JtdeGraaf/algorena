import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { useAuth } from '@/features/auth/AuthContext';
import { Bot, Swords, Trophy, Code2, Terminal, Zap } from 'lucide-react';

export function HomePage() {
  const { t } = useTranslation();
  const { isAuthenticated, login } = useAuth();

  const features = [
    {
      icon: Bot,
      title: 'Code Your Strategy',
      description: 'Write bots in any language using our REST API. No SDK required.',
      color: 'text-primary',
    },
    {
      icon: Swords,
      title: 'Battle & Compete',
      description: 'Challenge bots in Chess and Connect4. Watch replays and analyze every move.',
      color: 'text-blue-500',
    },
    {
      icon: Trophy,
      title: 'Climb Rankings',
      description: 'Track your win/loss record. Improve your algorithm and dominate the leaderboard.',
      color: 'text-yellow-500',
    },
    {
      icon: Zap,
      title: 'Real-time Matches',
      description: 'Fast response times. Clean APIs. Instant feedback on your bot\'s performance.',
      color: 'text-purple-500',
    },
  ];

  return (
    <div className="space-y-16">
      {/* Hero Section */}
      <section className="py-12 text-center">
        <div className="mx-auto max-w-3xl space-y-6">
          <h1 className="font-mono text-4xl font-bold tracking-tight text-text-primary sm:text-5xl md:text-6xl">
            <span className="text-primary">{'>'}</span> Competitive Bot Programming
          </h1>
          <p className="font-mono text-lg text-text-secondary sm:text-xl">
            # Build bots that battle in Chess and Connect4
            <br />
            <span className="text-text-muted"># Write algorithms. Test strategies. Dominate the arena.</span>
          </p>
          <div className="flex flex-wrap justify-center gap-4 pt-4">
            {isAuthenticated ? (
              <Link to="/bots">
                <Button size="lg" className="gap-2 font-mono">
                  <Terminal className="h-4 w-4" />
                  ./create-bot
                </Button>
              </Link>
            ) : (
              <Button size="lg" onClick={login} className="gap-2 font-mono">
                <Code2 className="h-4 w-4" />
                {t('home.getStarted')}
              </Button>
            )}
            <Link to="/docs">
              <Button variant="outline" size="lg" className="gap-2 font-mono">
                man algorena
              </Button>
            </Link>
          </div>
        </div>
      </section>

      {/* Features Grid */}
      <section className="grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
        {features.map((feature) => (
          <div
            key={feature.title}
            className="group rounded-lg border border-border bg-surface/30 p-6 transition-colors hover:bg-surface/50"
          >
            <feature.icon className={`h-10 w-10 ${feature.color} mb-4 transition-transform group-hover:scale-110`} />
            <h3 className="font-mono text-lg font-semibold text-text-primary">{feature.title}</h3>
            <p className="mt-2 text-sm text-text-secondary">{feature.description}</p>
          </div>
        ))}
      </section>

      {/* Quick Start */}
      <section className="mx-auto max-w-4xl">
        <div className="space-y-4">
          <div>
            <h2 className="font-mono text-2xl font-bold text-primary">$ cat bot.py</h2>
            <p className="mt-1 font-mono text-sm text-text-muted"># Minimal bot example - expose an endpoint that returns moves</p>
          </div>
          <div className="overflow-hidden rounded-lg border border-border bg-surface/50">
            <div className="border-b border-border bg-surface-elevated px-4 py-2">
              <div className="flex items-center gap-2">
                <div className="h-3 w-3 rounded-full bg-red-500"></div>
                <div className="h-3 w-3 rounded-full bg-yellow-500"></div>
                <div className="h-3 w-3 rounded-full bg-emerald-500"></div>
                <span className="ml-2 font-mono text-xs text-text-muted">~/my-bot/server.py</span>
              </div>
            </div>
            <pre className="overflow-x-auto bg-background p-6 text-sm">
              <code className="font-mono text-text-secondary">
{`from flask import Flask, request, jsonify
import random

app = Flask(__name__)

@app.route('/move', methods=['POST'])
def make_move():
    # Algorena sends: matchId, game, playerIndex, gameState, legalMoves
    data = request.json
    legal_moves = data['legalMoves']

    # Your algorithm here! This example picks randomly
    move = random.choice(legal_moves)

    # Return move in expected format
    return jsonify({'move': move})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=3000)`}
              </code>
            </pre>
          </div>
          <div className="rounded-lg border border-border bg-surface/30 p-4">
            <p className="font-mono text-xs text-text-muted">
              # Then register your bot at <span className="text-primary">https://your-bot.example.com/move</span>
              <br />
              # Algorena will POST to your endpoint when it's your turn
            </p>
          </div>
        </div>
      </section>

      {/* Supported Games */}
      <section className="mx-auto max-w-4xl">
        <div className="space-y-4">
          <div>
            <h2 className="font-mono text-2xl font-bold text-primary">$ ls games/</h2>
            <p className="mt-1 font-mono text-sm text-text-muted"># Currently supported game types</p>
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="rounded-lg border border-border bg-surface/30 p-6">
              <div className="flex items-center gap-3">
                <div className="text-4xl">♟️</div>
                <div>
                  <h3 className="font-mono text-lg font-semibold text-text-primary">CHESS</h3>
                  <p className="mt-1 text-sm text-text-secondary">
                    Standard chess rules. UCI move format. Full game history.
                  </p>
                </div>
              </div>
            </div>
            <div className="rounded-lg border border-border bg-surface/30 p-6">
              <div className="flex items-center gap-3">
                <div className="text-4xl">🔴</div>
                <div>
                  <h3 className="font-mono text-lg font-semibold text-text-primary">CONNECT4</h3>
                  <p className="mt-1 text-sm text-text-secondary">
                    Connect four in a row. 7x6 grid. Column-based moves.
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}

