import {
  render,
  screen,
  waitFor,
} from "@testing-library/react";

import userEvent from
  "@testing-library/user-event";

import {
  MemoryRouter,
  Route,
  Routes,
  useLocation,
} from "react-router-dom";

import {
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from "vitest";

import type {
  LoginResponse,
} from "../../../interfaces/LoginResponse";

import {
  OAuthCallbackPage,
} from "../../../pages/oauth-callback/OAuthCallbackPage";

import {
  useOAuthCallback,
} from "../../../hooks/useOAuthCallback";

vi.mock(
  "../../../hooks/useOAuthCallback"
);

const mockedUseOAuthCallback =
  vi.mocked(
    useOAuthCallback
  );

interface MutationOptions {
  onSuccess?: (
    response: LoginResponse
  ) => void;

  onError?: (
    error: Error
  ) => void;
}

const mutate =
  vi.fn<
    (
      code: string,
      options?: MutationOptions
    ) => void
  >();

function LocationDisplay() {
  const location =
    useLocation();

  return (
    <div
      data-testid="current-location"
    >
      {location.pathname}
      {location.search}
    </div>
  );
}

function configureOAuthHook() {
  mockedUseOAuthCallback
    .mockReturnValue({
      mutate,

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
    } as unknown as ReturnType<
      typeof useOAuthCallback
    >);
}

interface TestApplicationProps {
  initialEntry: string;
}

function TestApplication({
  initialEntry,
}: TestApplicationProps) {
  return (
    <MemoryRouter
      initialEntries={[
        initialEntry,
      ]}
    >
      <LocationDisplay />

      <Routes>
        <Route
          path="/oauth/callback"
          element={
            <OAuthCallbackPage />
          }
        />

        <Route
          path="/login"
          element={
            <h1>
              Login
            </h1>
          }
        />

        <Route
          path="/jobs"
          element={
            <h1>
              Vagas
            </h1>
          }
        />

        <Route
          path="/mfa/verify"
          element={
            <h1>
              Verificação MFA
            </h1>
          }
        />
      </Routes>
    </MemoryRouter>
  );
}

function renderPage(
  initialEntry: string
) {
  return render(
    <TestApplication
      initialEntry={initialEntry}
    />
  );
}

describe(
  "OAuthCallbackPage",
  () => {
    beforeEach(
      () => {
        vi.clearAllMocks();

        configureOAuthHook();

        vi.spyOn(
          window.history,
          "replaceState"
        );
      }
    );

    it(
      "shows loading state while exchanging code",
      () => {
        renderPage(
          "/oauth/callback?code=oauth-code"
        );

        expect(
          screen.getByRole(
            "heading",
            {
              name:
                "Concluindo autenticação",
            }
          )
        ).toBeInTheDocument();

        expect(
          screen.getByText(
            "Aguarde enquanto validamos o acesso ao AppRH."
          )
        ).toBeInTheDocument();
      }
    );

    it(
      "exchanges OAuth code",
      async () => {
        renderPage(
          "/oauth/callback?code=oauth-code"
        );

        await waitFor(
          () => {
            expect(
              mutate
            ).toHaveBeenCalledWith(
              "oauth-code",
              expect.objectContaining({
                onSuccess:
                  expect.any(
                    Function
                  ),

                onError:
                  expect.any(
                    Function
                  ),
              })
            );
          }
        );

        expect(
          window.history.replaceState
        ).toHaveBeenCalledWith(
          {},
          document.title,
          "/oauth/callback"
        );
      }
    );

    it(
      "navigates to jobs after successful exchange",
      async () => {
        mutate.mockImplementation(
          (
            _code,
            options
          ) => {
            options?.onSuccess?.({
              mfa_required:
                false,

              access_token:
                "oauth-jwt",

              token_type:
                "Bearer",

              expires_in:
                7200,
            });
          }
        );

        renderPage(
          "/oauth/callback?code=oauth-code"
        );

        expect(
          await screen.findByRole(
            "heading",
            {
              name:
                "Vagas",
            }
          )
        ).toBeInTheDocument();

        expect(
          screen.getByTestId(
            "current-location"
          )
        ).toHaveTextContent(
          "/jobs"
        );
      }
    );

    it(
      "navigates to MFA verification when required",
      async () => {
        mutate.mockImplementation(
          (
            _code,
            options
          ) => {
            options?.onSuccess?.({
              mfa_required:
                true,

              challenge_token:
                "challenge-token",
            });
          }
        );

        renderPage(
          "/oauth/callback?code=oauth-code"
        );

        expect(
          await screen.findByRole(
            "heading",
            {
              name:
                "Verificação MFA",
            }
          )
        ).toBeInTheDocument();

        expect(
          screen.getByTestId(
            "current-location"
          )
        ).toHaveTextContent(
          "/mfa/verify"
        );
      }
    );

    it(
      "shows an error when OAuth code is absent",
      () => {
        renderPage(
          "/oauth/callback"
        );

        expect(
          screen.getByRole(
            "alert"
          )
        ).toHaveTextContent(
          "O código OAuth não foi informado."
        );

        expect(
          mutate
        ).not.toHaveBeenCalled();

        expect(
          window.history.replaceState
        ).not.toHaveBeenCalled();
      }
    );

    it(
      "shows exchange error",
      async () => {
        mutate.mockImplementation(
          (
            _code,
            options
          ) => {
            options?.onError?.(
              new Error(
                "Código OAuth inválido ou expirado."
              )
            );
          }
        );

        renderPage(
          "/oauth/callback?code=oauth-code"
        );

        expect(
          await screen.findByRole(
            "alert"
          )
        ).toHaveTextContent(
          "Código OAuth inválido ou expirado."
        );

        expect(
          screen.getByRole(
            "heading",
            {
              name:
                "Não foi possível concluir o acesso",
            }
          )
        ).toBeInTheDocument();
      }
    );

    it(
      "returns to login after an error",
      async () => {
        const user =
          userEvent.setup();

        renderPage(
          "/oauth/callback"
        );

        await user.click(
          screen.getByRole(
            "button",
            {
              name:
                "Voltar ao login",
            }
          )
        );

        expect(
          screen.getByRole(
            "heading",
            {
              name:
                "Login",
            }
          )
        ).toBeInTheDocument();

        expect(
          screen.getByTestId(
            "current-location"
          )
        ).toHaveTextContent(
          "/login"
        );
      }
    );

    it(
      "does not exchange the same code twice after rerender",
      async () => {
        const {
          rerender,
        } =
          renderPage(
            "/oauth/callback?code=oauth-code"
          );

        await waitFor(
          () => {
            expect(
              mutate
            ).toHaveBeenCalledTimes(
              1
            );
          }
        );

        rerender(
          <TestApplication
            initialEntry={
              "/oauth/callback?code=oauth-code"
            }
          />
        );

        await waitFor(
          () => {
            expect(
              mutate
            ).toHaveBeenCalledTimes(
              1
            );
          }
        );

        expect(
          window.history.replaceState
        ).toHaveBeenCalledTimes(
          1
        );
      }
    );
  }
);