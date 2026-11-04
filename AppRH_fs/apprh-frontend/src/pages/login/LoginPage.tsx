import {
  useMemo,
  useState,
  type FormEvent,
} from "react";

import axios from "axios";

import {
  FaGithub,
  FaGoogle,
} from "react-icons/fa";

import {
  Navigate,
  useLocation,
  useNavigate,
} from "react-router-dom";

import {
  useAuth,
} from "../../hooks/useAuth";

import type {
  ApiErrorResponse,
} from "../../interfaces/ApiErrorResponse";

import {
  startGitHubLogin,
  startGoogleLogin,
} from "../../services/auth-service";

import "./login-page.css";

interface LocationState {
  from?: string;
  registrationSuccess?: boolean;
}

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
      ?? "Login ou senha inválidos. "
      + "Confira os dados e tente novamente."
    );
  }

  if (error instanceof Error) {
    return error.message;
  }

  return (
    "Login ou senha inválidos. "
    + "Confira os dados e tente novamente."
  );
}

export function LoginPage() {
  const [login, setLogin] =
    useState("");

  const [password, setPassword] =
    useState("");

  const [
    showPassword,
    setShowPassword,
  ] = useState(false);

  const [
    errorMessage,
    setErrorMessage,
  ] = useState("");

  const [
    isSubmitting,
    setIsSubmitting,
  ] = useState(false);

  const auth =
    useAuth();

  const navigate =
    useNavigate();

  const location =
    useLocation();

  const locationState =
    location.state as
      | LocationState
      | null;

  const queryMessage =
    useMemo(
      () => {
        const parameters =
          new URLSearchParams(
            location.search
          );

        const oauthError =
          parameters.get(
            "oauthError"
          );

        if (oauthError) {
          return oauthError;
        }

        if (
          parameters.get(
            "sessionExpired"
          ) === "true"
        ) {
          return (
            "Sua sessão expirou. "
            + "Entre novamente."
          );
        }

        return "";
      },
      [
        location.search,
      ]
    );

  const displayedError =
    errorMessage
    || queryMessage;

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

  async function handleSubmit(
    event: FormEvent<HTMLFormElement>
  ) {
    event.preventDefault();

    setErrorMessage("");
    setIsSubmitting(true);

    try {
      const response =
        await auth.login({
          login:
            login.trim(),
          password,
        });

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
        locationState?.from
          ?? "/jobs",
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

  function handleGoogleLogin() {
    setErrorMessage("");
    startGoogleLogin();
  }

  function handleGitHubLogin() {
    setErrorMessage("");
    startGitHubLogin();
  }

  function handleRegistration() {
    navigate(
      "/register"
    );
  }

  return (
    <main className="login-page">
      <section className="login-page__content">
        <div className="login-presentation">
          <div className="login-brand">
            <span className="login-brand__symbol">
              RH
            </span>

            <span className="login-brand__name">
              AppRH
            </span>
          </div>

          <div className="login-presentation__content">
            <span className="login-presentation__eyebrow">
              Recrutamento inteligente
            </span>

            <h1>
              O ponto de encontro entre
              talentos e oportunidades.
            </h1>

            <p>
              Consulte vagas, organize processos
              seletivos e gerencie oportunidades
              conforme o seu perfil de acesso.
            </p>
          </div>

          <div className="login-presentation__features">
            <article>
              <span>
                01
              </span>

              <div>
                <strong>
                  Para candidatos
                </strong>

                <p>
                  Consulte oportunidades disponíveis.
                </p>
              </div>
            </article>

            <article>
              <span>
                02
              </span>

              <div>
                <strong>
                  Para profissionais de RH
                </strong>

                <p>
                  Crie e gerencie suas próprias vagas.
                </p>
              </div>
            </article>

            <article>
              <span>
                03
              </span>

              <div>
                <strong>
                  Para administradores
                </strong>

                <p>
                  Tenha visão completa da plataforma.
                </p>
              </div>
            </article>
          </div>
        </div>

        <div className="login-card">
          <header className="login-card__header">
            <span className="login-card__eyebrow">
              Bem-vindo
            </span>

            <h2>
              Acesse sua conta
            </h2>

            <p>
              Informe suas credenciais
              para continuar.
            </p>
          </header>

          {locationState
            ?.registrationSuccess && (
            <div
              className="login-form__success"
              role="status"
            >
              Conta criada com sucesso.
              Agora você já pode entrar.
            </div>
          )}

          <form
            className="login-form"
            onSubmit={handleSubmit}
            noValidate
          >
            <div className="login-form__field">
              <label htmlFor="login">
                Login
              </label>

              <div className="login-form__control">
                <span
                  className="login-form__control-icon"
                  aria-hidden="true"
                >
                  @
                </span>

                <input
                  id="login"
                  name="login"
                  type="text"
                  autoComplete="username"
                  placeholder="Digite seu login"
                  value={login}
                  disabled={isSubmitting}
                  onChange={
                    event => {
                      setLogin(
                        event.target.value
                      );

                      setErrorMessage("");
                    }
                  }
                  required
                />
              </div>
            </div>

            <div className="login-form__field">
              <label htmlFor="password">
                Senha
              </label>

              <div className="login-form__control">
                <span
                  className="login-form__control-icon"
                  aria-hidden="true"
                >
                  •
                </span>

                <input
                  id="password"
                  name="password"
                  type={
                    showPassword
                      ? "text"
                      : "password"
                  }
                  autoComplete="current-password"
                  placeholder="Digite sua senha"
                  value={password}
                  disabled={isSubmitting}
                  onChange={
                    event => {
                      setPassword(
                        event.target.value
                      );

                      setErrorMessage("");
                    }
                  }
                  required
                />

                <button
                  type="button"
                  className="login-form__password-toggle"
                  aria-label={
                    showPassword
                      ? "Ocultar senha"
                      : "Mostrar senha"
                  }
                  disabled={isSubmitting}
                  onClick={
                    () => {
                      setShowPassword(
                        currentValue =>
                          !currentValue
                      );
                    }
                  }
                >
                  {showPassword
                    ? "Ocultar"
                    : "Mostrar"}
                </button>
              </div>
            </div>

            {displayedError && (
              <div
                className="login-form__error"
                role="alert"
              >
                <span aria-hidden="true">
                  !
                </span>

                <p>
                  {displayedError}
                </p>
              </div>
            )}

            <button
              type="submit"
              className="login-form__submit"
              disabled={
                isSubmitting
                || !login.trim()
                || !password
              }
            >
              <span>
                {isSubmitting
                  ? "Autenticando..."
                  : "Entrar na plataforma"}
              </span>

              {!isSubmitting && (
                <span aria-hidden="true">
                  →
                </span>
              )}
            </button>
          </form>

          <div
            className="login-social"
            aria-label="Outras formas de acesso"
          >
            <div className="login-social__separator">
              <span>
                ou continue com
              </span>
            </div>

            <div className="login-social__buttons">
              <button
                type="button"
                className="login-social__button login-social__button--google"
                disabled={isSubmitting}
                onClick={handleGoogleLogin}
              >
                <FaGoogle
                  className="login-social__icon"
                  aria-hidden="true"
                />

                <span>
                  Google
                </span>
              </button>

              <button
                type="button"
                className="login-social__button login-social__button--github"
                disabled={isSubmitting}
                onClick={handleGitHubLogin}
              >
                <FaGithub
                  className="login-social__icon"
                  aria-hidden="true"
                />

                <span>
                  GitHub
                </span>
              </button>
            </div>
          </div>

          <footer className="login-card__footer">
            <span>
              Ainda não possui cadastro?
            </span>

            <button
              type="button"
              onClick={handleRegistration}
            >
              Criar conta de candidato
            </button>
          </footer>
        </div>
      </section>

      <footer className="login-page__footer">
        <span>
          AppRH
        </span>

        <span>
          Gestão de vagas e candidaturas
        </span>
      </footer>
    </main>
  );
}