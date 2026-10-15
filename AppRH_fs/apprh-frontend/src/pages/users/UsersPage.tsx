import {
  useNavigate,
} from "react-router-dom";

import {
  AppHeader,
} from "../../components/header/AppHeader";

import {
  useAuth,
} from "../../hooks/useAuth";

export function UsersPage() {
  const auth = useAuth();
  const navigate = useNavigate();

  return (
    <>
      <AppHeader />

      <main className="users-page">
        <header className="users-page__header">
          <div>
            <span className="users-page__eyebrow">
              Administração
            </span>

            <h1>Gestão de usuários</h1>

            <p>
              Consulte usuários e gerencie os perfis
              disponíveis no AppRH.
            </p>
          </div>

          <button
            type="button"
            onClick={() => {
              navigate("/jobs");
            }}
          >
            Voltar para vagas
          </button>
        </header>

        <section className="users-page__status">
          <h2>Módulo em preparação</h2>

          <p>
            Usuário autenticado:{" "}
            <strong>
              {auth.user?.login}
            </strong>
          </p>

          <p>
            Perfil:{" "}
            <strong>
              {auth.user?.role}
            </strong>
          </p>

          <p>
            A listagem e a criação de usuários de RH
            exigem endpoints administrativos no
            backend.
          </p>
        </section>
      </main>
    </>
  );
}