export interface MfaSetupResponse {
  secret: string;
  otpauth_uri: string;
  recovery_codes: string[];
}