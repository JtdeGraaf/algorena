import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider, OAuth2RedirectHandler } from '@/features/auth/AuthContext';
import { Layout } from '@/components/Layout';
import { HomePage } from '@/routes/HomePage';
import { BotsPage } from '@/routes/BotsPage';
import { MatchesPage } from '@/routes/MatchesPage';
import { LeaderboardPage } from '@/routes/LeaderboardPage';
import { DocsPage } from '@/routes/DocsPage';
import { ProfilePage } from '@/routes/ProfilePage';
import { NotFoundPage } from '@/routes/NotFoundPage';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 60,
      retry: 1,
    },
  },
});

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <AuthProvider>
          <Routes>
            <Route path="/oauth2/redirect" element={<OAuth2RedirectHandler />} />
            <Route path="/" element={<Layout />}>
              <Route index element={<HomePage />} />
              <Route path="bots" element={<BotsPage />} />
              <Route path="matches" element={<MatchesPage />} />
              <Route path="leaderboard" element={<LeaderboardPage />} />
              <Route path="docs" element={<DocsPage />} />
              <Route path="profile" element={<ProfilePage />} />
              <Route path="*" element={<NotFoundPage />} />
            </Route>
          </Routes>
        </AuthProvider>
      </BrowserRouter>
    </QueryClientProvider>
  );
}

export default App;

