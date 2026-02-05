import { Button } from '@/components/ui/button';
import {
  ChevronLeft,
  ChevronRight,
  ChevronsLeft,
  ChevronsRight,
  Play,
  Pause,
  RotateCcw
} from 'lucide-react';
import { cn } from '@/lib/utils';

interface ReplayControlsProps {
  currentMoveIndex: number;
  totalMoves: number;
  isPlaying: boolean;
  playSpeed: number;
  onPlaySpeedChange: (speed: number) => void;
  goToStart: () => void;
  goToPrevious: () => void;
  togglePlay: () => void;
  goToNext: () => void;
  goToEnd: () => void;
}

/**
 * Playback controls for match replay.
 */
export function ReplayControls({
  currentMoveIndex,
  totalMoves,
  isPlaying,
  playSpeed,
  onPlaySpeedChange,
  goToStart,
  goToPrevious,
  togglePlay,
  goToNext,
  goToEnd
}: ReplayControlsProps) {
  const moveNumber = currentMoveIndex + 1;

  return (
    <div className="flex items-center justify-between border-t border-border pt-4">
      {/* Playback controls */}
      <div className="flex items-center gap-2">
        <Button
          variant="outline"
          size="icon"
          onClick={goToStart}
          disabled={currentMoveIndex <= -1}
          title="Go to start (Home)"
        >
          <ChevronsLeft className="h-4 w-4" />
        </Button>
        <Button
          variant="outline"
          size="icon"
          onClick={goToPrevious}
          disabled={currentMoveIndex <= -1}
          title="Previous move (←)"
        >
          <ChevronLeft className="h-4 w-4" />
        </Button>
        <Button
          variant={isPlaying ? 'default' : 'outline'}
          size="icon"
          onClick={togglePlay}
          title={isPlaying ? 'Pause (Space)' : 'Play (Space)'}
        >
          {isPlaying ? <Pause className="h-4 w-4" /> : <Play className="h-4 w-4" />}
        </Button>
        <Button
          variant="outline"
          size="icon"
          onClick={goToNext}
          disabled={currentMoveIndex >= totalMoves - 1}
          title="Next move (→)"
        >
          <ChevronRight className="h-4 w-4" />
        </Button>
        <Button
          variant="outline"
          size="icon"
          onClick={goToEnd}
          disabled={currentMoveIndex >= totalMoves - 1}
          title="Go to end (End)"
        >
          <ChevronsRight className="h-4 w-4" />
        </Button>
        <Button
          variant="ghost"
          size="icon"
          onClick={goToStart}
          title="Reset"
        >
          <RotateCcw className="h-4 w-4" />
        </Button>
      </div>

      {/* Progress indicator and speed control */}
      <div className="flex items-center gap-4">
        <div className="text-sm text-text-secondary">
          Move {moveNumber} of {totalMoves}
        </div>

        <div className="flex items-center gap-2">
          <span className="text-xs text-text-muted">Speed:</span>
          {[2000, 1000, 500, 250].map(speed => (
            <button
              key={speed}
              onClick={() => onPlaySpeedChange(speed)}
              className={cn(
                'rounded px-2 py-0.5 text-xs transition-colors',
                playSpeed === speed
                  ? 'bg-primary text-white'
                  : 'bg-surface-elevated text-text-secondary hover:bg-surface-hover'
              )}
            >
              {speed === 2000 ? '0.5x' : speed === 1000 ? '1x' : speed === 500 ? '2x' : '4x'}
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}
