import "./footer.css";

export function Footer() {
  const currentYear = new Date().getFullYear();

  return (
    <footer className="app-footer">
      <div className="app-footer__container">
        <div className="app-footer__brand">
          <span className="app-footer__brand-icon">
            RH
          </span>

          <div>
            <strong>AppRH</strong>

            <p>
              Gestão de vagas e candidatos
            </p>
          </div>
        </div>

        <div className="app-footer__information">
          <span>
            © {currentYear} AppRH
          </span>

          <span
            className="app-footer__separator"
            aria-hidden="true"
          >
            •
          </span>

          <span>
            Sistema de recrutamento
          </span>
        </div>
      </div>
    </footer>
  );
}