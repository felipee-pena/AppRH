import {
  NavLink,
  useNavigate,
} from "react-router-dom";

import {
  useAuth,
} from "../../hooks/useAuth";

import "./app-header.css";

export function AppHeader() {
  const auth = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    auth.logout();

    navigate("/login", {
      replace: true,
    });
  }

  return (
    <header className="app-header">
      <div className="app-header__brand">
        <span className="app-header__symbol">
          RH
        </span>

        <span>AppRH</span>
      </div>

      <nav className="app-header__navigation">
        <NavLink to="/jobs">
          Vagas
        </NavLink>

        {auth.hasRole(
          "ADMIN",
          "USERRH"
        ) && (
          <NavLink to="/management/jobs">
            Gerenciar vagas
          </NavLink>
        )}

        {auth.hasRole("ADMIN") && (
          <NavLink to="/admin/users">
            Usuários
          </NavLink>
        )}
      </nav>

      <div className="app-header__account">
        <div className="app-header__user">
          <strong>
            {auth.user?.login}
          </strong>

          <span>
            {auth.user?.role}
          </span>
        </div>

        <button
          type="button"
          className="app-header__logout"
          onClick={handleLogout}
        >
          Sair
        </button>
      </div>
    </header>
  );
}