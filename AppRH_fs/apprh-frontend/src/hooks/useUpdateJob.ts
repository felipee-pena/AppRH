import {
  useMutation,
  useQueryClient,
} from "@tanstack/react-query";

import type {
  JobData,
  JobRequestData,
} from "../interfaces/JobData";

import { api } from "../services/api";

interface UpdateJobVariables {
  code: number;
  job: JobRequestData;
}

async function updateJob({
  code,
  job,
}: UpdateJobVariables):
  Promise<JobData> {
  const response =
    await api.put<JobData>(
      `/api/management/jobs/${code}`,
      job
    );

  return response.data;
}

export function useUpdateJob() {
  const queryClient =
    useQueryClient();

  return useMutation({
    mutationFn: updateJob,

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