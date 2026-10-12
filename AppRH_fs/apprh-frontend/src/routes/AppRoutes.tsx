import {
  BrowserRouter,
  Navigate,
  Route,
  Routes,
} from "react-router-dom";

import { JobsPage } from "../pages/jobs/JobsPage";
import { HomePage } from "../pages/home/HomePage";

export function AppRoutes() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<HomePage />} />

        <Route path="/jobs" element={<JobsPage />} />

        <Route
          path="/jobs/:code"
          element={<div>Detalhes da vaga</div>}
        />

        <Route
          path="/candidates"
          element={<div>Candidatos</div>}
        />

        <Route
          path="*"
          element={<Navigate to="/" replace />}
        />
      </Routes>
    </BrowserRouter>
  );
}