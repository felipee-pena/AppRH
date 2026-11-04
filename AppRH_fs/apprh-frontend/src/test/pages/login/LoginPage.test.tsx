import {
  render,
  screen,
  waitFor,
} from "@testing-library/react";

import userEvent from "@testing-library/user-event";

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

import {
  LoginPage,
} from "../../../pages/login/LoginPage";

import {
  useAuth,
} from "../../../hooks/useAuth";

import {
  startGitHubLogin,
  startGoogleLogin,
} from "../../../services/auth-service";

vi.mock(
  "../../../hooks/useAuth"
);

vi.mock(
  "../../../services/auth-service",
  () => ({
    startGoogleLogin:
      vi.fn(),

    startGitHubLogin:
      vi.fn(),
  })
);

const mockedUseAuth =
  vi.mocked(
    useAuth
  );

const mockedStartGoogleLogin =
  vi.mocked(
    startGoogleLogin
  );

const mockedStartGitHubLogin =
  vi.mocked(
    startGitHubLogin
  );

const loginMock =
  vi.fn();

const completeMfaMock =
  vi.fn();

const processAuthenticationResponseMock =
  vi.fn();

const logoutMock =
  vi.fn();

const hasRoleMock =
  vi.fn();

function configureAuthContext(
  options: {
    isAuthenticated?: boolean;
    isInitializing?: boolean;
  } = {}
) {
  const {
    isAuthenticated = false,
    isInitializing = false,
  } = options;

  mockedUseAuth.mockReturnValue({
    user:
      isAuthenticated
        ? {
            login:
              "user@example.com",

            role:
              "USER",
          }
        : null,

    isAuthenticated,

    isInitializing,

    login:
      loginMock,

    completeMfa:
      completeMfaMock,

    processAuthenticationResponse:
      processAuthenticationResponseMock,

    logout:
      logoutMock,

    hasRole:
      hasRoleMock,
  });
}

function LocationDisplay() {
  const location =
    useLocation();

  return (
    <div
      data-testid="current-location"
    >
      {location.pathname}
    </div>
  );
}

