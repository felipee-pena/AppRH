import { useQuery } from "@tanstack/react-query";

import type { CandidateData } from "../interfaces/CandidateData";
import { api } from "../services/api";

async function fetchCandidates(): Promise<CandidateData[]> {
  const response = await api.get<CandidateData[]>(
    "/api/candidates"
  );

  return response.data;
}

export function useCandidates() {
  return useQuery({
    queryKey: ["candidates"],
    queryFn: fetchCandidates,
    retry: 1,
    enabled: false,
  });
}