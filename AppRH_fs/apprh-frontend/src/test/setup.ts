import "@testing-library/jest-dom/vitest";

import {
  afterEach,
  vi,
} from "vitest";

import {
  cleanup,
} from "@testing-library/react";

vi.stubEnv(
  "VITE_API_URL",
  "http://localhost:8081"
);

vi.stubEnv(
  "VITE_ACCESS_TOKEN_KEY",
  "apprh.test.access-token"
);

afterEach(
  () => {
    cleanup();

    localStorage.clear();
    sessionStorage.clear();

    vi.clearAllMocks();
  }
);
