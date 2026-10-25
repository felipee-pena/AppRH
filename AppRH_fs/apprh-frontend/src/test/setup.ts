import {
  afterEach,
} from "vitest";

import {
  cleanup,
} from "@testing-library/react";

import "@testing-library/jest-dom/vitest";

afterEach(() => {
  cleanup();

  localStorage.clear();
  sessionStorage.clear();

  document.body.classList.remove(
    "modal-open"
  );

  document.body.style.overflow = "";
});