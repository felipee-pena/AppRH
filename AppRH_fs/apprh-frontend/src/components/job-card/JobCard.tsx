import {
  useState,
} from "react";

import type {
  JobData,
} from "../../interfaces/JobData";

import {
  JobDetailsModal,
} from "../job-details-modal/JobDetailsModal";

import "./job-card.css";

interface JobCardProps {
  job: JobData;
  showManagementActions?: boolean;
  onEdit?: (job: JobData) => void;
  onDelete?: (code: number) => void;
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
    return "Não informada";
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
    !year
    || !month
    || !day
  ) {
    return value;
  }

  return `${day}/${month}/${year}`;
}

export function JobCard({
  job,
  showManagementActions = false,
  onEdit,
  onDelete,
}: JobCardProps) {
  const [
    isDetailsOpen,
    setIsDetailsOpen,
  ] = useState(false);

  function openDetails() {
    setIsDetailsOpen(true);
  }

  function closeDetails() {
    setIsDetailsOpen(false);
  }

  function handleEdit() {
    onEdit?.(job);
  }

  function handleDelete() {
    if (
      job.code === undefined
    ) {
      return;
    }

    onDelete?.(job.code);
  }

  return (
    <>
      <article className="job-card">
        <button
          type="button"
          className="job-card__content-button"
          aria-haspopup="dialog"
          aria-expanded={isDetailsOpen}
          onClick={openDetails}
        >
          <header className="job-card__header">
            <span className="job-card__badge">
              Oportunidade
            </span>

            <h2>
              {job.name}
            </h2>

            <p className="job-card__company">
              {job.company?.name
                ?? "Empresa não informada"}
            </p>
          </header>

          <p className="job-card__description">
            {job.description}
          </p>

          <div className="job-card__information">
            <p>
              <span>
                Salário
              </span>

              <strong>
                {formatCurrency(
                  job.salary
                )}
              </strong>
            </p>

            <p>
              <span>
                Publicada em
              </span>

              <strong>
                {formatDate(
                  job.createdAt
                )}
              </strong>
            </p>

            <p>
              <span>
                Expira em
              </span>

              <strong>
                {formatDate(
                  job.expirationDate
                )}
              </strong>
            </p>
          </div>

          <span className="job-card__details-button">
            Ver detalhes da vaga
          </span>
        </button>

        {showManagementActions && (
          <footer className="job-card__actions">
            {onEdit && (
              <button
                type="button"
                className="job-card__action job-card__action--edit"
                onClick={handleEdit}
              >
                Editar
              </button>
            )}

            {onDelete
              && job.code !== undefined
              && (
                <button
                  type="button"
                  className="job-card__action job-card__action--delete"
                  onClick={handleDelete}
                >
                  Excluir
                </button>
              )}
          </footer>
        )}
      </article>

      {isDetailsOpen && (
        <JobDetailsModal
          job={job}
          onClose={closeDetails}
        />
      )}
    </>
  );
}