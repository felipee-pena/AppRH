import {
  useState,
  type FormEvent,
} from "react";

import {
  Navigate,
  useNavigate,
} from "react-router-dom";

import type { AxiosError } from "axios";

import { useAuth } from "../../hooks/useAuth";
import { api } from "../../services/api";

import "../login/login-page.css";

interface RegisterRequest {
  login: string;
  password: string;
}

interface ApiErrorResponse {
  message?: string;
}

export function RegisterPage() {
  const [login, setLogin] =
    useState("");

  const [password, setPassword] =
    useState("");

  const [
    confirmPassword,
    setConfirmPassword,
  ] = useState("");

  const [errorMessage, setErrorMessage] =
    useState("");

  const [isSubmitting, setIsSubmitting] =
    useState(false);

  const auth = useAuth();
  const navigate = useNavigate();

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

    if (!login.trim()) {
      setErrorMessage(
        "Informe um login."
      );

      return;
    }

    if (password.length < 8) {
      setErrorMessage(
        "A senha deve possuir pelo menos 8 caracteres."
      );

      return;
    }

    if (password !== confirmPassword) {
      setErrorMessage(
        "A confirmação da senha não corresponde."
      );

      return;
    }

    setIsSubmitting(true);

    try {
      const payload: RegisterRequest = {
        login: login.trim(),
        password,
      };

      await api.post(
        "/auth/register",
        payload
      );

      navigate("/login", {
        replace: true,
        state: {
          registrationSuccess: true,
        },
      });
    } catch (error) {
      const axiosError =
        error as AxiosError<ApiErrorResponse>;

      if (
        axiosError.response?.status === 409
      ) {
        setErrorMessage(
          "Este login já está cadastrado."
        );
      } else if (
        axiosError.response?.data?.message
      ) {
        setErrorMessage(
          axiosError.response.data.message
        );
      } else {
        setErrorMessage(
          "Não foi possível realizar o cadastro."
        );
      }
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
              Novas oportunidades
            </span>

            <h1>
              Crie sua conta de candidato.
            </h1>

            <p>
              Cadastre-se para consultar as vagas
              disponíveis e encontrar novas
              oportunidades profissionais.
            </p>
          </div>

          <div className="login-presentation__features">
            <article>
              <span>01</span>

              <div>
                <strong>
                  Perfil de candidato
                </strong>

                <p>
                  O cadastro público cria somente
                  usuários com o perfil USER.
                </p>
              </div>
            </article>

            <article>
              <span>02</span>

              <div>
                <strong>
                  Catálogo de vagas
                </strong>

                <p>
                  Consulte todas as oportunidades
                  disponíveis na plataforma.
                </p>
              </div>
            </article>
          </div>
        </div>

        <div className="login-card">
          <header className="login-card__header">
            <span className="login-card__eyebrow">
              Cadastro
            </span>

            <h2>Criar conta</h2>

            <p>
              Informe seus dados para cadastrar
              um perfil de candidato.
            </p>
          </header>

          <form
            className="login-form"
            onSubmit={handleSubmit}
          >
            <div className="login-form__field">
              <label htmlFor="register-login">
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
                  id="register-login"
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
              <label htmlFor="register-password">
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
                  id="register-password"
                  name="password"
                  type="password"
                  autoComplete="new-password"
                  placeholder="Mínimo de 8 caracteres"
                  value={password}
                  disabled={isSubmitting}
                  onChange={(event) => {
                    setPassword(
                      event.target.value
                    );

                    setErrorMessage("");
                  }}
                  required
                />
              </div>
            </div>

            <div className="login-form__field">
              <label htmlFor="confirm-password">
                Confirmar senha
              </label>

              <div className="login-form__control">
                <span
                  className="login-form__control-icon"
                  aria-hidden="true"
                >
                  •
                </span>

                <input
                  id="confirm-password"
                  name="confirmPassword"
                  type="password"
                  autoComplete="new-password"
                  placeholder="Digite a senha novamente"
                  value={confirmPassword}
                  disabled={isSubmitting}
                  onChange={(event) => {
                    setConfirmPassword(
                      event.target.value
                    );

                    setErrorMessage("");
                  }}
                  required
                />
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
                !password ||
                !confirmPassword
              }
            >
              {isSubmitting
                ? "Cadastrando..."
                : "Criar conta"}
            </button>
          </form>

          <footer className="login-card__footer">
            <span>
              Já possui uma conta?
            </span>

            <button
              type="button"
              onClick={() => {
                navigate("/login");
              }}
            >
              Entrar na plataforma
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