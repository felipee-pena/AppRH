import {
  useMutation,
  useQueryClient,
} from "@tanstack/react-query";

import type { JobData } from "../interfaces/JobData";
import { api } from "../services/api";

interface UpdateJobVariables {
  code: number;
  job: JobData;
}

async function updateJob({
  code,
  job,
}: UpdateJobVariables): Promise<JobData> {
  const response = await api.put<JobData>(
    `/api/jobs/${code}`,
    job
  );

  return response.data;
}

export function useUpdateJob() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: updateJob,

    onSuccess: async (_, variables) => {
      await queryClient.invalidateQueries({
        queryKey: ["jobs"],
      });

      await queryClient.invalidateQueries({
        queryKey: ["jobs", variables.code],
      });
    },
  });
}