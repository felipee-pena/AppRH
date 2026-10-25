import {
  render,
  screen,
} from "@testing-library/react";

import {
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from "vitest";

import {
  JobsPage,
} from "../../../pages/jobs/JobsPage";

import {
  useJobs,
} from "../../../hooks/useJobs";

import type {
  JobData,
} from "../../../interfaces/JobData";

vi.mock(
  "../../../hooks/useJobs",
  () => ({
    useJobs: vi.fn(),
  })
);

vi.mock(
  "../../../components/header/AppHeader",
  () => ({
    AppHeader:
      () => (
        <header data-testid="app-header">
          Cabeçalho
        </header>
      ),
  })
);

vi.mock(
  "../../../components/job-card/JobCard",
  () => ({
    JobCard:
      ({
        job,
        showManagementActions,
      }: {
        job: JobData;
        showManagementActions?: boolean;
      }) => (
        <article
          data-testid="job-card"
          data-management-actions={
            String(
              showManagementActions
            )
          }
        >
          <h2>
            {job.name}
          </h2>

          <p>
            {job.description}
          </p>
        </article>
      ),
  })
);

const mockedUseJobs =
  vi.mocked(
    useJobs
  );

describe(
  "JobsPage",
  () => {
    beforeEach(
      () => {
        vi.clearAllMocks();
      }
    );

    it(
      "renders the page header",
      () => {
        mockedUseJobs.mockReturnValue({
          data: [],

          isLoading:
            false,

          isError:
            false,

          error:
            null,
        } as unknown as ReturnType<typeof useJobs>);

        render(
          <JobsPage />
        );

        expect(
          screen.getByTestId(
            "app-header"
          )
        ).toBeInTheDocument();

        expect(
          screen.getByRole(
            "heading",
            {
              name:
                "Vagas disponíveis",
            }
          )
        ).toBeInTheDocument();

        expect(
          screen.getByText(
            "Consulte as oportunidades disponíveis e veja os detalhes de cada vaga."
          )
        ).toBeInTheDocument();
      }
    );

    it(
      "shows loading state",
      () => {
        mockedUseJobs.mockReturnValue({
          data:
            undefined,

          isLoading:
            true,

          isError:
            false,

          error:
            null,
        } as unknown as ReturnType<typeof useJobs>);

        render(
          <JobsPage />
        );

        expect(
          screen.getByText(
            "Carregando vagas..."
          )
        ).toBeInTheDocument();

        expect(
          screen.queryByTestId(
            "job-card"
          )
        ).not.toBeInTheDocument();
      }
    );

    it(
      "shows error state using the returned error message",
      () => {
        mockedUseJobs.mockReturnValue({
          data:
            undefined,

          isLoading:
            false,

          isError:
            true,

          error:
            new Error(
              "Falha na API"
            ),
        } as unknown as ReturnType<typeof useJobs>);

        render(
          <JobsPage />
        );

        expect(
          screen.getByRole(
            "heading",
            {
              name:
                "Não foi possível carregar as vagas",
            }
          )
        ).toBeInTheDocument();

        expect(
          screen.getByText(
            "Falha na API"
          )
        ).toBeInTheDocument();

        expect(
          screen.queryByTestId(
            "job-card"
          )
        ).not.toBeInTheDocument();
      }
    );

    it(
      "shows generic error for an unknown error object",
      () => {
        mockedUseJobs.mockReturnValue({
          data:
            undefined,

          isLoading:
            false,

          isError:
            true,

          error: {
            reason:
              "erro desconhecido",
          },
        } as unknown as ReturnType<typeof useJobs>);

        render(
          <JobsPage />
        );

        expect(
          screen.getByText(
            "Ocorreu um erro desconhecido."
          )
        ).toBeInTheDocument();
      }
    );

    it(
      "shows empty state when no jobs are available",
      () => {
        mockedUseJobs.mockReturnValue({
          data: [],

          isLoading:
            false,

          isError:
            false,

          error:
            null,
        } as unknown as ReturnType<typeof useJobs>);

        render(
          <JobsPage />
        );

        expect(
          screen.getByRole(
            "heading",
            {
              name:
                "Nenhuma vaga disponível",
            }
          )
        ).toBeInTheDocument();

        expect(
          screen.getByText(
            "Novas oportunidades serão exibidas aqui quando forem cadastradas."
          )
        ).toBeInTheDocument();

        expect(
          screen.queryByTestId(
            "job-card"
          )
        ).not.toBeInTheDocument();
      }
    );

    it(
      "treats non-array data as an empty result",
      () => {
        mockedUseJobs.mockReturnValue({
          data: {
            content: [],
          },

          isLoading:
            false,

          isError:
            false,

          error:
            null,
        } as unknown as ReturnType<typeof useJobs>);

        render(
          <JobsPage />
        );

        expect(
          screen.getByRole(
            "heading",
            {
              name:
                "Nenhuma vaga disponível",
            }
          )
        ).toBeInTheDocument();
      }
    );

    it(
      "renders all returned jobs",
      () => {
        mockedUseJobs.mockReturnValue({
          data: [
            {
              code: 1,

              name:
                "Analista Java",

              description:
                "Desenvolvimento backend",

              expirationDate:
                "2026-12-20",

              salary:
                9000,
            },

            {
              code: 2,

              name:
                "Analista ERP",

              description:
                "Sistemas empresariais",

              expirationDate:
                "2026-12-21",

              salary:
                8500,
            },
          ],

          isLoading:
            false,

          isError:
            false,

          error:
            null,
        } as unknown as ReturnType<typeof useJobs>);

        render(
          <JobsPage />
        );

        expect(
          screen.getAllByTestId(
            "job-card"
          )
        ).toHaveLength(
          2
        );

        expect(
          screen.getByRole(
            "heading",
            {
              name:
                "Analista Java",
            }
          )
        ).toBeInTheDocument();

        expect(
          screen.getByText(
            "Desenvolvimento backend"
          )
        ).toBeInTheDocument();

        expect(
          screen.getByRole(
            "heading",
            {
              name:
                "Analista ERP",
            }
          )
        ).toBeInTheDocument();

        expect(
          screen.getByText(
            "Sistemas empresariais"
          )
        ).toBeInTheDocument();
      }
    );

    it(
      "does not enable management actions in the public catalog",
      () => {
        mockedUseJobs.mockReturnValue({
          data: [
            {
              code: 1,

              name:
                "Analista Java",

              description:
                "Desenvolvimento backend",

              expirationDate:
                "2026-12-20",

              salary:
                9000,
            },
          ],

          isLoading:
            false,

          isError:
            false,

          error:
            null,
        } as unknown as ReturnType<typeof useJobs>);

        render(
          <JobsPage />
        );

        expect(
          screen.getByTestId(
            "job-card"
          )
        ).toHaveAttribute(
          "data-management-actions",
          "false"
        );
      }
    );
  }
);