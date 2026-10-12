import { useState } from "react";
import { JobCard } from "../../components/job-card/JobCard";
import { CreateJobModal } from "../../components/create-job-modal/CreateJobModal";
import { useJobs } from "../../hooks/useJobs";
import "./jobs-page.css";

export function JobsPage() {
  const [isModalOpen, setIsModalOpen] = useState(false);

  const {
    data: jobs,
    isLoading,
    isError,
  } = useJobs();

  if (isLoading) {
    return <p>Carregando vagas...</p>;
  }

  if (isError) {
    return <p>Não foi possível carregar as vagas.</p>;
  }

  return (
    <main className="jobs-page">
      <header className="page-header">
        <div>
          <span className="page-subtitle">Recrutamento</span>
          <h1>Vagas cadastradas</h1>
        </div>

        <button
          type="button"
          className="primary-button"
          onClick={() => setIsModalOpen(true)}
        >
          Nova vaga
        </button>
      </header>

      {jobs?.length === 0 && (
        <section className="empty-state">
          <h2>Nenhuma vaga cadastrada</h2>
          <p>Cadastre a primeira vaga do AppRH.</p>
        </section>
      )}

      <section className="jobs-grid">
        {jobs?.map((job) => (
          <JobCard
            key={job.code}
            job={job}
            onEdit={(selectedJob) => {
              console.log(selectedJob);
            }}
            onDelete={(code) => {
              console.log(code);
            }}
          />
        ))}
      </section>

      {isModalOpen && (
        <CreateJobModal
          closeModal={() => setIsModalOpen(false)}
        />
      )}
    </main>
  );
}