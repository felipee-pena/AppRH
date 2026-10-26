import {
  fireEvent,
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
  JobManagementPage,
} from "../../../pages/job-management/JobManagementPage";

import {
  useManagedJobs,
} from "../../../hooks/useManagedJobs";

import {
  useDeleteJob,
} from "../../../hooks/useDeleteJob";

import type {
  JobData,
} from "../../../interfaces/JobData";

vi.mock(
  "../../../hooks/useManagedJobs",
  () => ({
    useManagedJobs:
      vi.fn(),
  })
);

vi.mock(
  "../../../hooks/useDeleteJob",
  () => ({
    useDeleteJob:
      vi.fn(),
  })
);

vi.mock(
  "../../../components/header/AppHeader",
  () => ({
    AppHeader:
      () => (
        <header>
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
        onEdit,
        onDelete,
      }: {
        job: JobData;
        onEdit?(
          job: JobData
        ): void;
        onDelete?(
          code: number
        ): void;
      }) => (
        <article>
          <h2>
            {job.name}
          </h2>

          <button
            type="button"
            onClick={
              () =>
                onEdit?.(
                  job
                )
            }
          >
            Editar {job.name}
          </button>

          <button
            type="button"
            onClick={
              () =>
                onDelete?.(
                  job.code as number
                )
            }
          >
            Excluir {job.name}
          </button>
        </article>
      ),
  })
);

vi.mock(
  "../../../components/create-job-modal/CreateJobModal",
  () => ({
    CreateJobModal:
      ({
        closeModal,
      }: {
        closeModal(): void;
      }) => (
        <section>
          <p>
            Modal de criação
          </p>

          <button
            type="button"
            onClick={
              closeModal
            }
          >
            Fechar criação
          </button>
        </section>
      ),
  })
);

vi.mock(
  "../../../components/edit-job-modal/EditJobModal",
  () => ({
    EditJobModal:
      ({
        job,
        closeModal,
      }: {
        job: JobData;
        closeModal(): void;
      }) => (
        <section>
          <p>
            Editando {job.name}
          </p>

          <button
            type="button"
            onClick={
              closeModal
            }
          >
            Fechar edição
          </button>
        </section>
      ),
  })
);

vi.mock(
  "../../../components/confirmation-modal/ConfirmationModal",
  () => ({
    ConfirmationModal:
      ({
        title,
        description,
        onCancel,
        onConfirm,
      }: {
        title: string;
        description: string;
        onCancel(): void;
        onConfirm(): void;
      }) => (
        <section>
          <h2>
            {title}
          </h2>

          <p>
            {description}
          </p>

          <button
            type="button"
            onClick={
              onCancel
            }
          >
            Cancelar exclusão
          </button>

          <button
            type="button"
            onClick={
              onConfirm
            }
          >
            Confirmar exclusão
          </button>
        </section>
      ),
  })
);

const mockedUseManagedJobs =
  vi.mocked(
    useManagedJobs
  );

const mockedUseDeleteJob =
  vi.mocked(
    useDeleteJob
  );

const job: JobData = {
  code: 10,
  name:
    "Analista ERP",
  description:
    "Sistemas empresariais",
  expirationDate:
    "2026-12-20",
  salary:
    9000,
};

function configureDeleteHook(
  mutate = vi.fn(),
  overrides: Record<
    string,
    unknown
  > = {}
) {
  mockedUseDeleteJob.mockReturnValue({
    mutate,
    isPending:
      false,
    isError:
      false,
    error:
      null,
    reset:
      vi.fn(),
    ...overrides,
  } as unknown as ReturnType<typeof useDeleteJob>);

  return mutate;
}

