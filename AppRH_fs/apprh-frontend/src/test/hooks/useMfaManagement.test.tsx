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
  useMfaManagement,
} from "../../hooks/useMfaManagement";

import {
  beginMfaSetup,
  confirmMfaSetup,
  disableMfa,
  getMfaStatus,
  regenerateRecoveryCodes,
} from "../../services/auth-service";

vi.mock(
  "../../services/auth-service",
  () => ({
    getMfaStatus:
      vi.fn(),

    beginMfaSetup:
      vi.fn(),

    confirmMfaSetup:
      vi.fn(),

    regenerateRecoveryCodes:
      vi.fn(),

    disableMfa:
      vi.fn(),
  })
);

const mockedGetMfaStatus =
  vi.mocked(
    getMfaStatus
  );

const mockedBeginMfaSetup =
  vi.mocked(
    beginMfaSetup
  );

const mockedConfirmMfaSetup =
  vi.mocked(
    confirmMfaSetup
  );

const mockedRegenerateRecoveryCodes =
  vi.mocked(
    regenerateRecoveryCodes
  );

const mockedDisableMfa =
  vi.mocked(
    disableMfa
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
  "useMfaManagement",
  () => {
    beforeEach(
      () => {
        vi.clearAllMocks();

        mockedGetMfaStatus
          .mockResolvedValue({
            mfa_enabled: false,
          });
      }
    );

    it(
      "loads MFA status",
      async () => {
        const {
          result,
        } =
          renderHook(
            () =>
              useMfaManagement(),
            {
              wrapper:
                createWrapper(),
            }
          );

        await waitFor(
          () => {
            expect(
              result.current
                .statusQuery
                .isSuccess
            ).toBe(true);
          }
        );

        expect(
          result.current
            .statusQuery
            .data
        ).toEqual({
          mfa_enabled: false,
        });
      }
    );

    it(
      "starts MFA setup",
      async () => {
        mockedBeginMfaSetup
          .mockResolvedValue({
            secret:
              "BASE32SECRET",
            otpauth_uri:
              "otpauth://totp/AppRH",
            recovery_codes: [
              "RECOVERY01",
            ],
          });

        const {
          result,
        } =
          renderHook(
            () =>
              useMfaManagement(),
            {
              wrapper:
                createWrapper(),
            }
          );

        act(
          () => {
            result.current
              .setupMutation
              .mutate();
          }
        );

        await waitFor(
          () => {
            expect(
              result.current
                .setupMutation
                .isSuccess
            ).toBe(true);
          }
        );

        expect(
          result.current
            .setupMutation
            .data
            ?.secret
        ).toBe(
          "BASE32SECRET"
        );
      }
    );

    it(
      "confirms MFA setup",
      async () => {
        mockedConfirmMfaSetup
          .mockResolvedValue({
            message:
              "MFA habilitado.",
          });

        const {
          result,
        } =
          renderHook(
            () =>
              useMfaManagement(),
            {
              wrapper:
                createWrapper(),
            }
          );

        act(
          () => {
            result.current
              .confirmMutation
              .mutate(
                "123456"
              );
          }
        );

        await waitFor(
          () => {
            expect(
              result.current
                .confirmMutation
                .isSuccess
            ).toBe(true);
          }
        );

        expect(
          mockedConfirmMfaSetup
        ).toHaveBeenCalledWith(
          "123456"
        );
      }
    );

    it(
      "rejects invalid setup confirmation code",
      async () => {
        const {
          result,
        } =
          renderHook(
            () =>
              useMfaManagement(),
            {
              wrapper:
                createWrapper(),
            }
          );

        act(
          () => {
            result.current
              .confirmMutation
              .mutate(
                "123"
              );
          }
        );

        await waitFor(
          () => {
            expect(
              result.current
                .confirmMutation
                .isError
            ).toBe(true);
          }
        );

        expect(
          mockedConfirmMfaSetup
        ).not.toHaveBeenCalled();
      }
    );

    it(
      "regenerates recovery codes",
      async () => {
        mockedRegenerateRecoveryCodes
          .mockResolvedValue({
            recovery_codes: [
              "RECOVERY01",
              "RECOVERY02",
            ],
          });

        const {
          result,
        } =
          renderHook(
            () =>
              useMfaManagement(),
            {
              wrapper:
                createWrapper(),
            }
          );

        act(
          () => {
            result.current
              .regenerateMutation
              .mutate();
          }
        );

        await waitFor(
          () => {
            expect(
              result.current
                .regenerateMutation
                .isSuccess
            ).toBe(true);
          }
        );

        expect(
          result.current
            .regenerateMutation
            .data
            ?.recovery_codes
        ).toEqual([
          "RECOVERY01",
          "RECOVERY02",
        ]);
      }
    );

    it(
      "disables MFA",
      async () => {
        mockedDisableMfa
          .mockResolvedValue({
            message:
              "MFA desabilitado.",
          });

        const {
          result,
        } =
          renderHook(
            () =>
              useMfaManagement(),
            {
              wrapper:
                createWrapper(),
            }
          );

        act(
          () => {
            result.current
              .disableMutation
              .mutate({
                password:
                  "password",
                code:
                  "123456",
              });
          }
        );

        await waitFor(
          () => {
            expect(
              result.current
                .disableMutation
                .isSuccess
            ).toBe(true);
          }
        );

        expect(
          mockedDisableMfa
        ).toHaveBeenCalledWith({
          password:
            "password",
          code:
            "123456",
        });
      }
    );

    it(
      "rejects MFA disable without password",
      async () => {
        const {
          result,
        } =
          renderHook(
            () =>
              useMfaManagement(),
            {
              wrapper:
                createWrapper(),
            }
          );

        act(
          () => {
            result.current
              .disableMutation
              .mutate({
                password:
                  " ",
                code:
                  "123456",
              });
          }
        );

        await waitFor(
          () => {
            expect(
              result.current
                .disableMutation
                .isError
            ).toBe(true);
          }
        );

        expect(
          mockedDisableMfa
        ).not.toHaveBeenCalled();
      }
    );
  }
);