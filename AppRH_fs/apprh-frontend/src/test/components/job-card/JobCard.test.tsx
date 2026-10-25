import {
  fireEvent,
  render,
  screen,
} from "@testing-library/react";

import {
  describe,
  expect,
  it,
  vi,
} from "vitest";

import {
  JobCard,
} from "../../../components/job-card/JobCard";

import type {
  JobData,
} from "../../../interfaces/JobData";

const job: JobData = {
  code: 10,
  name: "Analista de Sistemas",
  description:
    "Desenvolvimento de soluções corporativas.",
  expirationDate:
    "2026-12-20",
  createdAt:
    "2026-10-25T01:00:00-03:00",
  salary: 9000,
  contactEmail:
    "vagas@example.com",

  company: {
    id: 1,
    name: "Empresa Teste",
  },
};

describe(
  "JobCard",
  () => {
    it(
      "displays job information",
      () => {
        render(
          <JobCard
            job={job}
          />
        );

        expect(
          screen.getByText(
            "Analista de Sistemas"
          )
        ).toBeInTheDocument();

        expect(
          screen.getByText(
            "Empresa Teste"
          )
        ).toBeInTheDocument();

        expect(
          screen.getByText(
            "Desenvolvimento de soluções corporativas."
          )
        ).toBeInTheDocument();
      }
    );

    it(
      "opens the details modal when selected",
      () => {
        render(
          <JobCard
            job={job}
          />
        );

        fireEvent.click(
          screen.getByRole(
            "button",
            {
              name:
                /Analista de Sistemas/i,
            }
          )
        );

        expect(
          screen.getByRole(
            "dialog"
          )
        ).toBeInTheDocument();
      }
    );

    it(
      "executes the edit callback",
      () => {
        const onEdit =
          vi.fn();

        render(
          <JobCard
            job={job}
            showManagementActions
            onEdit={onEdit}
          />
        );

        fireEvent.click(
          screen.getByRole(
            "button",
            {
              name: "Editar",
            }
          )
        );

        expect(
          onEdit
        ).toHaveBeenCalledWith(
          job
        );
      }
    );

    it(
      "executes the delete callback",
      () => {
        const onDelete =
          vi.fn();

        render(
          <JobCard
            job={job}
            showManagementActions
            onDelete={onDelete}
          />
        );

        fireEvent.click(
          screen.getByRole(
            "button",
            {
              name: "Excluir",
            }
          )
        );

        expect(
          onDelete
        ).toHaveBeenCalledWith(
          10
        );
      }
    );
  }
);