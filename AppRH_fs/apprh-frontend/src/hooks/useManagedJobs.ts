import {
  useQuery,
} from "@tanstack/react-query";

import type {
  JobData,
} from "../interfaces/JobData";

import { api } from "../services/api";

async function fetchManagedJobs():
  Promise<JobData[]> {
  const response =
    await api.get<JobData[]>(
      "/api/management/jobs"
    );

  return response.data;
}

export function useManagedJobs() {
  return useQuery({
    queryKey: ["managed-jobs"],
    queryFn: fetchManagedJobs,
  });
}