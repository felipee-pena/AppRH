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
  useOAuthCallback,
} from "../../hooks/useOAuthCallback";

import {
  useAuth,
} from "../../hooks/useAuth";

import {
  exchangeOAuthCode,
} from "../../services/auth-service";

vi.mock(
  "../../hooks/useAuth"
);

vi.mock(
  "../../services/auth-service",
  () => ({
    exchangeOAuthCode:
      vi.fn(),
  })
);

const mockedUseAuth =
  vi.mocked(
    useAuth
  );

const mockedExchangeOAuthCode =
  vi.mocked(
    exchangeOAuthCode
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
  "useOAuthCallback",
  () => {
    const processAuthenticationResponse =
      vi.fn();

    beforeEach(
      () => {
        vi.clearAllMocks();

        mockedUseAuth.mockReturnValue({
          user: null,
          isAuthenticated: false,
          isInitializing: false,
          login: vi.fn(),
          completeMfa: vi.fn(),
          processAuthenticationResponse,
          logout: vi.fn(),
          hasRole: vi.fn(
            () => false
          ),
        });
      }
    );

    it(
      "exchanges OAuth code and processes response",
      async () => {
        mockedExchangeOAuthCode
          .mockResolvedValue({
            mfa_required: false,
            access_token:
              "oauth-jwt",
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
              useOAuthCallback(),
            {
              wrapper:
                createWrapper(),
            }
          );

        act(
          () => {
            result.current.mutate(
              " oauth-code "
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
          mockedExchangeOAuthCode
        ).toHaveBeenCalledWith(
          "oauth-code"
        );

        expect(
          processAuthenticationResponse
        ).toHaveBeenCalledWith({
          mfa_required: false,
          access_token:
            "oauth-jwt",
          token_type:
            "Bearer",
          expires_in:
            7200,
        });
      }
    );

    it(
      "processes MFA challenge returned by exchange",
      async () => {
        mockedExchangeOAuthCode
          .mockResolvedValue({
            mfa_required: true,
            challenge_token:
              "challenge-token",
          });

        const {
          result,
        } =
          renderHook(
            () =>
              useOAuthCallback(),
            {
              wrapper:
                createWrapper(),
            }
          );

        act(
          () => {
            result.current.mutate(
              "oauth-code"
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
          processAuthenticationResponse
        ).toHaveBeenCalledWith({
          mfa_required: true,
          challenge_token:
            "challenge-token",
        });
      }
    );

    it(
      "rejects blank OAuth code",
      async () => {
        const {
          result,
        } =
          renderHook(
            () =>
              useOAuthCallback(),
            {
              wrapper:
                createWrapper(),
            }
          );

        act(
          () => {
            result.current.mutate(
              " "
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
          "O código OAuth não foi informado."
        );

        expect(
          mockedExchangeOAuthCode
        ).not.toHaveBeenCalled();
      }
    );
  }
);