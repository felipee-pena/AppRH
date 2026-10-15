import {
  AppHeader,
} from "../../components/header/AppHeader";

import {
  JobCard,
} from "../../components/job-card/JobCard";

import {
  useJobs,
} from "../../hooks/useJobs";

import "./jobs-page.css";

export function JobsPage() {
  const {
    data,
    isLoading,
    isError,
    error,
  } = useJobs();

  const jobs =
    Array.isArray(data)
      ? data
      : [];

  return (
    <>
      <AppHeader />

      <main className="jobs-page">
        <header className="jobs-header">
          <div className="jobs-header__content">
            <span className="jobs-eyebrow">
              Oportunidades
            </span>

            <h1>Vagas disponíveis</h1>

            <p>
              Consulte as oportunidades disponíveis
              e veja os detalhes de cada vaga.
            </p>
          </div>
        </header>

        {isLoading && (
          <section className="jobs-status">
            Carregando vagas...
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
                Nenhuma vaga disponível
              </h2>

              <p>
                Novas oportunidades serão exibidas
                aqui quando forem cadastradas.
              </p>
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
                  showManagementActions={false}
                />
              ))}
            </section>
          )}
      </main>
    </>
  );
}