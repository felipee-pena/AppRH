import axios from "axios";

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

import type {
  MfaStatusResponse,
} from "../interfaces/MfaStatusResponse";

import type {
  MfaSetupResponse,
} from "../interfaces/MfaSetupResponse";

import type {
  MfaDisableRequest,
} from "../interfaces/MfaDisableRequest";

import type {
  RecoveryCodesResponse,
} from "../interfaces/RecoveryCodesResponse";

import {
  getAccessToken,
} from "./token-storage";

const API_URL =
  import.meta.env.VITE_API_URL
  ?? "http://localhost:8081";

function createAuthorizationHeaders() {
  const accessToken =
    getAccessToken();

  if (!accessToken) {
    return {};
  }

  return {
    Authorization:
      `Bearer ${accessToken}`,
  };
}

export async function login(
  credentials: LoginRequest
): Promise<LoginResponse> {
  const response =
    await axios.post<LoginResponse>(
      `${API_URL}/auth/login`,
      {
        login: credentials.login
          .trim()
          .toLowerCase(),
        password: credentials.password,
      },
      {
        headers: {
          Accept: "application/json",
          "Content-Type":
            "application/json",
        },
      }
    );

  return response.data;
}

export async function confirmEmail(
  request: EmailConfirmationRequest
): Promise<MessageResponse> {
  const response =
    await axios.post<MessageResponse>(
      `${API_URL}/auth/email/confirm`,
      request,
      {
        headers: {
          Accept: "application/json",
          "Content-Type":
            "application/json",
        },
      }
    );

  return response.data;
}

export async function resendEmailConfirmation(
  request: EmailResendRequest
): Promise<MessageResponse> {
  const response =
    await axios.post<MessageResponse>(
      `${API_URL}/auth/email/resend`,
      {
        login: request.login
          .trim()
          .toLowerCase(),
      },
      {
        headers: {
          Accept: "application/json",
          "Content-Type":
            "application/json",
        },
      }
    );

  return response.data;
}

export async function exchangeOAuthCode(
  code: string
): Promise<LoginResponse> {
  const response =
    await axios.post<LoginResponse>(
      `${API_URL}/auth/oauth/exchange`,
      {
        code,
      },
      {
        headers: {
          Accept: "application/json",
          "Content-Type":
            "application/json",
        },
      }
    );

  return response.data;
}

export async function verifyMfa(
  challengeToken: string,
  code: string
): Promise<LoginResponse> {
  const response =
    await axios.post<LoginResponse>(
      `${API_URL}/auth/mfa/verify`,
      {
        challenge_token:
          challengeToken,
        code,
      },
      {
        headers: {
          Accept: "application/json",
          "Content-Type":
            "application/json",
        },
      }
    );

  return response.data;
}

export async function getMfaStatus():
  Promise<MfaStatusResponse> {
  const response =
    await axios.get<MfaStatusResponse>(
      `${API_URL}/api/account/mfa/status`,
      {
        headers: {
          Accept: "application/json",
          ...createAuthorizationHeaders(),
        },
      }
    );

  return response.data;
}

export async function beginMfaSetup():
  Promise<MfaSetupResponse> {
  const response =
    await axios.post<MfaSetupResponse>(
      `${API_URL}/api/account/mfa/totp/setup`,
      {},
      {
        headers: {
          Accept: "application/json",
          "Content-Type":
            "application/json",
          ...createAuthorizationHeaders(),
        },
      }
    );

  return response.data;
}

export async function confirmMfaSetup(
  code: string
): Promise<MessageResponse> {
  const response =
    await axios.post<MessageResponse>(
      `${API_URL}/api/account/mfa/totp/confirm`,
      {
        code,
      },
      {
        headers: {
          Accept: "application/json",
          "Content-Type":
            "application/json",
          ...createAuthorizationHeaders(),
        },
      }
    );

  return response.data;
}

export async function regenerateRecoveryCodes():
  Promise<RecoveryCodesResponse> {
  const response =
    await axios.post<RecoveryCodesResponse>(
      `${API_URL}/api/account/mfa/recovery-codes/regenerate`,
      {},
      {
        headers: {
          Accept: "application/json",
          "Content-Type":
            "application/json",
          ...createAuthorizationHeaders(),
        },
      }
    );

  return response.data;
}

export async function disableMfa(
  request: MfaDisableRequest
): Promise<MessageResponse> {
  const response =
    await axios.delete<MessageResponse>(
      `${API_URL}/api/account/mfa/totp`,
      {
        headers: {
          Accept: "application/json",
          "Content-Type":
            "application/json",
          ...createAuthorizationHeaders(),
        },
        data: request,
      }
    );

  return response.data;
}

export function startGoogleLogin():
  void {
  window.location.assign(
    `${API_URL}/oauth2/authorization/google`
  );
}

export function startGitHubLogin():
  void {
  window.location.assign(
    `${API_URL}/oauth2/authorization/github`
  );
}