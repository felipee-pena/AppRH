import { Link, useParams } from "react-router-dom";

export function CandidateDetailsPage() {
  const { id } = useParams();

  return (
    <main className="details-page">
      <Link
        to="/candidates"
        className="details-page__back-link"
      >
        ← Voltar para candidatos
      </Link>

      <span className="details-page__eyebrow">
        Recrutamento
      </span>

      <h1>Detalhes do candidato</h1>

      <section className="details-page__card">
        <p>
          Candidato selecionado: {id}
        </p>

        <p>
          A integração será disponibilizada quando a API de
          candidatos estiver implementada no backend.
        </p>
      </section>
    </main>
  );
}