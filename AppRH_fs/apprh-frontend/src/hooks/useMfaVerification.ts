import {
  useMutation,
} from "@tanstack/react-query";

import type {
  LoginResponse,
} from "../interfaces/LoginResponse";

import {
  useAuth,
} from "./useAuth";

export function useMfaVerification() {
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
          code
            .trim()
            .replace(
              /\D/g,
              ""
            );

        if (
          !/^\d{6}$/.test(
            normalizedCode
          )
        ) {
          throw new Error(
            "O código MFA deve possuir 6 dígitos."
          );
        }

        return auth.completeMfa(
          normalizedCode
        );
      },
  });
}