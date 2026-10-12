import { Link } from "react-router-dom";

export function NotFoundPage() {
  return (
    <main className="not-found-page">
      <span className="not-found-page__code">
        404
      </span>

      <h1>Página não encontrada</h1>

      <p>
        O endereço informado não existe ou foi movido.
      </p>

      <Link
        to="/"
        className="not-found-page__button"
      >
        Voltar ao dashboard
      </Link>
    </main>
  );
}