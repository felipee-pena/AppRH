import {
  act,
  renderHook,
  waitFor,
} from "@testing-library/react";

import {
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from "vitest";

import type {
  ReactNode,
} from "react";

import {
  AuthProvider,
} from "../../contexts/AuthContext";

import {
  useAuth,
} from "../../hooks/useAuth";

import {
  login as authenticate,
} from "../../services/auth-service";

import {
  getAccessToken,
  removeAccessToken,
  saveAccessToken,
} from "../../services/token-storage";

import {
  extractUserFromToken,
  isTokenExpired,
} from "../../utils/jwt";

vi.mock(
  "../../services/auth-service",
  () => ({
    login:
      vi.fn(),
  })
);

vi.mock(
  "../../services/token-storage",
  () => ({
    getAccessToken:
      vi.fn(),

    removeAccessToken:
      vi.fn(),

    saveAccessToken:
      vi.fn(),
  })
);

vi.mock(
  "../../utils/jwt",
  () => ({
    extractUserFromToken:
      vi.fn(),

    isTokenExpired:
      vi.fn(),
  })
);

const mockedAuthenticate =
  vi.mocked(
    authenticate
  );

const mockedGetAccessToken =
  vi.mocked(
    getAccessToken
  );

const mockedRemoveAccessToken =
  vi.mocked(
    removeAccessToken
  );

const mockedSaveAccessToken =
  vi.mocked(
    saveAccessToken
  );

const mockedExtractUser =
  vi.mocked(
    extractUserFromToken
  );

const mockedIsTokenExpired =
  vi.mocked(
    isTokenExpired
  );

function Wrapper({
  children,
}: {
  children: ReactNode;
}) {
  return (
    <AuthProvider>
      {children}
    </AuthProvider>
  );
}

describe(
  "AuthProvider",
  () => {
    beforeEach(
      () => {
        vi.clearAllMocks();

        mockedGetAccessToken.mockReturnValue(
          null
        );

        mockedIsTokenExpired.mockReturnValue(
          false
        );

        mockedExtractUser.mockReturnValue(
          null
        );
      }
    );

    it(
      "initializes without an authenticated user when token is absent",
      async () => {
        const {
          result,
        } =
          renderHook(
            () =>
              useAuth(),
            {
              wrapper:
                Wrapper,
            }
          );

        await waitFor(
          () => {
            expect(
              result.current.isInitializing
            ).toBe(
              false
            );
          }
        );

        expect(
          result.current.user
        ).toBeNull();

        expect(
          result.current.isAuthenticated
        ).toBe(
          false
        );

        expect(
          mockedRemoveAccessToken
        ).toHaveBeenCalledTimes(
          1
        );
      }
    );

    it(
      "removes an expired token",
      async () => {
        mockedGetAccessToken.mockReturnValue(
          "expired-token"
        );

        mockedIsTokenExpired.mockReturnValue(
          true
        );

        const {
          result,
        } =
          renderHook(
            () =>
              useAuth(),
            {
              wrapper:
                Wrapper,
            }
          );

        await waitFor(
          () => {
            expect(
              result.current.isInitializing
            ).toBe(
              false
            );
          }
        );

        expect(
          mockedIsTokenExpired
        ).toHaveBeenCalledWith(
          "expired-token"
        );

        expect(
          mockedRemoveAccessToken
        ).toHaveBeenCalledTimes(
          1
        );

        expect(
          result.current.isAuthenticated
        ).toBe(
          false
        );
      }
    );

    it(
      "removes a token without an identifiable user",
      async () => {
        mockedGetAccessToken.mockReturnValue(
          "invalid-token"
        );

        mockedIsTokenExpired.mockReturnValue(
          false
        );

        mockedExtractUser.mockReturnValue(
          null
        );

        const {
          result,
        } =
          renderHook(
            () =>
              useAuth(),
            {
              wrapper:
                Wrapper,
            }
          );

        await waitFor(
          () => {
            expect(
              result.current.isInitializing
            ).toBe(
              false
            );
          }
        );

        expect(
          mockedExtractUser
        ).toHaveBeenCalledWith(
          "invalid-token"
        );

        expect(
          mockedRemoveAccessToken
        ).toHaveBeenCalledTimes(
          1
        );

        expect(
          result.current.user
        ).toBeNull();
      }
    );

    it(
      "restores an authenticated user from a valid token",
      async () => {
        mockedGetAccessToken.mockReturnValue(
          "valid-token"
        );

        mockedIsTokenExpired.mockReturnValue(
          false
        );

        mockedExtractUser.mockReturnValue({
          login:
            "admin@example.com",
          role:
            "ADMIN",
        });

        const {
          result,
        } =
          renderHook(
            () =>
              useAuth(),
            {
              wrapper:
                Wrapper,
            }
          );

        await waitFor(
          () => {
            expect(
              result.current.isAuthenticated
            ).toBe(
              true
            );
          }
        );

        expect(
          result.current.user
        ).toEqual({
          login:
            "admin@example.com",
          role:
            "ADMIN",
        });

        expect(
          mockedRemoveAccessToken
        ).not.toHaveBeenCalled();
      }
    );

    it(
      "authenticates and saves the access token",
      async () => {
        mockedAuthenticate.mockResolvedValue({
          access_token:
            "new-token",
          token_type:
            "Bearer",
          expires_in:
            3600,
        });

        mockedExtractUser.mockReturnValue({
          login:
            "rh@example.com",
          role:
            "USERRH",
        });

        const {
          result,
        } =
          renderHook(
            () =>
              useAuth(),
            {
              wrapper:
                Wrapper,
            }
          );

        await waitFor(
          () => {
            expect(
              result.current.isInitializing
            ).toBe(
              false
            );
          }
        );

        await act(
          async () => {
            await result.current.login({
              login:
                "rh@example.com",
              password:
                "senha-segura",
            });
          }
        );

        expect(
          mockedAuthenticate
        ).toHaveBeenCalledWith({
          login:
            "rh@example.com",
          password:
            "senha-segura",
        });

        expect(
          mockedSaveAccessToken
        ).toHaveBeenCalledWith(
          "new-token"
        );

        expect(
          result.current.user
        ).toEqual({
          login:
            "rh@example.com",
          role:
            "USERRH",
        });

        expect(
          result.current.isAuthenticated
        ).toBe(
          true
        );
      }
    );

    it(
      "rejects login when token does not identify a user",
      async () => {
        mockedAuthenticate.mockResolvedValue({
          access_token:
            "invalid-token",
          token_type:
            "Bearer",
          expires_in:
            3600,
        });

        mockedExtractUser.mockReturnValue(
          null
        );

        const {
          result,
        } =
          renderHook(
            () =>
              useAuth(),
            {
              wrapper:
                Wrapper,
            }
          );

        await waitFor(
          () => {
            expect(
              result.current.isInitializing
            ).toBe(
              false
            );
          }
        );

        await expect(
          result.current.login({
            login:
              "usuario@example.com",
            password:
              "senha-segura",
          })
        ).rejects.toThrow(
          "Não foi possível identificar o usuário autenticado."
        );

        expect(
          mockedRemoveAccessToken
        ).toHaveBeenCalled();

        expect(
          mockedSaveAccessToken
        ).not.toHaveBeenCalled();
      }
    );

    it(
      "logs out the authenticated user",
      async () => {
        mockedGetAccessToken.mockReturnValue(
          "valid-token"
        );

        mockedExtractUser.mockReturnValue({
          login:
            "admin@example.com",
          role:
            "ADMIN",
        });

        const {
          result,
        } =
          renderHook(
            () =>
              useAuth(),
            {
              wrapper:
                Wrapper,
            }
          );

        await waitFor(
          () => {
            expect(
              result.current.isAuthenticated
            ).toBe(
              true
            );
          }
        );

        act(
          () => {
            result.current.logout();
          }
        );

        expect(
          mockedRemoveAccessToken
        ).toHaveBeenCalled();

        expect(
          result.current.user
        ).toBeNull();

        expect(
          result.current.isAuthenticated
        ).toBe(
          false
        );
      }
    );

    it(
      "checks whether authenticated user has one of the roles",
      async () => {
        mockedGetAccessToken.mockReturnValue(
          "valid-token"
        );

        mockedExtractUser.mockReturnValue({
          login:
            "rh@example.com",
          role:
            "USERRH",
        });

        const {
          result,
        } =
          renderHook(
            () =>
              useAuth(),
            {
              wrapper:
                Wrapper,
            }
          );

        await waitFor(
          () => {
            expect(
              result.current.isAuthenticated
            ).toBe(
              true
            );
          }
        );

        expect(
          result.current.hasRole(
            "ADMIN",
            "USERRH"
          )
        ).toBe(
          true
        );

        expect(
          result.current.hasRole(
            "ADMIN"
          )
        ).toBe(
          false
        );
      }
    );
  }
);