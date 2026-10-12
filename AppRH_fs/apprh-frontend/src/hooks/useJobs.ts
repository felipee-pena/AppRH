import { useQuery } from "@tanstack/react-query";
import type { JobData } from "../interfaces/JobData";
import { api } from "../services/api";

async function fetchJobs(): Promise<JobData[]> {
  const response = await api.get<JobData[]>("/api/jobs");
  return response.data;
}

export function useJobs() {
  return useQuery({
    queryKey: ["jobs"],
    queryFn: fetchJobs,
    retry: 2,
  });
}