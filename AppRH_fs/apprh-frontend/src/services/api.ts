import axios from "axios";

import {
  getAccessToken,
  removeAccessToken,
} from "./token-storage";

const API_BASE_URL =
  import.meta.env.VITE_API_URL;

if (!API_BASE_URL) {
  throw new Error(
    "A variável VITE_API_URL não foi configurada."
  );
}

export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

api.interceptors.request.use(
  (config) => {
    const token = getAccessToken();

    if (token) {
      config.headers.Authorization =
        `Bearer ${token}`;
    }

    return config;
  }
);

api.interceptors.response.use(
  (response) => response,

  (error) => {
    const isLoginRequest =
      error.config?.url === "/auth/login";

    if (
      error.response?.status === 401 &&
      !isLoginRequest
    ) {
      removeAccessToken();

      if (
        window.location.pathname !== "/login"
      ) {
        window.location.href = "/login";
      }
    }

    return Promise.reject(error);
  }
);