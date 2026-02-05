import { Terminal, Key, Gamepad2, Code } from 'lucide-react';

export function DocsPage() {

  return (
    <div className="space-y-8">
      {/* Header */}
      <div>
        <h1 className="font-mono text-2xl font-bold text-primary">$ man algorena</h1>
        <p className="mt-1 font-mono text-sm text-text-muted"># Complete API reference and game rules</p>
      </div>

      {/* Getting Started */}
      <section className="space-y-4">
        <div className="flex items-center gap-2">
          <Terminal className="h-5 w-5 text-primary" />
          <h2 className="font-mono text-xl font-semibold text-text-primary">Getting Started</h2>
        </div>
        <div className="rounded-lg border border-border bg-surface/30 p-6">
          <ol className="space-y-3 font-mono text-sm text-text-primary">
            <li className="flex gap-3">
              <span className="text-primary">1.</span>
              <span>
                <span className="text-primary">Sign in</span> using Google OAuth
              </span>
            </li>
            <li className="flex gap-3">
              <span className="text-primary">2.</span>
              <span>
                Create a <span className="text-primary">web server</span> that exposes a <span className="text-primary">/move</span> endpoint
              </span>
            </li>
            <li className="flex gap-3">
              <span className="text-primary">3.</span>
              <span>
                Navigate to <span className="text-primary">My Bots</span> and register your bot with its endpoint URL
              </span>
            </li>
            <li className="flex gap-3">
              <span className="text-primary">4.</span>
              <span>
                <span className="text-primary">Generate an API key</span> (encrypted at rest, shown once)
              </span>
            </li>
            <li className="flex gap-3">
              <span className="text-primary">5.</span>
              <span>
                Challenge other bots — Algorena will <span className="text-primary">POST to your endpoint</span> when it's your turn
              </span>
            </li>
          </ol>
        </div>
      </section>

      {/* How It Works */}
      <section className="space-y-4">
        <div className="flex items-center gap-2">
          <Code className="h-5 w-5 text-primary" />
          <h2 className="font-mono text-xl font-semibold text-text-primary">How It Works</h2>
        </div>
        <div className="rounded-lg border border-border bg-surface/30 p-6 space-y-4">
          <p className="font-mono text-sm text-text-secondary">
            # Algorena uses a <span className="text-primary">server-queries-bot</span> architecture
          </p>
          <div className="space-y-3 font-mono text-xs text-text-secondary">
            <div className="flex items-start gap-2">
              <span className="text-primary">→</span>
              <span>
                When it's your bot's turn, Algorena <span className="text-text-primary">POSTs</span> a request to your endpoint
              </span>
            </div>
            <div className="flex items-start gap-2">
              <span className="text-primary">→</span>
              <span>
                Request includes: <span className="text-text-primary">matchId, game, playerIndex, gameState, legalMoves</span>
              </span>
            </div>
            <div className="flex items-start gap-2">
              <span className="text-primary">→</span>
              <span>
                Your bot calculates the best move and returns: <span className="text-text-primary">{`{ "move": "e2e4" }`}</span>
              </span>
            </div>
            <div className="flex items-start gap-2">
              <span className="text-primary">→</span>
              <span>
                Algorena validates and applies the move, then continues the game
              </span>
            </div>
          </div>
          <div className="rounded bg-background p-3 font-mono text-xs">
            <div className="text-text-muted"># Example request to your bot</div>
            <div className="text-text-primary mt-1">
              POST https://your-bot.example.com/move
              <br />
              Headers: x-algorena-api-key, x-algorena-match-id
              <br />
              <br />
              {'{'} <span className="text-emerald-500">"matchId"</span>: <span className="text-yellow-500">"uuid"</span>,
              <br />
              {'  '}<span className="text-emerald-500">"game"</span>: <span className="text-yellow-500">"CHESS"</span>,
              <br />
              {'  '}<span className="text-emerald-500">"playerIndex"</span>: <span className="text-yellow-500">0</span>,
              <br />
              {'  '}<span className="text-emerald-500">"gameState"</span>: {'{ ... }'},
              <br />
              {'  '}<span className="text-emerald-500">"legalMoves"</span>: [<span className="text-yellow-500">"e2e4"</span>, <span className="text-yellow-500">"d2d4"</span>, ...] {'}'}
            </div>
          </div>
        </div>
      </section>

      {/* Bot Endpoint Specification */}
      <section className="space-y-4">
        <div className="flex items-center gap-2">
          <Terminal className="h-5 w-5 text-primary" />
          <h2 className="font-mono text-xl font-semibold text-text-primary">Your Bot Endpoint</h2>
        </div>
        <div className="rounded-lg border border-border bg-surface/30 p-6 space-y-4">
          <div>
            <h3 className="font-mono text-sm font-semibold text-primary">Required Endpoint</h3>
            <code className="font-mono text-sm text-text-secondary">POST /move</code>
            <p className="mt-2 font-mono text-xs text-text-muted">
              # Your server must accept POST requests at any path (commonly /move)
            </p>
          </div>
          <div>
            <h3 className="font-mono text-sm font-semibold text-primary">Request Headers</h3>
            <div className="mt-2 space-y-1 font-mono text-xs">
              <div>
                <span className="text-primary">x-algorena-api-key:</span> <span className="text-text-secondary">Your bot's API key</span>
              </div>
              <div>
                <span className="text-primary">x-algorena-match-id:</span> <span className="text-text-secondary">Current match UUID</span>
              </div>
            </div>
          </div>
          <div>
            <h3 className="font-mono text-sm font-semibold text-primary">Request Body</h3>
            <div className="rounded bg-background p-3 font-mono text-xs text-text-primary">
              {'{'}
              <br />
              {'  '}<span className="text-emerald-500">"matchId"</span>: <span className="text-yellow-500">"uuid"</span>,
              <br />
              {'  '}<span className="text-emerald-500">"game"</span>: <span className="text-yellow-500">"CHESS"</span> | <span className="text-yellow-500">"CONNECT_FOUR"</span>,
              <br />
              {'  '}<span className="text-emerald-500">"playerIndex"</span>: <span className="text-yellow-500">0</span> | <span className="text-yellow-500">1</span>,
              <br />
              {'  '}<span className="text-emerald-500">"gameState"</span>: {'{ ... }'}, <span className="text-text-muted">// Game-specific</span>
              <br />
              {'  '}<span className="text-emerald-500">"legalMoves"</span>: <span className="text-yellow-500">["e2e4", ...]</span>
              <br />
              {'}'}
            </div>
          </div>
          <div>
            <h3 className="font-mono text-sm font-semibold text-primary">Expected Response (200 OK)</h3>
            <div className="rounded bg-background p-3 font-mono text-xs text-text-primary">
              {'{ '}<span className="text-emerald-500">"move"</span>: <span className="text-yellow-500">"e2e4"</span> {'}'}
            </div>
          </div>
        </div>
      </section>

      {/* Game Rules */}
      <section className="space-y-4">
        <div className="flex items-center gap-2">
          <Gamepad2 className="h-5 w-5 text-primary" />
          <h2 className="font-mono text-xl font-semibold text-text-primary">Game Rules</h2>
        </div>

        {/* Chess */}
        <div className="rounded-lg border border-border bg-surface/30 p-6 space-y-4">
          <div className="flex items-center gap-3">
            <span className="text-3xl">♟️</span>
            <h3 className="font-mono text-lg font-semibold text-primary">CHESS</h3>
          </div>
          <ul className="space-y-2 font-mono text-sm text-text-secondary">
            <li className="flex gap-2">
              <span className="text-primary">•</span>
              <span>Standard chess rules (FIDE) apply</span>
            </li>
            <li className="flex gap-2">
              <span className="text-primary">•</span>
              <span>
                Moves submitted in <span className="text-text-primary">UCI format</span> (e.g., "e2e4", "e7e8q" for pawn promotion)
              </span>
            </li>
            <li className="flex gap-2">
              <span className="text-primary">•</span>
              <span>
                Game state provided as <span className="text-text-primary">FEN</span> (position) and <span className="text-text-primary">PGN</span> (full history)
              </span>
            </li>
            <li className="flex gap-2">
              <span className="text-primary">•</span>
              <span>Invalid moves return HTTP 400 error</span>
            </li>
            <li className="flex gap-2">
              <span className="text-primary">•</span>
              <span>Win conditions: checkmate, resignation, timeout, disconnect</span>
            </li>
          </ul>
          <div className="rounded bg-background p-3 font-mono text-xs">
            <div className="text-text-muted">// Example move submission</div>
            <div className="text-text-primary mt-1">
              {'{'} <span className="text-emerald-500">"move"</span>: <span className="text-yellow-500">"e2e4"</span> {'}'}
            </div>
          </div>
        </div>

        {/* Connect4 */}
        <div className="rounded-lg border border-border bg-surface/30 p-6 space-y-4">
          <div className="flex items-center gap-3">
            <span className="text-3xl">🔴</span>
            <h3 className="font-mono text-lg font-semibold text-primary">CONNECT4</h3>
          </div>
          <ul className="space-y-2 font-mono text-sm text-text-secondary">
            <li className="flex gap-2">
              <span className="text-primary">•</span>
              <span>7 columns × 6 rows grid (42 cells total)</span>
            </li>
            <li className="flex gap-2">
              <span className="text-primary">•</span>
              <span>
                Moves submitted as <span className="text-text-primary">column number</span> (0-6, left to right)
              </span>
            </li>
            <li className="flex gap-2">
              <span className="text-primary">•</span>
              <span>
                Pieces drop to the <span className="text-text-primary">lowest empty row</span> in the selected column
              </span>
            </li>
            <li className="flex gap-2">
              <span className="text-primary">•</span>
              <span>Win condition: connect 4 pieces horizontally, vertically, or diagonally</span>
            </li>
            <li className="flex gap-2">
              <span className="text-primary">•</span>
              <span>Draw if board is full with no winner</span>
            </li>
            <li className="flex gap-2">
              <span className="text-primary">•</span>
              <span>Submitting a move to a full column returns HTTP 400 error</span>
            </li>
          </ul>
          <div className="rounded bg-background p-3 font-mono text-xs">
            <div className="text-text-muted">// Example move submission</div>
            <div className="text-text-primary mt-1">
              {'{'} <span className="text-emerald-500">"move"</span>: <span className="text-yellow-500">"3"</span> {'}'} <span className="text-text-muted">// Drop piece in middle column</span>
            </div>
          </div>
        </div>
      </section>

      {/* API Key Security */}
      <section className="space-y-4">
        <div className="flex items-center gap-2">
          <Key className="h-5 w-5 text-primary" />
          <h2 className="font-mono text-xl font-semibold text-text-primary">API Key Security</h2>
        </div>
        <div className="rounded-lg border border-border bg-surface/30 p-6">
          <ul className="space-y-2 font-mono text-sm text-text-secondary">
            <li className="flex gap-2">
              <span className="text-primary">•</span>
              <span>API keys are <span className="text-text-primary">encrypted at rest</span> using AES-256-GCM</span>
            </li>
            <li className="flex gap-2">
              <span className="text-primary">•</span>
              <span>Only shown <span className="text-text-primary">once</span> during creation - store it securely</span>
            </li>
            <li className="flex gap-2">
              <span className="text-primary">•</span>
              <span>Never commit API keys to version control</span>
            </li>
            <li className="flex gap-2">
              <span className="text-primary">•</span>
              <span>Regenerate keys if compromised</span>
            </li>
          </ul>
        </div>
      </section>
    </div>
  );
}

