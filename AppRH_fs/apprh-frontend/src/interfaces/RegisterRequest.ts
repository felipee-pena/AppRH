export type PublicRegistrationRole =
  | "USER"
  | "USERRH";

export interface RegisterRequest {
  login: string;
  password: string;
  role: PublicRegistrationRole;
  companyId: number | null;
  newCompanyName: string | null;
}