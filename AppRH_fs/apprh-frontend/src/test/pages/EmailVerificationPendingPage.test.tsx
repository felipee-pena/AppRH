import {
  fireEvent,
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
  EmailVerificationPendingPage,
} from "../../pages/EmailVerificationPendingPage";

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

describe(
  "EmailVerificationPendingPage",
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
      "shows the login field when navigation state is absent",
      () => {
        render(
          <MemoryRouter>
            <EmailVerificationPendingPage />
          </MemoryRouter>
        );

        expect(
          screen.getByLabelText(
            "E-mail"
          )
        ).toBeInTheDocument();

        expect(
          screen.getByRole(
            "button",
            {
              name:
                "Reenviar e-mail",
            }
          )
        ).toBeDisabled();
      }
    );

    it(
      "requests resend using the entered login",
      () => {
        const resend =
          vi.fn();

        mockedHook.mockReturnValue({
          status:
            "idle",
          message:
            "",
          confirm:
            vi.fn(),
          resend,
        });

        render(
          <MemoryRouter>
            <EmailVerificationPendingPage />
          </MemoryRouter>
        );

        fireEvent.change(
          screen.getByLabelText(
            "E-mail"
          ),
          {
            target: {
              value:
                "usuario@example.com",
            },
          }
        );

        fireEvent.click(
          screen.getByRole(
            "button",
            {
              name:
                "Reenviar e-mail",
            }
          )
        );

        expect(
          resend
        ).toHaveBeenCalledWith(
          "usuario@example.com"
        );
      }
    );

    it(
      "uses the login received through navigation state",
      () => {
        const resend =
          vi.fn();

        mockedHook.mockReturnValue({
          status:
            "idle",
          message:
            "",
          confirm:
            vi.fn(),
          resend,
        });

        render(
          <MemoryRouter
            initialEntries={[
              {
                pathname:
                  "/verificacao-pendente",
                state: {
                  login:
                    "usuario@example.com",
                },
              },
            ]}
          >
            <EmailVerificationPendingPage />
          </MemoryRouter>
        );

        expect(
          screen.queryByLabelText(
            "E-mail"
          )
        ).not.toBeInTheDocument();

        fireEvent.click(
          screen.getByRole(
            "button",
            {
              name:
                "Reenviar e-mail",
            }
          )
        );

        expect(
          resend
        ).toHaveBeenCalledWith(
          "usuario@example.com"
        );
      }
    );

    it(
      "shows loading feedback",
      () => {
        mockedHook.mockReturnValue({
          status:
            "loading",
          message:
            "Solicitando um novo e-mail...",
          confirm:
            vi.fn(),
          resend:
            vi.fn(),
        });

        render(
          <MemoryRouter
            initialEntries={[
              {
                pathname:
                  "/verificacao-pendente",
                state: {
                  login:
                    "usuario@example.com",
                },
              },
            ]}
          >
            <EmailVerificationPendingPage />
          </MemoryRouter>
        );

        expect(
          screen.getByRole(
            "button",
            {
              name:
                "Enviando...",
            }
          )
        ).toBeDisabled();

        expect(
          screen.getByRole(
            "status"
          )
        ).toHaveTextContent(
          "Solicitando um novo e-mail..."
        );
      }
    );
  }
);