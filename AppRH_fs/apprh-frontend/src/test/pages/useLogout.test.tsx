import type {
  ReactNode,
} from "react";

import {
  act,
  renderHook,
  waitFor,
} from "@testing-library/react";

import {
  QueryClient,
  QueryClientProvider,
} from "@tanstack/react-query";

import {
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from "vitest";

import {
  useLogout,
} from "../../hooks/useLogout";

import {
  removeAccessToken,
} from "../../services/token-storage";

vi.mock(
  "../../services/token-storage",
  () => ({
    removeAccessToken: vi.fn(),
  })
);

const mockedRemoveAccessToken =
  vi.mocked(
    removeAccessToken
  );

function createWrapper() {
  const queryClient =
    new QueryClient({
      defaultOptions: {
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
  "useLogout",
  () => {
    beforeEach(
      () => {
        vi.clearAllMocks();
      }
    );

    it(
      "removes the access token",
      async () => {
        const {
          result,
        } =
          renderHook(
            () =>
              useLogout(),
            {
              wrapper:
                createWrapper(),
            }
          );

        await act(
          async () => {
            await result.current.mutateAsync();
          }
        );

        expect(
          mockedRemoveAccessToken
        ).toHaveBeenCalledTimes(
          1
        );

        await waitFor(
          () => {
            expect(
              result.current.isSuccess
            ).toBe(
              true
            );
          }
        );
      }
    );
  }
);