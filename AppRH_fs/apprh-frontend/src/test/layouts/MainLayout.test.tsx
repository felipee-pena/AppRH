import {
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
  MainLayout,
} from "../../layouts/MainLayout";

vi.mock(
  "../../components/header/Header",
  () => ({
    Header:
      () => (
        <header>
          Cabeçalho principal
        </header>
      ),
  })
);

vi.mock(
  "../../components/footer/Footer",
  () => ({
    Footer:
      () => (
        <footer>
          Rodapé principal
        </footer>
      ),
  })
);

describe(
  "MainLayout",
  () => {
    it(
      "renders header, routed content and footer",
      () => {
        render(
          <MemoryRouter
            initialEntries={[
              "/content",
            ]}
          >
            <Routes>
              <Route
                element={
                  <MainLayout />
                }
              >
                <Route
                  path="/content"
                  element={
                    <p>
                      Conteúdo da rota
                    </p>
                  }
                />
              </Route>
            </Routes>
          </MemoryRouter>
        );

        expect(
          screen.getByText(
            "Cabeçalho principal"
          )
        ).toBeInTheDocument();

        expect(
          screen.getByText(
            "Conteúdo da rota"
          )
        ).toBeInTheDocument();

        expect(
          screen.getByText(
            "Rodapé principal"
          )
        ).toBeInTheDocument();
      }
    );
  }
);