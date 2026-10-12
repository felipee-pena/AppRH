import {
  BrowserRouter,
  Route,
  Routes,
} from "react-router-dom";

import { MainLayout } from "../layouts/MainLayout";
import { HomePage } from "../pages/home/HomePage";
import { JobsPage } from "../pages/jobs/JobsPage";
import { JobDetailsPage } from "../pages/jobs/JobDetailsPage";
import { CandidatesPage } from "../pages/candidates/CandidatesPage";
import { CandidateDetailsPage } from "../pages/candidates/CandidateDetailsPage";
import { NotFoundPage } from "../pages/not-found/NotFoundPage";

export function AppRoutes() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<MainLayout />}>
          <Route path="/" element={<HomePage />} />

          <Route path="/jobs" element={<JobsPage />} />

          <Route
            path="/jobs/:code"
            element={<JobDetailsPage />}
          />

          <Route
            path="/candidates"
            element={<CandidatesPage />}
          />

          <Route
            path="/candidates/:id"
            element={<CandidateDetailsPage />}
          />

          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}