import {
  useNavigate,
} from "react-router-dom";

import {
  AppHeader,
} from "../../components/header/AppHeader";

export function ForbiddenPage() {
  const navigate = useNavigate();

  return (
    <>
      <AppHeader />

      <main className="forbidden-page">
        <section className="forbidden-card">
          <span className="forbidden-card__status">
            403
          </span>

          <h1>Acesso negado</h1>

          <p>
            Seu perfil não possui permissão para
            acessar esta funcionalidade.
          </p>

          <button
            type="button"
            onClick={() => {
              navigate("/jobs");
            }}
          >
            Voltar para as vagas
          </button>
        </section>
      </main>
    </>
  );
}