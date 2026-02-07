import { useMutation } from '@tanstack/react-query';
import { updateCurrentUserProfile } from '@/api/generated';
import type { UpdateUserRequest } from '@/api/generated';

export function useUpdateUser() {
  return useMutation({
    mutationFn: async (data: UpdateUserRequest) => {
      const response = await updateCurrentUserProfile({ body: data });
      if (response.error) {
        throw new Error(response.error.message || 'Failed to update profile');
      }
      return response.data;
    },
    onSuccess: () => {
      // Reload the page to fetch updated user data
      // This ensures the AuthContext gets the latest user info
      window.location.reload();
    },
  });
}
