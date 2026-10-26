import {
  fireEvent,
  render,
  screen,
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
  AppHeader,
} from "../../../components/header/AppHeader";

import {
  useAuth,
} from "../../../hooks/useAuth";

vi.mock(
  "../../../hooks/useAuth",
  () => ({
    useAuth:
      vi.fn(),
  })
);

const mockedUseAuth =
  vi.mocked(
    useAuth
  );

function configureAuth(
  role:
    "ADMIN"
    | "USERRH"
    | "USER"
) {
  const logout =
    vi.fn();

  mockedUseAuth.mockReturnValue({
    user: {
      login:
        "usuario@example.com",
      role,
    },
    isAuthenticated:
      true,
    isInitializing:
      false,
    login:
      vi.fn(),
    logout,
    hasRole:
      vi.fn(
        (...roles) =>
          roles.includes(
            role
          )
      ),
  } as unknown as ReturnType<typeof useAuth>);

  return logout;
}

describe(
  "AppHeader",
  () => {
    beforeEach(
      () => {
        vi.clearAllMocks();
      }
    );

    it(
      "shows management links for administrator",
      () => {
        configureAuth(
          "ADMIN"
        );

        render(
          <MemoryRouter>
            <AppHeader />
          </MemoryRouter>
        );

        expect(
          screen.getByRole(
            "link",
            {
              name:
                "Gerenciar vagas",
            }
          )
        ).toBeInTheDocument();

        expect(
          screen.getByRole(
            "link",
            {
              name:
                "Usuários",
            }
          )
        ).toBeInTheDocument();
      }
    );

    it(
      "shows job management but not users for RH",
      () => {
        configureAuth(
          "USERRH"
        );

        render(
          <MemoryRouter>
            <AppHeader />
          </MemoryRouter>
        );

        expect(
          screen.getByRole(
            "link",
            {
              name:
                "Gerenciar vagas",
            }
          )
        ).toBeInTheDocument();

        expect(
          screen.queryByRole(
            "link",
            {
              name:
                "Usuários",
            }
          )
        ).not.toBeInTheDocument();
      }
    );

    it(
      "hides management links for candidate",
      () => {
        configureAuth(
          "USER"
        );

        render(
          <MemoryRouter>
            <AppHeader />
          </MemoryRouter>
        );

        expect(
          screen.queryByRole(
            "link",
            {
              name:
                "Gerenciar vagas",
            }
          )
        ).not.toBeInTheDocument();

        expect(
          screen.queryByRole(
            "link",
            {
              name:
                "Usuários",
            }
          )
        ).not.toBeInTheDocument();
      }
    );

    it(
      "logs out and navigates to login",
      () => {
        const logout =
          configureAuth(
            "ADMIN"
          );

        render(
          <MemoryRouter
            initialEntries={[
              "/jobs",
            ]}
          >
            <Routes>
              <Route
                path="/jobs"
                element={
                  <AppHeader />
                }
              />

              <Route
                path="/login"
                element={
                  <p>
                    Página de login
                  </p>
                }
              />
            </Routes>
          </MemoryRouter>
        );

        fireEvent.click(
          screen.getByRole(
            "button",
            {
              name:
                "Sair",
            }
          )
        );

        expect(
          logout
        ).toHaveBeenCalledTimes(
          1
        );

        expect(
          screen.getByText(
            "Página de login"
          )
        ).toBeInTheDocument();
      }
    );
  }
);