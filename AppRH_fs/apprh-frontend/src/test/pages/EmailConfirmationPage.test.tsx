import {
  render,
  screen,
} from "@testing-library/react";

import {
  MemoryRouter,
} from "react-router-dom";

import {
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from "vitest";

import {
  EmailConfirmationPage,
} from "../../pages/EmailConfirmationPage";

import {
  useEmailConfirmation,
} from "../../hooks/useEmailConfirmation";

vi.mock(
  "../../hooks/useEmailConfirmation",
  () => ({
    useEmailConfirmation:
      vi.fn(),
  })
);

const mockedHook =
  vi.mocked(
    useEmailConfirmation
  );

function renderPage(
  route: string
) {
  return render(
    <MemoryRouter
      initialEntries={[
        route,
      ]}
    >
      <EmailConfirmationPage />
    </MemoryRouter>
  );
}

describe(
  "EmailConfirmationPage",
  () => {
    beforeEach(
      () => {
        vi.clearAllMocks();

        mockedHook.mockReturnValue({
          status:
            "idle",
          message:
            "",
          confirm:
            vi.fn(),
          resend:
            vi.fn(),
        });
      }
    );

    it(
      "shows an error when the token is missing",
      () => {
        renderPage(
          "/confirm-email"
        );

        expect(
          screen.getByRole(
            "heading",
            {
              name:
                "Link inválido",
            }
          )
        ).toBeInTheDocument();

        expect(
          screen.getByRole(
            "alert"
          )
        ).toHaveTextContent(
          "O link de confirmação não possui um token válido."
        );
      }
    );

    it(
      "requests confirmation with the URL token",
      () => {
        const confirm =
          vi.fn();

        mockedHook.mockReturnValue({
          status:
            "loading",
          message:
            "Confirmando seu endereço de e-mail...",
          confirm,
          resend:
            vi.fn(),
        });

        renderPage(
          "/confirm-email?token=token-seguro"
        );

        expect(
          confirm
        ).toHaveBeenCalledWith(
          "token-seguro"
        );

        expect(
          screen.getByRole(
            "status"
          )
        ).toHaveTextContent(
          "Confirmando seu endereço de e-mail..."
        );
      }
    );

    it(
      "shows the login link after success",
      () => {
        mockedHook.mockReturnValue({
          status:
            "success",
          message:
            "E-mail confirmado com sucesso",
          confirm:
            vi.fn(),
          resend:
            vi.fn(),
        });

        renderPage(
          "/confirm-email?token=token-seguro"
        );

        expect(
          screen.getByRole(
            "link",
            {
              name:
                "Ir para o login",
            }
          )
        ).toHaveAttribute(
          "href",
          "/login"
        );
      }
    );

    it(
      "shows the resend link for an expired token",
      () => {
        mockedHook.mockReturnValue({
          status:
            "expired",
          message:
            "Token expirado",
          confirm:
            vi.fn(),
          resend:
            vi.fn(),
        });

        renderPage(
          "/confirm-email?token=token-expirado"
        );

        expect(
          screen.getByRole(
            "link",
            {
              name:
                "Solicitar novo e-mail",
            }
          )
        ).toHaveAttribute(
          "href",
          "/verificacao-pendente"
        );
      }
    );
  }
);
