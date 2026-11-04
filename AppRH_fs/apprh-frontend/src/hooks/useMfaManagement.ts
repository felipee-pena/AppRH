import {
  useMutation,
  useQuery,
  useQueryClient,
} from "@tanstack/react-query";

import type {
  MessageResponse,
} from "../interfaces/MessageResponse";

import type {
  MfaDisableRequest,
} from "../interfaces/MfaDisableRequest";

import type {
  MfaSetupResponse,
} from "../interfaces/MfaSetupResponse";

import type {
  RecoveryCodesResponse,
} from "../interfaces/RecoveryCodesResponse";

import {
  beginMfaSetup,
  confirmMfaSetup,
  disableMfa,
  getMfaStatus,
  regenerateRecoveryCodes,
} from "../services/auth-service";

export const MFA_STATUS_QUERY_KEY = [
  "account",
  "mfa",
  "status",
] as const;

export function useMfaManagement() {
  const queryClient =
    useQueryClient();

  const statusQuery =
    useQuery({
      queryKey:
        MFA_STATUS_QUERY_KEY,

      queryFn:
        getMfaStatus,
    });

  const setupMutation =
    useMutation<
      MfaSetupResponse,
      Error,
      void
    >({
      mutationFn:
        beginMfaSetup,
    });

  const confirmMutation =
    useMutation<
      MessageResponse,
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

          return confirmMfaSetup(
            normalizedCode
          );
        },

      onSuccess:
        async () => {
          await queryClient
            .invalidateQueries({
              queryKey:
                MFA_STATUS_QUERY_KEY,
            });
        },
    });

  const regenerateMutation =
    useMutation<
      RecoveryCodesResponse,
      Error,
      void
    >({
      mutationFn:
        regenerateRecoveryCodes,
    });

  const disableMutation =
    useMutation<
      MessageResponse,
      Error,
      MfaDisableRequest
    >({
      mutationFn:
        async request => {
          const normalizedCode =
            request.code
              .trim()
              .replace(
                /\D/g,
                ""
              );

          if (
            !request.password.trim()
          ) {
            throw new Error(
              "Informe a senha atual."
            );
          }

          if (
            !/^\d{6}$/.test(
              normalizedCode
            )
          ) {
            throw new Error(
              "O código MFA deve possuir 6 dígitos."
            );
          }

          return disableMfa({
            password:
              request.password,
            code:
              normalizedCode,
          });
        },

      onSuccess:
        async () => {
          await queryClient
            .invalidateQueries({
              queryKey:
                MFA_STATUS_QUERY_KEY,
            });
        },
    });

  return {
    statusQuery,
    setupMutation,
    confirmMutation,
    regenerateMutation,
    disableMutation,
  };
}