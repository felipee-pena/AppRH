import { Link, useParams } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";

import type { JobData } from "../../interfaces/JobData";
import { api } from "../../services/api";

async function fetchJob(code: string): Promise<JobData> {
  const response = await api.get<JobData>(
    `/api/jobs/${code}`
  );

  return response.data;
}

export function JobDetailsPage() {
  const { code } = useParams();

  const {
    data: job,
    isLoading,
    isError,
  } = useQuery({
    queryKey: ["jobs", code],
    queryFn: () => fetchJob(code as string),
    enabled: Boolean(code),
  });

  if (isLoading) {
    return (
      <main className="details-page">
        Carregando vaga...
      </main>
    );
  }

  if (isError || !job) {
    return (
      <main className="details-page">
        <h1>Vaga não encontrada</h1>
        <Link to="/jobs">Voltar para vagas</Link>
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

      <h1>{job.name}</h1>

      <section className="details-page__card">
        <div>
          <span>Descrição</span>
          <p>{job.description}</p>
        </div>

        <div>
          <span>Salário</span>
          <strong>R$ {job.salary}</strong>
        </div>

        <div>
          <span>Data de expiração</span>
          <strong>{job.date}</strong>
        </div>
      </section>
    </main>
  );
}