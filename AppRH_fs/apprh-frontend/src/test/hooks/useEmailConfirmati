import {
  act,
  renderHook,
  waitFor,
} from "@testing-library/react";

import axios from "axios";

import {
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from "vitest";

import {
  useEmailConfirmation,
} from "../../hooks/useEmailConfirmation";

import {
  confirmEmail,
  resendEmailConfirmation,
} from "../../services/auth-service";

vi.mock(
  "../../services/auth-service",
  () => ({
    confirmEmail:
      vi.fn(),

    resendEmailConfirmation:
      vi.fn(),
  })
);

vi.mock(
  "axios",
  async () => {
    const actual =
      await vi.importActual<
        typeof import("axios")
      >(
        "axios"
      );

    return {
      ...actual,
      default: {
        ...actual.default,

        isAxiosError:
          vi.fn(),
      },
    };
  }
);

const mockedConfirmEmail =
  vi.mocked(
    confirmEmail
  );

const mockedResend =
  vi.mocked(
    resendEmailConfirmation
  );

const mockedIsAxiosError =
  vi.mocked(
    axios.isAxiosError
  );

function createAxiosError(
  status: number,
  message: string
) {
  return {
    response: {
      status,
      data: {
        message,
      },
    },
  };
}

describe(
  "useEmailConfirmation",
  () => {
    beforeEach(
      () => {
        vi.clearAllMocks();
      }
    );

    it(
      "confirms an email successfully",
      async () => {
        mockedConfirmEmail.mockResolvedValue({
          message:
            "E-mail confirmado com sucesso",
        });

        const {
          result,
        } =
          renderHook(
            () =>
              useEmailConfirmation()
          );

        await act(
          async () => {
            await result.current.confirm(
              "token-seguro"
            );
          }
        );

        expect(
          mockedConfirmEmail
        ).toHaveBeenCalledWith({
          token:
            "token-seguro",
        });

        expect(
          result.current.status
        ).toBe(
          "success"
        );

        expect(
          result.current.message
        ).toBe(
          "E-mail confirmado com sucesso"
        );
      }
    );

    it(
      "handles an expired token",
      async () => {
        mockedIsAxiosError.mockReturnValue(
          true
        );

        mockedConfirmEmail.mockRejectedValue(
          createAxiosError(
            400,
            "Token expirado"
          )
        );

        const {
          result,
        } =
          renderHook(
            () =>
              useEmailConfirmation()
          );

        await act(
          async () => {
            await result.current.confirm(
              "token-expirado"
            );
          }
        );

        expect(
          result.current.status
        ).toBe(
          "expired"
        );

        expect(
          result.current.message
        ).toBe(
          "Token expirado"
        );
      }
    );

    it(
      "handles an already-used token",
      async () => {
        mockedIsAxiosError.mockReturnValue(
          true
        );

        mockedConfirmEmail.mockRejectedValue(
          createAxiosError(
            400,
            "Token já utilizado"
          )
        );

        const {
          result,
        } =
          renderHook(
            () =>
              useEmailConfirmation()
          );

        await act(
          async () => {
            await result.current.confirm(
              "token-utilizado"
            );
          }
        );

        expect(
          result.current.status
        ).toBe(
          "already-used"
        );
      }
    );

    it(
      "handles an invalid token",
      async () => {
        mockedIsAxiosError.mockReturnValue(
          true
        );

        mockedConfirmEmail.mockRejectedValue(
          createAxiosError(
            400,
            "Token inválido"
          )
        );

        const {
          result,
        } =
          renderHook(
            () =>
              useEmailConfirmation()
          );

        await act(
          async () => {
            await result.current.confirm(
              "token-invalido"
            );
          }
        );

        expect(
          result.current.status
        ).toBe(
          "invalid"
        );
      }
    );

    it(
      "normalizes login when requesting another email",
      async () => {
        mockedResend.mockResolvedValue({
          message:
            "Novo e-mail enviado",
        });

        const {
          result,
        } =
          renderHook(
            () =>
              useEmailConfirmation()
          );

        await act(
          async () => {
            await result.current.resend(
              "  Usuario@Example.COM  "
            );
          }
        );

        await waitFor(
          () => {
            expect(
              result.current.status
            ).toBe(
              "success"
            );
          }
        );

        expect(
          mockedResend
        ).toHaveBeenCalledWith({
          login:
            "usuario@example.com",
        });
      }
    );
  }
);