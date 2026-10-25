import {
  useState,
} from "react";

import {
  Link,
  useLocation,
} from "react-router-dom";

import {
  useEmailConfirmation,
} from "../hooks/useEmailConfirmation";

import {
  FeedbackMessage,
} from "../components/FeedbackMessage";

interface LocationState {
  login?: string;
}

export function EmailVerificationPendingPage() {
  const location =
    useLocation();

  const state =
    location.state as LocationState | null;

  const [login, setLogin] =
    useState(
      state?.login
      ?? ""
    );

  const [validationMessage, setValidationMessage] =
    useState("");

  const {
    status,
    message,
    resend,
  } =
    useEmailConfirmation();

  async function handleResend() {
    const normalizedLogin =
      login.trim();

    setValidationMessage("");

    if (!normalizedLogin) {
      setValidationMessage(
        "Informe o e-mail utilizado no cadastro."
      );

      return;
    }

    await resend(
      normalizedLogin
    );
  }

  return (
    <main className="login-page">
      <section className="login-page__content">
        <div className="login-card">
          <header className="login-card__header">
            <span className="login-card__eyebrow">
              Cadastro realizado
            </span>

            <h1>
              Confirme seu e-mail
            </h1>

            <p>
              Enviamos uma mensagem com o link
              de confirmação. Verifique também
              a pasta de spam.
            </p>
          </header>

          {!state?.login && (
            <div className="login-form__field">
              <label htmlFor="verification-login">
                E-mail
              </label>

              <div className="login-form__control">
                <span
                  className="login-form__control-icon"
                  aria-hidden="true"
                >
                  @
                </span>

                <input
                  id="verification-login"
                  type="email"
                  value={login}
                  disabled={
                    status === "loading"
                  }
                  onChange={
                    (event) => {
                      setLogin(
                        event.target.value
                      );

                      setValidationMessage("");
                    }
                  }
                  autoComplete="email"
                  placeholder="usuario@example.com"
                />
              </div>
            </div>
          )}

          {validationMessage && (
            <FeedbackMessage
              type="error"
              message={validationMessage}
            />
          )}

          {message && (
            <FeedbackMessage
              type={
                status === "success"
                  ? "success"
                  : status === "loading"
                    ? "info"
                    : "error"
              }
              message={message}
            />
          )}

          <button
            type="button"
            className="login-form__submit"
            disabled={
              status === "loading"
              || !login.trim()
            }
            onClick={
              () =>
                void handleResend()
            }
          >
            {status === "loading"
              ? "Enviando..."
              : "Reenviar e-mail"}
          </button>

          <footer className="login-card__footer">
            <Link to="/login">
              Voltar ao login
            </Link>
          </footer>
        </div>
      </section>
    </main>
  );
}