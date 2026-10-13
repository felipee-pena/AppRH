import type { JobData } from "../../interfaces/JobData";
import "./job-card.css";

interface JobCardProps {
  job: JobData;
  onEdit: (job: JobData) => void;
  onDelete: (code: number) => void;
}

export function JobCard({
  job,
  onEdit,
  onDelete,
}: JobCardProps) {
  function formatSalary(value: string) {
    const salary = Number(value);

    if (Number.isNaN(salary)) {
      return value;
    }

    return new Intl.NumberFormat("pt-BR", {
      style: "currency",
      currency: "BRL",
    }).format(salary);
  }

  function formatDate(value: string) {
    if (!value) {
      return "Não informada";
    }

    const date = new Date(`${value}T00:00:00`);

    if (Number.isNaN(date.getTime())) {
      return value;
    }

    return new Intl.DateTimeFormat("pt-BR").format(date);
  }

  return (
    <article className="job-card">
      <header className="job-card__header">
        <span className="job-card__badge">
          Vaga aberta
        </span>

        <h2>{job.name}</h2>
      </header>

      <p className="job-card__description">
        {job.description}
      </p>

      <div className="job-card__information">
        <div className="job-card__information-item">
          <span>Salário</span>
          <strong>{formatSalary(job.salary)}</strong>
        </div>

        <div className="job-card__information-item">
          <span>Expira em</span>
          <strong>{formatDate(job.date)}</strong>
        </div>
      </div>

      <footer className="job-card__actions">
        <button
          type="button"
          className="job-card__button job-card__button--secondary"
          onClick={() => onEdit(job)}
        >
          Editar
        </button>

        <button
          type="button"
          className="job-card__button job-card__button--danger"
          disabled={job.code === undefined}
          onClick={() => {
            if (job.code !== undefined) {
              onDelete(job.code);
            }
          }}
        >
          Excluir
        </button>
      </footer>
    </article>
  );
}