import {
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from "vitest";

import {
  api,
} from "../../services/api";

import {
  confirmEmail,
  login,
  resendEmailConfirmation,
} from "../../services/auth-service";

vi.mock(
  "../../services/api",
  () => ({
    api: {
      post:
        vi.fn(),
    },
  })
);

const mockedPost =
  vi.mocked(
    api.post
  );

describe(
  "auth-service",
  () => {
    beforeEach(
      () => {
        vi.clearAllMocks();
      }
    );

    it(
      "sends login credentials",
      async () => {
        const response = {
          accessToken:
            "jwt-token",
        };

        mockedPost.mockResolvedValue({
          data:
            response,
        });

        const result =
          await login({
            login:
              "usuario@example.com",
            password:
              "senha-segura",
          });

        expect(
          mockedPost
        ).toHaveBeenCalledWith(
          "/auth/login",
          {
            login:
              "usuario@example.com",
            password:
              "senha-segura",
          }
        );

        expect(
          result
        ).toEqual(
          response
        );
      }
    );

    it(
      "sends the confirmation token",
      async () => {
        mockedPost.mockResolvedValue({
          data: {
            message:
              "E-mail confirmado",
          },
        });

        const result =
          await confirmEmail({
            token:
              "token-seguro",
          });

        expect(
          mockedPost
        ).toHaveBeenCalledWith(
          "/auth/email/confirm",
          {
            token:
              "token-seguro",
          }
        );

        expect(
          result.message
        ).toBe(
          "E-mail confirmado"
        );
      }
    );

    it(
      "requests a new verification email",
      async () => {
        mockedPost.mockResolvedValue({
          data: {
            message:
              "Novo e-mail enviado",
          },
        });

        const result =
          await resendEmailConfirmation({
            login:
              "usuario@example.com",
          });

        expect(
          mockedPost
        ).toHaveBeenCalledWith(
          "/auth/email/resend",
          {
            login:
              "usuario@example.com",
          }
        );

        expect(
          result.message
        ).toBe(
          "Novo e-mail enviado"
        );
      }
    );
  }
);