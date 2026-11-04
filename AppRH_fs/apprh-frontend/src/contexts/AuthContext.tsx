import {
  createContext,
  useCallback,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";

import type {
  AuthUser,
  UserRole,
} from "../interfaces/AuthUser";

import type {
  LoginRequest,
} from "../interfaces/LoginRequest";

import type {
  LoginResponse,
} from "../interfaces/LoginResponse";

import {
  login as authenticate,
  verifyMfa,
} from "../services/auth-service";

import {
  getAccessToken,
  removeAccessToken,
  saveAccessToken,
} from "../services/token-storage";

import {
  getMfaChallenge,
  removeMfaChallenge,
  saveMfaChallenge,
} from "../services/mfa-storage";

import {
  extractUserFromToken,
  isTokenExpired,
} from "../utils/jwt";

interface AuthContextData {
  user: AuthUser | null;

  isAuthenticated: boolean;

  isInitializing: boolean;

  login(
    credentials: LoginRequest
  ): Promise<LoginResponse>;

  completeMfa(
    code: string
  ): Promise<LoginResponse>;

  processAuthenticationResponse(
    response: LoginResponse
  ): void;

  logout(): void;

  hasRole(
    ...roles: UserRole[]
  ): boolean;
}

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthContext =
  createContext<AuthContextData | null>(
    null
  );

export function AuthProvider({
  children,
}: AuthProviderProps) {
  const [user, setUser] =
    useState<AuthUser | null>(null);

  const [
    isInitializing,
    setIsInitializing,
  ] = useState(true);

  useEffect(() => {
    const token =
      getAccessToken();

    if (
      !token
      || isTokenExpired(token)
    ) {
      removeAccessToken();
      setUser(null);
      setIsInitializing(false);

      return;
    }

    const authenticatedUser =
      extractUserFromToken(token);

    if (!authenticatedUser) {
      removeAccessToken();
      setUser(null);
      setIsInitializing(false);

      return;
    }

    setUser(authenticatedUser);
    setIsInitializing(false);
  }, []);

  const processAuthenticationResponse =
    useCallback(
      (
        response: LoginResponse
      ) => {
        if (
          response.mfa_required
          && response.challenge_token
        ) {
          removeAccessToken();

          saveMfaChallenge(
            response.challenge_token
          );

          setUser(null);

          return;
        }

        const accessToken =
          response.access_token;

        if (!accessToken) {
          removeAccessToken();
          removeMfaChallenge();
          setUser(null);

          throw new Error(
            "A resposta de autenticação "
              + "não contém token nem desafio MFA."
          );
        }

        const authenticatedUser =
          extractUserFromToken(
            accessToken
          );

        if (!authenticatedUser) {
          removeAccessToken();
          removeMfaChallenge();
          setUser(null);

          throw new Error(
            "Não foi possível identificar "
              + "o usuário autenticado."
          );
        }

        saveAccessToken(
          accessToken
        );

        removeMfaChallenge();

        setUser(
          authenticatedUser
        );
      },
      []
    );

  const login =
    useCallback(
      async (
        credentials: LoginRequest
      ): Promise<LoginResponse> => {
        const response =
          await authenticate(
            credentials
          );

        processAuthenticationResponse(
          response
        );

        return response;
      },
      [
        processAuthenticationResponse,
      ]
    );

  const completeMfa =
    useCallback(
      async (
        code: string
      ): Promise<LoginResponse> => {
        const challengeToken =
          getMfaChallenge();

        if (!challengeToken) {
          throw new Error(
            "O desafio MFA não foi encontrado."
          );
        }

        const response =
          await verifyMfa(
            challengeToken,
            code
          );

        processAuthenticationResponse(
          response
        );

        return response;
      },
      [
        processAuthenticationResponse,
      ]
    );

  const logout =
    useCallback(
      () => {
        removeAccessToken();
        removeMfaChallenge();
        setUser(null);
      },
      []
    );

  const hasRole =
    useCallback(
      (
        ...roles: UserRole[]
      ) => {
        if (!user) {
          return false;
        }

        return roles.includes(
          user.role
        );
      },
      [user]
    );

  const contextValue =
    useMemo<AuthContextData>(
      () => ({
        user,
        isAuthenticated:
          user !== null,
        isInitializing,
        login,
        completeMfa,
        processAuthenticationResponse,
        logout,
        hasRole,
      }),
      [
        user,
        isInitializing,
        login,
        completeMfa,
        processAuthenticationResponse,
        logout,
        hasRole,
      ]
    );

  return (
    <AuthContext.Provider
      value={contextValue}
    >
      {children}
    </AuthContext.Provider>
  );
}