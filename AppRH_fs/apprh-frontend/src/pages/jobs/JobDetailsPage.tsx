import {
  Link,
  useParams,
} from "react-router-dom";

import {
  useQuery,
} from "@tanstack/react-query";

import type {
  JobData,
} from "../../interfaces/JobData";

import {
  api,
} from "../../services/api";

import "./details-page.css";

async function fetchJob(
  code: string
): Promise<JobData> {
  const response =
    await api.get<JobData>(
      `/api/jobs/${code}`
    );

  return response.data;
}

function formatExpirationDate(
  expirationDate: string
): string {
  if (!expirationDate) {
    return "Não informada";
  }

  const date =
    new Date(
      `${expirationDate}T00:00:00`
    );

  if (
    Number.isNaN(
      date.getTime()
    )
  ) {
    return expirationDate;
  }

  return new Intl.DateTimeFormat(
    "pt-BR"
  ).format(
    date
  );
}

function formatSalary(
  salary: number
): string {
  return new Intl.NumberFormat(
    "pt-BR",
    {
      style: "currency",
      currency: "BRL",
    }
  ).format(
    salary
  );
}

export function JobDetailsPage() {
  const {
    code,
  } =
    useParams<{
      code: string;
    }>();

  const {
    data: job,
    isLoading,
    isError,
  } =
    useQuery({
      queryKey: [
        "jobs",
        code,
      ],

      queryFn: () =>
        fetchJob(
          code!
        ),

      enabled:
        Boolean(code),
    });

  if (!code) {
    return (
      <main className="details-page">
        <h1>
          Código da vaga inválido
        </h1>

        <Link to="/jobs">
          Voltar para vagas
        </Link>
      </main>
    );
  }

  if (isLoading) {
    return (
      <main className="details-page">
        <p
          role="status"
          aria-live="polite"
        >
          Carregando vaga...
        </p>
      </main>
    );
  }

  if (
    isError
    || !job
  ) {
    return (
      <main className="details-page">
        <h1>
          Vaga não encontrada
        </h1>

        <Link to="/jobs">
          Voltar para vagas
        </Link>
      </main>
    );
  }

  return (
    <main className="details-page">
      <Link
        to="/jobs"
        className="details-page__back-link"
      >
        ← Voltar para vagas
      </Link>

      <span className="details-page__eyebrow">
        Detalhes da oportunidade
      </span>

      <h1>
        {job.name}
      </h1>

      <section className="details-page__card">
        <div>
          <span>
            Descrição
          </span>

          <p>
            {job.description}
          </p>
        </div>

        <div>
          <span>
            Salário
          </span>

          <strong>
            {formatSalary(
              job.salary
            )}
          </strong>
        </div>

        <div>
          <span>
            Data de expiração
          </span>

          <strong>
            {formatExpirationDate(
              job.expirationDate
            )}
          </strong>
        </div>

        {job.company && (
          <div>
            <span>
              Empresa
            </span>

            <strong>
              {job.company.name}
            </strong>
          </div>
        )}

        {job.contactEmail && (
          <div>
            <span>
              Contato
            </span>

            <a href={`mailto:${job.contactEmail}`}>
              {job.contactEmail}
            </a>
          </div>
        )}

      </section>
    </main>
  );
}