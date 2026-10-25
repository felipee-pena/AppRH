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
  EditJobModal,
} from "../../../components/edit-job-modal/EditJobModal";

import {
  useUpdateJob,
} from "../../../hooks/useUpdateJob";

import type {
  JobData,
} from "../../../interfaces/JobData";

vi.mock(
  "../../../hooks/useUpdateJob",
  () => ({
    useUpdateJob:
      vi.fn(),
  })
);

const mockedUpdateJob =
  vi.mocked(
    useUpdateJob
  );

const job: JobData = {
  code: 10,
  name:
    "Analista Java",
  description:
    "Descrição original",
  expirationDate:
    "2026-12-20",
  salary:
    9000,
  contactEmail:
    "vagas@example.com",
  company: {
    id: 1,
    name:
      "Empresa Teste",
  },
};

describe(
  "EditJobModal",
  () => {
    beforeEach(
      () => {
        vi.clearAllMocks();

        mockedUpdateJob.mockReturnValue({
          mutate:
            vi.fn(),
          isPending:
            false,
          isSuccess:
            false,
          isError:
            false,
          error:
            null,
          reset:
            vi.fn(),
        } as unknown as ReturnType<typeof useUpdateJob>);
      }
    );

    it(
      "loads current job values",
      () => {
        render(
          <EditJobModal
            job={job}
            closeModal={vi.fn()}
          />
        );

        expect(
          screen.getByLabelText(
            "Nome da vaga"
          )
        ).toHaveValue(
          "Analista Java"
        );

        expect(
          screen.getByLabelText(
            "Salário"
          )
        ).toHaveValue(
          9000
        );
      }
    );

    it(
      "updates the job",
      () => {
        const mutate =
          vi.fn();

        mockedUpdateJob.mockReturnValue({
          mutate,
          isPending:
            false,
          isSuccess:
            false,
          isError:
            false,
          error:
            null,
          reset:
            vi.fn(),
        } as unknown as ReturnType<typeof useUpdateJob>);

        render(
          <EditJobModal
            job={job}
            closeModal={vi.fn()}
          />
        );

        fireEvent.change(
          screen.getByLabelText(
            "Nome da vaga"
          ),
          {
            target: {
              value:
                "  Analista Java Pleno  ",
            },
          }
        );

        fireEvent.change(
          screen.getByLabelText(
            "Descrição da vaga"
          ),
          {
            target: {
              value:
                "  Nova descrição  ",
            },
          }
        );

        fireEvent.click(
          screen.getByRole(
            "button",
            {
              name:
                "Salvar alterações",
            }
          )
        );

        expect(
          mutate
        ).toHaveBeenCalledWith({
          code:
            10,
          job: {
            name:
              "Analista Java Pleno",
            description:
              "Nova descrição",
            expirationDate:
              "2026-12-20",
            salary:
              9000,
            contactEmail:
              "vagas@example.com",
            companyId:
              1,
          },
        });
      }
    );

    it(
      "closes using Escape",
      () => {
        const closeModal =
          vi.fn();

        render(
          <EditJobModal
            job={job}
            closeModal={closeModal}
          />
        );

        fireEvent.keyDown(
          window,
          {
            key:
              "Escape",
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