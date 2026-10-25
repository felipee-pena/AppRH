import {
  useCallback,
  useEffect,
  useMemo,
  useState,
  type FormEvent,
} from "react";

import {
  createPortal,
} from "react-dom";

import {
  useAuth,
} from "../../hooks/useAuth";

import {
  useCompanies,
} from "../../hooks/useCompanies";

import {
  useCreateJob,
} from "../../hooks/useCreateJob";

import {
  useRhCompany,
} from "../../hooks/useRhCompany";

import type {
  JobRequestData,
} from "../../interfaces/JobData";

import {
  normalizeBrazilianCurrency,
} from "../../utils/currency";

import "./create-job-modal.css";

interface CreateJobModalProps {
  closeModal(): void;
}

export function CreateJobModal({
  closeModal,
}: CreateJobModalProps) {
  const [name, setName] =
    useState("");

  const [
    description,
    setDescription,
  ] = useState("");

  const [
    expirationDate,
    setExpirationDate,
  ] = useState("");

  const [salary, setSalary] =
    useState("");

  const [
    contactEmail,
    setContactEmail,
  ] = useState("");

  const [
    selectedCompanyId,
    setSelectedCompanyId,
  ] = useState("");

  const [
    companySearch,
    setCompanySearch,
  ] = useState("");

  const auth = useAuth();

  const isAdmin =
    auth.hasRole("ADMIN");

  const isRh =
    auth.user?.role === "USERRH";

  const {
    data: companies = [],
    isLoading: isLoadingCompanies,
  } = useCompanies();

  const {
    data: rhCompany,
    isLoading: isLoadingRhCompany,
  } = useRhCompany(isRh);

  const {
    mutate,
    isPending,
    isSuccess,
    isError,
    error,
    reset,
  } = useCreateJob();

  const filteredCompanies =
    useMemo(() => {
      const search =
        companySearch
          .trim()
          .toLowerCase();

      if (!search) {
        return companies;
      }

      return companies.filter(
        (company) =>
          company.name
            .toLowerCase()
            .includes(search)
      );
    }, [
      companies,
      companySearch,
    ]);

  function submit(
    event:
      FormEvent<HTMLFormElement>
  ) {
    event.preventDefault();

    if (
      isAdmin &&
      !selectedCompanyId
    ) {
      return;
    }

    const normalizedSalary =
      normalizeBrazilianCurrency(
        salary
      );

    const numericSalary =
      Number(normalizedSalary);

    if (
      Number.isNaN(numericSalary)
    ) {
      return;
    }

    const job: JobRequestData = {
      name: name.trim(),
      description:
        description.trim(),
      expirationDate,
      salary: numericSalary,
      contactEmail:
        contactEmail.trim()
          ? contactEmail
              .trim()
              .toLowerCase()
          : null,

      companyId:
        isAdmin
          ? Number(
              selectedCompanyId
            )
          : null,
    };

    mutate(job);
  }

  const handleClose =
    useCallback(
      () => {
        if (isPending) {
          return;
        }

        reset();
        closeModal();
      },
      [
        isPending,
        reset,
        closeModal,
      ]
    );

  useEffect(
    () => {
      document.body.classList.add(
        "modal-open"
      );

      function handleEscape(
        event: KeyboardEvent
      ) {
        if (
          event.key === "Escape"
          && !isPending
        ) {
          handleClose();
        }
      }

      window.addEventListener(
        "keydown",
        handleEscape
      );

      return () => {
        document.body.classList.remove(
          "modal-open"
        );

        window.removeEventListener(
          "keydown",
          handleEscape
        );
      };
    },
    [
      isPending,
      handleClose,
    ]
  );

  useEffect(() => {
    if (isSuccess) {
      closeModal();
    }
  }, [
    isSuccess,
    closeModal,
  ]);

  return createPortal(
    <div
      className="job-modal__overlay"
      role="dialog"
      aria-modal="true"
      aria-labelledby="create-job-title"
      onMouseDown={handleClose}
    >
      <section
        className="job-modal"
        onMouseDown={(event) => {
          event.stopPropagation();
        }}
      >
        <header className="job-modal__header">
          <div>
            <span className="job-modal__eyebrow">
              Nova oportunidade
            </span>

            <h2 id="create-job-title">
              Cadastrar nova vaga
            </h2>

            <p>
              Informe os dados que
              serão apresentados aos
              candidatos.
            </p>
          </div>

          <button
            type="button"
            className="job-modal__close-button"
            onClick={handleClose}
            disabled={isPending}
            aria-label="Fechar modal"
          >
            ×
          </button>
        </header>

        <form
          className="job-modal__form"
          onSubmit={submit}
        >
          <div className="job-modal__field">
            <label htmlFor="job-name">
              Nome da vaga
            </label>

            <input
              id="job-name"
              type="text"
              maxLength={255}
              value={name}
              placeholder="Ex.: Desenvolvedor Java"
              disabled={isPending}
              onChange={(event) => {
                setName(
                  event.target.value
                );
              }}
              autoFocus
              required
            />
          </div>

          <div className="job-modal__field">
            <label htmlFor="job-expiration-date">
              Data de expiração
            </label>

            <input
              id="job-expiration-date"
              type="date"
              value={expirationDate}
              disabled={isPending}
              onChange={(event) => {
                setExpirationDate(
                  event.target.value
                );
              }}
              required
            />
          </div>

          <div className="job-modal__field">
            <label htmlFor="job-salary">
              Salário
            </label>

            <input
              id="job-salary"
              type="text"
              inputMode="decimal"
              value={salary}
              placeholder="Ex.: 9.500,00"
              disabled={isPending}
              onChange={(event) => {
                setSalary(
                  event.target.value
                );
              }}
              required
            />
          </div>

          <div className="job-modal__field">
            <label htmlFor="job-contact-email">
              E-mail de contato
            </label>

            <input
              id="job-contact-email"
              type="email"
              maxLength={255}
              value={contactEmail}
              placeholder="Opcional"
              disabled={isPending}
              onChange={(event) => {
                setContactEmail(
                  event.target.value
                );
              }}
            />
          </div>

          {isRh && (
            <div className="job-modal__field job-modal__field--full">
              <label>
                Empresa vinculada
              </label>

              <div className="job-modal__readonly">
                {isLoadingRhCompany
                  ? "Carregando empresa..."
                  : rhCompany?.name
                    ?? "Empresa não identificada"}
              </div>
            </div>
          )}

          {isAdmin && (
            <section className="job-modal__field job-modal__field--full">
              <label htmlFor="job-company-search">
                Empresa
              </label>

              <input
                id="job-company-search"
                type="search"
                value={companySearch}
                placeholder="Pesquisar empresa"
                disabled={
                  isPending ||
                  isLoadingCompanies
                }
                onChange={(event) => {
                  setCompanySearch(
                    event.target.value
                  );
                }}
              />

              <div className="job-modal__company-list">
                {filteredCompanies.map(
                  (company) => (
                    <button
                      key={company.id}
                      type="button"
                      className={
                        selectedCompanyId
                          === String(
                            company.id
                          )
                          ? "job-modal__company-option job-modal__company-option--selected"
                          : "job-modal__company-option"
                      }
                      onClick={() => {
                        setSelectedCompanyId(
                          String(
                            company.id
                          )
                        );
                      }}
                    >
                      {company.name}
                    </button>
                  )
                )}
              </div>
            </section>
          )}

          <div className="job-modal__field job-modal__field--full">
            <label htmlFor="job-description">
              Descrição da vaga
            </label>

            <textarea
              id="job-description"
              rows={8}
              maxLength={5000}
              value={description}
              placeholder="Descreva responsabilidades, requisitos e benefícios."
              disabled={isPending}
              onChange={(event) => {
                setDescription(
                  event.target.value
                );
              }}
              required
            />

            <span className="job-modal__counter">
              {description.length}/5000
            </span>
          </div>

          {isError && (
            <div className="job-modal__error">
              <strong>
                Não foi possível
                cadastrar a vaga.
              </strong>

              <span>
                {error instanceof Error
                  ? error.message
                  : "Verifique os dados e tente novamente."}
              </span>
            </div>
          )}

          <footer className="job-modal__actions">
            <button
              type="button"
              className="job-modal__button job-modal__button--secondary"
              onClick={handleClose}
              disabled={isPending}
            >
              Cancelar
            </button>

            <button
              type="submit"
              className="job-modal__button job-modal__button--primary"
              disabled={
                isPending ||
                !name.trim() ||
                !description.trim() ||
                !expirationDate ||
                !salary.trim() ||
                (
                  isAdmin &&
                  !selectedCompanyId
                )
              }
            >
              {isPending
                ? "Cadastrando..."
                : "Cadastrar vaga"}
            </button>
          </footer>
        </form>
      </section>
    </div>,
    document.body
  );
}