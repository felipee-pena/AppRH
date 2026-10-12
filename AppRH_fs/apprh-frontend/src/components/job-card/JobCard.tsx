import type { JobData } from "../../interfaces/JobData";
import "./job-card.css";

interface JobCardProps {
  job: JobData;
  onEdit(job: JobData): void;
  onDelete(code: number): void;
}

export function JobCard({
  job,
  onEdit,
  onDelete,
}: JobCardProps) {
  return (
    <article className="job-card">
      <header className="job-card__header">
        <span className="job-card__badge">Vaga aberta</span>
        <h2>{job.name}</h2>
      </header>

      <p className="job-card__description">
        {job.description}
      </p>

      <div className="job-card__information">
        <p>
          <strong>Salário:</strong> R$ {job.salary}
        </p>

        <p>
          <strong>Expira em:</strong> {job.date}
        </p>
      </div>

      <footer className="job-card__actions">
        <button type="button" onClick={() => onEdit(job)}>
          Editar
        </button>

        <button
          type="button"
          className="danger-button"
          onClick={() => job.code && onDelete(job.code)}
        >
          Excluir
        </button>
      </footer>
    </article>
  );
}