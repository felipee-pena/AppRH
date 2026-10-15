import { api } from "./api";

import type { LoginRequest } from "../interfaces/LoginRequest";
import type { LoginResponse } from "../interfaces/LoginResponse";

export async function login(
  credentials: LoginRequest
): Promise<LoginResponse> {
  const response = await api.post<LoginResponse>(
    "/auth/login",
    credentials
  );

  return response.data;
}