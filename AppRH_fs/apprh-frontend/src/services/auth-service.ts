import {
  api,
} from "./api";

import type {
  LoginRequest,
} from "../interfaces/LoginRequest";

import type {
  LoginResponse,
} from "../interfaces/LoginResponse";

import type {
  EmailConfirmationRequest,
} from "../interfaces/EmailConfirmationRequest";

import type {
  EmailResendRequest,
} from "../interfaces/EmailResendRequest";

import type {
  MessageResponse,
} from "../interfaces/MessageResponse";

export async function login(
  credentials: LoginRequest
): Promise<LoginResponse> {
  const response =
    await api.post<LoginResponse>(
      "/auth/login",
      credentials
    );

  return response.data;
}

export async function confirmEmail(
  request: EmailConfirmationRequest
): Promise<MessageResponse> {
  const response =
    await api.post<MessageResponse>(
      "/auth/email/confirm",
      request
    );

  return response.data;
}

export async function resendEmailConfirmation(
  request: EmailResendRequest
): Promise<MessageResponse> {
  const response =
    await api.post<MessageResponse>(
      "/auth/email/resend",
      request
    );

  return response.data;
}