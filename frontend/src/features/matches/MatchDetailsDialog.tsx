import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Select } from '@/components/ui/select';
import { Chessboard } from '@/components/Chessboard';
import { Connect4Board } from '@/components/Connect4Board';
import { MatchReplayDialog } from './MatchReplayDialog';
import { useMatchMoves, useAbortMatch, useMakeMove, useMatch, useLegalMoves } from './useMatches';
import { useBots } from '@/features/bots/useBots';
import {
  Loader2, Swords, Clock, CheckCircle, XCircle, PlayCircle,
  StopCircle, Play, Maximize2, Minimize2, Hand
} from 'lucide-react';
import type { MatchDto } from '@/api/generated';
import { cn } from '@/lib/utils';

interface MatchDetailsDialogProps {
  match: MatchDto | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function MatchDetailsDialog({ match: initialMatch, open, onOpenChange }: MatchDetailsDialogProps) {
  const { t } = useTranslation();
  const { data: freshMatch, refetch: refetchMatch } = useMatch(initialMatch?.id || '');
  const { data: moves, isLoading: movesLoading, refetch: refetchMoves } = useMatchMoves(initialMatch?.id || '');
  const { data: legalMoves, refetch: refetchLegalMoves } = useLegalMoves(initialMatch?.id || '');
  const abortMatch = useAbortMatch();
  const makeMoveAction = useMakeMove();
  const { data: botsPage } = useBots();

  const [replayOpen, setReplayOpen] = useState(false);
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [manualMoveMode, setManualMoveMode] = useState(false);
  const [selectedBotId, setSelectedBotId] = useState<number | null>(null);
  const [moveError, setMoveError] = useState<string | null>(null);

  // Use fresh match data if available, otherwise fall back to initial
  const match = freshMatch || initialMatch;

  if (!match) return null;

  const getStatusIcon = () => {
    switch (match.status) {
      case 'CREATED':
        return <Clock className="h-5 w-5 text-yellow-500" />;
      case 'IN_PROGRESS':
        return <PlayCircle className="h-5 w-5 text-blue-500" />;
      case 'FINISHED':
        return <CheckCircle className="h-5 w-5 text-emerald-500" />;
      case 'ABORTED':
        return <XCircle className="h-5 w-5 text-red-500" />;
      default:
        return null;
    }
  };

  const getStatusColor = () => {
    switch (match.status) {
      case 'CREATED':
        return 'bg-yellow-500/10 text-yellow-500';
      case 'IN_PROGRESS':
        return 'bg-blue-500/10 text-blue-500';
      case 'FINISHED':
        return 'bg-emerald-500/10 text-emerald-500';
      case 'ABORTED':
        return 'bg-red-500/10 text-red-500';
      default:
        return 'bg-zinc-500/10 text-zinc-500';
    }
  };

  const formatDate = (dateString?: string) => {
    if (!dateString) return '—';
    return new Date(dateString).toLocaleString();
  };

  const participants = match.participants || [];
  const player1 = participants.find(p => p.playerIndex === 0);
  const player2 = participants.find(p => p.playerIndex === 1);

  const canAbort = match.status === 'CREATED' || match.status === 'IN_PROGRESS';
  const canReplay = moves && moves.length > 0;
  const canManualMove = match.status === 'IN_PROGRESS';

  // Get user's bots that are in this match
  const myBots = botsPage?.content || [];
  const myBotsInMatch = myBots.filter(bot =>
    participants.some(p => p.botId === bot.id)
  );

  const handleAbort = async () => {
    if (!match.id) return;
    try {
      await abortMatch.mutateAsync(match.id);
      onOpenChange(false);
    } catch (error) {
      console.error('Failed to abort match:', error);
    }
  };

  const handleManualMove = async (from: string, to: string) => {
    if (!match.id || !selectedBotId) {
      setMoveError('Please select a bot first');
      return;
    }

    setMoveError(null);

    try {
      await makeMoveAction.mutateAsync({
        matchId: match.id,
        botId: selectedBotId,
        move: `${from}${to}`,
      });
      // Refetch to get updated state - match, moves, and legal moves
      await Promise.all([refetchMatch(), refetchMoves(), refetchLegalMoves()]);
    } catch (error) {
      setMoveError((error as Error).message);
    }
  };

  const toggleFullscreen = () => {
    setIsFullscreen(!isFullscreen);
  };

  return (
    <>
      <Dialog open={open} onOpenChange={onOpenChange}>
        <DialogContent className={cn(
          'transition-all duration-200',
          isFullscreen ? 'max-w-6xl h-[90vh]' : 'max-w-3xl'
        )}>
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

          <div className={cn(
            'space-y-6 py-4',
            isFullscreen && 'overflow-y-auto max-h-[calc(90vh-8rem)]'
          )}>
            {/* Status and info */}
            <div className="flex items-center justify-between">
              <div className={cn('flex items-center gap-2 rounded-full px-3 py-1 text-sm font-medium', getStatusColor())}>
                {getStatusIcon()}
                {t(`matches.status.${match.status?.toLowerCase() || 'created'}`)}
              </div>
              <div className="text-sm text-zinc-400">
                <span className="font-mono text-xs uppercase">{match.game}</span>
              </div>
            </div>

            {/* Main content - Game Board and info side by side */}
            <div className={cn(
              'flex gap-6',
              isFullscreen ? 'flex-row' : 'flex-col md:flex-row'
            )}>
              {/* Left: Game Board */}
              <div className="flex flex-col items-center gap-3">
                {match.game === 'CHESS' && match.state && 'fen' in match.state && (
                  <Chessboard
                    fen={match.state.fen as string}
                    size={isFullscreen ? 'xl' : 'lg'}
                    interactive={manualMoveMode && !!selectedBotId}
                    legalMoves={legalMoves}
                    onMove={handleManualMove}
                    onInvalidMove={(reason) => setMoveError(reason || 'Invalid move')}
                  />
                )}

                {match.game === 'CONNECT_FOUR' && match.state && 'board' in match.state && (
                  <Connect4Board
                    board={match.state.board as string}
                    size={isFullscreen ? 'xl' : 'lg'}
                    interactive={manualMoveMode && !!selectedBotId}
                    legalMoves={legalMoves}
                    onMove={(col) => handleManualMove('', col)} // From is empty for C4
                    onInvalidMove={(reason) => setMoveError(reason || 'Invalid move')}
                  />
                )}

                <div className="flex gap-2">
                  {canReplay && (
                    <Button
                      variant="outline"
                      size="sm"
                      className="gap-2"
                      onClick={() => setReplayOpen(true)}
                    >
                      <Play className="h-4 w-4" />
                      Replay
                    </Button>
                  )}
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={toggleFullscreen}
                  >
                    {isFullscreen ? <Minimize2 className="h-4 w-4" /> : <Maximize2 className="h-4 w-4" />}
                  </Button>
                </div>

                {/* Manual move controls */}
                {canManualMove && myBotsInMatch.length > 0 && (
                  <div className="w-full space-y-2 rounded-lg border border-zinc-800 bg-zinc-900/50 p-3">
                    <div className="flex items-center gap-2">
                      <Hand className="h-4 w-4 text-amber-500" />
                      <span className="text-sm font-medium">Manual Move</span>
                    </div>

                    <div className="space-y-2">
                      <div className="flex items-center gap-2">
                        <Label htmlFor="bot-select" className="text-xs whitespace-nowrap">Play as:</Label>
                        <Select
                          id="bot-select"
                          value={selectedBotId?.toString() || ''}
                          onChange={(e: React.ChangeEvent<HTMLSelectElement>) => {
                            setSelectedBotId(e.target.value ? parseInt(e.target.value) : null);
                            setManualMoveMode(!!e.target.value);
                          }}
                          className="flex-1"
                        >
                          <option value="">Select bot...</option>
                          {myBotsInMatch.map(bot => {
                            const participant = participants.find(p => p.botId === bot.id);
                            return (
                              <option key={bot.id} value={bot.id?.toString()}>
                                {bot.name} ({participant?.playerIndex === 0 
                                  ? (match.game === 'CONNECT_FOUR' ? 'Player 1' : 'White') 
                                  : (match.game === 'CONNECT_FOUR' ? 'Player 2' : 'Black')})
                              </option>
                            );
                          })}
                        </Select>
                      </div>

                      {manualMoveMode && (
                        <p className="text-xs text-zinc-500">
                          {match.game === 'CHESS' 
                            ? 'Click a piece, then click the target square to move' 
                            : 'Click a column to drop a piece'}
                        </p>
                      )}

                      {moveError && (
                        <p className="text-xs text-red-400">{moveError}</p>
                      )}

                      {makeMoveAction.isPending && (
                        <div className="flex items-center gap-2 text-xs text-zinc-400">
                          <Loader2 className="h-3 w-3 animate-spin" />
                          Making move...
                        </div>
                      )}
                    </div>
                  </div>
                )}
              </div>

              {/* Right: Match info */}
              <div className="flex-1 space-y-4">
                {/* Participants */}
                <div className="grid grid-cols-2 gap-3">
                  <div className="rounded-lg border border-zinc-800 bg-zinc-900/50 p-3">
                    <div className="text-xs text-zinc-500">
                        {match.game === 'CONNECT_FOUR' ? 'Player 1 (Red)' : 'Player 1 (White)'}
                    </div>
                    <div className="mt-1 font-medium">{player1?.botName || 'Unknown'}</div>
                    {match.status === 'FINISHED' && (
                      <div className="mt-1 text-xl font-bold text-emerald-500">{player1?.score ?? 0}</div>
                    )}
                  </div>
                  <div className="rounded-lg border border-zinc-800 bg-zinc-900/50 p-3">
                    <div className="text-xs text-zinc-500">
                        {match.game === 'CONNECT_FOUR' ? 'Player 2 (Yellow)' : 'Player 2 (Black)'}
                    </div>
                    <div className="mt-1 font-medium">{player2?.botName || 'Unknown'}</div>
                    {match.status === 'FINISHED' && (
                      <div className="mt-1 text-xl font-bold text-emerald-500">{player2?.score ?? 0}</div>
                    )}
                  </div>
                </div>

                {/* Timestamps */}
                <div className="text-sm">
                  <div className="flex justify-between">
                    <span className="text-zinc-500">Started:</span>
                    <span>{formatDate(match.startedAt)}</span>
                  </div>
                  {match.finishedAt && (
                    <div className="flex justify-between mt-1">
                      <span className="text-zinc-500">Finished:</span>
                      <span>{formatDate(match.finishedAt)}</span>
                    </div>
                  )}
                </div>

                {/* FEN (Chess only) */}
                {match.game === 'CHESS' && match.state && 'fen' in match.state && (
                  <div className="text-sm">
                    <span className="font-medium text-zinc-400">FEN:</span>
                    <code className="ml-2 block mt-1 break-all rounded bg-zinc-900 px-2 py-1 text-xs text-zinc-300">
                      {match.state.fen as string}
                    </code>
                  </div>
                )}

                {/* Moves list */}
                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <h4 className="font-medium">{t('matches.moves')} ({moves?.length || 0})</h4>
                    {canReplay && (
                      <Button
                        variant="ghost"
                        size="sm"
                        className="gap-2 text-xs"
                        onClick={() => setReplayOpen(true)}
                      >
                        <Play className="h-3 w-3" />
                        Open Replay
                      </Button>
                    )}
                  </div>
                  {movesLoading ? (
                    <div className="flex justify-center py-4">
                      <Loader2 className="h-5 w-5 animate-spin text-zinc-500" />
                    </div>
                  ) : moves && moves.length > 0 ? (
                    <div className={cn(
                      'overflow-y-auto rounded-lg bg-zinc-900 p-3',
                      isFullscreen ? 'max-h-48' : 'max-h-32'
                    )}>
                      <div className="flex flex-wrap gap-1 text-sm font-mono">
                        {moves.map((move, index) => {
                          const moveNum = Math.floor(index / 2) + 1;
                          const isWhite = move.playerIndex === 0;
                          return (
                            <span key={move.id || index} className="text-zinc-300">
                              {isWhite && <span className="text-zinc-500">{moveNum}.</span>}
                              {move.moveNotation}
                            </span>
                          );
                        })}
                      </div>
                    </div>
                  ) : (
                    <p className="text-sm text-zinc-500">No moves yet</p>
                  )}
                </div>

                {/* PGN (only in fullscreen or if short) */}
                {match.game === 'CHESS' && match.state && 'pgn' in match.state && (isFullscreen || (match.state.pgn as string).length < 200) && (
                  <div className="space-y-2">
                    <h4 className="text-sm font-medium">PGN</h4>
                    <pre className="overflow-x-auto whitespace-pre-wrap rounded-lg bg-zinc-900 p-3 text-xs text-zinc-300 max-h-24">
                      {match.state.pgn as string}
                    </pre>
                  </div>
                )}
              </div>
            </div>

            {/* Actions */}
            {canAbort && (
              <div className="flex justify-end border-t border-zinc-800 pt-4">
                <Button
                  variant="destructive"
                  onClick={handleAbort}
                  disabled={abortMatch.isPending}
                  className="gap-2"
                >
                  {abortMatch.isPending ? (
                    <Loader2 className="h-4 w-4 animate-spin" />
                  ) : (
                    <StopCircle className="h-4 w-4" />
                  )}
                  Abort Match
                </Button>
              </div>
            )}
          </div>
        </DialogContent>
      </Dialog>

      <MatchReplayDialog
        match={match}
        open={replayOpen}
        onOpenChange={setReplayOpen}
      />
    </>
  );
}
