import {
  useEffect,
  useState,
  type FormEvent,
} from "react";
import { createPortal } from "react-dom";

import { useCreateJob } from "../../hooks/useCreateJob";
import type { JobData } from "../../interfaces/JobData";
import { normalizeBrazilianCurrency } from "../../utils/currency";

import "./create-job-modal.css";

interface CreateJobModalProps {
  closeModal(): void;
}

export function CreateJobModal({
  closeModal,
}: CreateJobModalProps) {
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [date, setDate] = useState("");
  const [salary, setSalary] = useState("");

  const {
    mutate,
    isPending,
    isSuccess,
    isError,
    error,
    reset,
  } = useCreateJob();

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const normalizedSalary =
      normalizeBrazilianCurrency(salary);

    const job: JobData = {
      name: name.trim(),
      description: description.trim(),
      date,
      salary: normalizedSalary,
    };

    mutate(job);
  }

  function handleClose() {
    if (isPending) {
      return;
    }

    reset();
    closeModal();
  }

  useEffect(() => {
    document.body.classList.add("modal-open");

    function handleEscape(event: KeyboardEvent) {
      if (event.key === "Escape") {
        handleClose();
      }
    }

    window.addEventListener("keydown", handleEscape);

    return () => {
      document.body.classList.remove("modal-open");
      window.removeEventListener("keydown", handleEscape);
    };
  }, [isPending]);

  useEffect(() => {
    if (isSuccess) {
      closeModal();
    }
  }, [isSuccess, closeModal]);

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
        onMouseDown={(event) => event.stopPropagation()}
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
              Informe os dados da oportunidade que será
              disponibilizada aos candidatos.
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
              value={name}
              placeholder="Ex.: Desenvolvedor Java"
              onChange={(event) =>
                setName(event.target.value)
              }
              autoFocus
              required
            />
          </div>

          <div className="job-modal__field">
            <label htmlFor="job-date">
              Data de expiração
            </label>

            <input
              id="job-date"
              type="date"
              value={date}
              onChange={(event) =>
                setDate(event.target.value)
              }
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
              min="0"
              step="0.01"
              value={salary}
              placeholder="Ex.: 5500.00"
              onChange={(event) =>
                setSalary(event.target.value)
              }
              required
            />
          </div>

          <div className="job-modal__field job-modal__field--full">
            <label htmlFor="job-description">
              Descrição da vaga
            </label>

            <textarea
              id="job-description"
              rows={8}
              maxLength={2000}
              value={description}
              placeholder="Descreva responsabilidades e requisitos."
              onChange={(event) =>
                setDescription(event.target.value)
              }
              required
            />

            <span className="job-modal__counter">
              {description.length}/2000
            </span>
          </div>

          {isError && (
            <div className="job-modal__error">
              <strong>
                Não foi possível cadastrar a vaga.
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
              disabled={isPending}
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