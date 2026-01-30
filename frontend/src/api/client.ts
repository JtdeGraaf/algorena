const TOKEN_STORAGE_KEY = 'algorena_access_token';

export function getAccessToken(): string | null {
  return localStorage.getItem(TOKEN_STORAGE_KEY);
}

export function setAccessToken(token: string): void {
  localStorage.setItem(TOKEN_STORAGE_KEY, token);
}

export function clearAccessToken(): void {
  localStorage.removeItem(TOKEN_STORAGE_KEY);
}

// Configure the API client with auth interceptor
import { client } from './generated/client.gen';

// Use empty baseUrl - Vite proxy handles the routing in dev,
// and in production, the frontend will be served from the same domain
client.setConfig({ baseUrl: '' });

// Set up auth interceptor
client.interceptors.request.use((request) => {
  const token = getAccessToken();
  if (token) {
    request.headers.set('Authorization', `Bearer ${token}`);
  }
  return request;
});

// Re-export client for direct use
export { client };
