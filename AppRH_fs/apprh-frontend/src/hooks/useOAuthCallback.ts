import {
  useMutation,
} from "@tanstack/react-query";

import type {
  LoginResponse,
} from "../interfaces/LoginResponse";

import {
  exchangeOAuthCode,
} from "../services/auth-service";

import {
  useAuth,
} from "./useAuth";

export function useOAuthCallback() {
  const auth =
    useAuth();

  return useMutation<
    LoginResponse,
    Error,
    string
  >({
    mutationFn:
      async (
        code: string
      ) => {
        const normalizedCode =
          code.trim();

        if (!normalizedCode) {
          throw new Error(
            "O código OAuth não foi informado."
          );
        }

        return exchangeOAuthCode(
          normalizedCode
        );
      },

    onSuccess:
      response => {
        auth.processAuthenticationResponse(
          response
        );
      },
  });
}