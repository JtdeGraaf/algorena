import {
  createContext,
  useContext,
  useState,
  useCallback,
  useEffect,
  type ReactNode,
} from 'react';
import { useSearchParams } from 'react-router-dom';
import { setAccessToken, clearAccessToken, getAccessToken } from '@/api/client';
import { config } from '@/lib/config';

interface User {
  id: number;
  username: string;
  name: string;
}

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: () => void;
  logout: () => void;
  getToken: () => string | null;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  const login = useCallback(() => {
    window.location.href = config.oauth2LoginUrl;
  }, []);

  const logout = useCallback(() => {
    clearAccessToken();
    setUser(null);
  }, []);

  const getToken = useCallback(() => {
    return getAccessToken();
  }, []);

  const fetchUser = useCallback(async () => {
    const token = getAccessToken();
    console.log('[Auth] fetchUser called, token exists:', !!token);
    if (!token) {
      setIsLoading(false);
      return;
    }

    try {
      const response = await fetch('/api/v1/users/me', {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      console.log('[Auth] /api/v1/users/me response status:', response.status);

      if (response.ok) {
        const userData = await response.json();
        console.log('[Auth] User data received:', userData);
        setUser(userData);
      } else {
        console.log('[Auth] Response not ok, clearing token');
        clearAccessToken();
        setUser(null);
      }
    } catch (err) {
      console.error('[Auth] Error fetching user:', err);
      clearAccessToken();
      setUser(null);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchUser();
  }, [fetchUser]);

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user,
        isLoading,
        login,
        logout,
        getToken,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}

export function OAuth2RedirectHandler() {
  const [searchParams] = useSearchParams();
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    const token = searchParams.get('token');
    const errorParam = searchParams.get('error');

    console.log('[OAuth2Redirect] token from URL:', token ? `${token.substring(0, 20)}...` : null);
    console.log('[OAuth2Redirect] error from URL:', errorParam);

    if (errorParam) {
      setError(errorParam);
      return;
    }

    if (token) {
      console.log('[OAuth2Redirect] Saving token to localStorage...');
      setAccessToken(token);

      // Verify token was saved
      const savedToken = getAccessToken();
      console.log('[OAuth2Redirect] Token saved successfully:', !!savedToken);
      console.log('[OAuth2Redirect] Saved token value:', savedToken ? `${savedToken.substring(0, 20)}...` : null);

      setSuccess(true);

      // Delay redirect to see the logs
      setTimeout(() => {
        window.location.href = '/';
      }, 1000);
    } else {
      setError('No token received');
    }
  }, [searchParams]);

  if (error) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="text-center">
          <h1 className="text-2xl font-bold text-red-500">Login Failed</h1>
          <p className="mt-2 text-zinc-400">{error}</p>
          <a href="/" className="mt-4 inline-block text-emerald-500 hover:underline">
            Go back home
          </a>
        </div>
      </div>
    );
  }

  if (success) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="text-center">
          <h1 className="text-2xl font-bold text-emerald-500">Login Successful!</h1>
          <p className="mt-2 text-zinc-400">Redirecting...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="flex min-h-screen items-center justify-center">
      <div className="text-center">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-emerald-500 border-t-transparent"></div>
        <p className="mt-4 text-zinc-400">Logging in...</p>
      </div>
    </div>
  );
}

