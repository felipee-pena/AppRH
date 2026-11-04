    import type { ReactNode } from "react";

    import {
      Navigate,
      useLocation,
    } from "react-router-dom";

    import type {
      UserRole,
    } from "../interfaces/AuthUser";

    import { useAuth } from "../hooks/useAuth";

    interface ProtectedRouteProps {
      children: ReactNode;
      allowedRoles?: UserRole[];
    }

    export function ProtectedRoute({
      children,
      allowedRoles,
    }: ProtectedRouteProps) {
      const auth = useAuth();
      const location = useLocation();

      if (auth.isInitializing) {
        return (
          <main>
            <p>Verificando autenticação...</p>
          </main>
        );
      }

      if (!auth.isAuthenticated) {
        return (
          <Navigate
            to="/login"
            replace
            state={{
              from: location.pathname,
            }}
          />
        );
      }

      if (
        allowedRoles &&
        !auth.hasRole(...allowedRoles)
      ) {
        return (
          <Navigate
            to="/forbidden"
            replace
          />
        );
      }

      return <>{children}</>;
    }