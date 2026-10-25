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
  ConfirmationModal,
} from "../../../components/confirmation-modal/ConfirmationModal";

describe(
  "ConfirmationModal",
  () => {
    it(
      "renders title and description",
      () => {
        render(
          <ConfirmationModal
            title="Excluir vaga"
            description="Esta ação não poderá ser desfeita."
            onConfirm={vi.fn()}
            onCancel={vi.fn()}
          />
        );

        expect(
          screen.getByRole(
            "heading",
            {
              name:
                "Excluir vaga",
            }
          )
        ).toBeInTheDocument();

        expect(
          screen.getByText(
            "Esta ação não poderá ser desfeita."
          )
        ).toBeInTheDocument();
      }
    );

    it(
      "executes confirmation",
      () => {
        const onConfirm =
          vi.fn();

        render(
          <ConfirmationModal
            title="Excluir vaga"
            description="Confirma a exclusão?"
            onConfirm={onConfirm}
            onCancel={vi.fn()}
          />
        );

        fireEvent.click(
          screen.getByRole(
            "button",
            {
              name:
                "Excluir",
            }
          )
        );

        expect(
          onConfirm
        ).toHaveBeenCalledTimes(
          1
        );
      }
    );

    it(
      "executes cancellation",
      () => {
        const onCancel =
          vi.fn();

        render(
          <ConfirmationModal
            title="Excluir vaga"
            description="Confirma a exclusão?"
            onConfirm={vi.fn()}
            onCancel={onCancel}
          />
        );

        fireEvent.click(
          screen.getByRole(
            "button",
            {
              name:
                "Cancelar",
            }
          )
        );

        expect(
          onCancel
        ).toHaveBeenCalledTimes(
          1
        );
      }
    );

    it(
      "disables actions while pending",
      () => {
        render(
          <ConfirmationModal
            title="Excluir vaga"
            description="Confirma a exclusão?"
            isPending
            onConfirm={vi.fn()}
            onCancel={vi.fn()}
          />
        );

        expect(
          screen.getByRole(
            "button",
            {
              name:
                "Cancelar",
            }
          )
        ).toBeDisabled();

        expect(
          screen.getByRole(
            "button",
            {
              name:
                "Excluindo...",
            }
          )
        ).toBeDisabled();
      }
    );
  }
);