import { useState, useCallback } from 'react';
import type { GamePosition } from '../components/games/types';

export interface UseReplayControlsProps<T extends GamePosition> {
  positions: T[];
}

export interface UseReplayControlsReturn<T extends GamePosition> {
  currentMoveIndex: number;
  setCurrentMoveIndex: (index: number | ((prev: number) => number)) => void;
  currentPosition: T;
  isPlaying: boolean;
  setIsPlaying: (playing: boolean | ((prev: boolean) => boolean)) => void;
  playSpeed: number;
  setPlaySpeed: (speed: number) => void;
  goToStart: () => void;
  goToEnd: () => void;
  goToPrevious: () => void;
  goToNext: () => void;
  togglePlay: () => void;
}

/**
 * Hook for managing replay state and controls.
 * Provides navigation functions and playback state for game replays.
 *
 * @param positions - Array of game positions (index 0 = initial, index i+1 = after move i)
 * @returns Replay control state and navigation functions
 */
export function useReplayControls<T extends GamePosition>({
  positions
}: UseReplayControlsProps<T>): UseReplayControlsReturn<T> {
  const [currentMoveIndex, setCurrentMoveIndex] = useState(-1); // -1 = initial position
  const [isPlaying, setIsPlaying] = useState(false);
  const [playSpeed, setPlaySpeed] = useState(1000); // ms per move

  // Current position (positions array is shifted by 1: positions[0] = initial)
  const currentPosition = positions[currentMoveIndex + 1] || positions[0];

  const goToStart = useCallback(() => {
    setCurrentMoveIndex(-1);
    setIsPlaying(false);
  }, []);

  const goToEnd = useCallback(() => {
    setCurrentMoveIndex(positions.length - 2);
    setIsPlaying(false);
  }, [positions.length]);

  const goToPrevious = useCallback(() => {
    setCurrentMoveIndex(prev => Math.max(-1, prev - 1));
  }, []);

  const goToNext = useCallback(() => {
    setCurrentMoveIndex(prev => Math.min(positions.length - 2, prev + 1));
  }, [positions.length]);

  const togglePlay = useCallback(() => {
    if (currentMoveIndex >= positions.length - 2) {
      // If at end, restart from beginning
      setCurrentMoveIndex(-1);
    }
    setIsPlaying(prev => !prev);
  }, [currentMoveIndex, positions.length]);

  return {
    currentMoveIndex,
    setCurrentMoveIndex,
    currentPosition,
    isPlaying,
    setIsPlaying,
    playSpeed,
    setPlaySpeed,
    goToStart,
    goToEnd,
    goToPrevious,
    goToNext,
    togglePlay
  };
}
