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
  describe,
  expect,
  it,
  vi,
} from "vitest";

import {
  ForbiddenPage,
} from "../../../pages/forbidden/ForbiddenPage";

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

describe(
  "ForbiddenPage",
  () => {
    it(
      "renders access denied feedback",
      () => {
        render(
          <MemoryRouter>
            <ForbiddenPage />
          </MemoryRouter>
        );

        expect(
          screen.getByText(
            "403"
          )
        ).toBeInTheDocument();

        expect(
          screen.getByRole(
            "heading",
            {
              name:
                "Acesso negado",
            }
          )
        ).toBeInTheDocument();

        expect(
          screen.getByText(
            "Seu perfil não possui permissão para acessar esta funcionalidade."
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
              "/forbidden",
            ]}
          >
            <Routes>
              <Route
                path="/forbidden"
                element={
                  <ForbiddenPage />
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
                "Voltar para as vagas",
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