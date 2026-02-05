import { useTranslation } from 'react-i18next';
import { useAuth } from '@/features/auth/AuthContext';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { User } from 'lucide-react';

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
      <div>
        <h1 className="text-2xl font-bold">{t('nav.profile')}</h1>
        <p className="text-text-secondary">Manage your account settings</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Account Information</CardTitle>
          <CardDescription>Your profile details</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div>
            <label className="text-sm font-medium text-text-secondary">Username</label>
            <p className="text-lg">{user?.username || '—'}</p>
          </div>
          <div>
            <label className="text-sm font-medium text-text-secondary">Name</label>
            <p className="text-lg">{user?.name || '—'}</p>
          </div>
          <div>
            <label className="text-sm font-medium text-text-secondary">User ID</label>
            <p className="font-mono text-sm text-text-muted">{user?.id}</p>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Actions</CardTitle>
        </CardHeader>
        <CardContent>
          <Button variant="destructive" onClick={logout}>
            {t('nav.logout')}
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}

