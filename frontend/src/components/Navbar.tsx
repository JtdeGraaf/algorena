import { Link, useLocation } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth } from '@/features/auth/AuthContext';
import { Button } from '@/components/ui/button';
import { Bot, Swords, Trophy, BookOpen, User, LogOut } from 'lucide-react';
import { cn } from '@/lib/utils';

export function Navbar() {
  const { t } = useTranslation();
  const { user, isAuthenticated, login, logout } = useAuth();
  const location = useLocation();

  const navLinks = [
    { to: '/', label: t('nav.home'), icon: null },
    { to: '/bots', label: t('nav.bots'), icon: Bot },
    { to: '/matches', label: t('nav.matches'), icon: Swords },
    { to: '/leaderboard', label: t('nav.leaderboard'), icon: Trophy },
    { to: '/docs', label: t('nav.docs'), icon: BookOpen },
  ];

  return (
    <header className="sticky top-0 z-50 border-b border-zinc-800 bg-zinc-950/80 backdrop-blur-sm">
      <div className="container mx-auto flex h-14 items-center justify-between px-4">
        <div className="flex items-center gap-6">
          <Link to="/" className="flex items-center gap-2">
            <span className="text-xl font-bold text-emerald-500">⚔</span>
            <span className="font-mono text-lg font-bold text-zinc-100">Algorena</span>
          </Link>

          <nav className="hidden items-center gap-1 md:flex">
            {navLinks.map((link) => (
              <Link
                key={link.to}
                to={link.to}
                className={cn(
                  'flex items-center gap-1.5 rounded-md px-3 py-1.5 text-sm font-medium transition-colors',
                  location.pathname === link.to
                    ? 'bg-zinc-800 text-emerald-500'
                    : 'text-zinc-400 hover:bg-zinc-800 hover:text-zinc-100'
                )}
              >
                {link.icon && <link.icon className="h-4 w-4" />}
                {link.label}
              </Link>
            ))}
          </nav>
        </div>

        <div className="flex items-center gap-2">
          {isAuthenticated ? (
            <>
              <Link to="/profile">
                <Button variant="ghost" size="sm" className="gap-1.5">
                  <User className="h-4 w-4" />
                  <span className="hidden sm:inline">{user?.username || user?.name}</span>
                </Button>
              </Link>
              <Button variant="ghost" size="icon" onClick={logout} title={t('nav.logout')}>
                <LogOut className="h-4 w-4" />
              </Button>
            </>
          ) : (
            <Button onClick={login} size="sm">
              {t('nav.login')}
            </Button>
          )}
        </div>
      </div>
    </header>
  );
}

