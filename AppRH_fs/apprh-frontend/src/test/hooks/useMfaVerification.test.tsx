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

function createWrapper() {
  const queryClient =
    new QueryClient({
      defaultOptions: {
        queries: {
          retry: false,
        },
        mutations: {
          retry: false,
        },
      },
    });

  return function Wrapper({
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
  };
}

describe(
  "useMfaVerification",
  () => {
    const completeMfa =
      vi.fn();

    beforeEach(
      () => {
        vi.clearAllMocks();

        mockedUseAuth.mockReturnValue({
          user: null,
          isAuthenticated: false,
          isInitializing: false,
          login: vi.fn(),
          completeMfa,
          processAuthenticationResponse:
            vi.fn(),
          logout: vi.fn(),
          hasRole: vi.fn(
            () => false
          ),
        });
      }
    );

    it(
      "validates MFA code",
      async () => {
        completeMfa
          .mockResolvedValue({
            mfa_required: false,
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
        ).toHaveBeenCalledWith(
          "123456"
        );
      }
    );

    it(
      "removes non-numeric characters",
      async () => {
        completeMfa
          .mockResolvedValue({
            mfa_required: false,
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
      "rejects invalid MFA code",
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
          result.current.error?.message
        ).toBe(
          "O código MFA deve possuir 6 dígitos."
        );

        expect(
          completeMfa
        ).not.toHaveBeenCalled();
      }
    );
  }
);