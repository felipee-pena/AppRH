import {
  useCallback,
  useState,
} from "react";

import axios from "axios";

import {
  confirmEmail,
  resendEmailConfirmation,
} from "../services/auth-service";

import type {
  ApiErrorResponse,
} from "../interfaces/ApiErrorResponse";

export type ConfirmationStatus =
  | "idle"
  | "loading"
  | "success"
  | "invalid"
  | "expired"
  | "already-used"
  | "error";

export function useEmailConfirmation() {
  const [status, setStatus] =
    useState<ConfirmationStatus>(
      "idle"
    );

  const [message, setMessage] =
    useState("");

  const confirm =
    useCallback(
      async (
        token: string
      ) => {
        setStatus(
          "loading"
        );

        setMessage(
          "Confirmando seu endereço de e-mail..."
        );

        try {
          const response =
            await confirmEmail({
              token,
            });

          setStatus(
            "success"
          );

          setMessage(
            response.message
          );
        } catch (error) {
          if (
            !axios.isAxiosError<ApiErrorResponse>(
              error
            )
          ) {
            setStatus(
              "error"
            );

            setMessage(
              "Não foi possível confirmar o e-mail. "
              + "Tente novamente mais tarde."
            );

            return;
          }

          const responseMessage =
            error.response?.data?.message
            ?? "Não foi possível confirmar o e-mail.";

          const normalizedMessage =
            responseMessage.toLowerCase();

          if (
            normalizedMessage.includes(
              "expirado"
            )
          ) {
            setStatus(
              "expired"
            );

            setMessage(
              responseMessage
            );

            return;
          }

          if (
            normalizedMessage.includes(
              "já utilizado"
            )
            || normalizedMessage.includes(
              "já foi confirmado"
            )
            || normalizedMessage.includes(
              "já confirmado"
            )
          ) {
            setStatus(
              "already-used"
            );

            setMessage(
              responseMessage
            );

            return;
          }

          if (
            error.response?.status === 400
          ) {
            setStatus(
              "invalid"
            );

            setMessage(
              responseMessage
            );

            return;
          }

          setStatus(
            "error"
          );

          setMessage(
            responseMessage
          );
        }
      },
      []
    );

  const resend =
    useCallback(
      async (
        login: string
      ) => {
        setStatus(
          "loading"
        );

        setMessage(
          "Solicitando um novo e-mail..."
        );

        try {
          const response =
            await resendEmailConfirmation({
              login:
                login
                  .trim()
                  .toLowerCase(),
            });

          setStatus(
            "success"
          );

          setMessage(
            response.message
          );
        } catch (error) {
          if (
            axios.isAxiosError<ApiErrorResponse>(
              error
            )
          ) {
            setStatus(
              "error"
            );

            setMessage(
              error.response?.data?.message
              ?? "Não foi possível reenviar o e-mail."
            );

            return;
          }

          setStatus(
            "error"
          );

          setMessage(
            "Não foi possível reenviar o e-mail."
          );
        }
      },
      []
    );

  return {
    status,
    message,
    confirm,
    resend,
  };
}