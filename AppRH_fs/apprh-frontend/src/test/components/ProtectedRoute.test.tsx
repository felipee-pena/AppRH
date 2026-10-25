import {
  render,
  screen,
} from "@testing-library/react";

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
  ProtectedRoute,
} from "../../components/ProtectedRoute";

import {
  useAuth,
} from "../../hooks/useAuth";

vi.mock(
  "../../hooks/useAuth",
  () => ({
    useAuth: vi.fn(),
  })
);

const mockedUseAuth =
  vi.mocked(
    useAuth
  );

function LocationViewer() {
  const location =
    useLocation();

  const state =
    location.state as {
      from?: string;
    } | null;

  return (
    <div>
      <span data-testid="current-path">
        {location.pathname}
      </span>

      {state?.from && (
        <span data-testid="origin-path">
          {state.from}
        </span>
      )}
    </div>
  );
}

function renderProtectedRoute() {
  return render(
    <MemoryRouter
      initialEntries={[
        "/jobs",
      ]}
    >
      <Routes>
        <Route
          path="/jobs"
          element={
            <ProtectedRoute
              allowedRoles={[
                "ADMIN",
              ]}
            >
              <p>
                Conteúdo protegido
              </p>
            </ProtectedRoute>
          }
        />

        <Route
          path="/login"
          element={
            <LocationViewer />
          }
        />

        <Route
          path="/forbidden"
          element={
            <LocationViewer />
          }
        />
      </Routes>
    </MemoryRouter>
  );
}

describe(
  "ProtectedRoute",
  () => {
    beforeEach(
      () => {
        vi.clearAllMocks();
      }
    );

    it(
      "shows initialization feedback",
      () => {
        mockedUseAuth.mockReturnValue({
          user: null,

          isAuthenticated:
            false,

          isInitializing:
            true,

          hasRole:
            vi.fn(
              () => false
            ),

          login:
            vi.fn(),

          logout:
            vi.fn(),
        } as unknown as ReturnType<typeof useAuth>);

        renderProtectedRoute();

        expect(
          screen.getByText(
            "Verificando autenticação..."
          )
        ).toBeInTheDocument();

        expect(
          screen.queryByText(
            "Conteúdo protegido"
          )
        ).not.toBeInTheDocument();
      }
    );

    it(
      "redirects unauthenticated users to login",
      () => {
        mockedUseAuth.mockReturnValue({
          user: null,

          isAuthenticated:
            false,

          isInitializing:
            false,

          hasRole:
            vi.fn(
              () => false
            ),

          login:
            vi.fn(),

          logout:
            vi.fn(),
        } as unknown as ReturnType<typeof useAuth>);

        renderProtectedRoute();

        expect(
          screen.getByTestId(
            "current-path"
          )
        ).toHaveTextContent(
          "/login"
        );

        expect(
          screen.getByTestId(
            "origin-path"
          )
        ).toHaveTextContent(
          "/jobs"
        );

        expect(
          screen.queryByText(
            "Conteúdo protegido"
          )
        ).not.toBeInTheDocument();
      }
    );

    it(
      "redirects authenticated users without required role",
      () => {
        const hasRole =
          vi.fn(
            () => false
          );

        mockedUseAuth.mockReturnValue({
          user: {
            role:
              "USER",
          },

          isAuthenticated:
            true,

          isInitializing:
            false,

          hasRole,

          login:
            vi.fn(),

          logout:
            vi.fn(),
        } as unknown as ReturnType<typeof useAuth>);

        renderProtectedRoute();

        expect(
          hasRole
        ).toHaveBeenCalledWith(
          "ADMIN"
        );

        expect(
          screen.getByTestId(
            "current-path"
          )
        ).toHaveTextContent(
          "/forbidden"
        );

        expect(
          screen.queryByText(
            "Conteúdo protegido"
          )
        ).not.toBeInTheDocument();
      }
    );

    it(
      "renders content for authenticated users with required role",
      () => {
        const hasRole =
          vi.fn(
            () => true
          );

        mockedUseAuth.mockReturnValue({
          user: {
            role:
              "ADMIN",
          },

          isAuthenticated:
            true,

          isInitializing:
            false,

          hasRole,

          login:
            vi.fn(),

          logout:
            vi.fn(),
        } as unknown as ReturnType<typeof useAuth>);

        renderProtectedRoute();

        expect(
          hasRole
        ).toHaveBeenCalledWith(
          "ADMIN"
        );

        expect(
          screen.getByText(
            "Conteúdo protegido"
          )
        ).toBeInTheDocument();

        expect(
          screen.queryByTestId(
            "current-path"
          )
        ).not.toBeInTheDocument();
      }
    );

    it(
      "renders content when no role restriction is provided",
      () => {
        mockedUseAuth.mockReturnValue({
          user: {
            role:
              "USER",
          },

          isAuthenticated:
            true,

          isInitializing:
            false,

          hasRole:
            vi.fn(
              () => false
            ),

          login:
            vi.fn(),

          logout:
            vi.fn(),
        } as unknown as ReturnType<typeof useAuth>);

        render(
          <MemoryRouter
            initialEntries={[
              "/jobs",
            ]}
          >
            <ProtectedRoute>
              <p>
                Rota autenticada
              </p>
            </ProtectedRoute>
          </MemoryRouter>
        );

        expect(
          screen.getByText(
            "Rota autenticada"
          )
        ).toBeInTheDocument();
      }
    );
  }
);