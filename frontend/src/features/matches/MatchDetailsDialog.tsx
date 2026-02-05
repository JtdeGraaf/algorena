import { useState } from 'react';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { MatchReplayDialog } from './MatchReplayDialog';
import { useMatch, useMatchMoves } from './useMatches';
import { Loader2, Maximize2, Minimize2, Play, Swords } from 'lucide-react';
import type { MatchDto } from '@/api/generated';
import { cn } from '@/lib/utils';
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
  const [isFullscreen, setIsFullscreen] = useState(false);

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

  const toggleFullscreen = () => {
    setIsFullscreen(!isFullscreen);
  };

  return (
    <>
      <Dialog open={open} onOpenChange={onOpenChange}>
        <DialogContent
          className={cn(
            'transition-all duration-200',
            isFullscreen ? 'max-w-6xl h-[90vh]' : 'max-w-3xl'
          )}
        >
          <DialogHeader>
            <div className="flex items-center justify-between">
              <DialogTitle className="flex items-center gap-2">
                <Swords className="h-5 w-5 text-emerald-500" />
                Match Details
              </DialogTitle>
              <Button
                variant="ghost"
                size="icon"
                onClick={toggleFullscreen}
                title={isFullscreen ? 'Exit fullscreen' : 'Fullscreen'}
              >
                {isFullscreen ? <Minimize2 className="h-4 w-4" /> : <Maximize2 className="h-4 w-4" />}
              </Button>
            </div>
            <DialogDescription>
              {player1?.botName} vs {player2?.botName}
            </DialogDescription>
          </DialogHeader>

          <div
            className={cn(
              'space-y-6 py-4',
              isFullscreen && 'overflow-y-auto max-h-[calc(90vh-8rem)]'
            )}
          >
            {/* Status and info */}
            <MatchStatusInfo match={match} />

            {/* Main content - Game Board and info side by side */}
            <div
              className={cn(
                'flex gap-6',
                isFullscreen ? 'flex-row' : 'flex-col md:flex-row'
              )}
            >
              {/* Left: Game Board */}
              <div className="flex flex-col items-center">
                {DetailsComponent ? (
                  <DetailsComponent match={match} moves={moves} isFullscreen={isFullscreen} />
                ) : (
                  <div className="flex flex-col items-center justify-center p-8 text-center">
                    <Swords className="h-16 w-16 text-zinc-600" />
                    <p className="mt-4 text-sm text-zinc-500">
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
                  <Loader2 className="h-5 w-5 animate-spin text-zinc-500" />
                </div>
              ) : moves && moves.length > 0 ? (
                <div
                  className={cn(
                    'overflow-y-auto rounded-lg bg-zinc-900 p-3',
                    isFullscreen ? 'max-h-48' : 'max-h-32'
                  )}
                >
                  <div className="flex flex-wrap gap-1 text-sm font-mono">
                    {moves.map((move, index) => {
                      const moveNum = Math.floor(index / 2) + 1;
                      const isWhite = move.playerIndex === 0;
                      return (
                        <span key={move.id || index} className="text-zinc-300">
                          {isWhite && <span className="text-zinc-500">{moveNum}.</span>}
                          {move.moveNotation || `Col ${move.toSquare}`}
                        </span>
                      );
                    })}
                  </div>
                </div>
              ) : (
                <p className="text-sm text-zinc-500">No moves yet</p>
              )}
            </div>

            {/* PGN (Chess only, in fullscreen or if short) */}
            {match.game === 'CHESS' &&
              match.state &&
              'pgn' in match.state &&
              match.state.pgn &&
              (isFullscreen || (match.state.pgn as string).length < 200) && (
                <div className="space-y-2">
                  <h4 className="text-sm font-medium">PGN</h4>
                  <pre className="overflow-x-auto whitespace-pre-wrap rounded-lg bg-zinc-900 p-3 text-xs text-zinc-300 max-h-24">
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
