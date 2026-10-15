import {
  useState,
} from "react";

import type {
  JobData,
} from "../../interfaces/JobData";

import "./job-card.css";

interface JobCardProps {
  job: JobData;
  showManagementActions?: boolean;
  onEdit?: (job: JobData) => void;
  onDelete?: (code: number) => void;
}

export function JobCard({
  job,
  showManagementActions = false,
  onEdit,
  onDelete,
}: JobCardProps) {
  const [isExpanded, setIsExpanded] =
    useState(false);

  return (
    <article className="job-card">
      <header className="job-card__header">
        <span className="job-card__badge">
          Oportunidade
        </span>

        <h2>{job.name}</h2>
      </header>

      <p
        className={
          isExpanded
            ? "job-card__description "
                + "job-card__description--expanded"
            : "job-card__description"
        }
      >
        {job.description}
      </p>

      <button
        type="button"
        className="job-card__details-button"
        onClick={() => {
          setIsExpanded(
            (currentValue) =>
              !currentValue
          );
        }}
      >
        {isExpanded
          ? "Mostrar menos"
          : "Ver descrição completa"}
      </button>

      <div className="job-card__information">
        <p>
          <strong>Salário:</strong>{" "}
          {job.salary}
        </p>

        <p>
          <strong>Data:</strong>{" "}
          {job.date}
        </p>
      </div>

      {showManagementActions && (
        <footer className="job-card__actions">
          {onEdit && (
            <button
              type="button"
              className={
                "job-card__action "
                + "job-card__action--edit"
              }
              onClick={() => {
                onEdit(job);
              }}
            >
              Editar
            </button>
          )}

          {onDelete &&
            job.code !== undefined && (
              <button
                type="button"
                className={
                  "job-card__action "
                  + "job-card__action--delete"
                }
                onClick={() => {
                  onDelete(job.code);
                }}
              >
                Excluir
              </button>
            )}
        </footer>
      )}
    </article>
  );
}