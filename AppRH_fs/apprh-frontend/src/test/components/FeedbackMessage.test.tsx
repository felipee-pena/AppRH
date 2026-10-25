import {
  render,
  screen,
} from "@testing-library/react";

import {
  describe,
  expect,
  it,
} from "vitest";

import {
  FeedbackMessage,
} from "../../components/FeedbackMessage";

describe(
  "FeedbackMessage",
  () => {
    it(
      "renders success feedback",
      () => {
        render(
          <FeedbackMessage
            type="success"
            message="E-mail confirmado com sucesso"
          />
        );

        expect(
          screen.getByRole(
            "status"
          )
        ).toHaveTextContent(
          "E-mail confirmado com sucesso"
        );
      }
    );

    it(
      "renders error feedback as alert",
      () => {
        render(
          <FeedbackMessage
            type="error"
            message="Token inválido"
          />
        );

        expect(
          screen.getByRole(
            "alert"
          )
        ).toHaveTextContent(
          "Token inválido"
        );
      }
    );

    it(
      "renders informational feedback",
      () => {
        render(
          <FeedbackMessage
            type="info"
            message="Processando solicitação"
          />
        );

        expect(
          screen.getByRole(
            "status"
          )
        ).toHaveTextContent(
          "Processando solicitação"
        );
      }
    );
  }
);