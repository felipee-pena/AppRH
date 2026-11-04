import type {
  ReactNode,
} from "react";

import {
  QueryClient,
  QueryClientProvider,
} from "@tanstack/react-query";

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

import {
  useMfaVerification,
} from "../../hooks/useMfaVerification";

import {
  useAuth,
} from "../../hooks/useAuth";

vi.mock(
  "../../hooks/useAuth"
);

const mockedUseAuth =
  vi.mocked(
    useAuth
  );

const completeMfa =
  vi.fn();

function createWrapper() {
  const queryClient =
    new QueryClient({
      defaultOptions: {
        queries: {
          retry:
            false,
        },

        mutations: {
          retry:
            false,
        },
      },
    });

  function Wrapper({
    children,
  }: {
    children: ReactNode;
  }) {
    return (
      <QueryClientProvider
        client={queryClient}
      >
        {children}
      </QueryClientProvider>
    );
  }

  return Wrapper;
}

function configureAuthContext() {
  mockedUseAuth.mockReturnValue({
    user:
      null,

    isAuthenticated:
      false,

    isInitializing:
      false,

    login:
      vi.fn(),

    completeMfa,

    processAuthenticationResponse:
      vi.fn(),

    logout:
      vi.fn(),

    hasRole:
      vi.fn(
        () => false
      ),
  });
}

describe(
  "useMfaVerification",
  () => {
    beforeEach(
      () => {
        vi.clearAllMocks();

        configureAuthContext();
      }
    );

    it(
      "validates a six-digit MFA code",
      async () => {
        completeMfa.mockResolvedValue({
          mfa_required:
            false,

          access_token:
            "jwt-final",

          token_type:
            "Bearer",

          expires_in:
            7200,
        });

        const {
          result,
        } =
          renderHook(
            () =>
              useMfaVerification(),
            {
              wrapper:
                createWrapper(),
            }
          );

        act(
          () => {
            result.current.mutate(
              "123456"
            );
          }
        );

        await waitFor(
          () => {
            expect(
              result.current.isSuccess
            ).toBe(true);
          }
        );

        expect(
          completeMfa
        ).toHaveBeenCalledOnce();

        expect(
          completeMfa
        ).toHaveBeenCalledWith(
          "123456"
        );

        expect(
          result.current.data
        ).toEqual({
          mfa_required:
            false,

          access_token:
            "jwt-final",

          token_type:
            "Bearer",

          expires_in:
            7200,
        });
      }
    );

    it(
      "removes separators from MFA code",
      async () => {
        completeMfa.mockResolvedValue({
          mfa_required:
            false,

          access_token:
            "jwt-final",

          token_type:
            "Bearer",

          expires_in:
            7200,
        });

        const {
          result,
        } =
          renderHook(
            () =>
              useMfaVerification(),
            {
              wrapper:
                createWrapper(),
            }
          );

        act(
          () => {
            result.current.mutate(
              "123-456"
            );
          }
        );

        await waitFor(
          () => {
            expect(
              result.current.isSuccess
            ).toBe(true);
          }
        );

        expect(
          completeMfa
        ).toHaveBeenCalledWith(
          "123456"
        );
      }
    );

    it(
      "removes spaces from MFA code",
      async () => {
        completeMfa.mockResolvedValue({
          mfa_required:
            false,

          access_token:
            "jwt-final",

          token_type:
            "Bearer",

          expires_in:
            7200,
        });

        const {
          result,
        } =
          renderHook(
            () =>
              useMfaVerification(),
            {
              wrapper:
                createWrapper(),
            }
          );

        act(
          () => {
            result.current.mutate(
              "123 456"
            );
          }
        );

        await waitFor(
          () => {
            expect(
              result.current.isSuccess
            ).toBe(true);
          }
        );

        expect(
          completeMfa
        ).toHaveBeenCalledWith(
          "123456"
        );
      }
    );

    it(
      "rejects a code shorter than six digits",
      async () => {
        const {
          result,
        } =
          renderHook(
            () =>
              useMfaVerification(),
            {
              wrapper:
                createWrapper(),
            }
          );

        act(
          () => {
            result.current.mutate(
              "123"
            );
          }
        );

        await waitFor(
          () => {
            expect(
              result.current.isError
            ).toBe(true);
          }
        );

        expect(
          result.current.error
            ?.message
        ).toBe(
          "O código MFA deve possuir 6 dígitos."
        );

        expect(
          completeMfa
        ).not.toHaveBeenCalled();
      }
    );

    it(
      "rejects an empty code",
      async () => {
        const {
          result,
        } =
          renderHook(
            () =>
              useMfaVerification(),
            {
              wrapper:
                createWrapper(),
            }
          );

        act(
          () => {
            result.current.mutate(
              ""
            );
          }
        );

        await waitFor(
          () => {
            expect(
              result.current.isError
            ).toBe(true);
          }
        );

        expect(
          completeMfa
        ).not.toHaveBeenCalled();
      }
    );

    it(
      "exposes backend validation error",
      async () => {
        completeMfa.mockRejectedValue(
          new Error(
            "Código MFA inválido."
          )
        );

        const {
          result,
        } =
          renderHook(
            () =>
              useMfaVerification(),
            {
              wrapper:
                createWrapper(),
            }
          );

        act(
          () => {
            result.current.mutate(
              "999999"
            );
          }
        );

        await waitFor(
          () => {
            expect(
              result.current.isError
            ).toBe(true);
          }
        );

        expect(
          result.current.error
            ?.message
        ).toBe(
          "Código MFA inválido."
        );
      }
    );
  }
);