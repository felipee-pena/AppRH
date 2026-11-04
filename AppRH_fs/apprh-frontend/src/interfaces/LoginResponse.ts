export interface LoginResponse {
  mfa_required?: boolean;

  challenge_token?: string;

  access_token?: string;

  token_type?: "Bearer";

  expires_in?: number;
}