import {
  useEffect,
} from "react";

import {
  createPortal,
} from "react-dom";

import type {
  JobData,
} from "../../interfaces/JobData";

import "./job-details-modal.css";

interface JobDetailsModalProps {
  job: JobData;
  onClose(): void;
}

function formatCurrency(
  value: number
): string {
  return new Intl.NumberFormat(
    "pt-BR",
    {
      style: "currency",
      currency: "BRL",
    }
  ).format(value);
}

function formatDate(
  value?: string
): string {
  if (!value) {
    return "Não informado";
  }

  const datePart =
    value.includes("T")
      ? value.split("T")[0]
      : value;

  const [
    year,
    month,
    day,
  ] = datePart.split("-");

  if (
    !year ||
    !month ||
    !day
  ) {
    return value;
  }

  return `${day}/${month}/${year}`;
}

export function JobDetailsModal({
  job,
  onClose,
}: JobDetailsModalProps) {
  useEffect(() => {
    document.body.classList.add(
      "modal-open"
    );

    function handleEscape(
      event: KeyboardEvent
    ) {
      if (event.key === "Escape") {
        onClose();
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
  }, [onClose]);

  return createPortal(
    <div
      className="job-details__overlay"
      role="dialog"
      aria-modal="true"
      aria-labelledby="job-details-title"
      onMouseDown={onClose}
    >
      <article
        className="job-details"
        onMouseDown={(event) => {
          event.stopPropagation();
        }}
      >
        <header className="job-details__header">
          <div>
            <span className="job-details__eyebrow">
              Detalhes da oportunidade
            </span>

            <h2 id="job-details-title">
              {job.name}
            </h2>

            <p>
              {job.company?.name
                ?? "Empresa não informada"}
            </p>
          </div>

          <button
            type="button"
            className="job-details__close"
            onClick={onClose}
            aria-label="Fechar detalhes"
          >
            ×
          </button>
        </header>

        <section className="job-details__summary">
          <article>
            <span>Salário</span>

            <strong>
              {formatCurrency(
                job.salary
              )}
            </strong>
          </article>

          <article>
            <span>
              Data de criação
            </span>

            <strong>
              {formatDate(
                job.createdAt
              )}
            </strong>
          </article>

          <article>
            <span>
              Data de expiração
            </span>

            <strong>
              {formatDate(
                job.expirationDate
              )}
            </strong>
          </article>

          <article>
            <span>Empresa</span>

            <strong>
              {job.company?.name
                ?? "Não informada"}
            </strong>
          </article>
        </section>

        {job.contactEmail && (
          <section className="job-details__contact">
            <span>
              E-mail para contato
            </span>

            <a
              href={
                `mailto:${job.contactEmail}`
              }
            >
              {job.contactEmail}
            </a>
          </section>
        )}

        <section className="job-details__description">
          <h3>
            Descrição da vaga
          </h3>

          <p>
            {job.description}
          </p>
        </section>

        <footer className="job-details__footer">
          <button
            type="button"
            onClick={onClose}
          >
            Fechar detalhes
          </button>
        </footer>
      </article>
    </div>,
    document.body
  );
}