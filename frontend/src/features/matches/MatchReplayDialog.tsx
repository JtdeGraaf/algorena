import { useMemo } from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogClose } from '@/components/ui/dialog';
import { useMatchMoves } from './useMatches';
import { Loader2, Swords } from 'lucide-react';
import type { MatchDto } from '@/api/generated';
import { getGameComponents, getReplayEngine, isGameRegistered } from './components/games/registry';
import { ReplayControls } from './components/shared/ReplayControls';
import { useReplayControls } from './hooks/useReplayControls';
import { useReplayAutoplay } from './hooks/useReplayAutoplay';
import { useReplayKeyboard } from './hooks/useReplayKeyboard';

interface MatchReplayDialogProps {
  match: MatchDto | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function MatchReplayDialog({ match, open, onOpenChange }: MatchReplayDialogProps) {
  const { data: moves, isLoading: movesLoading } = useMatchMoves(match?.id || '');

  // Check if game type is registered
  const isRegistered = match ? isGameRegistered(match.game) : false;

  // Get game-specific components and engine
  const gameComponents = useMemo(() => {
    if (!match || !isRegistered) return null;
    try {
      const components = getGameComponents(match.game);
      const engine = getReplayEngine(match.game);
      return { components, engine };
    } catch {
      console.error('Failed to initialize replay for game:', match?.game);
      return null;
    }
  }, [match, isRegistered]);

  // Calculate positions using useMemo
  const positions = useMemo(() => {
    if (!gameComponents || !moves) return [{ state: null, move: null }];
    return gameComponents.engine.calculatePositions(moves);
  }, [gameComponents, moves]);

  // Shared replay controls
  const controls = useReplayControls({ positions });

  // Autoplay
  useReplayAutoplay({
    isPlaying: controls.isPlaying,
    currentMoveIndex: controls.currentMoveIndex,
    totalMoves: moves?.length || 0,
    playSpeed: controls.playSpeed,
    onNext: controls.goToNext,
    onStop: () => controls.setIsPlaying(false)
  });

  // Keyboard shortcuts
  useReplayKeyboard({
    enabled: open,
    onPrevious: controls.goToPrevious,
    onNext: controls.goToNext,
    onStart: controls.goToStart,
    onEnd: controls.goToEnd,
    onTogglePlay: controls.togglePlay
  });

  // Early return after hooks
  if (!match) return null;

  const participants = match.participants || [];
  const player1 = participants.find(p => p.playerIndex === 0);
  const player2 = participants.find(p => p.playerIndex === 1);

  const ReplayComponent = gameComponents?.components.ReplayComponent;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent key={`${open}-${match?.id}`} className="max-w-fit">
        <DialogClose onClose={() => onOpenChange(false)} />
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <Swords className="h-5 w-5 text-emerald-500" />
            Match Replay
          </DialogTitle>
          <DialogDescription>
            {player1?.botName} vs {player2?.botName} — Use arrow keys or controls to step through moves
          </DialogDescription>
        </DialogHeader>

        {movesLoading ? (
          <div className="flex justify-center py-16">
            <Loader2 className="h-8 w-8 animate-spin text-text-muted" />
          </div>
        ) : !isRegistered ? (
          <div className="flex flex-col items-center justify-center py-16 text-center">
            <Swords className="h-16 w-16 text-surface-muted" />
            <h3 className="mt-4 text-lg font-semibold">Replay Not Available</h3>
            <p className="mt-2 text-sm text-text-muted">
              Match replay is not supported for this game type.
            </p>
            <p className="text-sm text-text-muted">Game type: {match.game}</p>
          </div>
        ) : !moves || moves.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-16 text-center">
            <Swords className="h-16 w-16 text-surface-muted" />
            <h3 className="mt-4 text-lg font-semibold">No Moves Yet</h3>
            <p className="mt-2 text-sm text-text-muted">
              This match has no moves to replay.
            </p>
          </div>
        ) : ReplayComponent ? (
          <>
            {/* Game-specific replay component */}
            <ReplayComponent
              match={match}
              moves={moves}
              positions={positions}
              currentMoveIndex={controls.currentMoveIndex}
              onMoveSelect={(index: number) => controls.setCurrentMoveIndex(index)}
            />

            {/* Shared playback controls */}
            <ReplayControls
              currentMoveIndex={controls.currentMoveIndex}
              totalMoves={moves.length}
              isPlaying={controls.isPlaying}
              playSpeed={controls.playSpeed}
              onPlaySpeedChange={controls.setPlaySpeed}
              goToStart={controls.goToStart}
              goToPrevious={controls.goToPrevious}
              togglePlay={controls.togglePlay}
              goToNext={controls.goToNext}
              goToEnd={controls.goToEnd}
            />
          </>
        ) : (
          <div className="flex flex-col items-center justify-center py-16 text-center">
            <Swords className="h-16 w-16 text-surface-muted" />
            <h3 className="mt-4 text-lg font-semibold">Error Loading Replay</h3>
            <p className="mt-2 text-sm text-text-muted">
              Failed to load replay component for {match.game}
            </p>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
}
