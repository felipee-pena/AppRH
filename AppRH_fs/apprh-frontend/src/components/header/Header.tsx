import {
  NavLink,
  useNavigate,
} from "react-router-dom";

import {
  useLogout,
} from "../../hooks/useLogout";

import "./header.css";

export function Header() {
  const navigate =
    useNavigate();

  const {
    mutate: logout,
    isPending,
  } =
    useLogout();

  function handleLogout() {
    logout(
      undefined,
      {
        onSuccess: () => {
          localStorage.clear();
          sessionStorage.clear();

          navigate(
            "/login",
            {
              replace: true,
            }
          );
        },

        onError: (
          error: Error
        ) => {
          console.error(
            "Erro ao sair:",
            error
          );
        },
      }
    );
  }

  const getLinkClass = ({
    isActive,
  }: {
    isActive: boolean;
  }) => {
    return isActive
      ? "app-header__link app-header__link--active"
      : "app-header__link";
  };

  return (
    <header className="app-header">
      <div className="app-header__container">
        <NavLink
          to="/jobs"
          className="app-header__brand"
        >
          <span className="app-header__brand-icon">
            RH
          </span>

          <span className="app-header__brand-text">
            AppRH
          </span>
        </NavLink>

        <nav
          className="app-header__navigation"
          aria-label="Navegação principal"
        >
          <NavLink
            to="/jobs"
            end
            className={getLinkClass}
          >
            Vagas
          </NavLink>

          <NavLink
            to="/management/jobs"
            className={getLinkClass}
          >
            Gestão de vagas
          </NavLink>

          <NavLink
            to="/candidates"
            className={getLinkClass}
          >
            Candidatos
          </NavLink>
        </nav>

        <div className="app-header__actions">
          <div className="app-header__profile">
            <span className="app-header__avatar">
              FP
            </span>

            <div className="app-header__profile-information">
              <span className="app-header__profile-name">
                Felipe
              </span>

              <span className="app-header__profile-role">
                Administrador
              </span>
            </div>
          </div>

          <button
            type="button"
            className="app-header__logout-button"
            onClick={handleLogout}
            disabled={isPending}
          >
            {isPending
              ? "Saindo..."
              : "Sair"}
          </button>
        </div>
      </div>
    </header>
  );
}