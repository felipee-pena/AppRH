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
  JobDetailsModal,
} from "../../../components/job-details-modal/JobDetailsModal";

import type {
  JobData,
} from "../../../interfaces/JobData";

const job: JobData = {
  code: 10,
  name: "Analista de Sistemas",
  description:
    "Desenvolvimento de aplicações empresariais.",
  expirationDate:
    "2026-12-20",
  createdAt:
    "2026-10-25T01:00:00-03:00",
  salary: 8500,
  contactEmail:
    "vagas@example.com",

  company: {
    id: 1,
    name: "Empresa Teste",
  },
};

describe(
  "JobDetailsModal",
  () => {
    it(
      "displays job information",
      () => {
        render(
          <JobDetailsModal
            job={job}
            onClose={vi.fn()}
          />
        );

        expect(
          screen.getByRole(
            "dialog"
          )
        ).toBeInTheDocument();

        expect(
          screen.getByText(
            "Analista de Sistemas"
          )
        ).toBeInTheDocument();

        expect(
          screen.getAllByText(
            "Empresa Teste"
          ).length
        ).toBeGreaterThan(
          0
        );

        expect(
          screen.getByText(
            "Desenvolvimento de aplicações empresariais."
          )
        ).toBeInTheDocument();
      }
    );

    it(
      "closes when the close button is selected",
      () => {
        const onClose =
          vi.fn();

        render(
          <JobDetailsModal
            job={job}
            onClose={onClose}
          />
        );

        fireEvent.click(
          screen.getByRole(
            "button",
            {
              name:
                "Fechar detalhes da vaga",
            }
          )
        );

        expect(
          onClose
        ).toHaveBeenCalledTimes(
          1
        );
      }
    );

    it(
      "closes when Escape is pressed",
      () => {
        const onClose =
          vi.fn();

        render(
          <JobDetailsModal
            job={job}
            onClose={onClose}
          />
        );

        fireEvent.keyDown(
          window,
          {
            key: "Escape",
          }
        );

        expect(
          onClose
        ).toHaveBeenCalledTimes(
          1
        );
      }
    );

    it(
      "blocks and restores background scrolling",
      () => {
        const {
          unmount,
        } =
          render(
            <JobDetailsModal
              job={job}
              onClose={vi.fn()}
            />
          );

        expect(
          document.body.style.overflow
        ).toBe(
          "hidden"
        );

        unmount();

        expect(
          document.body.style.overflow
        ).toBe(
          ""
        );
      }
    );
  }
);