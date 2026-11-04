import {
  useState,
  type FormEvent,
} from "react";

import {
  Navigate,
  useNavigate,
} from "react-router-dom";

import axios from "axios";

import {
  useAuth,
} from "../../hooks/useAuth";

import {
  getMfaChallenge,
  removeMfaChallenge,
} from "../../services/mfa-storage";

import type {
  ApiErrorResponse,
} from "../../interfaces/ApiErrorResponse";

import "./mfa-verify-page.css";

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
      ?? "Código MFA inválido."
    );
  }

  if (error instanceof Error) {
    return error.message;
  }

  return "Código MFA inválido.";
}

export function MfaVerifyPage() {
  const auth =
    useAuth();

  const navigate =
    useNavigate();

  const challengeToken =
    getMfaChallenge();

  const [code, setCode] =
    useState("");

  const [errorMessage, setErrorMessage] =
    useState("");

  const [isSubmitting, setIsSubmitting] =
    useState(false);

  if (
    auth.isAuthenticated
    && !auth.isInitializing
  ) {
    return (
      <Navigate
        to="/jobs"
        replace
      />
    );
  }

  if (!challengeToken) {
    return (
      <Navigate
        to="/login"
        replace
      />
    );
  }

  async function handleSubmit(
    event: FormEvent<HTMLFormElement>
  ) {
    event.preventDefault();

    if (!/^\d{6}$/.test(code)) {
      setErrorMessage(
        "Informe o código de 6 dígitos."
      );

      return;
    }

    try {
      setIsSubmitting(true);
      setErrorMessage("");

      await auth.completeMfa(
        code
      );

      navigate(
        "/jobs",
        {
          replace: true,
        }
      );
    } catch (error) {
      setErrorMessage(
        extractErrorMessage(
          error
        )
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  function handleCancel() {
    removeMfaChallenge();

    navigate(
      "/login",
      {
        replace: true,
      }
    );
  }

  return (
    <main className="mfa-page">
      <section className="mfa-card">
        <span className="mfa-card__brand">
          AppRH
        </span>

        <h1>
          Verificação em duas etapas
        </h1>

        <p>
          Informe o código atual exibido
          no aplicativo autenticador.
        </p>

        {errorMessage && (
          <div
            className="mfa-card__error"
            role="alert"
          >
            {errorMessage}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <label htmlFor="mfa-code">
            Código de segurança
          </label>

          <input
            id="mfa-code"
            name="mfa-code"
            inputMode="numeric"
            autoComplete="one-time-code"
            placeholder="000000"
            maxLength={6}
            value={code}
            disabled={isSubmitting}
            onChange={
              event => {
                setCode(
                  event.target.value
                    .replace(/\D/g, "")
                    .slice(0, 6)
                );

                setErrorMessage("");
              }
            }
          />

          <button
            type="submit"
            disabled={
              isSubmitting
              || code.length !== 6
            }
          >
            {isSubmitting
              ? "Validando..."
              : "Validar código"}
          </button>
        </form>

        <button
          type="button"
          className="mfa-card__cancel"
          onClick={handleCancel}
          disabled={isSubmitting}
        >
          Cancelar e voltar ao login
        </button>
      </section>
    </main>
  );
}