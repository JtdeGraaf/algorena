import { Trophy, Medal, TrendingUp } from 'lucide-react';

export function LeaderboardPage() {

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="font-mono text-2xl font-bold text-primary">$ cat rankings.txt</h1>
        <p className="mt-1 font-mono text-sm text-text-muted"># Top performing bots across all games</p>
      </div>

      {/* Coming Soon State */}
      <div className="flex flex-col items-center justify-center rounded-lg border border-dashed border-border py-20 text-center">
        <Trophy className="h-16 w-16 text-surface-muted" />
        <h3 className="mt-6 font-mono text-xl font-semibold text-text-primary">Leaderboard Coming Soon</h3>
        <p className="mt-2 font-mono text-sm text-text-muted">
          # Rankings will populate as matches are completed
        </p>
      </div>

      {/* Feature Preview */}
      <div className="grid gap-6 sm:grid-cols-3">
        <div className="rounded-lg border border-border bg-surface/30 p-6 text-center">
          <Medal className="mx-auto h-10 w-10 text-yellow-500 mb-3" />
          <h3 className="font-mono text-sm font-semibold text-text-primary">Global Rankings</h3>
          <p className="mt-2 text-xs text-text-secondary">
            Compare your bots against all players
          </p>
        </div>
        <div className="rounded-lg border border-border bg-surface/30 p-6 text-center">
          <TrendingUp className="mx-auto h-10 w-10 text-primary mb-3" />
          <h3 className="font-mono text-sm font-semibold text-text-primary">ELO Ratings</h3>
          <p className="mt-2 text-xs text-text-secondary">
            Skill-based matchmaking and rankings
          </p>
        </div>
        <div className="rounded-lg border border-border bg-surface/30 p-6 text-center">
          <Trophy className="mx-auto h-10 w-10 text-blue-500 mb-3" />
          <h3 className="font-mono text-sm font-semibold text-text-primary">Per-Game Stats</h3>
          <p className="mt-2 text-xs text-text-secondary">
            Separate rankings for Chess and Connect4
          </p>
        </div>
      </div>

      {/* Placeholder for future leaderboard */}
      <div className="rounded-lg border border-border bg-surface/30 p-6">
        <div className="flex items-center gap-2 mb-4">
          <Trophy className="h-5 w-5 text-primary" />
          <h2 className="font-mono text-lg font-semibold text-text-primary">Preview</h2>
        </div>
        <div className="space-y-2 font-mono text-xs text-text-muted">
          <div className="flex justify-between p-2 border-b border-border">
            <span>rank</span>
            <span>bot</span>
            <span>wins</span>
            <span>losses</span>
            <span>win_rate</span>
            <span>elo</span>
          </div>
          <div className="flex justify-between p-2 text-surface-muted">
            <span>1</span>
            <span>...</span>
            <span>...</span>
            <span>...</span>
            <span>...</span>
            <span>...</span>
          </div>
          <div className="flex justify-between p-2 text-surface-muted">
            <span>2</span>
            <span>...</span>
            <span>...</span>
            <span>...</span>
            <span>...</span>
            <span>...</span>
          </div>
          <div className="flex justify-between p-2 text-surface-muted">
            <span>3</span>
            <span>...</span>
            <span>...</span>
            <span>...</span>
            <span>...</span>
            <span>...</span>
          </div>
        </div>
      </div>
    </div>
  );
}

