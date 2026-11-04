import {
  useEffect,
  useRef,
  useState,
} from "react";

import axios from "axios";

import {
  useNavigate,
  useSearchParams,
} from "react-router-dom";

import {
  useOAuthCallback,
} from "../../hooks/useOAuthCallback";

import type {
  ApiErrorResponse,
} from "../../interfaces/ApiErrorResponse";

import "./oauth-callback-page.css";

function extractErrorMessage(
  error: unknown
): string {
  if (
    axios.isAxiosError<ApiErrorResponse>(
      error
    )
  ) {
    return (
      error.response?.data?.message
      ?? "Não foi possível concluir "
      + "o login social."
    );
  }

  if (error instanceof Error) {
    return error.message;
  }

  return (
    "Não foi possível concluir "
    + "o login social."
  );
}

export function OAuthCallbackPage() {
  const [
    searchParameters,
  ] =
    useSearchParams();

  const navigate =
    useNavigate();

  const oauthCallback =
    useOAuthCallback();

  const processed =
    useRef(false);

  const code =
    searchParameters.get(
      "code"
    );

  const [
    requestErrorMessage,
    setRequestErrorMessage,
  ] =
    useState("");

  const missingCodeMessage =
    code
      ? ""
      : "O código OAuth não foi informado.";

  const errorMessage =
    requestErrorMessage
    || missingCodeMessage;

  const mutateOAuth =
    oauthCallback.mutate;

  useEffect(
    () => {
      if (
        processed.current
        || !code
      ) {
        return;
      }

      processed.current =
        true;

      window.history.replaceState(
        {},
        document.title,
        "/oauth/callback"
      );

      mutateOAuth(
        code,
        {
          onSuccess:
            response => {
              if (
                response.mfa_required
                && response.challenge_token
              ) {
                navigate(
                  "/mfa/verify",
                  {
                    replace: true,
                  }
                );

                return;
              }

              navigate(
                "/jobs",
                {
                  replace: true,
                }
              );
            },

          onError:
            error => {
              setRequestErrorMessage(
                extractErrorMessage(
                  error
                )
              );
            },
        }
      );
    },
    [
      code,
      mutateOAuth,
      navigate,
    ]
  );

  function handleReturnToLogin() {
    navigate(
      "/login",
      {
        replace: true,
      }
    );
  }

  return (
    <main className="oauth-callback-page">
      <section className="oauth-callback-card">
        <span className="oauth-callback-card__brand">
          AppRH
        </span>

        {errorMessage ? (
          <>
            <h1>
              Não foi possível concluir o acesso
            </h1>

            <p role="alert">
              {errorMessage}
            </p>

            <button
              type="button"
              onClick={
                handleReturnToLogin
              }
            >
              Voltar ao login
            </button>
          </>
        ) : (
          <>
            <div
              className="oauth-callback-card__loader"
              aria-hidden="true"
            />

            <h1>
              Concluindo autenticação
            </h1>

            <p>
              Aguarde enquanto validamos
              o acesso ao AppRH.
            </p>
          </>
        )}
      </section>
    </main>
  );
}