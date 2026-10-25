import {
  useEffect,
  useRef,
} from "react";

import {
  Link,
  useSearchParams,
} from "react-router-dom";

import {
  useEmailConfirmation,
} from "../hooks/useEmailConfirmation";

import {
  FeedbackMessage,
} from "../components/FeedbackMessage";

export function EmailConfirmationPage() {
  const [searchParams] =
    useSearchParams();

  const requestStarted =
    useRef(false);

  const {
    status,
    message,
    confirm,
  } =
    useEmailConfirmation();

  const token =
    searchParams
      .get("token")
      ?.trim();

  useEffect(
    () => {
      if (requestStarted.current) {
        return;
      }

      requestStarted.current =
        true;

      if (!token) {
        return;
      }

      void confirm(
        token
      );
    },
    [
      token,
      confirm,
    ]
  );

  if (!token) {
    return (
      <main className="login-page">
        <section className="login-page__content">
          <div className="login-card">
            <header className="login-card__header">
              <span className="login-card__eyebrow">
                Confirmação de e-mail
              </span>

              <h1>
                Link inválido
              </h1>
            </header>

            <FeedbackMessage
              type="error"
              message={
                "O link de confirmação não possui "
                + "um token válido."
              }
            />

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

  const confirmationSucceeded =
    status === "success"
    || status === "already-used";

  return (
    <main className="login-page">
      <section className="login-page__content">
        <div className="login-card">
          <header className="login-card__header">
            <span className="login-card__eyebrow">
              Segurança da conta
            </span>

            <h1>
              Confirmação de e-mail
            </h1>

            <p>
              Estamos validando o link enviado
              para seu endereço de e-mail.
            </p>
          </header>

          {status === "loading" && (
            <FeedbackMessage
              type="info"
              message={message}
            />
          )}

          {status !== "idle"
            && status !== "loading"
            && (
              <FeedbackMessage
                type={
                  confirmationSucceeded
                    ? "success"
                    : "error"
                }
                message={message}
              />
            )}

          <footer className="login-card__footer">
            {confirmationSucceeded && (
              <Link to="/login">
                Ir para o login
              </Link>
            )}

            {status === "expired" && (
              <Link to="/verificacao-pendente">
                Solicitar novo e-mail
              </Link>
            )}

            {status === "invalid" && (
              <Link to="/login">
                Voltar ao login
              </Link>
            )}

            {status === "error" && (
              <button
                type="button"
                onClick={
                  () =>
                    window.location.reload()
                }
              >
                Tentar novamente
              </button>
            )}
          </footer>
        </div>
      </section>
    </main>
  );
}