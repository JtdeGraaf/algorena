import { useMutation } from '@tanstack/react-query';
import { updateCurrentUserProfile } from '@/api/generated';
import type { UpdateUserRequest } from '@/api/generated';

interface UseUpdateUserOptions {
  onSuccess?: () => void;
}

export function useUpdateUser(options?: UseUpdateUserOptions) {
  return useMutation({
    mutationFn: async (data: UpdateUserRequest) => {
      const response = await updateCurrentUserProfile({ body: data });
      if (response.error) {
        throw new Error(response.error.message || 'Failed to update profile');
      }
      return response.data;
    },
    onSuccess: () => {
      options?.onSuccess?.();
    },
  });
}
