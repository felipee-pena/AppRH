import {
  useMutation,
  useQueryClient,
} from "@tanstack/react-query";

import type {
  JobData,
  JobRequestData,
} from "../interfaces/JobData";

import { api } from "../services/api";

async function createJob(
  job: JobRequestData
): Promise<JobData> {
  const response =
    await api.post<JobData>(
      "/api/management/jobs",
      job
    );

  return response.data;
}

export function useCreateJob() {
  const queryClient =
    useQueryClient();

  return useMutation({
    mutationFn: createJob,

    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({
          queryKey: ["jobs"],
        }),

        queryClient.invalidateQueries({
          queryKey: ["managed-jobs"],
        }),
      ]);
    },
  });
}