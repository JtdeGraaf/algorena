export const config = {
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL || '',
  oauth2LoginUrl: '/oauth2/authorization/google',
} as const;

