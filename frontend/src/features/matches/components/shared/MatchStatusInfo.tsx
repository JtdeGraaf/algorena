import { useTranslation } from 'react-i18next';
import { CheckCircle, Clock, PlayCircle, XCircle } from 'lucide-react';
import { cn } from '@/lib/utils';
import type { MatchDto } from '@/api/generated';

interface MatchStatusInfoProps {
  match: MatchDto;
}

/**
 * Displays match status badge with icon and color coding.
 */
export function MatchStatusInfo({ match }: MatchStatusInfoProps) {
  const { t } = useTranslation();

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

  return (
    <div className="flex items-center justify-between">
      <div
        className={cn('flex items-center gap-2 rounded-full px-3 py-1 text-sm font-medium', getStatusColor())}
      >
        {getStatusIcon()}
        {t(`matches.status.${match.status?.toLowerCase() || 'created'}`)}
      </div>
      <div className="text-sm text-text-secondary">
        <span className="font-mono text-xs uppercase">{match.game}</span>
      </div>
    </div>
  );
}