describe(
  "JobManagementPage",
  () => {
    beforeEach(
      () => {
        vi.clearAllMocks();

        configureDeleteHook();
      }
    );

    it(
      "shows loading state",
      () => {
        mockedUseManagedJobs.mockReturnValue({
          data:
            undefined,
          isLoading:
            true,
          isError:
            false,
          error:
            null,
        } as unknown as ReturnType<typeof useManagedJobs>);

        render(
          <JobManagementPage />
        );

        expect(
          screen.getByText(
            "Carregando vagas gerenciadas..."
          )
        ).toBeInTheDocument();
      }
    );

    it(
      "shows request error",
      () => {
        mockedUseManagedJobs.mockReturnValue({
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
        } as unknown as ReturnType<typeof useManagedJobs>);

        render(
          <JobManagementPage />
        );

        expect(
          screen.getByText(
            "Falha na API"
          )
        ).toBeInTheDocument();
      }
    );

    it(
      "shows empty state",
      () => {
        mockedUseManagedJobs.mockReturnValue({
          data: [],
          isLoading:
            false,
          isError:
            false,
          error:
            null,
        } as unknown as ReturnType<typeof useManagedJobs>);

        render(
          <JobManagementPage />
        );

        expect(
          screen.getByText(
            "Nenhuma vaga para gerenciar"
          )
        ).toBeInTheDocument();
      }
    );

    it(
      "opens and closes create modal",
      () => {
        mockedUseManagedJobs.mockReturnValue({
          data: [],
          isLoading:
            false,
          isError:
            false,
          error:
            null,
        } as unknown as ReturnType<typeof useManagedJobs>);

        render(
          <JobManagementPage />
        );

        fireEvent.click(
          screen.getByRole(
            "button",
            {
              name:
                "Nova vaga",
            }
          )
        );

        expect(
          screen.getByText(
            "Modal de criação"
          )
        ).toBeInTheDocument();

        fireEvent.click(
          screen.getByRole(
            "button",
            {
              name:
                "Fechar criação",
            }
          )
        );

        expect(
          screen.queryByText(
            "Modal de criação"
          )
        ).not.toBeInTheDocument();
      }
    );

    it(
      "opens and closes edit modal",
      () => {
        mockedUseManagedJobs.mockReturnValue({
          data: [
            job,
          ],
          isLoading:
            false,
          isError:
            false,
          error:
            null,
        } as unknown as ReturnType<typeof useManagedJobs>);

        render(
          <JobManagementPage />
        );

        fireEvent.click(
          screen.getByRole(
            "button",
            {
              name:
                "Editar Analista ERP",
            }
          )
        );

        expect(
          screen.getByText(
            "Editando Analista ERP"
          )
        ).toBeInTheDocument();

        fireEvent.click(
          screen.getByRole(
            "button",
            {
              name:
                "Fechar edição",
            }
          )
        );

        expect(
          screen.queryByText(
            "Editando Analista ERP"
          )
        ).not.toBeInTheDocument();
      }
    );

    it(
      "requests job deletion",
      () => {
        const deleteJob =
          configureDeleteHook();

        mockedUseManagedJobs.mockReturnValue({
          data: [
            job,
          ],
          isLoading:
            false,
          isError:
            false,
          error:
            null,
        } as unknown as ReturnType<typeof useManagedJobs>);

        render(
          <JobManagementPage />
        );

        fireEvent.click(
          screen.getByRole(
            "button",
            {
              name:
                "Excluir Analista ERP",
            }
          )
        );

        expect(
          screen.getByRole(
            "heading",
            {
              name:
                "Excluir vaga?",
            }
          )
        ).toBeInTheDocument();

        expect(
          screen.getByText(
            'A vaga "Analista ERP" será excluída permanentemente.'
          )
        ).toBeInTheDocument();

        fireEvent.click(
          screen.getByRole(
            "button",
            {
              name:
                "Confirmar exclusão",
            }
          )
        );

        expect(
          deleteJob
        ).toHaveBeenCalledWith(
          10,
          expect.objectContaining({
            onSuccess:
              expect.any(
                Function
              ),
          })
        );
      }
    );

    it(
      "shows delete error",
      () => {
        configureDeleteHook(
          vi.fn(),
          {
            isError:
              true,
            error:
              new Error(
                "Vaga vinculada a candidato"
              ),
          }
        );

        mockedUseManagedJobs.mockReturnValue({
          data: [
            job,
          ],
          isLoading:
            false,
          isError:
            false,
          error:
            null,
        } as unknown as ReturnType<typeof useManagedJobs>);

        render(
          <JobManagementPage />
        );


        fireEvent.click(
          screen.getByRole(
            "button",
            {
              name:
                "Excluir Analista ERP",
            }
          )
        );

        expect(
          screen.getByText(
            "Vaga vinculada a candidato"
          )
        ).toBeInTheDocument();
      }
    );
  }
);