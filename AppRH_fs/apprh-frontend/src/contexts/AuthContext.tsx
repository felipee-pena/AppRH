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

import {
  login as authenticate,
} from "../services/auth-service";

import {
  getAccessToken,
  removeAccessToken,
  saveAccessToken,
} from "../services/token-storage";

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
  ): Promise<void>;

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
    const token = getAccessToken();

    if (
      !token ||
      isTokenExpired(token)
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

  const login = useCallback(
    async (
      credentials: LoginRequest
    ) => {
      const response =
        await authenticate(credentials);

      const accessToken =
        response.access_token;

      const authenticatedUser =
        extractUserFromToken(
          accessToken
        );

      if (!authenticatedUser) {
        removeAccessToken();

        throw new Error(
          "Não foi possível identificar "
            + "o usuário autenticado."
        );
      }

      saveAccessToken(accessToken);
      setUser(authenticatedUser);
    },
    []
  );

  const logout = useCallback(() => {
    removeAccessToken();
    setUser(null);
  }, []);

  const hasRole = useCallback(
    (...roles: UserRole[]) => {
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
        logout,
        hasRole,
      }),
      [
        user,
        isInitializing,
        login,
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