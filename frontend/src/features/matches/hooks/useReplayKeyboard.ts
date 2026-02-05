import { useEffect } from 'react';

export interface UseReplayKeyboardProps {
  enabled: boolean;
  onPrevious: () => void;
  onNext: () => void;
  onStart: () => void;
  onEnd: () => void;
  onTogglePlay: () => void;
}

/**
 * Hook for keyboard shortcuts in replay mode.
 * Supports:
 * - Arrow Left: Previous move
 * - Arrow Right: Next move
 * - Home: Go to start
 * - End: Go to end
 * - Space: Toggle play/pause
 *
 * @param enabled - Whether keyboard shortcuts are active
 * @param onPrevious - Callback for previous move
 * @param onNext - Callback for next move
 * @param onStart - Callback to go to start
 * @param onEnd - Callback to go to end
 * @param onTogglePlay - Callback to toggle playback
 */
export function useReplayKeyboard({
  enabled,
  onPrevious,
  onNext,
  onStart,
  onEnd,
  onTogglePlay
}: UseReplayKeyboardProps): void {
  useEffect(() => {
    if (!enabled) return;

    const handleKeyDown = (e: KeyboardEvent) => {
      switch (e.key) {
        case 'ArrowLeft':
          e.preventDefault();
          onPrevious();
          break;
        case 'ArrowRight':
          e.preventDefault();
          onNext();
          break;
        case 'Home':
          e.preventDefault();
          onStart();
          break;
        case 'End':
          e.preventDefault();
          onEnd();
          break;
        case ' ':
          e.preventDefault();
          onTogglePlay();
          break;
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [enabled, onPrevious, onNext, onStart, onEnd, onTogglePlay]);
}
