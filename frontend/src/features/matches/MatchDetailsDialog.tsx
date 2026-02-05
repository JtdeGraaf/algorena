import { useState } from 'react';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogClose } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { MatchReplayDialog } from './MatchReplayDialog';
import { useMatch, useMatchMoves } from './useMatches';
import { Loader2, Play, Swords } from 'lucide-react';
import type { MatchDto } from '@/api/generated';
import { getGameComponents, isGameRegistered } from './components/games/registry';
import type { GameDetailsProps } from './components/games/types';
import { MatchStatusInfo } from './components/shared/MatchStatusInfo';
import { MatchInfoPanel } from './components/shared/MatchInfoPanel';
import { MatchActions } from './components/shared/MatchActions';

interface MatchDetailsDialogProps {
  match: MatchDto | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function MatchDetailsDialog({ match: initialMatch, open, onOpenChange }: MatchDetailsDialogProps) {
  const { data: freshMatch } = useMatch(initialMatch?.id || '');
  const { data: moves, isLoading: movesLoading } = useMatchMoves(initialMatch?.id || '');

  const [replayOpen, setReplayOpen] = useState(false);

  // Use fresh match data if available, otherwise fall back to initial
  const match = freshMatch || initialMatch;

  if (!match) return null;

  const participants = match.participants || [];
  const player1 = participants.find(p => p.playerIndex === 0);
  const player2 = participants.find(p => p.playerIndex === 1);

  // Check if game type is registered for replay
  const canReplay = isGameRegistered(match.game) && moves && moves.length > 0;

  // Get game-specific component
  let DetailsComponent: React.ComponentType<GameDetailsProps> | null = null;
  try {
    const components = getGameComponents(match.game);
    DetailsComponent = components.DetailsComponent;
  } catch {
    console.error('Game type not registered:', match.game);
  }

  return (
    <>
      <Dialog open={open} onOpenChange={onOpenChange}>
        <DialogContent className="max-w-fit">
          <DialogClose onClose={() => onOpenChange(false)} />

          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <Swords className="h-5 w-5 text-emerald-500" />
              Match Details
            </DialogTitle>
            <DialogDescription>
              {player1?.botName} vs {player2?.botName}
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-6 py-4">
            {/* Status and info */}
            <MatchStatusInfo match={match} />

            {/* Main content - Game Board and info side by side */}
            <div className="flex flex-col gap-6 md:flex-row">
              {/* Left: Game Board */}
              <div className="flex flex-col items-center flex-1">
                {DetailsComponent ? (
                  <DetailsComponent match={match} moves={moves} isFullscreen={true} />
                ) : (
                  <div className="flex flex-col items-center justify-center p-8 text-center">
                    <Swords className="h-16 w-16 text-surface-muted" />
                    <p className="mt-4 text-sm text-text-muted">
                      Game board not available for {match.game}
                    </p>
                  </div>
                )}
              </div>

              {/* Right: Match info */}
              <MatchInfoPanel match={match} moves={moves} />
            </div>

            {/* Moves list */}
            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <h4 className="font-medium">Moves ({moves?.length || 0})</h4>
                {canReplay && (
                  <Button
                    variant="ghost"
                    size="sm"
                    className="gap-2"
                    onClick={() => setReplayOpen(true)}
                  >
                    <Play className="h-4 w-4" />
                    Replay
                  </Button>
                )}
              </div>
              {movesLoading ? (
                <div className="flex justify-center py-4">
                  <Loader2 className="h-5 w-5 animate-spin text-text-muted" />
                </div>
              ) : moves && moves.length > 0 ? (
                <div className="overflow-y-auto rounded-lg bg-surface p-3 max-h-32">
                  <div className="flex flex-wrap gap-1 text-sm font-mono">
                    {moves.map((move, index) => {
                      const moveNum = Math.floor(index / 2) + 1;
                      const isWhite = move.playerIndex === 0;
                      return (
                        <span key={move.id || index} className="text-text-primary">
                          {isWhite && <span className="text-text-muted">{moveNum}.</span>}
                          {move.moveNotation || `Col ${move.toSquare}`}
                        </span>
                      );
                    })}
                  </div>
                </div>
              ) : (
                <p className="text-sm text-text-muted">No moves yet</p>
              )}
            </div>

            {/* PGN (Chess only, if short) */}
            {match.game === 'CHESS' &&
              match.state &&
              'pgn' in match.state &&
              match.state.pgn &&
              (match.state.pgn as string).length < 200 && (
                <div className="space-y-2">
                  <h4 className="text-sm font-medium">PGN</h4>
                  <pre className="overflow-x-auto whitespace-pre-wrap rounded-lg bg-surface p-3 text-xs text-text-primary max-h-24">
                    {match.state.pgn as string}
                  </pre>
                </div>
              )}

            {/* Actions */}
            <MatchActions match={match} onClose={() => onOpenChange(false)} />
          </div>
        </DialogContent>
      </Dialog>

      <MatchReplayDialog match={match} open={replayOpen} onOpenChange={setReplayOpen} />
    </>
  );
}
