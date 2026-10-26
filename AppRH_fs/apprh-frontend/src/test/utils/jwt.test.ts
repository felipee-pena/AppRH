import {
  afterEach,
  describe,
  expect,
  it,
  vi,
} from "vitest";

import type {
  UserRole,
} from "../../interfaces/AuthUser";

import {
  decodeJwt,
  extractUserFromToken,
  isTokenExpired,
} from "../../utils/jwt";

interface TestJwtPayload {
  sub?: string;
  role?: UserRole;
  exp?: number;
  description?: string;
}

function encodeBase64Url(
  value: string
): string {
  const binaryValue =
    encodeURIComponent(
      value
    ).replace(
      /%([0-9A-F]{2})/g,
      (
        _match,
        hexadecimalValue: string
      ) => {
        return String.fromCharCode(
          Number.parseInt(
            hexadecimalValue,
            16
          )
        );
      }
    );

  return window
    .btoa(
      binaryValue
    )
    .replace(
      /=+$/g,
      ""
    )
    .replace(
      /\+/g,
      "-"
    )
    .replace(
      /\//g,
      "_"
    );
}

function createToken(
  payload: TestJwtPayload
): string {
  const header =
    encodeBase64Url(
      JSON.stringify({
        alg: "HS256",
        typ: "JWT",
      })
    );

  const encodedPayload =
    encodeBase64Url(
      JSON.stringify(
        payload
      )
    );

  const signature =
    encodeBase64Url(
      "test-signature"
    );

  return [
    header,
    encodedPayload,
    signature,
  ].join(
    "."
  );
}

describe(
  "jwt utilities",
  () => {
    afterEach(
      () => {
        vi.useRealTimers();
      }
    );

    describe(
      "decodeJwt",
      () => {
        it(
          "decodes a valid JWT payload",
          () => {
            const token =
              createToken({
                sub:
                  "usuario@example.com",

                role:
                  "ADMIN",

                exp:
                  2000000000,
              });

            expect(
              decodeJwt(
                token
              )
            ).toEqual({
              sub:
                "usuario@example.com",

              role:
                "ADMIN",

              exp:
                2000000000,
            });
          }
        );

        it(
          "decodes a payload containing accented characters",
          () => {
            const token =
              createToken({
                sub:
                  "usuário@example.com",

                role:
                  "USER",

                description:
                  "Usuário autenticado",
              });

            expect(
              decodeJwt(
                token
              )
            ).toEqual({
              sub:
                "usuário@example.com",

              role:
                "USER",

              description:
                "Usuário autenticado",
            });
          }
        );

        it(
          "returns null when token does not contain three parts",
          () => {
            expect(
              decodeJwt(
                "invalid-token"
              )
            ).toBeNull();

            expect(
              decodeJwt(
                "header.payload"
              )
            ).toBeNull();

            expect(
              decodeJwt(
                "header.payload.signature.extra"
              )
            ).toBeNull();
          }
        );

        it(
          "returns null when payload is not valid Base64",
          () => {
            expect(
              decodeJwt(
                "header.%%%invalid%%%.signature"
              )
            ).toBeNull();
          }
        );

        it(
          "returns null when payload is not valid JSON",
          () => {
            const invalidPayload =
              encodeBase64Url(
                "not-json"
              );

            const token = [
              "header",
              invalidPayload,
              "signature",
            ].join(
              "."
            );

            expect(
              decodeJwt(
                token
              )
            ).toBeNull();
          }
        );

        it(
          "returns null for an empty token",
          () => {
            expect(
              decodeJwt(
                ""
              )
            ).toBeNull();
          }
        );
      }
    );

    describe(
      "extractUserFromToken",
      () => {
        it(
          "extracts authenticated user information",
          () => {
            const token =
              createToken({
                sub:
                  "admin@example.com",

                role:
                  "ADMIN",

                exp:
                  2000000000,
              });

            expect(
              extractUserFromToken(
                token
              )
            ).toEqual({
              login:
                "admin@example.com",

              role:
                "ADMIN",
            });
          }
        );

        it.each([
          "ADMIN",
          "USERRH",
          "USER",
        ] as UserRole[])(
          "extracts user with role %s",
          (
            role
          ) => {
            const token =
              createToken({
                sub:
                  "usuario@example.com",

                role,
              });

            expect(
              extractUserFromToken(
                token
              )
            ).toEqual({
              login:
                "usuario@example.com",

              role,
            });
          }
        );

        it(
          "returns null when subject is missing",
          () => {
            const token =
              createToken({
                role:
                  "USER",

                exp:
                  2000000000,
              });

            expect(
              extractUserFromToken(
                token
              )
            ).toBeNull();
          }
        );

        it(
          "returns null when role is missing",
          () => {
            const token =
              createToken({
                sub:
                  "usuario@example.com",

                exp:
                  2000000000,
              });

            expect(
              extractUserFromToken(
                token
              )
            ).toBeNull();
          }
        );

        it(
          "returns null when token is invalid",
          () => {
            expect(
              extractUserFromToken(
                "invalid-token"
              )
            ).toBeNull();
          }
        );
      }
    );

    describe(
      "isTokenExpired",
      () => {
        it(
          "returns false when expiration is in the future",
          () => {
            vi.useFakeTimers();

            vi.setSystemTime(
              new Date(
                "2026-10-26T12:00:00Z"
              )
            );

            const currentTime =
              Math.floor(
                Date.now()
                  / 1000
              );

            const token =
              createToken({
                sub:
                  "usuario@example.com",

                role:
                  "USER",

                exp:
                  currentTime
                  + 3600,
              });

            expect(
              isTokenExpired(
                token
              )
            ).toBe(
              false
            );
          }
        );

        it(
          "returns true when expiration is in the past",
          () => {
            vi.useFakeTimers();

            vi.setSystemTime(
              new Date(
                "2026-10-26T12:00:00Z"
              )
            );

            const currentTime =
              Math.floor(
                Date.now()
                  / 1000
              );

            const token =
              createToken({
                sub:
                  "usuario@example.com",

                role:
                  "USER",

                exp:
                  currentTime
                  - 1,
              });

            expect(
              isTokenExpired(
                token
              )
            ).toBe(
              true
            );
          }
        );

        it(
          "returns true when expiration equals current time",
          () => {
            vi.useFakeTimers();

            vi.setSystemTime(
              new Date(
                "2026-10-26T12:00:00Z"
              )
            );

            const currentTime =
              Math.floor(
                Date.now()
                  / 1000
              );

            const token =
              createToken({
                sub:
                  "usuario@example.com",

                role:
                  "USER",

                exp:
                  currentTime,
              });

            expect(
              isTokenExpired(
                token
              )
            ).toBe(
              true
            );
          }
        );

        it(
          "returns true when expiration is missing",
          () => {
            const token =
              createToken({
                sub:
                  "usuario@example.com",

                role:
                  "USER",
              });

            expect(
              isTokenExpired(
                token
              )
            ).toBe(
              true
            );
          }
        );

        it(
          "returns true when token is invalid",
          () => {
            expect(
              isTokenExpired(
                "invalid-token"
              )
            ).toBe(
              true
            );
          }
        );

        it(
          "returns true when expiration is zero",
          () => {
            const token =
              createToken({
                sub:
                  "usuario@example.com",

                role:
                  "USER",

                exp:
                  0,
              });

            expect(
              isTokenExpired(
                token
              )
            ).toBe(
              true
            );
          }
        );
      }
    );
  }
);