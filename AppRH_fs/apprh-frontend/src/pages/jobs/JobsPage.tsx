import { useState } from "react";

import type { JobData } from "../../interfaces/JobData";

import { JobCard } from "../../components/job-card/JobCard";
import { CreateJobModal } from "../../components/create-job-modal/CreateJobModal";
import { EditJobModal } from "../../components/edit-job-modal/EditJobModal";
import { ConfirmationModal } from "../../components/confirmation-modal/ConfirmationModal";

import { useJobs } from "../../hooks/useJobs";
import { useDeleteJob } from "../../hooks/useDeleteJob";

import "./jobs-page.css";

export function JobsPage() {
  const [isCreateModalOpen, setIsCreateModalOpen] =
    useState(false);

  const [jobToEdit, setJobToEdit] =
    useState<JobData | null>(null);

  const [jobToDelete, setJobToDelete] =
    useState<JobData | null>(null);

  const {
    data,
    isLoading,
    isError,
    error,
  } = useJobs();

  const {
    mutate: deleteJob,
    isPending: isDeleting,
    isError: isDeleteError,
    error: deleteError,
    reset: resetDelete,
  } = useDeleteJob();

  const jobs = Array.isArray(data) ? data : [];

  function openDeleteModal(code: number) {
    const selectedJob = jobs.find(
      (job) => job.code === code
    );

    if (!selectedJob) {
      return;
    }

    resetDelete();
    setJobToDelete(selectedJob);
  }

  function closeDeleteModal() {
    if (isDeleting) {
      return;
    }

    resetDelete();
    setJobToDelete(null);
  }

  function confirmDelete() {
    if (jobToDelete?.code === undefined) {
      return;
    }

    deleteJob(jobToDelete.code, {
      onSuccess: () => {
        setJobToDelete(null);
      },
    });
  }

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
          <h2>
            Não foi possível carregar as vagas
          </h2>

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
            Gerencie as oportunidades disponíveis e
            acompanhe os processos seletivos do AppRH.
          </p>
        </div>

        <button
          type="button"
          className="jobs-primary-button"
          onClick={() =>
            setIsCreateModalOpen(true)
          }
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
            onClick={() =>
              setIsCreateModalOpen(true)
            }
          >
            Cadastrar primeira vaga
          </button>
        </section>
      ) : (
        <section className="jobs-grid">
          {jobs.map((job) => (
            <JobCard
              key={job.code ?? job.name}
              job={job}
              onEdit={(selectedJob) => {
                setJobToEdit(selectedJob);
              }}
              onDelete={openDeleteModal}
            />
          ))}
        </section>
      )}

      {isCreateModalOpen && (
        <CreateJobModal
          closeModal={() =>
            setIsCreateModalOpen(false)
          }
        />
      )}

      {jobToEdit && (
        <EditJobModal
          job={jobToEdit}
          closeModal={() =>
            setJobToEdit(null)
          }
        />
      )}

      {jobToDelete && (
        <ConfirmationModal
          title="Excluir vaga?"
          description={
            isDeleteError
              ? deleteError instanceof Error
                ? deleteError.message
                : "Não foi possível excluir a vaga."
              : `A vaga "${jobToDelete.name}" será excluída permanentemente.`
          }
          confirmLabel="Excluir vaga"
          isPending={isDeleting}
          onCancel={closeDeleteModal}
          onConfirm={confirmDelete}
        />
      )}
    </main>
  );
}