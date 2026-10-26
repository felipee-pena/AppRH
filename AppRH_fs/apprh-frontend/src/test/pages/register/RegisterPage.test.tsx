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
  RegisterPage,
} from "../../../pages/register/RegisterPage";

import {
  useAuth,
} from "../../../hooks/useAuth";

import {
  useCompanies,
} from "../../../hooks/useCompanies";

import {
  api,
} from "../../../services/api";

vi.mock(
  "../../../hooks/useAuth",
  () => ({
    useAuth: vi.fn(),
  })
);

vi.mock(
  "../../../hooks/useCompanies",
  () => ({
    useCompanies: vi.fn(),
  })
);

vi.mock(
  "../../../services/api",
  () => ({
    api: {
      post: vi.fn(),
    },
  })
);

const mockedUseAuth =
  vi.mocked(
    useAuth
  );

const mockedUseCompanies =
  vi.mocked(
    useCompanies
  );

const mockedPost =
  vi.mocked(
    api.post
  );

function configureMocks() {
  mockedUseAuth.mockReturnValue({
    user: null,
    isAuthenticated: false,
    isInitializing: false,
    login: vi.fn(),
    logout: vi.fn(),
    hasRole: vi.fn(
      () => false
    ),
  } as unknown as ReturnType<typeof useAuth>);

  mockedUseCompanies.mockReturnValue({
    data: [
      {
        id: 1,
        name:
          "Empresa Teste",
      },
      {
        id: 2,
        name:
          "Evomanex",
      },
    ],
    isLoading: false,
    isError: false,
  } as unknown as ReturnType<typeof useCompanies>);
}

