import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Terminal } from 'lucide-react';

export function NotFoundPage() {
  const { t } = useTranslation();

  return (
    <div className="flex flex-col items-center justify-center py-16 text-center">
      <Terminal className="h-16 w-16 text-surface-muted mb-4" />
      <h1 className="font-mono text-6xl font-bold text-primary">404</h1>
      <h2 className="mt-4 font-mono text-xl font-semibold text-text-primary">
        $ command not found
      </h2>
      <p className="mt-2 font-mono text-sm text-text-muted">
        # The page you're looking for doesn't exist
      </p>
      <Link to="/">
        <Button className="mt-6 gap-2 font-mono">
          cd ~/ {t('common.back')} home
        </Button>
      </Link>
    </div>
  );
}

