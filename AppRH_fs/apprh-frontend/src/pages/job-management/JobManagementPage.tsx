import {
  useState,
} from "react";

import type {
  JobData,
} from "../../interfaces/JobData";

import {
  AppHeader,
} from "../../components/header/AppHeader";

import {
  JobCard,
} from "../../components/job-card/JobCard";

import {
  CreateJobModal,
} from "../../components/create-job-modal/CreateJobModal";

import {
  EditJobModal,
} from "../../components/edit-job-modal/EditJobModal";

import {
  ConfirmationModal,
} from "../../components/confirmation-modal/ConfirmationModal";

import {
  useManagedJobs,
} from "../../hooks/useManagedJobs";

import {
  useDeleteJob,
} from "../../hooks/useDeleteJob";

import "../jobs/jobs-page.css";

export function JobManagementPage() {
  const [
    isCreateModalOpen,
    setIsCreateModalOpen,
  ] = useState(false);

  const [
    jobToEdit,
    setJobToEdit,
  ] = useState<JobData | null>(null);

  const [
    jobToDelete,
    setJobToDelete,
  ] = useState<JobData | null>(null);

  const {
    data,
    isLoading,
    isError,
    error,
  } = useManagedJobs();

  const {
    mutate: deleteJob,
    isPending: isDeleting,
    isError: isDeleteError,
    error: deleteError,
    reset: resetDelete,
  } = useDeleteJob();

  const jobs =
    Array.isArray(data)
      ? data
      : [];

  function openEditModal(
    selectedJob: JobData
  ) {
    setJobToEdit(selectedJob);
  }

  function closeEditModal() {
    setJobToEdit(null);
  }

  function openDeleteModal(
    code: number
  ) {
    const selectedJob =
      jobs.find(
        (job) =>
          job.code === code
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
    if (
      jobToDelete?.code === undefined
    ) {
      return;
    }

    deleteJob(
      jobToDelete.code,
      {
        onSuccess: () => {
          setJobToDelete(null);
        },
      }
    );
  }

  return (
    <>
      <AppHeader />

      <main className="jobs-page">
        <header className="jobs-header">
          <div className="jobs-header__content">
            <span className="jobs-eyebrow">
              Gestão de vagas
            </span>

            <h1>Gerenciar vagas</h1>

            <p>
              O administrador gerencia todas as vagas.
              O RH gerencia somente as vagas que criou.
            </p>
          </div>

          <button
            type="button"
            className="jobs-primary-button"
            onClick={() => {
              setIsCreateModalOpen(true);
            }}
          >
            <span aria-hidden="true">
              +
            </span>

            Nova vaga
          </button>
        </header>

        {isLoading && (
          <section className="jobs-status">
            Carregando vagas gerenciadas...
          </section>
        )}

        {isError && (
          <section
            className={
              "jobs-status "
              + "jobs-status--error"
            }
          >
            <h2>
              Não foi possível carregar as vagas
            </h2>

            <p>
              {error instanceof Error
                ? error.message
                : "Ocorreu um erro desconhecido."}
            </p>
          </section>
        )}

        {!isLoading &&
          !isError &&
          jobs.length === 0 && (
            <section className="jobs-empty-state">
              <div className="jobs-empty-state__icon">
                RH
              </div>

              <h2>
                Nenhuma vaga para gerenciar
              </h2>

              <p>
                Cadastre sua primeira oportunidade
                para começar a gerenciar vagas.
              </p>

              <button
                type="button"
                className="jobs-primary-button"
                onClick={() => {
                  setIsCreateModalOpen(true);
                }}
              >
                Cadastrar primeira vaga
              </button>
            </section>
          )}

        {!isLoading &&
          !isError &&
          jobs.length > 0 && (
            <section className="jobs-grid">
              {jobs.map((job) => (
                <JobCard
                  key={
                    job.code ??
                    job.name
                  }
                  job={job}
                  showManagementActions
                  onEdit={openEditModal}
                  onDelete={openDeleteModal}
                />
              ))}
            </section>
          )}

        {isCreateModalOpen && (
          <CreateJobModal
            closeModal={() => {
              setIsCreateModalOpen(false);
            }}
          />
        )}

        {jobToEdit && (
          <EditJobModal
            job={jobToEdit}
            closeModal={closeEditModal}
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
    </>
  );
}