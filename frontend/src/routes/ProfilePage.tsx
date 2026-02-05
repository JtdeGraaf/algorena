import { useTranslation } from 'react-i18next';
import { useAuth } from '@/features/auth/AuthContext';
import { Button } from '@/components/ui/button';
import { User, LogOut, Shield } from 'lucide-react';

export function ProfilePage() {
  const { t } = useTranslation();
  const { user, isAuthenticated, login, logout } = useAuth();

  if (!isAuthenticated) {
    return (
      <div className="flex flex-col items-center justify-center py-16 text-center">
        <User className="h-16 w-16 text-surface-muted" />
        <h2 className="mt-4 text-xl font-semibold">{t('errors.unauthorized')}</h2>
        <p className="mt-2 text-text-secondary">Login to view your profile</p>
        <Button onClick={login} className="mt-6">
          {t('nav.login')}
        </Button>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-2xl space-y-6">
      {/* Header */}
      <div>
        <h1 className="font-mono text-2xl font-bold text-primary">$ whoami</h1>
        <p className="mt-1 font-mono text-sm text-text-muted"># Your account information</p>
      </div>

      {/* Account Info */}
      <div className="rounded-lg border border-border bg-surface/30 p-6 space-y-6">
        <div className="flex items-center gap-2">
          <User className="h-5 w-5 text-primary" />
          <h2 className="font-mono text-lg font-semibold text-text-primary">Account Details</h2>
        </div>

        <div className="space-y-4 font-mono text-sm">
          <div className="flex flex-col gap-1">
            <span className="text-text-muted">username:</span>
            <span className="text-text-primary text-base">{user?.username || '—'}</span>
          </div>
          <div className="flex flex-col gap-1">
            <span className="text-text-muted">name:</span>
            <span className="text-text-primary text-base">{user?.name || '—'}</span>
          </div>
          <div className="flex flex-col gap-1">
            <span className="text-text-muted">user_id:</span>
            <span className="text-text-secondary text-xs">{user?.id}</span>
          </div>
        </div>
      </div>

      {/* Authentication Info */}
      <div className="rounded-lg border border-border bg-surface/30 p-6 space-y-4">
        <div className="flex items-center gap-2">
          <Shield className="h-5 w-5 text-primary" />
          <h2 className="font-mono text-lg font-semibold text-text-primary">Authentication</h2>
        </div>

        <div className="space-y-2 font-mono text-sm">
          <div className="flex items-center gap-2">
            <span className="text-text-muted">provider:</span>
            <span className="rounded bg-surface-elevated px-2 py-0.5 text-xs text-primary">Google OAuth2</span>
          </div>
          <p className="text-xs text-text-secondary">
            # Your account is secured via Google authentication
          </p>
        </div>
      </div>

      {/* Actions */}
      <div className="rounded-lg border border-border bg-surface/30 p-6 space-y-4">
        <h2 className="font-mono text-lg font-semibold text-text-primary">Actions</h2>
        <div className="flex gap-2">
          <Button
            variant="destructive"
            onClick={logout}
            className="gap-2 font-mono"
          >
            <LogOut className="h-4 w-4" />
            logout
          </Button>
        </div>
      </div>
    </div>
  );
}

