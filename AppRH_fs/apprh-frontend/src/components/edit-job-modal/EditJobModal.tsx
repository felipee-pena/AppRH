import {
  useCallback,
  useEffect,
  useState,
  type FormEvent,
  type KeyboardEvent,
  type MouseEvent,
} from "react";

import {
  createPortal,
} from "react-dom";

import type {
  JobData,
} from "../../interfaces/JobData";

import {
  useUpdateJob,
} from "../../hooks/useUpdateJob";

import "../create-job-modal/create-job-modal.css";

interface EditJobModalProps {
  job: JobData;
  closeModal(): void;
}

export function EditJobModal({
  job,
  closeModal,
}: EditJobModalProps) {
  const [name, setName] =
    useState(
      job.name
    );

  const [
    description,
    setDescription,
  ] =
    useState(
      job.description
    );

  const [
    expirationDate,
    setExpirationDate,
  ] =
    useState(
      job.expirationDate
    );

  const [salary, setSalary] =
    useState<number>(
      job.salary
    );

  const [
    contactEmail,
    setContactEmail,
  ] =
    useState(
      job.contactEmail
      ?? ""
    );

  const {
    mutate,
    isPending,
    isSuccess,
    isError,
    error,
    reset,
  } =
    useUpdateJob();

  function submit(
    event: FormEvent<HTMLFormElement>
  ) {
    event.preventDefault();

    if (
      job.code === undefined
    ) {
      return;
    }

    mutate({
      code: job.code,

      job: {
        name:
          name.trim(),

        description:
          description.trim(),

        expirationDate,

        salary,

        contactEmail:
          contactEmail.trim()
          || null,

        companyId:
          job.company?.id
          ?? null,
      },
    });
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

  function handleOverlayMouseDown(
    event: MouseEvent<HTMLDivElement>
  ) {
    if (
      event.target
      !== event.currentTarget
    ) {
      return;
    }

    handleClose();
  }

  function handleOverlayKeyDown(
    event: KeyboardEvent<HTMLDivElement>
  ) {
    if (
      event.target
      !== event.currentTarget
    ) {
      return;
    }

    if (
      event.key === "Enter"
      || event.key === " "
    ) {
      event.preventDefault();
      handleClose();
    }
  }

  useEffect(
    () => {
      document.body.classList.add(
        "modal-open"
      );

      function handleEscape(
        event: globalThis.KeyboardEvent
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

  useEffect(
    () => {
      if (isSuccess) {
        closeModal();
      }
    },
    [
      isSuccess,
      closeModal,
    ]
  );

  return createPortal(
    <div
      className="job-modal__overlay"
      role="button"
      tabIndex={0}
      aria-label="Fechar modal de edição de vaga"
      onMouseDown={handleOverlayMouseDown}
      onKeyDown={handleOverlayKeyDown}
    >
      <section
        className="job-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="edit-job-title"
        aria-describedby="edit-job-description"
      >
        <header className="job-modal__header">
          <div>
            <span className="job-modal__eyebrow">
              Atualização da oportunidade
            </span>

            <h2 id="edit-job-title">
              Editar vaga
            </h2>

            <p id="edit-job-description">
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
              disabled={isPending}
              onChange={(event) =>
                setName(
                  event.target.value
                )
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
              value={expirationDate}
              disabled={isPending}
              onChange={(event) =>
                setExpirationDate(
                  event.target.value
                )
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
              disabled={isPending}
              onChange={(event) => {
                const numericValue =
                  event.target.valueAsNumber;

                setSalary(
                  Number.isNaN(
                    numericValue
                  )
                    ? 0
                    : numericValue
                );
              }}
              required
            />
          </div>

          <div className="job-modal__field">
            <label htmlFor="edit-job-contact-email">
              E-mail para contato
            </label>

            <input
              id="edit-job-contact-email"
              type="email"
              value={contactEmail}
              disabled={isPending}
              onChange={(event) =>
                setContactEmail(
                  event.target.value
                )
              }
              placeholder="vagas@empresa.com"
            />
          </div>

          <div className="job-modal__field job-modal__field--full">
            <label htmlFor="edit-job-description-field">
              Descrição da vaga
            </label>

            <textarea
              id="edit-job-description-field"
              rows={5}
              maxLength={254}
              value={description}
              disabled={isPending}
              onChange={(event) =>
                setDescription(
                  event.target.value
                )
              }
              required
            />

            <span className="job-modal__counter">
              {description.length}/254
            </span>
          </div>

          {isError && (
            <div
              className="job-modal__error"
              role="alert"
            >
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
              disabled={
                isPending
                || !name.trim()
                || !description.trim()
                || !expirationDate
                || salary <= 0
              }
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