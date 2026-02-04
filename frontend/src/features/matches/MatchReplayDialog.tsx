import { useState, useMemo, useEffect, useCallback } from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Chessboard } from '@/components/Chessboard';
import { useMatchMoves } from './useMatches';
import {
  Loader2, Swords, ChevronLeft, ChevronRight,
  ChevronsLeft, ChevronsRight, Play, Pause, RotateCcw
} from 'lucide-react';
import type { MatchDto, MatchMoveDto } from '@/api/generated';
import { cn } from '@/lib/utils';
import { Chess } from 'chess.js';

interface MatchReplayDialogProps {
  match: MatchDto | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

const INITIAL_FEN = 'rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1';

export function MatchReplayDialog({ match, open, onOpenChange }: MatchReplayDialogProps) {
  const { data: moves, isLoading: movesLoading } = useMatchMoves(match?.id || '');

  const [currentMoveIndex, setCurrentMoveIndex] = useState(-1); // -1 = initial position
  const [isPlaying, setIsPlaying] = useState(false);
  const [playSpeed, setPlaySpeed] = useState(1000); // ms per move

  // Only support Chess replay for now
  const isChess = match?.game === 'CHESS';

  // Calculate all positions from moves
  const positions = useMemo(() => {
    if (!isChess || !moves || moves.length === 0) {
      return [{ fen: INITIAL_FEN, move: null as MatchMoveDto | null }];
    }

    const chess = new Chess();
    const result: { fen: string; move: MatchMoveDto | null }[] = [
      { fen: chess.fen(), move: null }
    ];

    for (const move of moves) {
      try {
        if (move.fromSquare && move.toSquare) {
          chess.move({
            from: move.fromSquare.toLowerCase(),
            to: move.toSquare.toLowerCase(),
            promotion: move.promotionPiece?.toLowerCase() as 'q' | 'r' | 'b' | 'n' | undefined,
          });
        } else if (move.moveNotation) {
          // Move notation might also need lowercasing for the square parts
          chess.move(move.moveNotation.toLowerCase());
        }
        result.push({ fen: chess.fen(), move });
      } catch (e) {
        console.error('Failed to apply move:', move, e);
      }
    }

    return result;
  }, [moves, isChess]);

  // Current position
  const currentPosition = positions[currentMoveIndex + 1] || positions[0];
  const currentMove = currentMoveIndex >= 0 ? moves?.[currentMoveIndex] : null;

  // Navigation functions
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

  // Auto-play effect
  useEffect(() => {
    if (!isPlaying) return;

    const interval = setInterval(() => {
      setCurrentMoveIndex(prev => {
        if (prev >= positions.length - 2) {
          setIsPlaying(false);
          return prev;
        }
        return prev + 1;
      });
    }, playSpeed);

    return () => clearInterval(interval);
  }, [isPlaying, positions.length, playSpeed]);

  // State is reset when dialog opens by using match?.id as key on DialogContent below

  // Keyboard navigation
  useEffect(() => {
    if (!open) return;

    const handleKeyDown = (e: KeyboardEvent) => {
      switch (e.key) {
        case 'ArrowLeft':
          e.preventDefault();
          goToPrevious();
          break;
        case 'ArrowRight':
          e.preventDefault();
          goToNext();
          break;
        case 'Home':
          e.preventDefault();
          goToStart();
          break;
        case 'End':
          e.preventDefault();
          goToEnd();
          break;
        case ' ':
          e.preventDefault();
          togglePlay();
          break;
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [open, goToPrevious, goToNext, goToStart, goToEnd, togglePlay]);

  if (!match) return null;

  const participants = match.participants || [];
  const player1 = participants.find(p => p.playerIndex === 0);
  const player2 = participants.find(p => p.playerIndex === 1);

  const totalMoves = moves?.length || 0;
  const moveNumber = currentMoveIndex + 1;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent key={`${open}-${match?.id}`} className="max-w-4xl">
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
            <Loader2 className="h-8 w-8 animate-spin text-zinc-500" />
          </div>
        ) : !isChess ? (
          <div className="flex flex-col items-center justify-center py-16 text-center">
            <Swords className="h-16 w-16 text-zinc-600" />
            <h3 className="mt-4 text-lg font-semibold">Replay Not Available</h3>
            <p className="mt-2 text-sm text-zinc-500">
              Match replay is currently only supported for Chess games.
            </p>
            <p className="text-sm text-zinc-500">
              Game type: {match.game}
            </p>
          </div>
        ) : (
          <div className="flex gap-6 py-4">
            {/* Chessboard */}
            <div className="flex flex-col items-center gap-4">
              <Chessboard
                fen={currentPosition.fen}
                size="xl"
                highlightSquares={
                  currentMove
                    ? { from: currentMove.fromSquare?.toLowerCase(), to: currentMove.toSquare?.toLowerCase() }
                    : undefined
                }
              />

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

              {/* Progress indicator */}
              <div className="text-sm text-zinc-400">
                Move {moveNumber} of {totalMoves}
              </div>

              {/* Speed control */}
              <div className="flex items-center gap-2 text-sm">
                <span className="text-zinc-500">Speed:</span>
                {[2000, 1000, 500, 250].map(speed => (
                  <button
                    key={speed}
                    onClick={() => setPlaySpeed(speed)}
                    className={cn(
                      'rounded px-2 py-0.5 text-xs transition-colors',
                      playSpeed === speed
                        ? 'bg-emerald-500 text-white'
                        : 'bg-zinc-800 text-zinc-400 hover:bg-zinc-700'
                    )}
                  >
                    {speed === 2000 ? '0.5x' : speed === 1000 ? '1x' : speed === 500 ? '2x' : '4x'}
                  </button>
                ))}
              </div>
            </div>

            {/* Move list */}
            <div className="flex-1 space-y-3">
              <h4 className="font-medium">Move History</h4>

              {/* Current position info */}
              <div className="rounded-lg bg-zinc-900 p-3 text-sm">
                <div className="text-zinc-400">
                  <span className="font-medium">FEN:</span>
                  <code className="ml-2 break-all text-xs text-zinc-300">{currentPosition.fen}</code>
                </div>
                {currentMove && (
                  <div className="mt-2 text-zinc-300">
                    <span className="font-medium text-zinc-400">Current move:</span>{' '}
                    <span className="font-mono">{currentMove.moveNotation}</span>
                    <span className="ml-2 text-zinc-500">
                      ({currentMove.fromSquare} → {currentMove.toSquare})
                    </span>
                  </div>
                )}
              </div>

              {/* Scrollable move list */}
              <div className="max-h-72 overflow-y-auto rounded-lg border border-zinc-800 bg-zinc-950 p-2">
                <div className="grid grid-cols-2 gap-1 text-sm">
                  {moves?.map((move, index) => {
                    const moveNum = Math.floor(index / 2) + 1;
                    const isWhite = move.playerIndex === 0;
                    const isCurrentMove = index === currentMoveIndex;

                    return (
                      <button
                        key={move.id || index}
                        onClick={() => setCurrentMoveIndex(index)}
                        className={cn(
                          'flex items-center gap-2 rounded px-2 py-1 text-left transition-colors',
                          isCurrentMove
                            ? 'bg-emerald-500/20 text-emerald-400'
                            : 'hover:bg-zinc-800',
                          isWhite ? '' : ''
                        )}
                      >
                        <span className="w-6 text-zinc-500">{isWhite ? `${moveNum}.` : ''}</span>
                        <span className="font-mono">{move.moveNotation}</span>
                        <span className="ml-auto text-xs text-zinc-600">
                          {isWhite ? player1?.botName?.slice(0, 8) : player2?.botName?.slice(0, 8)}
                        </span>
                      </button>
                    );
                  })}
                </div>
              </div>

              {/* Match result */}
              {match.status === 'FINISHED' && (
                <div className="rounded-lg border border-zinc-800 bg-zinc-900/50 p-3">
                  <div className="text-sm text-zinc-400">Result</div>
                  <div className="mt-1 flex items-center gap-4">
                    <div className={cn(
                      'font-medium',
                      (player1?.score ?? 0) > (player2?.score ?? 0) && 'text-emerald-500'
                    )}>
                      {player1?.botName}: {player1?.score}
                    </div>
                    <span className="text-zinc-600">—</span>
                    <div className={cn(
                      'font-medium',
                      (player2?.score ?? 0) > (player1?.score ?? 0) && 'text-emerald-500'
                    )}>
                      {player2?.botName}: {player2?.score}
                    </div>
                  </div>
                </div>
              )}
            </div>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
}

