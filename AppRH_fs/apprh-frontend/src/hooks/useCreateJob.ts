import { useMutation, useQueryClient } from "@tanstack/react-query";
import type { JobData } from "../interfaces/JobData";
import { api } from "../services/api";

async function createJob(data: JobData): Promise<JobData> {
  const response = await api.post<JobData>("/api/jobs", data);
  return response.data;
}

export function useCreateJob() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: createJob,

    onSuccess: async () => {
      await queryClient.invalidateQueries({
        queryKey: ["jobs"],
      });
    },
  });
}