function renderPage() {
  return render(
    <MemoryRouter
      initialEntries={[
        "/register",
      ]}
    >
      <Routes>
        <Route
          path="/register"
          element={
            <RegisterPage />
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

        <Route
          path="/jobs"
          element={
            <p>
              Página de vagas
            </p>
          }
        />

        <Route
          path="/verificacao-pendente"
          element={
            <p>
              Verificação pendente
            </p>
          }
        />
      </Routes>
    </MemoryRouter>
  );
}

function fillCommonFields(
  login =
    "Usuario@Example.COM",
  password =
    "senha-segura"
) {
  fireEvent.change(
    screen.getByLabelText(
      "Login"
    ),
    {
      target: {
        value:
          login,
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
          password,
      },
    }
  );

  fireEvent.change(
    screen.getByLabelText(
      "Confirmar senha"
    ),
    {
      target: {
        value:
          password,
      },
    }
  );
}

describe(
  "RegisterPage",
  () => {
    beforeEach(
      () => {
        vi.clearAllMocks();

        configureMocks();

        mockedPost.mockResolvedValue({
          data: {
            message:
              "Cadastro realizado",
          },
        });
      }
    );

    it(
      "renders candidate registration by default",
      () => {
        renderPage();

        expect(
          screen.getByRole(
            "heading",
            {
              name:
                "Criar conta",
            }
          )
        ).toBeInTheDocument();

        expect(
          screen.getByRole(
            "button",
            {
              name:
                "Criar conta de candidato",
            }
          )
        ).toBeInTheDocument();

        expect(
          screen.queryByText(
            "Empresa",
            {
              selector:
                "strong",
            }
          )
        ).not.toBeInTheDocument();
      }
    );

    it(
      "registers a candidate",
      async () => {
        renderPage();

        fillCommonFields();

        fireEvent.click(
          screen.getByRole(
            "button",
            {
              name:
                "Criar conta de candidato",
            }
          )
        );

        await waitFor(
          () => {
            expect(
              mockedPost
            ).toHaveBeenCalledWith(
              "/auth/register",
              {
                login:
                  "usuario@example.com",
                password:
                  "senha-segura",
                role:
                  "USER",
                companyId:
                  null,
                newCompanyName:
                  null,
              }
            );
          }
        );

        await waitFor(
          () => {
            expect(
              screen.getByText(
                "Verificação pendente"
              )
            ).toBeInTheDocument();
          }
        );
      }
    );

    it(
      "validates minimum password length",
      () => {
        renderPage();

        fillCommonFields(
          "usuario@example.com",
          "123"
        );

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
          screen.getByRole(
            "alert"
          )
        ).toHaveTextContent(
          "A senha deve possuir pelo menos 8 caracteres."
        );

        expect(
          mockedPost
        ).not.toHaveBeenCalled();
      }
    );

    it(
      "validates password confirmation",
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

        fireEvent.change(
          screen.getByLabelText(
            "Confirmar senha"
          ),
          {
            target: {
              value:
                "senha-diferente",
            },
          }
        );

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
          screen.getByRole(
            "alert"
          )
        ).toHaveTextContent(
          "A confirmação da senha não corresponde."
        );

        expect(
          mockedPost
        ).not.toHaveBeenCalled();
      }
    );

    it(
      "requires a company for RH registration",
      () => {
        renderPage();

        fireEvent.click(
          screen.getByRole(
            "button",
            {
              name:
                /Profissional de RH/i,
            }
          )
        );

        fillCommonFields(
          "rh@example.com"
        );

        fireEvent.click(
          screen.getByRole(
            "button",
            {
              name:
                "Criar conta de RH",
            }
          )
        );

        expect(
          screen.getByRole(
            "alert"
          )
        ).toHaveTextContent(
          "Selecione uma empresa ou cadastre uma nova."
        );

        expect(
          mockedPost
        ).not.toHaveBeenCalled();
      }
    );

    it(
      "registers RH with an existing company",
      async () => {
        renderPage();

        fireEvent.click(
          screen.getByRole(
            "button",
            {
              name:
                /Profissional de RH/i,
            }
          )
        );

        fillCommonFields(
          "RH@Example.COM"
        );

        fireEvent.click(
          screen.getByRole(
            "option",
            {
              name:
                /Empresa Teste/i,
            }
          )
        );

        fireEvent.click(
          screen.getByRole(
            "button",
            {
              name:
                "Criar conta de RH",
            }
          )
        );

        await waitFor(
          () => {
            expect(
              mockedPost
            ).toHaveBeenCalledWith(
              "/auth/register",
              {
                login:
                  "rh@example.com",
                password:
                  "senha-segura",
                role:
                  "USERRH",
                companyId:
                  1,
                newCompanyName:
                  null,
              }
            );
          }
        );

        await waitFor(
          () => {
            expect(
              screen.getByText(
                "Verificação pendente"
              )
            ).toBeInTheDocument();
          }
        );
      }
    );

    it(
      "filters available companies",
      () => {
        renderPage();

        fireEvent.click(
          screen.getByRole(
            "button",
            {
              name:
                /Profissional de RH/i,
            }
          )
        );

        fireEvent.change(
          screen.getByLabelText(
            "Pesquisar empresa"
          ),
          {
            target: {
              value:
                "evoma",
            },
          }
        );

        expect(
          screen.getByRole(
            "option",
            {
              name:
                "Evomanex",
            }
          )
        ).toBeInTheDocument();

        expect(
          screen.queryByRole(
            "option",
            {
              name:
                "Empresa Teste",
            }
          )
        ).not.toBeInTheDocument();
      }
    );

    it(
      "shows empty company search result",
      () => {
        renderPage();

        fireEvent.click(
          screen.getByRole(
            "button",
            {
              name:
                /Profissional de RH/i,
            }
          )
        );

        fireEvent.change(
          screen.getByLabelText(
            "Pesquisar empresa"
          ),
          {
            target: {
              value:
                "empresa inexistente",
            },
          }
        );

        expect(
          screen.getByText(
            "Nenhuma empresa encontrada."
          )
        ).toBeInTheDocument();
      }
    );

    it(
      "registers RH with a new company",
      async () => {
        renderPage();

        fireEvent.click(
          screen.getByRole(
            "button",
            {
              name:
                /Profissional de RH/i,
            }
          )
        );

        fillCommonFields(
          "rh@example.com"
        );

        fireEvent.click(
          screen.getByRole(
            "checkbox",
            {
              name:
                /Minha empresa ainda não está cadastrada/i,
            }
          )
        );

        fireEvent.change(
          screen.getByLabelText(
            "Nome da nova empresa"
          ),
          {
            target: {
              value:
                "  Nova Empresa  ",
            },
          }
        );

        fireEvent.click(
          screen.getByRole(
            "button",
            {
              name:
                "Criar conta de RH",
            }
          )
        );

        await waitFor(
          () => {
            expect(
              mockedPost
            ).toHaveBeenCalledWith(
              "/auth/register",
              {
                login:
                  "rh@example.com",
                password:
                  "senha-segura",
                role:
                  "USERRH",
                companyId:
                  null,
                newCompanyName:
                  "Nova Empresa",
              }
            );
          }
        );

        await waitFor(
          () => {
            expect(
              screen.getByText(
                "Verificação pendente"
              )
            ).toBeInTheDocument();
          }
        );
      }
    );

    it(
      "requires the new company name",
      () => {
        renderPage();

        fireEvent.click(
          screen.getByRole(
            "button",
            {
              name:
                /Profissional de RH/i,
            }
          )
        );

        fillCommonFields(
          "rh@example.com"
        );

        fireEvent.click(
          screen.getByRole(
            "checkbox",
            {
              name:
                /Minha empresa ainda não está cadastrada/i,
            }
          )
        );

        const submitButton =
          screen.getByRole(
            "button",
            {
              name:
                "Criar conta de RH",
            }
          );

        const form =
          submitButton.closest(
            "form"
          );

        expect(
          form
        ).not.toBeNull();

        fireEvent.submit(
          form as HTMLFormElement
        );

        expect(
          screen.getByRole(
            "alert"
          )
        ).toHaveTextContent(
          "Informe o nome da nova empresa."
        );

        expect(
          mockedPost
        ).not.toHaveBeenCalled();
      }
    );

    it(
      "shows API message errors",
      async () => {
        mockedPost.mockRejectedValue({
          response: {
            status:
              400,
            data: {
              message:
                "Cadastro não permitido",
            },
          },
        });

        renderPage();

        fillCommonFields();

        fireEvent.click(
          screen.getByRole(
            "button",
            {
              name:
                "Criar conta de candidato",
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
              "Cadastro não permitido"
            );
          }
        );
      }
    );

    it(
      "shows API field validation errors",
      async () => {
        mockedPost.mockRejectedValue({
          response: {
            status:
              400,
            data: {
              errors: {
                login:
                  "Login inválido.",
                password:
                  "Senha inválida.",
              },
            },
          },
        });

        renderPage();

        fillCommonFields();

        fireEvent.click(
          screen.getByRole(
            "button",
            {
              name:
                "Criar conta de candidato",
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
              "Login inválido. Senha inválida."
            );
          }
        );
      }
    );

    it(
      "shows duplicate login error",
      async () => {
        mockedPost.mockRejectedValue({
          response: {
            status:
              409,
            data: {},
          },
        });

        renderPage();

        fillCommonFields();

        fireEvent.click(
          screen.getByRole(
            "button",
            {
              name:
                "Criar conta de candidato",
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
              "Este login já está cadastrado."
            );
          }
        );
      }
    );

    it(
      "shows generic registration error",
      async () => {
        mockedPost.mockRejectedValue(
          new Error(
            "Falha inesperada"
          )
        );

        renderPage();

        fillCommonFields();

        fireEvent.click(
          screen.getByRole(
            "button",
            {
              name:
                "Criar conta de candidato",
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
              "Não foi possível realizar o cadastro."
            );
          }
        );
      }
    );

    it(
      "navigates back to login",
      () => {
        renderPage();

        fireEvent.click(
          screen.getByRole(
            "button",
            {
              name:
                "Entrar na plataforma",
            }
          )
        );

        expect(
          screen.getByText(
            "Página de login"
          )
        ).toBeInTheDocument();
      }
    );

    it(
      "redirects authenticated users to jobs",
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
                "Criar conta",
            }
          )
        ).not.toBeInTheDocument();
      }
    );
  }
);