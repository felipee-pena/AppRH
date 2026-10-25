import {
  beforeEach,
  describe,
  expect,
  it,
} from "vitest";

import {
  getAccessToken,
  removeAccessToken,
  saveAccessToken,
} from "../../services/token-storage";

describe(
  "token-storage",
  () => {
    beforeEach(
      () => {
        localStorage.clear();
        sessionStorage.clear();
      }
    );

    it(
      "saves and returns the access token",
      () => {
        saveAccessToken(
          "jwt-token"
        );

        expect(
          getAccessToken()
        ).toBe(
          "jwt-token"
        );
      }
    );

    it(
      "removes the access token",
      () => {
        saveAccessToken(
          "jwt-token"
        );

        removeAccessToken();

        expect(
          getAccessToken()
        ).toBeNull();
      }
    );

    it(
      "returns null when the token does not exist",
      () => {
        expect(
          getAccessToken()
        ).toBeNull();
      }
    );
  }
);