function renderPage(
  initialEntry:
    | string
    | {
        pathname: string;
        search?: string;
        state?: unknown;
      } = "/login"
) {
  return render(
    <MemoryRouter
      initialEntries={[
        initialEntry,
      ]}
    >
      <LocationDisplay />

      <Routes>
        <Route
          path="/login"
          element={
            <LoginPage />
          }
        />

        <Route
          path="/register"
          element={
            <h1>
              Cadastro
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

describe(
  "LoginPage",
  () => {
    beforeEach(
      () => {
        vi.clearAllMocks();

        configureAuthContext();
      }
    );

    it(
      "renders login form",
      () => {
        renderPage();

        expect(
          screen.getByRole(
            "heading",
            {
              name:
                "Acesse sua conta",
            }
          )
        ).toBeInTheDocument();

        expect(
          screen.getByLabelText(
            "Login"
          )
        ).toBeInTheDocument();

        expect(
          screen.getByLabelText(
            "Senha"
          )
        ).toBeInTheDocument();

        expect(
          screen.getByRole(
            "button",
            {
              name:
                "Entrar na plataforma",
            }
          )
        ).toBeDisabled();
      }
    );

    it(
      "submits traditional login and navigates to jobs",
      async () => {
        const user =
          userEvent.setup();

        loginMock.mockResolvedValue({
          mfa_required:
            false,

          access_token:
            "jwt-token",

          token_type:
            "Bearer",

          expires_in:
            7200,
        });

        renderPage();

        await user.type(
          screen.getByLabelText(
            "Login"
          ),
          "USER@EXAMPLE.COM"
        );

        await user.type(
          screen.getByLabelText(
            "Senha"
          ),
          "password"
        );

        await user.click(
          screen.getByRole(
            "button",
            {
              name:
                "Entrar na plataforma",
            }
          )
        );

        await waitFor(
          () => {
            expect(
              loginMock
            ).toHaveBeenCalledWith({
              login:
                "USER@EXAMPLE.COM",

              password:
                "password",
            });
          }
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
        const user =
          userEvent.setup();

        loginMock.mockResolvedValue({
          mfa_required:
            true,

          challenge_token:
            "mfa-challenge-token",
        });

        renderPage();

        await user.type(
          screen.getByLabelText(
            "Login"
          ),
          "user@example.com"
        );

        await user.type(
          screen.getByLabelText(
            "Senha"
          ),
          "password"
        );

        await user.click(
          screen.getByRole(
            "button",
            {
              name:
                "Entrar na plataforma",
            }
          )
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
      "starts Google login",
      async () => {
        const user =
          userEvent.setup();

        renderPage();

        await user.click(
          screen.getByRole(
            "button",
            {
              name:
                "Google",
            }
          )
        );

        expect(
          mockedStartGoogleLogin
        ).toHaveBeenCalledOnce();
      }
    );

    it(
      "starts GitHub login",
      async () => {
        const user =
          userEvent.setup();

        renderPage();

        await user.click(
          screen.getByRole(
            "button",
            {
              name:
                "GitHub",
            }
          )
        );

        expect(
          mockedStartGitHubLogin
        ).toHaveBeenCalledOnce();
      }
    );

    it(
      "shows OAuth error from URL",
      () => {
        renderPage(
          "/login?oauthError=Falha%20no%20OAuth"
        );

        expect(
          screen.getByRole(
            "alert"
          )
        ).toHaveTextContent(
          "Falha no OAuth"
        );
      }
    );

    it(
      "shows session expiration message",
      () => {
        renderPage(
          "/login?sessionExpired=true"
        );

        expect(
          screen.getByRole(
            "alert"
          )
        ).toHaveTextContent(
          "Sua sessão expirou. Entre novamente."
        );
      }
    );

    it(
      "shows registration success message",
      () => {
        renderPage({
          pathname:
            "/login",

          state: {
            registrationSuccess:
              true,
          },
        });

        expect(
          screen.getByRole(
            "status"
          )
        ).toHaveTextContent(
          "Conta criada com sucesso."
        );
      }
    );

    it(
      "shows invalid credentials error",
      async () => {
        const user =
          userEvent.setup();

        loginMock.mockRejectedValue(
          new Error(
            "Login ou senha inválidos."
          )
        );

        renderPage();

        await user.type(
          screen.getByLabelText(
            "Login"
          ),
          "user@example.com"
        );

        await user.type(
          screen.getByLabelText(
            "Senha"
          ),
          "wrong-password"
        );

        await user.click(
          screen.getByRole(
            "button",
            {
              name:
                "Entrar na plataforma",
            }
          )
        );

        expect(
          await screen.findByRole(
            "alert"
          )
        ).toHaveTextContent(
          "Login ou senha inválidos."
        );
      }
    );

    it(
      "toggles password visibility",
      async () => {
        const user =
          userEvent.setup();

        renderPage();

        const passwordInput =
          screen.getByLabelText(
            "Senha"
          );

        expect(
          passwordInput
        ).toHaveAttribute(
          "type",
          "password"
        );

        await user.click(
          screen.getByRole(
            "button",
            {
              name:
                "Mostrar senha",
            }
          )
        );

        expect(
          passwordInput
        ).toHaveAttribute(
          "type",
          "text"
        );

        await user.click(
          screen.getByRole(
            "button",
            {
              name:
                "Ocultar senha",
            }
          )
        );

        expect(
          passwordInput
        ).toHaveAttribute(
          "type",
          "password"
        );
      }
    );

    it(
      "redirects authenticated user to jobs",
      () => {
        configureAuthContext({
          isAuthenticated:
            true,

          isInitializing:
            false,
        });

        renderPage();

        expect(
          screen.getByRole(
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
      "opens registration page",
      async () => {
        const user =
          userEvent.setup();

        renderPage();

        await user.click(
          screen.getByRole(
            "button",
            {
              name:
                "Criar conta de candidato",
            }
          )
        );

        expect(
          await screen.findByRole(
            "heading",
            {
              name:
                "Cadastro",
            }
          )
        ).toBeInTheDocument();

        expect(
          screen.getByTestId(
            "current-location"
          )
        ).toHaveTextContent(
          "/register"
        );

        expect(
          screen.queryByRole(
            "heading",
            {
              name:
                "Acesse sua conta",
            }
          )
        ).not.toBeInTheDocument();
      }
    );
  }
);