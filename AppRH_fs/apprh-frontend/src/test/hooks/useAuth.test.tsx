import type {
  ReactNode,
} from "react";

import {
  renderHook,
} from "@testing-library/react";

import {
  describe,
  expect,
  it,
  vi,
} from "vitest";

import {
  AuthContext,
} from "../../contexts/AuthContext";

import {
  useAuth,
} from "../../hooks/useAuth";

import type {
  LoginRequest,
} from "../../interfaces/LoginRequest";

import type {
  LoginResponse,
} from "../../interfaces/LoginResponse";

function createWrapper() {
  const login =
    vi.fn(
      async (
        _credentials: LoginRequest
      ): Promise<LoginResponse> => ({
        mfa_required: false,
        access_token: "jwt-token",
        token_type: "Bearer",
        expires_in: 7200,
      })
    );

  const completeMfa =
    vi.fn(
      async (
        _code: string
      ): Promise<LoginResponse> => ({
        mfa_required: false,
        access_token: "mfa-jwt-token",
        token_type: "Bearer",
        expires_in: 7200,
      })
    );

  const processAuthenticationResponse =
    vi.fn(
      (
        _response: LoginResponse
      ): void => {
        // Não é necessário executar lógica neste teste.
      }
    );

  const logout =
    vi.fn(
      (): void => {
        // Não é necessário executar lógica neste teste.
      }
    );

  const hasRole =
    vi.fn(
      (): boolean => true
    );

  const contextValue = {
    user: {
      login:
        "admin@example.com",
      role:
        "ADMIN" as const,
    },

    isAuthenticated:
      true,

    isInitializing:
      false,

    login,

    completeMfa,

    processAuthenticationResponse,

    logout,

    hasRole,
  };

  function Wrapper({
    children,
  }: {
    children: ReactNode;
  }) {
    return (
      <AuthContext.Provider
        value={contextValue}
      >
        {children}
      </AuthContext.Provider>
    );
  }

  return {
    Wrapper,
    contextValue,
  };
}

describe(
  "useAuth",
  () => {
    it(
      "returns the authentication context",
      () => {
        const {
          Wrapper,
          contextValue,
        } =
          createWrapper();

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

        expect(
          result.current
        ).toBe(
          contextValue
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
          result.current.isAuthenticated
        ).toBe(true);

        expect(
          result.current.isInitializing
        ).toBe(false);
      }
    );

    it(
      "exposes authentication operations",
      () => {
        const {
          Wrapper,
        } =
          createWrapper();

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

        expect(
          result.current.login
        ).toEqual(
          expect.any(Function)
        );

        expect(
          result.current.completeMfa
        ).toEqual(
          expect.any(Function)
        );

        expect(
          result.current
            .processAuthenticationResponse
        ).toEqual(
          expect.any(Function)
        );

        expect(
          result.current.logout
        ).toEqual(
          expect.any(Function)
        );

        expect(
          result.current.hasRole
        ).toEqual(
          expect.any(Function)
        );
      }
    );

    it(
      "throws when used outside AuthProvider",
      () => {
        expect(
          () => {
            renderHook(
              () =>
                useAuth()
            );
          }
        ).toThrow(
          "useAuth deve ser utilizado dentro de AuthProvider"
        );
      }
    );
  }
);