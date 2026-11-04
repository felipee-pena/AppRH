import {
  render,
  screen,
  waitFor,
} from "@testing-library/react";

import userEvent from "@testing-library/user-event";

import {
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from "vitest";

import {
  AccountSecurityPage,
} from "../../../pages/account-security/AccountSecurityPage";

import {
  useMfaManagement,
} from "../../../hooks/useMfaManagement";

vi.mock(
  "../../../hooks/useMfaManagement"
);

vi.mock(
  "qrcode.react",
  () => ({
    QRCodeSVG: ({
      value,
    }: {
      value: string;
    }) => (
      <div
        data-testid="qr-code"
        data-value={value}
      />
    ),
  })
);

const mockedUseMfaManagement =
  vi.mocked(
    useMfaManagement
  );

type MfaManagementResult =
  ReturnType<
    typeof useMfaManagement
  >;

function createMutationMock(
  overrides: Record<
    string,
    unknown
  > = {}
) {
  return {
    mutate:
      vi.fn(),

    mutateAsync:
      vi.fn(),

    reset:
      vi.fn(),

    data:
      undefined,

    error:
      null,

    isPending:
      false,

    isPaused:
      false,

    isSuccess:
      false,

    isError:
      false,

    isIdle:
      true,

    status:
      "idle",

    variables:
      undefined,

    context:
      undefined,

    failureCount:
      0,

    failureReason:
      null,

    submittedAt:
      0,

    ...overrides,
  };
}

function createStatusQueryMock(
  mfaEnabled: boolean,
  overrides: Record<
    string,
    unknown
  > = {}
) {
  return {
    data: {
      mfa_enabled:
        mfaEnabled,
    },

    dataUpdatedAt:
      Date.now(),

    error:
      null,

    errorUpdatedAt:
      0,

    errorUpdateCount:
      0,

    failureCount:
      0,

    failureReason:
      null,

    fetchStatus:
      "idle",

    isError:
      false,

    isFetched:
      true,

    isFetchedAfterMount:
      true,

    isFetching:
      false,

    isInitialLoading:
      false,

    isLoading:
      false,

    isLoadingError:
      false,

    isPaused:
      false,

    isPending:
      false,

    isPlaceholderData:
      false,

    isRefetchError:
      false,

    isRefetching:
      false,

    isStale:
      false,

    isSuccess:
      true,

    status:
      "success",

    refetch:
      vi.fn(),

    promise:
      Promise.resolve({
        mfa_enabled:
          mfaEnabled,
      }),

    ...overrides,
  };
}

function createHookResult(
  options: {
    mfaEnabled?: boolean;

    statusOverrides?: Record<
      string,
      unknown
    >;

    setupOverrides?: Record<
      string,
      unknown
    >;

    confirmOverrides?: Record<
      string,
      unknown
    >;

    regenerateOverrides?: Record<
      string,
      unknown
    >;

    disableOverrides?: Record<
      string,
      unknown
    >;
  } = {}
): MfaManagementResult {
  const {
    mfaEnabled = false,
    statusOverrides = {},
    setupOverrides = {},
    confirmOverrides = {},
    regenerateOverrides = {},
    disableOverrides = {},
  } = options;

  const result = {
    statusQuery:
      createStatusQueryMock(
        mfaEnabled,
        statusOverrides
      ),

    setupMutation:
      createMutationMock(
        setupOverrides
      ),

    confirmMutation:
      createMutationMock(
        confirmOverrides
      ),

    regenerateMutation:
      createMutationMock(
        regenerateOverrides
      ),

    disableMutation:
      createMutationMock(
        disableOverrides
      ),
  };

  return (
    result as unknown
  ) as MfaManagementResult;
}

describe(
  "AccountSecurityPage",
  () => {
    beforeEach(
      () => {
        vi.clearAllMocks();

        mockedUseMfaManagement
          .mockReturnValue(
            createHookResult()
          );
      }
    );

    it(
      "shows loading state",
      () => {
        mockedUseMfaManagement
          .mockReturnValue(
            createHookResult({
              statusOverrides: {
                data:
                  undefined,

                isPending:
                  true,

                isLoading:
                  true,

                isSuccess:
                  false,

                status:
                  "pending",
              },
            })
          );

        render(
          <AccountSecurityPage />
        );

        expect(
          screen.getByText(
            "Carregando segurança da conta..."
          )
        ).toBeInTheDocument();
      }
    );

    it(
      "shows disabled MFA status",
      () => {
        mockedUseMfaManagement
          .mockReturnValue(
            createHookResult({
              mfaEnabled:
                false,
            })
          );

        render(
          <AccountSecurityPage />
        );

        expect(
          screen.getByText(
            "Desabilitado"
          )
        ).toBeInTheDocument();

        expect(
          screen.getByText(
            "A autenticação em duas etapas está desabilitada."
          )
        ).toBeInTheDocument();

        expect(
          screen.getByRole(
            "button",
            {
              name:
                "Habilitar MFA",
            }
          )
        ).toBeInTheDocument();
      }
    );

    it(
      "starts MFA setup",
      async () => {
        const user =
          userEvent.setup();

        const mutate =
          vi.fn();

        mockedUseMfaManagement
          .mockReturnValue(
            createHookResult({
              setupOverrides: {
                mutate,
              },
            })
          );

        render(
          <AccountSecurityPage />
        );

        await user.click(
          screen.getByRole(
            "button",
            {
              name:
                "Habilitar MFA",
            }
          )
        );

        expect(
          mutate
        ).toHaveBeenCalledOnce();
      }
    );

    it(
      "shows QR Code, secret and recovery codes",
      async () => {
        mockedUseMfaManagement
          .mockReturnValue(
            createHookResult({
              setupOverrides: {
                isSuccess:
                  true,

                isIdle:
                  false,

                status:
                  "success",

                data: {
                  secret:
                    "BASE32SECRET",

                  otpauth_uri:
                    "otpauth://totp/AppRH",

                  recovery_codes: [
                    "RECOVERY01",
                    "RECOVERY02",
                  ],
                },
              },
            })
          );

        render(
          <AccountSecurityPage />
        );

        await waitFor(
          () => {
            expect(
              screen.getByTestId(
                "qr-code"
              )
            ).toBeInTheDocument();
          }
        );

        expect(
          screen.getByTestId(
            "qr-code"
          )
        ).toHaveAttribute(
          "data-value",
          "otpauth://totp/AppRH"
        );

        expect(
          screen.getByText(
            "BASE32SECRET"
          )
        ).toBeInTheDocument();

        expect(
          screen.getByText(
            "RECOVERY01"
          )
        ).toBeInTheDocument();

        expect(
          screen.getByText(
            "RECOVERY02"
          )
        ).toBeInTheDocument();
      }
    );

    it(
      "confirms MFA setup",
      async () => {
        const user =
          userEvent.setup();

        const mutate =
          vi.fn();

        mockedUseMfaManagement
          .mockReturnValue(
            createHookResult({
              setupOverrides: {
                isSuccess:
                  true,

                isIdle:
                  false,

                status:
                  "success",

                data: {
                  secret:
                    "BASE32SECRET",

                  otpauth_uri:
                    "otpauth://totp/AppRH",

                  recovery_codes: [],
                },
              },

              confirmOverrides: {
                mutate,
              },
            })
          );

        render(
          <AccountSecurityPage />
        );

        const codeInput =
          await screen.findByLabelText(
            "Código atual do autenticador"
          );

        await user.type(
          codeInput,
          "123456"
        );

        await user.click(
          screen.getByRole(
            "button",
            {
              name:
                "Confirmar configuração",
            }
          )
        );

        expect(
          mutate
        ).toHaveBeenCalledWith(
          "123456",
          expect.objectContaining({
            onSuccess:
              expect.any(
                Function
              ),
          })
        );
      }
    );

    it(
      "keeps confirmation button disabled for invalid code",
      async () => {
        const user =
          userEvent.setup();

        const mutate =
          vi.fn();

        mockedUseMfaManagement
          .mockReturnValue(
            createHookResult({
              setupOverrides: {
                isSuccess:
                  true,

                isIdle:
                  false,

                status:
                  "success",

                data: {
                  secret:
                    "BASE32SECRET",

                  otpauth_uri:
                    "otpauth://totp/AppRH",

                  recovery_codes: [],
                },
              },

              confirmOverrides: {
                mutate,
              },
            })
          );

        render(
          <AccountSecurityPage />
        );

        const codeInput =
          await screen.findByLabelText(
            "Código atual do autenticador"
          );

        await user.type(
          codeInput,
          "123"
        );

        expect(
          screen.getByRole(
            "button",
            {
              name:
                "Confirmar configuração",
            }
          )
        ).toBeDisabled();

        expect(
          mutate
        ).not.toHaveBeenCalled();
      }
    );

    it(
      "shows enabled MFA controls",
      () => {
        mockedUseMfaManagement
          .mockReturnValue(
            createHookResult({
              mfaEnabled:
                true,
            })
          );

        render(
          <AccountSecurityPage />
        );

        expect(
          screen.getByText(
            "Habilitado"
          )
        ).toBeInTheDocument();

        expect(
          screen.getByText(
            "A autenticação em duas etapas está habilitada."
          )
        ).toBeInTheDocument();

        expect(
          screen.getByRole(
            "button",
            {
              name:
                "Regenerar códigos",
            }
          )
        ).toBeInTheDocument();

        expect(
          screen.getByRole(
            "button",
            {
              name:
                "Desabilitar MFA",
            }
          )
        ).toBeInTheDocument();
      }
    );

    it(
      "regenerates recovery codes",
      async () => {
        const user =
          userEvent.setup();

        const mutate =
          vi.fn();

        mockedUseMfaManagement
          .mockReturnValue(
            createHookResult({
              mfaEnabled:
                true,

              regenerateOverrides: {
                mutate,
              },
            })
          );

        render(
          <AccountSecurityPage />
        );

        await user.click(
          screen.getByRole(
            "button",
            {
              name:
                "Regenerar códigos",
            }
          )
        );

        expect(
          mutate
        ).toHaveBeenCalledOnce();
      }
    );

    it(
      "keeps disable button disabled without password",
      async () => {
        const user =
          userEvent.setup();

        mockedUseMfaManagement
          .mockReturnValue(
            createHookResult({
              mfaEnabled:
                true,
            })
          );

        render(
          <AccountSecurityPage />
        );

        await user.type(
          screen.getByLabelText(
            "Código atual do autenticador"
          ),
          "123456"
        );

        expect(
          screen.getByRole(
            "button",
            {
              name:
                "Desabilitar MFA",
            }
          )
        ).toBeDisabled();
      }
    );

    it(
      "disables MFA with password and TOTP",
      async () => {
        const user =
          userEvent.setup();

        const mutate =
          vi.fn();

        mockedUseMfaManagement
          .mockReturnValue(
            createHookResult({
              mfaEnabled:
                true,

              disableOverrides: {
                mutate,
              },
            })
          );

        render(
          <AccountSecurityPage />
        );

        await user.type(
          screen.getByLabelText(
            "Senha atual"
          ),
          "current-password"
        );

        await user.type(
          screen.getByLabelText(
            "Código atual do autenticador"
          ),
          "123456"
        );

        await user.click(
          screen.getByRole(
            "button",
            {
              name:
                "Desabilitar MFA",
            }
          )
        );

        expect(
          mutate
        ).toHaveBeenCalledWith(
          {
            password:
              "current-password",

            code:
              "123456",
          },
          expect.objectContaining({
            onSuccess:
              expect.any(
                Function
              ),
          })
        );
      }
    );

    it(
      "shows status error",
      () => {
        mockedUseMfaManagement
          .mockReturnValue(
            createHookResult({
              statusOverrides: {
                error:
                  new Error(
                    "Falha ao consultar MFA."
                  ),

                isError:
                  true,

                isSuccess:
                  false,

                status:
                  "error",
              },
            })
          );

        render(
          <AccountSecurityPage />
        );

        expect(
          screen.getByRole(
            "alert"
          )
        ).toHaveTextContent(
          "Falha ao consultar MFA."
        );
      }
    );
  }
);