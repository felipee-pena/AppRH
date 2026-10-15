import {
  useMutation,
  useQueryClient,
} from "@tanstack/react-query";

import { api } from "../services/api";

async function deleteJob(
  code: number
): Promise<void> {
  await api.delete(
    `/api/management/jobs/${code}`
  );
}

export function useDeleteJob() {
  const queryClient =
    useQueryClient();

  return useMutation({
    mutationFn: deleteJob,

    onSuccess: async () => {
      await queryClient.invalidateQueries({
        queryKey: ["managed-jobs"],
      });

      await queryClient.invalidateQueries({
        queryKey: ["jobs"],
      });
    },
  });
}