import { useQuery } from '@tanstack/react-query';
import { getBotLeaderboard, getUserLeaderboard, getBotRanking, getBotRatingHistory } from '@/api/generated';
import type { BotDto } from '@/api/generated';

type Game = NonNullable<BotDto['game']>;

// Query key factory
export const leaderboardKeys = {
  all: ['leaderboard'] as const,
  bots: (game: Game, page: number, size: number) => [...leaderboardKeys.all, 'bots', game, page, size] as const,
  botRanking: (botId: number, game: Game) => [...leaderboardKeys.all, 'botRanking', botId, game] as const,
  users: (game: Game, page: number, size: number) => [...leaderboardKeys.all, 'users', game, page, size] as const,
  ratingHistory: (botId: number, game: Game, limit: number) => [...leaderboardKeys.all, 'ratingHistory', botId, game, limit] as const,
};

export function useBotLeaderboard(game: Game, page = 0, size = 50) {
  return useQuery({
    queryKey: leaderboardKeys.bots(game, page, size),
    queryFn: async () => {
      const response = await getBotLeaderboard({
        query: { game, page, size }
      });
      if (response.error) {
        throw new Error(response.error.message || 'Failed to fetch bot leaderboard');
      }
      return response.data;
    },
    staleTime: 60000, // 1 minute
  });
}

export function useUserLeaderboard(game: Game, page = 0, size = 50) {
  return useQuery({
    queryKey: leaderboardKeys.users(game, page, size),
    queryFn: async () => {
      const response = await getUserLeaderboard({
        query: { game, page, size }
      });
      if (response.error) {
        throw new Error(response.error.message || 'Failed to fetch user leaderboard');
      }
      return response.data;
    },
    staleTime: 60000,
  });
}

export function useBotRanking(botId: number, game: Game) {
  return useQuery({
    queryKey: leaderboardKeys.botRanking(botId, game),
    queryFn: async () => {
      const response = await getBotRanking({
        path: { botId },
        query: { game }
      });
      if (response.error) {
        throw new Error(response.error.message || 'Failed to fetch bot ranking');
      }
      return response.data;
    },
  });
}

export function useBotRatingHistory(botId: number, game: Game, limit = 20) {
  return useQuery({
    queryKey: leaderboardKeys.ratingHistory(botId, game, limit),
    queryFn: async () => {
      const response = await getBotRatingHistory({
        path: { botId },
        query: { game, limit }
      });
      if (response.error) {
        throw new Error(response.error.message || 'Failed to fetch rating history');
      }
      return response.data;
    },
  });
}
