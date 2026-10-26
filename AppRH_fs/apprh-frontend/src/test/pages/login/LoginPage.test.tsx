import {
  fireEvent,
  render,
  screen,
  waitFor,
} from "@testing-library/react";

import {
  MemoryRouter,
  Route,
  Routes,
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

vi.mock(
  "../../../hooks/useAuth",
  () => ({
    useAuth: vi.fn(),
  })
);

const mockedUseAuth =
  vi.mocked(
    useAuth
  );

function configureAuth(
  login = vi.fn()
) {
  mockedUseAuth.mockReturnValue({
    user: null,
    isAuthenticated: false,
    isInitializing: false,
    login,
    logout: vi.fn(),
    hasRole: vi.fn(
      () => false
    ),
  } as unknown as ReturnType<typeof useAuth>);

  return login;
}

function renderPage(
  initialEntry:
    | string
    | {
        pathname: string;
        state?: unknown;
      } = "/login"
) {
  return render(
    <MemoryRouter
      initialEntries={[
        initialEntry,
      ]}
    >
      <Routes>
        <Route
          path="/login"
          element={
            <LoginPage />
          }
        />

        <Route
          path="/jobs"
          element={
            <p>
              Página de vagas
            </p>
          }
        />

        <Route
          path="/management/jobs"
          element={
            <p>
              Gestão de vagas
            </p>
          }
        />

        <Route
          path="/register"
          element={
            <p>
              Página de cadastro
            </p>
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
        configureAuth();
      }
    );

    it(
      "renders login fields",
      () => {
        renderPage();

        expect(
          screen.getByLabelText(
            "Login"
          )
        ).toBeInTheDocument();

        expect(
          screen.getByLabelText(
            "Senha"
          )
        ).toHaveAttribute(
          "type",
          "password"
        );

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
      "enables login after filling required fields",
      () => {
        renderPage();

        fireEvent.change(
          screen.getByLabelText(
            "Login"
          ),
          {
            target: {
              value:
                "usuario@example.com",
            },
          }
        );

        fireEvent.change(
          screen.getByLabelText(
            "Senha"
          ),
          {
            target: {
              value:
                "senha-segura",
            },
          }
        );

        expect(
          screen.getByRole(
            "button",
            {
              name:
                "Entrar na plataforma",
            }
          )
        ).toBeEnabled();
      }
    );

    it(
      "shows and hides password",
      () => {
        renderPage();

        const passwordInput =
          screen.getByLabelText(
            "Senha"
          );

        fireEvent.click(
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

        expect(
          screen.getByRole(
            "button",
            {
              name:
                "Ocultar senha",
            }
          )
        ).toBeInTheDocument();

        fireEvent.click(
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
      "authenticates using trimmed login",
      async () => {
        const login =
          configureAuth(
            vi.fn().mockResolvedValue(
              undefined
            )
          );

        renderPage();

        fireEvent.change(
          screen.getByLabelText(
            "Login"
          ),
          {
            target: {
              value:
                "  usuario@example.com  ",
            },
          }
        );

        fireEvent.change(
          screen.getByLabelText(
            "Senha"
          ),
          {
            target: {
              value:
                "senha-segura",
            },
          }
        );

        fireEvent.click(
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
              login
            ).toHaveBeenCalledWith({
              login:
                "usuario@example.com",
              password:
                "senha-segura",
            });
          }
        );

        await waitFor(
          () => {
            expect(
              screen.getByText(
                "Página de vagas"
              )
            ).toBeInTheDocument();
          }
        );
      }
    );

    it(
      "returns to the original protected route",
      async () => {
        const login =
          configureAuth(
            vi.fn().mockResolvedValue(
              undefined
            )
          );

        renderPage({
          pathname:
            "/login",
          state: {
            from:
              "/management/jobs",
          },
        });

        fireEvent.change(
          screen.getByLabelText(
            "Login"
          ),
          {
            target: {
              value:
                "rh@example.com",
            },
          }
        );

        fireEvent.change(
          screen.getByLabelText(
            "Senha"
          ),
          {
            target: {
              value:
                "senha-segura",
            },
          }
        );

        fireEvent.click(
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
              login
            ).toHaveBeenCalledWith({
              login:
                "rh@example.com",
              password:
                "senha-segura",
            });
          }
        );

        await waitFor(
          () => {
            expect(
              screen.getByText(
                "Gestão de vagas"
              )
            ).toBeInTheDocument();
          }
        );
      }
    );

    it(
      "shows authentication error",
      async () => {
        configureAuth(
          vi.fn().mockRejectedValue(
            new Error(
              "Unauthorized"
            )
          )
        );

        renderPage();

        fireEvent.change(
          screen.getByLabelText(
            "Login"
          ),
          {
            target: {
              value:
                "usuario@example.com",
            },
          }
        );

        fireEvent.change(
          screen.getByLabelText(
            "Senha"
          ),
          {
            target: {
              value:
                "senha-incorreta",
            },
          }
        );

        fireEvent.click(
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
              screen.getByRole(
                "alert"
              )
            ).toHaveTextContent(
              "Login ou senha inválidos. Confira os dados e tente novamente."
            );
          }
        );

        expect(
          screen.queryByText(
            "Página de vagas"
          )
        ).not.toBeInTheDocument();
      }
    );

    it(
      "clears the authentication error when login changes",
      async () => {
        configureAuth(
          vi.fn().mockRejectedValue(
            new Error(
              "Unauthorized"
            )
          )
        );

        renderPage();

        const loginInput =
          screen.getByLabelText(
            "Login"
          );

        fireEvent.change(
          loginInput,
          {
            target: {
              value:
                "usuario@example.com",
            },
          }
        );

        fireEvent.change(
          screen.getByLabelText(
            "Senha"
          ),
          {
            target: {
              value:
                "senha-incorreta",
            },
          }
        );

        fireEvent.click(
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
              screen.getByRole(
                "alert"
              )
            ).toBeInTheDocument();
          }
        );

        fireEvent.change(
          loginInput,
          {
            target: {
              value:
                "outro@example.com",
            },
          }
        );

        expect(
          screen.queryByRole(
            "alert"
          )
        ).not.toBeInTheDocument();
      }
    );

    it(
      "shows registration success feedback",
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
          "Conta criada com sucesso. Agora você já pode entrar."
        );
      }
    );

    it(
      "navigates to registration",
      () => {
        renderPage();

        fireEvent.click(
          screen.getByRole(
            "button",
            {
              name:
                "Criar conta de candidato",
            }
          )
        );

        expect(
          screen.getByText(
            "Página de cadastro"
          )
        ).toBeInTheDocument();
      }
    );

    it(
      "redirects authenticated user to jobs",
      () => {
        mockedUseAuth.mockReturnValue({
          user: {
            login:
              "admin@example.com",
            role:
              "ADMIN",
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
        } as unknown as ReturnType<typeof useAuth>);

        renderPage();

        expect(
          screen.getByText(
            "Página de vagas"
          )
        ).toBeInTheDocument();

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

    it(
      "keeps login page visible during authentication initialization",
      () => {
        mockedUseAuth.mockReturnValue({
          user: {
            login:
              "admin@example.com",
            role:
              "ADMIN",
          },
          isAuthenticated:
            true,
          isInitializing:
            true,
          login:
            vi.fn(),
          logout:
            vi.fn(),
          hasRole:
            vi.fn(
              () => true
            ),
        } as unknown as ReturnType<typeof useAuth>);

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
          screen.queryByText(
            "Página de vagas"
          )
        ).not.toBeInTheDocument();
      }
    );
  }
);