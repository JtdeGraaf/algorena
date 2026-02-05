import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { Button } from '@/components/ui/button';

export function NotFoundPage() {
  const { t } = useTranslation();

  return (
    <div className="flex flex-col items-center justify-center py-16 text-center">
      <h1 className="font-mono text-6xl font-bold text-primary">404</h1>
      <h2 className="mt-4 text-xl font-semibold">{t('errors.notFound')}</h2>
      <p className="mt-2 text-text-secondary">The page you're looking for doesn't exist.</p>
      <Link to="/">
        <Button className="mt-6">{t('common.back')} Home</Button>
      </Link>
    </div>
  );
}

