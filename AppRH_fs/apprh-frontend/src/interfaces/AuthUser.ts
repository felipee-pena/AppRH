export type UserRole =
  | "ADMIN"
  | "USERRH"
  | "USER";

export interface AuthUser {
  login: string;
  role: UserRole;
}