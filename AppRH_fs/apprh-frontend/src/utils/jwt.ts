import type {
  AuthUser,
  UserRole,
} from "../interfaces/AuthUser";

interface JwtPayload {
  sub?: string;
  role?: UserRole;
  exp?: number;
}

export function decodeJwt(
  token: string
): JwtPayload | null {
  try {
    const parts = token.split(".");

    if (parts.length !== 3) {
      return null;
    }

    const payload = parts[1]
      .replace(/-/g, "+")
      .replace(/_/g, "/");

    const decodedPayload =
      decodeURIComponent(
        window
          .atob(payload)
          .split("")
          .map((character) => {
            const code = character
              .charCodeAt(0)
              .toString(16)
              .padStart(2, "0");

            return `%${code}`;
          })
          .join("")
      );

    return JSON.parse(
      decodedPayload
    ) as JwtPayload;
  } catch {
    return null;
  }
}

export function extractUserFromToken(
  token: string
): AuthUser | null {
  const payload = decodeJwt(token);

  if (!payload?.sub || !payload.role) {
    return null;
  }

  return {
    login: payload.sub,
    role: payload.role,
  };
}

export function isTokenExpired(
  token: string
): boolean {
  const payload = decodeJwt(token);

  if (!payload?.exp) {
    return true;
  }

  const currentTime =
    Math.floor(Date.now() / 1000);

  return payload.exp <= currentTime;
}