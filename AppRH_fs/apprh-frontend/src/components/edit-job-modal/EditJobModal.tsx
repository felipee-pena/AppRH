import {
  useEffect,
  useState,
  type FormEvent,
} from "react";
import { createPortal } from "react-dom";

import type { JobData } from "../../interfaces/JobData";
import { useUpdateJob } from "../../hooks/useUpdateJob";

import "../create-job-modal/create-job-modal.css";

interface EditJobModalProps {
  job: JobData;
  closeModal(): void;
}

export function EditJobModal({
  job,
  closeModal,
}: EditJobModalProps) {
  const [name, setName] = useState(job.name);
  const [description, setDescription] =
    useState(job.description);
  const [date, setDate] = useState(job.date);
  const [salary, setSalary] = useState(job.salary);

  const {
    mutate,
    isPending,
    isSuccess,
    isError,
    error,
    reset,
  } = useUpdateJob();

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (job.code === undefined) {
      return;
    }

    mutate({
      code: job.code,
      job: {
        code: job.code,
        name: name.trim(),
        description: description.trim(),
        date,
        salary,
      },
    });
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
      if (event.key === "Escape" && !isPending) {
        handleClose();
      }
    }

    window.addEventListener("keydown", handleEscape);

    return () => {
      document.body.classList.remove("modal-open");
      window.removeEventListener(
        "keydown",
        handleEscape
      );
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
      aria-labelledby="edit-job-title"
      onMouseDown={handleClose}
    >
      <section
        className="job-modal"
        onMouseDown={(event) =>
          event.stopPropagation()
        }
      >
        <header className="job-modal__header">
          <div>
            <span className="job-modal__eyebrow">
              Atualização da oportunidade
            </span>

            <h2 id="edit-job-title">
              Editar vaga
            </h2>

            <p>
              Atualize os dados da vaga selecionada.
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
            <label htmlFor="edit-job-name">
              Nome da vaga
            </label>

            <input
              id="edit-job-name"
              type="text"
              value={name}
              onChange={(event) =>
                setName(event.target.value)
              }
              autoFocus
              required
            />
          </div>

          <div className="job-modal__field">
            <label htmlFor="edit-job-date">
              Data de expiração
            </label>

            <input
              id="edit-job-date"
              type="date"
              value={date}
              onChange={(event) =>
                setDate(event.target.value)
              }
              required
            />
          </div>

          <div className="job-modal__field">
            <label htmlFor="edit-job-salary">
              Salário
            </label>

            <input
              id="edit-job-salary"
              type="number"
              min="0"
              step="0.01"
              value={salary}
              onChange={(event) =>
                setSalary(event.target.value)
              }
              required
            />
          </div>

          <div className="job-modal__field job-modal__field--full">
            <label htmlFor="edit-job-description">
              Descrição da vaga
            </label>

            <textarea
              id="edit-job-description"
              rows={5}
              maxLength={254}
              value={description}
              onChange={(event) =>
                setDescription(event.target.value)
              }
              required
            />

            <span className="job-modal__counter">
              {description.length}/254
            </span>
          </div>

          {isError && (
            <div className="job-modal__error">
              <strong>
                Não foi possível atualizar a vaga.
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
                ? "Salvando..."
                : "Salvar alterações"}
            </button>
          </footer>
        </form>
      </section>
    </div>,
    document.body
  );
}