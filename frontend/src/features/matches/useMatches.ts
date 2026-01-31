import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getMatches, getRecentMatches, getMatch, getMatchMoves, createMatch, abortMatch, getBots } from '@/api/generated';
import type { CreateMatchRequest } from '@/api/generated';

export const matchKeys = {
  all: ['matches'] as const,
  lists: () => [...matchKeys.all, 'list'] as const,
  list: (filters: Record<string, unknown>) => [...matchKeys.lists(), filters] as const,
  recent: () => [...matchKeys.all, 'recent'] as const,
  details: () => [...matchKeys.all, 'detail'] as const,
  detail: (id: string) => [...matchKeys.details(), id] as const,
  moves: (id: string) => [...matchKeys.all, 'moves', id] as const,
};

export const botKeys = {
  all: ['bots'] as const,
  lists: () => [...botKeys.all, 'list'] as const,
  list: (filters: Record<string, unknown>) => [...botKeys.lists(), filters] as const,
};

export function useMatches(botId?: number) {
  return useQuery({
    queryKey: matchKeys.list({ botId }),
    queryFn: async () => {
      const response = await getMatches({ query: { botId } });
      if (response.error) {
        throw new Error(response.error.message || 'Failed to fetch matches');
      }
      return response.data;
    },
  });
}

export function useRecentMatches() {
  return useQuery({
    queryKey: matchKeys.recent(),
    queryFn: async () => {
      const response = await getRecentMatches();
      if (response.error) {
        throw new Error(response.error.message || 'Failed to fetch recent matches');
      }
      return response.data;
    },
  });
}

export function useMatch(matchId: string) {
  return useQuery({
    queryKey: matchKeys.detail(matchId),
    queryFn: async () => {
      const response = await getMatch({ path: { matchId } });
      if (response.error) {
        throw new Error(response.error.message || 'Failed to fetch match');
      }
      return response.data;
    },
    enabled: !!matchId,
  });
}

export function useMatchMoves(matchId: string) {
  return useQuery({
    queryKey: matchKeys.moves(matchId),
    queryFn: async () => {
      const response = await getMatchMoves({ path: { matchId } });
      if (response.error) {
        throw new Error(response.error.message || 'Failed to fetch match moves');
      }
      return response.data;
    },
    enabled: !!matchId,
  });
}

export function useCreateMatch() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (data: CreateMatchRequest) => {
      const response = await createMatch({ body: data });
      if (response.error) {
        throw new Error(response.error.message || 'Failed to create match');
      }
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: matchKeys.lists() });
      queryClient.invalidateQueries({ queryKey: matchKeys.recent() });
    },
  });
}

export function useAbortMatch() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (matchId: string) => {
      const response = await abortMatch({ path: { matchId } });
      if (response.error) {
        throw new Error(response.error.message || 'Failed to abort match');
      }
      return response.data;
    },
    onSuccess: (_, matchId) => {
      queryClient.invalidateQueries({ queryKey: matchKeys.lists() });
      queryClient.invalidateQueries({ queryKey: matchKeys.recent() });
      queryClient.invalidateQueries({ queryKey: matchKeys.detail(matchId) });
    },
  });
}

// Hook to get all bots (for opponent selection)
export function useAllBots(filters?: { game?: 'CHESS'; name?: string; active?: boolean }) {
  return useQuery({
    queryKey: botKeys.list(filters || {}),
    queryFn: async () => {
      const response = await getBots({
        query: {
          ...filters,
          active: true, // Only show active bots
          size: 100, // Get more bots for selection
        }
      });
      if (response.error) {
        throw new Error(response.error.message || 'Failed to fetch bots');
      }
      return response.data;
    },
  });
}

// Hook to get user's own bots
export function useMyBots(userId?: number) {
  return useQuery({
    queryKey: botKeys.list({ userId, own: true }),
    queryFn: async () => {
      const response = await getBots({
        query: {
          userId,
          active: true,
        }
      });
      if (response.error) {
        throw new Error(response.error.message || 'Failed to fetch bots');
      }
      return response.data;
    },
    enabled: !!userId,
  });
}

