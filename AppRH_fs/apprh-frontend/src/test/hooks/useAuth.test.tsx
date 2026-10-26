import {
  renderHook,
} from "@testing-library/react";

import {
  describe,
  expect,
  it,
  vi,
} from "vitest";

import type {
  ReactNode,
} from "react";

import {
  AuthContext,
} from "../../contexts/AuthContext";

import {
  useAuth,
} from "../../hooks/useAuth";

function createWrapper() {
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

    login:
      vi.fn(),

    logout:
      vi.fn(),

    hasRole:
      vi.fn(
        () => true
      ),
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