import { useMutation } from "@tanstack/react-query";
import { api } from "../services/api";

async function logout(): promise<void> {
  await api.post("/api/logout");
}

export function useLogout() {
  return useMutation({
   mutationFn: logout,
  });
}
