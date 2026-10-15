import {
  useMutation,
  useQueryClient,
} from "@tanstack/react-query";

import type {
  JobData,
} from "../interfaces/JobData";

import { api } from "../services/api";

type CreateJobRequest =
  Omit<JobData, "code">;

async function createJob(
  job: CreateJobRequest
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
      await queryClient.invalidateQueries({
        queryKey: ["managed-jobs"],
      });

      await queryClient.invalidateQueries({
        queryKey: ["jobs"],
      });
    },
  });
}