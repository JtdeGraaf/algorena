import { Button } from '@/components/ui/button';
import { Loader2, StopCircle } from 'lucide-react';
import type { MatchDto } from '@/api/generated';
import { useAbortMatch } from '../../useMatches';

interface MatchActionsProps {
  match: MatchDto;
  onClose: () => void;
}

/**
 * Displays match actions like abort button.
 */
export function MatchActions({ match, onClose }: MatchActionsProps) {
  const abortMatch = useAbortMatch();

  const canAbort = match.status === 'CREATED' || match.status === 'IN_PROGRESS';

  const handleAbort = async () => {
    if (match.id === undefined) return;
    try {
      await abortMatch.mutateAsync(match.id);
      onClose();
    } catch (error) {
      console.error('Failed to abort match:', error);
    }
  };

  if (!canAbort) return null;

  return (
    <div className="flex justify-end border-t border-border pt-4">
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
  );
}
