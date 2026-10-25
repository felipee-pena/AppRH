import {
  useMutation,
} from "@tanstack/react-query";

import {
  removeAccessToken,
} from "../services/token-storage";

async function logout(): Promise<void> {
  removeAccessToken();
}

export function useLogout() {
  return useMutation({
    mutationFn:
      logout,
  });
}