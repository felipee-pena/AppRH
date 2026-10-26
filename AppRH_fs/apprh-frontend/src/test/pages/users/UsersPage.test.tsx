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
  UsersPage,
} from "../../../pages/users/UsersPage";

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

vi.mock(
  "../../../components/header/AppHeader",
  () => ({
    AppHeader:
      () => (
        <header>
          Cabeçalho
        </header>
      ),
  })
);

const mockedUseAuth =
  vi.mocked(
    useAuth
  );

describe(
  "UsersPage",
  () => {
    beforeEach(
      () => {
        vi.clearAllMocks();

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
      }
    );

    it(
      "renders authenticated administrator details",
      () => {
        render(
          <MemoryRouter>
            <UsersPage />
          </MemoryRouter>
        );

        expect(
          screen.getByText(
            "admin@example.com"
          )
        ).toBeInTheDocument();

        expect(
          screen.getByText(
            "ADMIN"
          )
        ).toBeInTheDocument();

        expect(
          screen.getByText(
            "Módulo em preparação"
          )
        ).toBeInTheDocument();
      }
    );

    it(
      "navigates back to jobs",
      () => {
        render(
          <MemoryRouter
            initialEntries={[
              "/admin/users",
            ]}
          >
            <Routes>
              <Route
                path="/admin/users"
                element={
                  <UsersPage />
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
            </Routes>
          </MemoryRouter>
        );

        fireEvent.click(
          screen.getByRole(
            "button",
            {
              name:
                "Voltar para vagas",
            }
          )
        );

        expect(
          screen.getByText(
            "Página de vagas"
          )
        ).toBeInTheDocument();
      }
    );
  }
);