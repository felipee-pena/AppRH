import axios from "axios";

import {
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from "vitest";

import {
  beginMfaSetup,
  confirmEmail,
  confirmMfaSetup,
  disableMfa,
  exchangeOAuthCode,
  getMfaStatus,
  login,
  regenerateRecoveryCodes,
  resendEmailConfirmation,
  verifyMfa,
} from "../../services/auth-service";

import {
  getAccessToken,
} from "../../services/token-storage";

vi.mock(
  "axios"
);

vi.mock(
  "../../services/token-storage",
  () => ({
    getAccessToken:
      vi.fn(),
    saveAccessToken:
      vi.fn(),
    removeAccessToken:
      vi.fn(),
  })
);

const mockedAxios =
  vi.mocked(
    axios,
    true
  );

const mockedGetAccessToken =
  vi.mocked(
    getAccessToken
  );

describe(
  "auth-service",
  () => {
    beforeEach(
      () => {
        vi.clearAllMocks();

        mockedGetAccessToken
          .mockReturnValue(
            null
          );
      }
    );

    it(
      "sends normalized login credentials",
      async () => {
        mockedAxios.post
          .mockResolvedValueOnce({
            data: {
              mfa_required: false,
              access_token:
                "jwt-token",
              token_type:
                "Bearer",
              expires_in:
                7200,
            },
          });

        const result =
          await login({
            login:
              "  USER@EXAMPLE.COM  ",
            password:
              "password",
          });

        expect(
          mockedAxios.post
        ).toHaveBeenCalledWith(
          expect.stringContaining(
            "/auth/login"
          ),
          {
            login:
              "user@example.com",
            password:
              "password",
          },
          expect.objectContaining({
            headers:
              expect.objectContaining({
                "Content-Type":
                  "application/json",
              }),
          })
        );

        expect(
          result.access_token
        ).toBe(
          "jwt-token"
        );
      }
    );

    it(
      "returns MFA challenge",
      async () => {
        mockedAxios.post
          .mockResolvedValueOnce({
            data: {
              mfa_required: true,
              challenge_token:
                "challenge-token",
            },
          });

        const result =
          await login({
            login:
              "user@example.com",
            password:
              "password",
          });

        expect(
          result.mfa_required
        ).toBe(true);

        expect(
          result.challenge_token
        ).toBe(
          "challenge-token"
        );
      }
    );

    it(
      "sends the confirmation token",
      async () => {
        mockedAxios.post
          .mockResolvedValueOnce({
            data: {
              message:
                "E-mail confirmado com sucesso",
            },
          });

        const result =
          await confirmEmail({
            token:
              "token-seguro",
          });

        expect(
          mockedAxios.post
        ).toHaveBeenCalledWith(
          expect.stringContaining(
            "/auth/email/confirm"
          ),
          {
            token:
              "token-seguro",
          },
          expect.any(Object)
        );

        expect(
          result.message
        ).toBe(
          "E-mail confirmado com sucesso"
        );
      }
    );

    it(
      "requests a new verification email",
      async () => {
        mockedAxios.post
          .mockResolvedValueOnce({
            data: {
              message:
                "Solicitação processada",
            },
          });

        const result =
          await resendEmailConfirmation({
            login:
              "  USER@EXAMPLE.COM  ",
          });

        expect(
          mockedAxios.post
        ).toHaveBeenCalledWith(
          expect.stringContaining(
            "/auth/email/resend"
          ),
          {
            login:
              "user@example.com",
          },
          expect.any(Object)
        );

        expect(
          result.message
        ).toBe(
          "Solicitação processada"
        );
      }
    );

    it(
      "exchanges OAuth code",
      async () => {
        mockedAxios.post
          .mockResolvedValueOnce({
            data: {
              mfa_required: false,
              access_token:
                "oauth-jwt",
              token_type:
                "Bearer",
              expires_in:
                7200,
            },
          });

        const result =
          await exchangeOAuthCode(
            "oauth-code"
          );

        expect(
          mockedAxios.post
        ).toHaveBeenCalledWith(
          expect.stringContaining(
            "/auth/oauth/exchange"
          ),
          {
            code:
              "oauth-code",
          },
          expect.any(Object)
        );

        expect(
          result.access_token
        ).toBe(
          "oauth-jwt"
        );
      }
    );

    it(
      "verifies MFA challenge",
      async () => {
        mockedAxios.post
          .mockResolvedValueOnce({
            data: {
              mfa_required: false,
              access_token:
                "mfa-jwt",
              token_type:
                "Bearer",
              expires_in:
                7200,
            },
          });

        const result =
          await verifyMfa(
            "challenge-token",
            "123456"
          );

        expect(
          mockedAxios.post
        ).toHaveBeenCalledWith(
          expect.stringContaining(
            "/auth/mfa/verify"
          ),
          {
            challenge_token:
              "challenge-token",
            code:
              "123456",
          },
          expect.any(Object)
        );

        expect(
          result.access_token
        ).toBe(
          "mfa-jwt"
        );
      }
    );

    it(
      "retrieves MFA status with JWT",
      async () => {
        mockedGetAccessToken
          .mockReturnValue(
            "jwt-token"
          );

        mockedAxios.get
          .mockResolvedValueOnce({
            data: {
              mfa_enabled: true,
            },
          });

        const result =
          await getMfaStatus();

        expect(
          mockedAxios.get
        ).toHaveBeenCalledWith(
          expect.stringContaining(
            "/api/account/mfa/status"
          ),
          expect.objectContaining({
            headers:
              expect.objectContaining({
                Authorization:
                  "Bearer jwt-token",
              }),
          })
        );

        expect(
          result.mfa_enabled
        ).toBe(true);
      }
    );

    it(
      "begins MFA setup",
      async () => {
        mockedGetAccessToken
          .mockReturnValue(
            "jwt-token"
          );

        mockedAxios.post
          .mockResolvedValueOnce({
            data: {
              secret:
                "BASE32",
              otpauth_uri:
                "otpauth://totp/AppRH",
              recovery_codes: [
                "RECOVERY01",
              ],
            },
          });

        const result =
          await beginMfaSetup();

        expect(
          result.secret
        ).toBe(
          "BASE32"
        );
      }
    );

    it(
      "confirms MFA setup",
      async () => {
        mockedGetAccessToken
          .mockReturnValue(
            "jwt-token"
          );

        mockedAxios.post
          .mockResolvedValueOnce({
            data: {
              message:
                "MFA habilitado",
            },
          });

        const result =
          await confirmMfaSetup(
            "123456"
          );

        expect(
          result.message
        ).toBe(
          "MFA habilitado"
        );
      }
    );

    it(
      "regenerates recovery codes",
      async () => {
        mockedGetAccessToken
          .mockReturnValue(
            "jwt-token"
          );

        mockedAxios.post
          .mockResolvedValueOnce({
            data: {
              recovery_codes: [
                "RECOVERY01",
                "RECOVERY02",
              ],
            },
          });

        const result =
          await regenerateRecoveryCodes();

        expect(
          result.recovery_codes
        ).toEqual([
          "RECOVERY01",
          "RECOVERY02",
        ]);
      }
    );

    it(
      "disables MFA",
      async () => {
        mockedGetAccessToken
          .mockReturnValue(
            "jwt-token"
          );

        mockedAxios.delete
          .mockResolvedValueOnce({
            data: {
              message:
                "MFA desabilitado",
            },
          });

        const result =
          await disableMfa({
            password:
              "password",
            code:
              "123456",
          });

        expect(
          mockedAxios.delete
        ).toHaveBeenCalledWith(
          expect.stringContaining(
            "/api/account/mfa/totp"
          ),
          expect.objectContaining({
            data: {
              password:
                "password",
              code:
                "123456",
            },
          })
        );

        expect(
          result.message
        ).toBe(
          "MFA desabilitado"
        );
      }
    );
  }
);