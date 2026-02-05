import { useEffect } from 'react';

export interface UseReplayAutoplayProps {
  isPlaying: boolean;
  currentMoveIndex: number;
  totalMoves: number;
  playSpeed: number;
  onNext: () => void;
  onStop: () => void;
}

/**
 * Hook for automatic replay playback.
 * Advances to the next move at specified intervals when playing.
 *
 * @param isPlaying - Whether autoplay is active
 * @param currentMoveIndex - Current move index (-1 = initial position)
 * @param totalMoves - Total number of moves
 * @param playSpeed - Milliseconds between moves
 * @param onNext - Callback to advance to next move
 * @param onStop - Callback to stop playback when reaching the end
 */
export function useReplayAutoplay({
  isPlaying,
  currentMoveIndex,
  totalMoves,
  playSpeed,
  onNext,
  onStop
}: UseReplayAutoplayProps): void {
  useEffect(() => {
    if (!isPlaying) return;

    const interval = setInterval(() => {
      if (currentMoveIndex >= totalMoves - 1) {
        onStop();
      } else {
        onNext();
      }
    }, playSpeed);

    return () => clearInterval(interval);
  }, [isPlaying, currentMoveIndex, totalMoves, playSpeed, onNext, onStop]);
}
