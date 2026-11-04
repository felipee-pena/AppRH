import {
  Navigate,
  Route,
  Routes,
} from "react-router-dom";

import {
  ProtectedRoute,
} from "../components/ProtectedRoute";

import {
  AccountSecurityPage,
} from "../pages/account-security/AccountSecurityPage";

import {
  EmailConfirmationPage,
} from "../pages/EmailConfirmationPage";

import {
  EmailVerificationPendingPage,
} from "../pages/EmailVerificationPendingPage";

import {
  ForbiddenPage,
} from "../pages/forbidden/ForbiddenPage";

import {
  JobManagementPage,
} from "../pages/job-management/JobManagementPage";

import {
  JobsPage,
} from "../pages/jobs/JobsPage";

import {
  LoginPage,
} from "../pages/login/LoginPage";

import {
  MfaVerifyPage,
} from "../pages/mfa-verify/MfaVerifyPage";

import {
  OAuthCallbackPage,
} from "../pages/oauth-callback/OAuthCallbackPage";

import {
  RegisterPage,
} from "../pages/register/RegisterPage";

import {
  UsersPage,
} from "../pages/users/UsersPage";

export function AppRoutes() {
  return (
    <Routes>
      <Route
        path="/login"
        element={
          <LoginPage />
        }
      />

      <Route
        path="/register"
        element={
          <RegisterPage />
        }
      />

      <Route
        path="/confirm-email"
        element={
          <EmailConfirmationPage />
        }
      />

      <Route
        path="/verificacao-pendente"
        element={
          <EmailVerificationPendingPage />
        }
      />

      <Route
        path="/oauth/callback"
        element={
          <OAuthCallbackPage />
        }
      />

      <Route
        path="/mfa/verify"
        element={
          <MfaVerifyPage />
        }
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
        path="/account/security"
        element={
          <ProtectedRoute>
            <AccountSecurityPage />
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
            allowedRoles={[
              "ADMIN",
            ]}
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
            to="/login"
            replace
          />
        }
      />

      <Route
        path="*"
        element={
          <Navigate
            to="/login"
            replace
          />
        }
      />
    </Routes>
  );
}