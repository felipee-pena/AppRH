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
    Accept: "application/json",
    "Content-Type": "application/json",
  },
});

api.interceptors.request.use(
  (config) => {
    const token =
      getAccessToken();

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
    const requestUrl =
      error.config?.url ?? "";

    const publicAuthenticationEndpoints = [
      "/auth/login",
      "/auth/register",
      "/auth/email/confirm",
      "/auth/email/resend",
    ];

    const isPublicAuthenticationRequest =
      publicAuthenticationEndpoints.some(
        (endpoint) =>
          requestUrl.startsWith(endpoint)
      );

    if (
      error.response?.status === 401
      && !isPublicAuthenticationRequest
    ) {
      removeAccessToken();

      if (
        window.location.pathname !== "/login"
      ) {
        window.location.href =
          "/login";
      }
    }

    return Promise.reject(error);
  }
);