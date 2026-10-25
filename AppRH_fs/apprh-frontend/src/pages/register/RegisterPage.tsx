import {
  useMemo,
  useState,
  type FormEvent,
} from "react";

import {
  Navigate,
  useNavigate,
} from "react-router-dom";

import type {
  AxiosError,
} from "axios";

import {
  useAuth,
} from "../../hooks/useAuth";

import {
  useCompanies,
} from "../../hooks/useCompanies";

import type {
  PublicRegistrationRole,
  RegisterRequest,
} from "../../interfaces/RegisterRequest";

import { api } from "../../services/api";

import "../login/login-page.css";
import "./register-page.css";

import type {
  ApiErrorResponse,
} from "../../interfaces/ApiErrorResponse";

export function RegisterPage() {
  const [login, setLogin] =
    useState("");

  const [password, setPassword] =
    useState("");

  const [
    confirmPassword,
    setConfirmPassword,
  ] = useState("");

  const [role, setRole] =
    useState<PublicRegistrationRole>(
      "USER"
    );

  const [
    selectedCompanyId,
    setSelectedCompanyId,
  ] = useState("");

  const [
    companySearch,
    setCompanySearch,
  ] = useState("");

  const [
    useNewCompany,
    setUseNewCompany,
  ] = useState(false);

  const [
    newCompanyName,
    setNewCompanyName,
  ] = useState("");

  const [
    errorMessage,
    setErrorMessage,
  ] = useState("");

  const [
    isSubmitting,
    setIsSubmitting,
  ] = useState(false);

  const auth = useAuth();
  const navigate = useNavigate();

  const {
    data: companies = [],
    isLoading: isLoadingCompanies,
    isError: isCompaniesError,
  } = useCompanies();

  const filteredCompanies =
    useMemo(() => {
      const normalizedSearch =
        companySearch
          .trim()
          .toLowerCase();

      if (!normalizedSearch) {
        return companies;
      }

      return companies.filter(
        (company) =>
          company.name
            .toLowerCase()
            .includes(
              normalizedSearch
            )
      );
    }, [
      companies,
      companySearch,
    ]);

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

  function handleRoleChange(
    selectedRole:
      PublicRegistrationRole
  ) {
    setRole(selectedRole);
    setErrorMessage("");

    if (selectedRole === "USER") {
      setSelectedCompanyId("");
      setCompanySearch("");
      setUseNewCompany(false);
      setNewCompanyName("");
    }
  }

  function validateForm():
    string | null {
    if (!login.trim()) {
      return "Informe um login.";
    }

    if (password.length < 8) {
      return (
        "A senha deve possuir pelo "
        + "menos 8 caracteres."
      );
    }

    if (
      password !== confirmPassword
    ) {
      return (
        "A confirmação da senha "
        + "não corresponde."
      );
    }

    if (role === "USERRH") {
      if (
        useNewCompany &&
        !newCompanyName.trim()
      ) {
        return (
          "Informe o nome da "
          + "nova empresa."
        );
      }

      if (
        !useNewCompany &&
        !selectedCompanyId
      ) {
        return (
          "Selecione uma empresa "
          + "ou cadastre uma nova."
        );
      }
    }

    return null;
  }

  async function handleSubmit(
    event: FormEvent<HTMLFormElement>
  ) {
    event.preventDefault();

    setErrorMessage("");

    const validationError =
      validateForm();

    if (validationError) {
      setErrorMessage(
        validationError
      );

      return;
    }

    const payload: RegisterRequest = {
      login: login
        .trim()
        .toLowerCase(),

      password,

      role,

      companyId:
        role === "USERRH" &&
        !useNewCompany
          ? Number(
              selectedCompanyId
            )
          : null,

      newCompanyName:
        role === "USERRH" &&
        useNewCompany
          ? newCompanyName.trim()
          : null,
    };

    setIsSubmitting(true);

    try {
      await api.post(
        "/auth/register",
        payload
      );

      navigate(
        "/verificacao-pendente",
        {
          replace: true,

          state: {
            login:
              payload.login,
          },
        }
      );
    } catch (error) {
      const axiosError =
        error as AxiosError<ApiErrorResponse>;

      const fieldErrors =
        axiosError.response?.data
          ?.errors;

      if (
        fieldErrors &&
        Object.keys(fieldErrors)
          .length > 0
      ) {
        setErrorMessage(
          Object.values(
            fieldErrors
          ).join(" ")
        );
      } else if (
        axiosError.response?.data
          ?.message
      ) {
        setErrorMessage(
          axiosError.response
            .data.message
        );
      } else if (
        axiosError.response
          ?.status === 409
      ) {
        setErrorMessage(
          "Este login já está cadastrado."
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
              Encontre talentos ou
              novas oportunidades.
            </h1>

            <p>
              Crie um perfil de candidato
              para consultar vagas ou um
              perfil de RH para publicar
              oportunidades.
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
                  Consulte vagas publicadas
                  por diferentes empresas.
                </p>
              </div>
            </article>

            <article>
              <span>02</span>

              <div>
                <strong>
                  Perfil de RH
                </strong>

                <p>
                  Publique e gerencie vagas
                  vinculadas à sua empresa.
                </p>
              </div>
            </article>
          </div>
        </div>

        <div className="login-card register-card">
          <header className="login-card__header">
            <span className="login-card__eyebrow">
              Cadastro
            </span>

            <h2>Criar conta</h2>

            <p>
              Escolha como deseja utilizar
              o AppRH.
            </p>
          </header>

          <form
            className="login-form"
            onSubmit={handleSubmit}
          >
            <fieldset className="register-role">
              <legend>
                Tipo de perfil
              </legend>

              <div className="register-role__options">
                <button
                  type="button"
                  className={
                    role === "USER"
                      ? "register-role__option register-role__option--active"
                      : "register-role__option"
                  }
                  onClick={() => {
                    handleRoleChange(
                      "USER"
                    );
                  }}
                >
                  <strong>
                    Candidato
                  </strong>

                  <span>
                    Quero consultar vagas
                  </span>
                </button>

                <button
                  type="button"
                  className={
                    role === "USERRH"
                      ? "register-role__option register-role__option--active"
                      : "register-role__option"
                  }
                  onClick={() => {
                    handleRoleChange(
                      "USERRH"
                    );
                  }}
                >
                  <strong>
                    Profissional de RH
                  </strong>

                  <span>
                    Quero publicar vagas
                  </span>
                </button>
              </div>
            </fieldset>

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
                  type="text"
                  autoComplete="username"
                  placeholder="Digite seu login"
                  value={login}
                  disabled={isSubmitting}
                  onChange={(event) => {
                    setLogin(
                      event.target.value
                    );

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

            {role === "USERRH" && (
              <section className="register-company">
                <div className="register-company__header">
                  <div>
                    <strong>
                      Empresa
                    </strong>

                    <p>
                      Selecione uma empresa
                      existente ou cadastre
                      uma nova.
                    </p>
                  </div>
                </div>

                <label className="register-company__toggle">
                  <input
                    type="checkbox"
                    checked={useNewCompany}
                    disabled={isSubmitting}
                    onChange={(event) => {
                      setUseNewCompany(
                        event.target.checked
                      );

                      setSelectedCompanyId("");
                      setCompanySearch("");
                      setNewCompanyName("");
                      setErrorMessage("");
                    }}
                  />

                  <span>
                    Minha empresa ainda
                    não está cadastrada
                  </span>
                </label>

                {useNewCompany ? (
                  <div className="login-form__field">
                    <label htmlFor="new-company">
                      Nome da nova empresa
                    </label>

                    <div className="login-form__control">
                      <input
                        id="new-company"
                        type="text"
                        maxLength={150}
                        placeholder="Ex.: Claro"
                        value={newCompanyName}
                        disabled={isSubmitting}
                        onChange={(event) => {
                          setNewCompanyName(
                            event.target.value
                          );

                          setErrorMessage("");
                        }}
                        required
                      />
                    </div>
                  </div>
                ) : (
                  <>
                    <div className="login-form__field">
                      <label htmlFor="company-search">
                        Pesquisar empresa
                      </label>

                      <div className="login-form__control">
                        <input
                          id="company-search"
                          type="search"
                          placeholder="Digite o nome da empresa"
                          value={companySearch}
                          disabled={
                            isSubmitting ||
                            isLoadingCompanies
                          }
                          onChange={(event) => {
                            setCompanySearch(
                              event.target.value
                            );
                          }}
                        />
                      </div>
                    </div>

                    <div
                      className="register-company__list"
                      role="listbox"
                      aria-label="Empresas disponíveis"
                    >
                      {isLoadingCompanies && (
                        <p className="register-company__status">
                          Carregando empresas...
                        </p>
                      )}

                      {isCompaniesError && (
                        <p className="register-company__status register-company__status--error">
                          Não foi possível carregar
                          as empresas.
                        </p>
                      )}

                      {!isLoadingCompanies &&
                        !isCompaniesError &&
                        filteredCompanies.length === 0 && (
                          <p className="register-company__status">
                            Nenhuma empresa encontrada.
                          </p>
                        )}

                      {filteredCompanies.map(
                        (company) => (
                          <button
                            key={company.id}
                            type="button"
                            role="option"
                            aria-selected={
                              selectedCompanyId
                                === String(
                                  company.id
                                )
                            }
                            className={
                              selectedCompanyId
                                === String(
                                  company.id
                                )
                                ? "register-company__item register-company__item--selected"
                                : "register-company__item"
                            }
                            disabled={isSubmitting}
                            onClick={() => {
                              setSelectedCompanyId(
                                String(
                                  company.id
                                )
                              );

                              setErrorMessage("");
                            }}
                          >
                            <span>
                              {company.name}
                            </span>

                            {selectedCompanyId
                              === String(
                                company.id
                              ) && (
                              <strong>
                                Selecionada
                              </strong>
                            )}
                          </button>
                        )
                      )}
                    </div>
                  </>
                )}
              </section>
            )}

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
              disabled={isSubmitting}
            >
              {isSubmitting
                ? "Cadastrando..."
                : role === "USERRH"
                  ? "Criar conta de RH"
                  : "Criar conta de candidato"}
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
    </main>
  );
}