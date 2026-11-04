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

import {
  MfaVerifyPage,
} from "../../../pages/mfa-verify/MfaVerifyPage";

import {
  useAuth,
} from "../../../hooks/useAuth";

import {
  getMfaChallenge,
  removeMfaChallenge,
} from "../../../services/mfa-storage";

vi.mock(
  "../../../hooks/useAuth"
);

vi.mock(
  "../../../services/mfa-storage",
  () => ({
    getMfaChallenge:
      vi.fn(),

    removeMfaChallenge:
      vi.fn(),
  })
);

const mockedUseAuth =
  vi.mocked(
    useAuth
  );

const mockedGetMfaChallenge =
  vi.mocked(
    getMfaChallenge
  );

const mockedRemoveMfaChallenge =
  vi.mocked(
    removeMfaChallenge
  );

const completeMfa =
  vi.fn();

function configureAuth(
  options: {
    authenticated?: boolean;
    initializing?: boolean;
  } = {}
) {
  const {
    authenticated = false,
    initializing = false,
  } = options;

  mockedUseAuth.mockReturnValue({
    user:
      authenticated
        ? {
            login:
              "usuario@example.com",

            role:
              "USER",
          }
        : null,

    isAuthenticated:
      authenticated,

    isInitializing:
      initializing,

    login:
      vi.fn(),

    completeMfa,

    processAuthenticationResponse:
      vi.fn(),

    logout:
      vi.fn(),

    hasRole:
      vi.fn(
        () => false
      ),
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

function renderPage() {
  return render(
    <MemoryRouter
      initialEntries={[
        "/mfa/verify",
      ]}
    >
      <LocationDisplay />

      <Routes>
        <Route
          path="/mfa/verify"
          element={
            <MfaVerifyPage />
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
      </Routes>
    </MemoryRouter>
  );
}

describe(
  "MfaVerifyPage",
  () => {
    beforeEach(
      () => {
        vi.clearAllMocks();

        configureAuth();

        mockedGetMfaChallenge
          .mockReturnValue(
            "challenge-token"
          );
      }
    );

    it(
      "renders MFA verification form",
      () => {
        renderPage();

        expect(
          screen.getByRole(
            "heading",
            {
              name:
                "Verificação em duas etapas",
            }
          )
        ).toBeInTheDocument();

        expect(
          screen.getByLabelText(
            "Código de segurança"
          )
        ).toBeInTheDocument();

        expect(
          screen.getByRole(
            "button",
            {
              name:
                "Validar código",
            }
          )
        ).toBeDisabled();
      }
    );

    it(
      "accepts only numeric characters",
      async () => {
        const user =
          userEvent.setup();

        renderPage();

        const input =
          screen.getByLabelText(
            "Código de segurança"
          );

        await user.type(
          input,
          "12a3-456x"
        );

        expect(
          input
        ).toHaveValue(
          "123456"
        );
      }
    );

    it(
      "limits the code to six digits",
      async () => {
        const user =
          userEvent.setup();

        renderPage();

        const input =
          screen.getByLabelText(
            "Código de segurança"
          );

        await user.type(
          input,
          "123456789"
        );

        expect(
          input
        ).toHaveValue(
          "123456"
        );
      }
    );

    it(
      "validates MFA and navigates to jobs",
      async () => {
        const user =
          userEvent.setup();

        completeMfa.mockResolvedValue({
          mfa_required:
            false,

          access_token:
            "jwt-final",

          token_type:
            "Bearer",

          expires_in:
            7200,
        });

        renderPage();

        await user.type(
          screen.getByLabelText(
            "Código de segurança"
          ),
          "123456"
        );

        await user.click(
          screen.getByRole(
            "button",
            {
              name:
                "Validar código",
            }
          )
        );

        await waitFor(
          () => {
            expect(
              completeMfa
            ).toHaveBeenCalledWith(
              "123456"
            );
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
      "shows MFA validation error",
      async () => {
        const user =
          userEvent.setup();

        completeMfa.mockRejectedValue(
          new Error(
            "Código MFA inválido."
          )
        );

        renderPage();

        await user.type(
          screen.getByLabelText(
            "Código de segurança"
          ),
          "999999"
        );

        await user.click(
          screen.getByRole(
            "button",
            {
              name:
                "Validar código",
            }
          )
        );

        expect(
          await screen.findByRole(
            "alert"
          )
        ).toHaveTextContent(
          "Código MFA inválido."
        );

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
      "redirects to login when challenge is absent",
      () => {
        mockedGetMfaChallenge
          .mockReturnValue(
            null
          );

        renderPage();

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
      "redirects authenticated user to jobs",
      () => {
        configureAuth({
          authenticated:
            true,

          initializing:
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
      "cancels MFA verification",
      async () => {
        const user =
          userEvent.setup();

        renderPage();

        await user.click(
          screen.getByRole(
            "button",
            {
              name:
                "Cancelar e voltar ao login",
            }
          )
        );

        expect(
          mockedRemoveMfaChallenge
        ).toHaveBeenCalledOnce();

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
  }
);