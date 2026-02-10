import {useMutation, useQuery, useQueryClient} from '@tanstack/react-query';
import type {CreateMatchRequest} from '@/api/generated';
import {abortMatch, createMatch, getBots, getMatch, getMatches, getMatchMoves, getRecentMatches} from '@/api/generated';
import {getAccessToken} from '@/api/client';

export const matchKeys = {
    all: ['matches'] as const,
    lists: () => [...matchKeys.all, 'list'] as const,
    list: (filters: Record<string, unknown>) => [...matchKeys.lists(), filters] as const,
    recent: () => [...matchKeys.all, 'recent'] as const,
    details: () => [...matchKeys.all, 'detail'] as const,
    detail: (id: number) => [...matchKeys.details(), id] as const,
    moves: (id: number) => [...matchKeys.all, 'moves', id] as const,
    legalMoves: (id: number) => [...matchKeys.all, 'legal-moves', id] as const,
};

export const botKeys = {
    all: ['bots'] as const,
    lists: () => [...botKeys.all, 'list'] as const,
    list: (filters: Record<string, unknown>) => [...botKeys.lists(), filters] as const,
};

export function useMatches(botId?: number) {
    return useQuery({
        queryKey: matchKeys.list({botId}),
        queryFn: async () => {
            const response = await getMatches({query: {botId}});
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

export function useMatch(matchId: number) {
    return useQuery({
        queryKey: matchKeys.detail(matchId),
        queryFn: async () => {
            const response = await getMatch({path: {matchId}});
            if (response.error) {
                throw new Error(response.error.message || 'Failed to fetch match');
            }
            return response.data;
        },
        enabled: !!matchId,
    });
}

export function useMatchMoves(matchId: number) {
    return useQuery({
        queryKey: matchKeys.moves(matchId),
        queryFn: async () => {
            const response = await getMatchMoves({path: {matchId}});
            if (response.error) {
                throw new Error(response.error.message || 'Failed to fetch match moves');
            }
            return response.data;
        },
        enabled: !!matchId,
    });
}

export function useLegalMoves(matchId: number) {
    return useQuery({
        queryKey: matchKeys.legalMoves(matchId),
        queryFn: async () => {
            // Manual fetch because client generation is skipped or doesn't support this endpoint yet
            const token = getAccessToken();
            const headers: HeadersInit = {
                'Content-Type': 'application/json',
            };
            if (token) {
                headers['Authorization'] = `Bearer ${token}`;
            }

            const res = await fetch(`/api/v1/matches/${matchId}/legal-moves`, {headers});
            if (!res.ok) {
                throw new Error('Failed to fetch legal moves');
            }
            return res.json() as Promise<string[]>;
        },
        enabled: !!matchId,
    });
}

export function useCreateMatch() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async (data: CreateMatchRequest) => {
            const response = await createMatch({body: data});
            if (response.error) {
                throw new Error(response.error.message || 'Failed to create match');
            }
            return response.data;
        },
        onSuccess: () => {
            queryClient.invalidateQueries({queryKey: matchKeys.lists()});
            queryClient.invalidateQueries({queryKey: matchKeys.recent()});
        },
    });
}

export function useAbortMatch() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async (matchId: number) => {
            const response = await abortMatch({path: {matchId}});
            if (response.error) {
                throw new Error(response.error.message || 'Failed to abort match');
            }
            return response.data;
        },
        onSuccess: (_, matchId) => {
            queryClient.invalidateQueries({queryKey: matchKeys.lists()});
            queryClient.invalidateQueries({queryKey: matchKeys.recent()});
            queryClient.invalidateQueries({queryKey: matchKeys.detail(matchId)});
        },
    });
}

// Hook to get all bots (for opponent selection)
export function useAllBots(filters?: { game?: 'CHESS' | 'CONNECT_FOUR'; name?: string; active?: boolean }) {
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
        queryKey: botKeys.list({userId, own: true}),
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

