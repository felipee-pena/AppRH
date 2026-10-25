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
  CreateJobModal,
} from "../../../components/create-job-modal/CreateJobModal";

import {
  useAuth,
} from "../../../hooks/useAuth";

import {
  useCompanies,
} from "../../../hooks/useCompanies";

import {
  useCreateJob,
} from "../../../hooks/useCreateJob";

import {
  useRhCompany,
} from "../../../hooks/useRhCompany";

vi.mock(
  "../../../hooks/useAuth",
  () => ({
    useAuth:
      vi.fn(),
  })
);

vi.mock(
  "../../../hooks/useCompanies",
  () => ({
    useCompanies:
      vi.fn(),
  })
);

vi.mock(
  "../../../hooks/useCreateJob",
  () => ({
    useCreateJob:
      vi.fn(),
  })
);

vi.mock(
  "../../../hooks/useRhCompany",
  () => ({
    useRhCompany:
      vi.fn(),
  })
);

const mockedUseAuth =
  vi.mocked(
    useAuth
  );

const mockedUseCompanies =
  vi.mocked(
    useCompanies
  );

const mockedUseCreateJob =
  vi.mocked(
    useCreateJob
  );

const mockedUseRhCompany =
  vi.mocked(
    useRhCompany
  );

function configureMocks(
  mutate: ReturnType<typeof vi.fn>
) {
  mockedUseAuth.mockReturnValue({
    user: {
      role: "ADMIN",
    },

    isAuthenticated: true,
    isInitializing: false,

    hasRole:
      vi.fn(
        (role: string) =>
          role === "ADMIN"
      ),

    login:
      vi.fn(),

    logout:
      vi.fn(),
  } as unknown as ReturnType<typeof useAuth>);

  mockedUseCompanies.mockReturnValue({
    data: [
      {
        id: 1,
        name: "Empresa Teste",
      },
    ],

    isLoading: false,
  } as unknown as ReturnType<typeof useCompanies>);

  mockedUseRhCompany.mockReturnValue({
    data: undefined,
    isLoading: false,
  } as unknown as ReturnType<typeof useRhCompany>);

  mockedUseCreateJob.mockReturnValue({
    mutate,
    isPending: false,
    isSuccess: false,
    isError: false,
    error: null,
    reset: vi.fn(),
  } as unknown as ReturnType<typeof useCreateJob>);
}

describe(
  "CreateJobModal",
  () => {
    beforeEach(
      () => {
        vi.clearAllMocks();

        document.body.classList.remove(
          "modal-open"
        );
      }
    );

    it(
      "creates a job with normalized values",
      () => {
        const mutate =
          vi.fn();

        configureMocks(
          mutate
        );

        render(
          <CreateJobModal
            closeModal={vi.fn()}
          />
        );

        fireEvent.change(
          screen.getByLabelText(
            "Nome da vaga"
          ),
          {
            target: {
              value: "  Analista Java  ",
            },
          }
        );

        fireEvent.change(
          screen.getByLabelText(
            "Data de expiração"
          ),
          {
            target: {
              value: "2026-12-20",
            },
          }
        );

        fireEvent.change(
          screen.getByLabelText(
            "Salário"
          ),
          {
            target: {
              value: "9.500,00",
            },
          }
        );

        fireEvent.change(
          screen.getByLabelText(
            "E-mail de contato"
          ),
          {
            target: {
              value: "  VAGAS@EXAMPLE.COM  ",
            },
          }
        );

        fireEvent.change(
          screen.getByLabelText(
            "Descrição da vaga"
          ),
          {
            target: {
              value: "  Desenvolvimento backend  ",
            },
          }
        );

        fireEvent.click(
          screen.getByRole(
            "button",
            {
              name: "Empresa Teste",
            }
          )
        );

        fireEvent.click(
          screen.getByRole(
            "button",
            {
              name: "Cadastrar vaga",
            }
          )
        );

        expect(
          mutate
        ).toHaveBeenCalledWith({
          name: "Analista Java",
          description: "Desenvolvimento backend",
          expirationDate: "2026-12-20",
          salary: 9500,
          contactEmail: "vagas@example.com",
          companyId: 1,
        });
      }
    );

    it(
      "closes with Escape",
      () => {
        const closeModal =
          vi.fn();

        configureMocks(
          vi.fn()
        );

        render(
          <CreateJobModal
            closeModal={closeModal}
          />
        );

        fireEvent.keyDown(
          window,
          {
            key: "Escape",
          }
        );

        expect(
          closeModal
        ).toHaveBeenCalledTimes(
          1
        );
      }
    );
  }
);