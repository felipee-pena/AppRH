import { useQuery } from "@tanstack/react-query";
import type { JobData } from "../interfaces/JobData";
import { api } from "../services/api";

async function fetchJobs(): Promise<JobData[]> {
  const response = await api.get<JobData[]>("/api/jobs");

  console.log("response.data:", response.data);
  console.log("É array?", Array.isArray(response.data));

  return response.data;
}

export function useJobs() {
  return useQuery<JobData[]>({
    queryKey: ["jobs"],
    queryFn: fetchJobs,
    retry: 2,
  });
}