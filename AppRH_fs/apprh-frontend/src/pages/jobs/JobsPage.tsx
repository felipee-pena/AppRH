import { useState } from "react";

import { JobCard } from "../../components/job-card/JobCard";
import { CreateJobModal } from "../../components/create-job-modal/CreateJobModal";
import { useJobs } from "../../hooks/useJobs";

import "./jobs-page.css";
export function JobsPage() {
  const [isModalOpen, setIsModalOpen] = useState(false);

  const {
    data,
    isLoading,
    isError,
    error,
  } = useJobs();

  const jobs = Array.isArray(data) ? data : [];

  if (isLoading) {
    return (
      <main className="jobs-page">
        <div className="jobs-status">
          Carregando vagas...
        </div>
      </main>
    );
  }

  if (isError) {
    return (
      <main className="jobs-page">
        <div className="jobs-status jobs-status--error">
          <h2>Não foi possível carregar as vagas</h2>

          <p>
            {error instanceof Error
              ? error.message
              : "Ocorreu um erro desconhecido."}
          </p>
        </div>
      </main>
    );
  }

  return (
    <main className="jobs-page">
      <header className="jobs-header">
        <div className="jobs-header__content">
          <span className="jobs-eyebrow">
            Recrutamento
          </span>

          <h1>Vagas cadastradas</h1>

          <p>
            Gerencie as oportunidades disponíveis e acompanhe
            os processos seletivos do AppRH.
          </p>
        </div>

        <button
          type="button"
          className="jobs-primary-button"
          onClick={() => setIsModalOpen(true)}
        >
          <span aria-hidden="true">+</span>
          Nova vaga
        </button>
      </header>

      {jobs.length === 0 ? (
        <section className="jobs-empty-state">
          <div className="jobs-empty-state__icon">
            RH
          </div>

          <h2>Nenhuma vaga cadastrada</h2>

          <p>
            Cadastre a primeira oportunidade para começar
            a gerenciar os processos seletivos.
          </p>

          <button
            type="button"
            className="jobs-primary-button"
            onClick={() => setIsModalOpen(true)}
          >
            Cadastrar primeira vaga
          </button>
        </section>
      ) : (
        <section className="jobs-grid">
          {jobs.map((job) => (
            <JobCard
              key={job.code}
              job={job}
              onEdit={(selectedJob) => {
                console.log("Editar:", selectedJob);
              }}
              onDelete={(code) => {
                console.log("Excluir:", code);
              }}
            />
          ))}
        </section>
      )}

      {isModalOpen && (
        <CreateJobModal
          closeModal={() => setIsModalOpen(false)}
        />
      )}
    </main>
  );
}

