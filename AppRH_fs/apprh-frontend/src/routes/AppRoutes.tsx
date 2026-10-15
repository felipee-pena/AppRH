import {
  Navigate,
  Route,
  Routes,
} from "react-router-dom";

import {
  ProtectedRoute,
} from "../components/ProtectedRoute";

import {
  LoginPage,
} from "../pages/login/LoginPage";

import {
  RegisterPage,
} from "../pages/register/RegisterPage";

import {
  JobsPage,
} from "../pages/jobs/JobsPage";

import {
  JobManagementPage,
} from "../pages/job-management/JobManagementPage";

import {
  UsersPage,
} from "../pages/users/UsersPage";

import {
  ForbiddenPage,
} from "../pages/forbidden/ForbiddenPage";

export function AppRoutes() {
  return (
    <Routes>
      <Route
        path="/login"
        element={<LoginPage />}
      />

      <Route
        path="/register"
        element={<RegisterPage />}
      />

      <Route
        path="/jobs"
        element={
          <ProtectedRoute>
            <JobsPage />
          </ProtectedRoute>
        }
      />

      <Route
        path="/management/jobs"
        element={
          <ProtectedRoute
            allowedRoles={[
              "ADMIN",
              "USERRH",
            ]}
          >
            <JobManagementPage />
          </ProtectedRoute>
        }
      />

      <Route
        path="/admin/users"
        element={
          <ProtectedRoute
            allowedRoles={["ADMIN"]}
          >
            <UsersPage />
          </ProtectedRoute>
        }
      />

      <Route
        path="/forbidden"
        element={
          <ProtectedRoute>
            <ForbiddenPage />
          </ProtectedRoute>
        }
      />

      <Route
        path="/"
        element={
          <Navigate
            to="/jobs"
            replace
          />
        }
      />

      <Route
        path="*"
        element={
          <Navigate
            to="/jobs"
            replace
          />
        }
      />
    </Routes>
  );
}