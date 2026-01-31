import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getBots, createBot, updateBot, deleteBot, getBotStats, getBotApiKeys, createApiKey, revokeApiKey } from '@/api/generated';
import type { CreateBotRequest, UpdateBotRequest, CreateApiKeyRequest } from '@/api/generated';

export const botKeys = {
  all: ['bots'] as const,
  lists: () => [...botKeys.all, 'list'] as const,
  list: (filters: Record<string, unknown>) => [...botKeys.lists(), filters] as const,
  details: () => [...botKeys.all, 'detail'] as const,
  detail: (id: number) => [...botKeys.details(), id] as const,
  stats: (id: number) => [...botKeys.all, 'stats', id] as const,
  apiKeys: (id: number) => [...botKeys.all, 'apiKeys', id] as const,
};

export function useBots() {
  return useQuery({
    queryKey: botKeys.lists(),
    queryFn: async () => {
      const response = await getBots();
      if (response.error) {
        throw new Error(response.error.message || 'Failed to fetch bots');
      }
      return response.data;
    },
  });
}

export function useBotStats(botId: number) {
  return useQuery({
    queryKey: botKeys.stats(botId),
    queryFn: async () => {
      const response = await getBotStats({ path: { botId } });
      if (response.error) {
        throw new Error(response.error.message || 'Failed to fetch bot stats');
      }
      return response.data;
    },
    enabled: !!botId,
  });
}

export function useBotApiKeys(botId: number) {
  return useQuery({
    queryKey: botKeys.apiKeys(botId),
    queryFn: async () => {
      const response = await getBotApiKeys({ path: { botId } });
      if (response.error) {
        throw new Error(response.error.message || 'Failed to fetch API keys');
      }
      return response.data;
    },
    enabled: !!botId,
  });
}

export function useCreateBot() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (data: CreateBotRequest) => {
      const response = await createBot({ body: data });
      if (response.error) {
        throw new Error(response.error.message || 'Failed to create bot');
      }
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: botKeys.lists() });
    },
  });
}

export function useUpdateBot() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ botId, data }: { botId: number; data: UpdateBotRequest }) => {
      const response = await updateBot({ path: { botId }, body: data });
      if (response.error) {
        throw new Error(response.error.message || 'Failed to update bot');
      }
      return response.data;
    },
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: botKeys.lists() });
      queryClient.invalidateQueries({ queryKey: botKeys.detail(variables.botId) });
    },
  });
}

export function useDeleteBot() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (botId: number) => {
      const response = await deleteBot({ path: { botId } });
      if (response.error) {
        throw new Error(response.error.message || 'Failed to delete bot');
      }
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: botKeys.lists() });
    },
  });
}

export function useCreateApiKey() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ botId, data }: { botId: number; data: CreateApiKeyRequest }) => {
      const response = await createApiKey({ path: { botId }, body: data });
      if (response.error) {
        throw new Error(response.error.message || 'Failed to create API key');
      }
      return response.data;
    },
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: botKeys.apiKeys(variables.botId) });
    },
  });
}

export function useRevokeApiKey() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ botId, apiKeyId }: { botId: number; apiKeyId: number }) => {
      const response = await revokeApiKey({ path: { botId, apiKeyId } });
      if (response.error) {
        throw new Error(response.error.message || 'Failed to revoke API key');
      }
      return response.data;
    },
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: botKeys.apiKeys(variables.botId) });
    },
  });
}

