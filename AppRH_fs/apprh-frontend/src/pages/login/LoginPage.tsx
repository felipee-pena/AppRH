import {
  useState,
  type FormEvent,
} from "react";

import {
  Navigate,
  useLocation,
  useNavigate,
} from "react-router-dom";

import { useAuth } from "../../hooks/useAuth";

import "./login-page.css";

interface LocationState {
  from?: string;
  registrationSuccess?: boolean;
}

export function LoginPage() {
  const [login, setLogin] =
    useState("");

  const [password, setPassword] =
    useState("");

  const [showPassword, setShowPassword] =
    useState(false);

  const [errorMessage, setErrorMessage] =
    useState("");

  const [isSubmitting, setIsSubmitting] =
    useState(false);

  const auth = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const locationState =
    location.state as LocationState | null;

  /*
   * Usuário autenticado não deve permanecer
   * na página de login.
   *
   * Todos os hooks foram executados antes
   * deste retorno condicional.
   */
  if (
    auth.isAuthenticated &&
    !auth.isInitializing
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
      await auth.login({
        login: login.trim(),
        password,
      });

      navigate(
        locationState?.from ?? "/jobs",
        {
          replace: true,
        }
      );
    } catch {
      setErrorMessage(
        "Login ou senha inválidos. "
          + "Confira os dados e tente novamente."
      );
    } finally {
      setIsSubmitting(false);
    }
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
              <span>01</span>

              <div>
                <strong>Para candidatos</strong>

                <p>
                  Consulte oportunidades disponíveis.
                </p>
              </div>
            </article>

            <article>
              <span>02</span>

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
              <span>03</span>

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

            <h2>Acesse sua conta</h2>

            <p>
              Informe suas credenciais para continuar.
            </p>
          </header>

          {locationState?.registrationSuccess && (
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
                  onChange={(event) => {
                    setLogin(event.target.value);
                    setErrorMessage("");
                  }}
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
                  onChange={(event) => {
                    setPassword(event.target.value);
                    setErrorMessage("");
                  }}
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
                  onClick={() => {
                    setShowPassword(
                      (currentValue) =>
                        !currentValue
                    );
                  }}
                >
                  {showPassword
                    ? "Ocultar"
                    : "Mostrar"}
                </button>
              </div>
            </div>

            {errorMessage && (
              <div
                className="login-form__error"
                role="alert"
              >
                <span aria-hidden="true">
                  !
                </span>

                <p>{errorMessage}</p>
              </div>
            )}

            <button
              type="submit"
              className="login-form__submit"
              disabled={
                isSubmitting ||
                !login.trim() ||
                !password
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

          <footer className="login-card__footer">
            <span>
              Ainda não possui cadastro?
            </span>

            <button
              type="button"
              onClick={() => {
                navigate("/register");
              }}
            >
              Criar conta de candidato
            </button>
          </footer>
        </div>
      </section>

      <footer className="login-page__footer">
        <span>AppRH</span>

        <span>
          Gestão de vagas e candidaturas
        </span>
      </footer>
    </main>
  );
}