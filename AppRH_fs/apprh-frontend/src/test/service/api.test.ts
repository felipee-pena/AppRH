import type {
  AxiosRequestConfig,
  AxiosResponse,
} from "axios";

import {
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from "vitest";

const axiosMockState =
  vi.hoisted(
    () => ({
      requestFulfilled:
        undefined as
          | ((
              config:
                AxiosRequestConfig
            ) =>
              AxiosRequestConfig)
          | undefined,

      responseFulfilled:
        undefined as
          | ((
              response:
                AxiosResponse
            ) =>
              AxiosResponse)
          | undefined,

      responseRejected:
        undefined as
          | ((
              error:
                unknown
            ) =>
              Promise<never>)
          | undefined,

      create:
        vi.fn(),

      getAccessToken:
        vi.fn(),

      removeAccessToken:
        vi.fn(),
    })
  );

vi.mock(
  "axios",
  () => {
    const apiInstance = {
      interceptors: {
        request: {
          use:
            vi.fn(
              (
                fulfilled:
                  (
                    config:
                      AxiosRequestConfig
                  ) =>
                    AxiosRequestConfig
              ) => {
                axiosMockState.requestFulfilled =
                  fulfilled;

                return 1;
              }
            ),
        },

        response: {
          use:
            vi.fn(
              (
                fulfilled:
                  (
                    response:
                      AxiosResponse
                  ) =>
                    AxiosResponse,

                rejected:
                  (
                    error:
                      unknown
                  ) =>
                    Promise<never>
              ) => {
                axiosMockState.responseFulfilled =
                  fulfilled;

                axiosMockState.responseRejected =
                  rejected;

                return 2;
              }
            ),
        },
      },
    };

    axiosMockState.create.mockReturnValue(
      apiInstance
    );

    return {
      default: {
        create:
          axiosMockState.create,
      },
    };
  }
);

vi.mock(
  "../../services/token-storage",
  () => ({
    getAccessToken:
      axiosMockState.getAccessToken,

    removeAccessToken:
      axiosMockState.removeAccessToken,
  })
);

describe(
  "api",
  () => {
    beforeEach(
      async () => {
        vi.clearAllMocks();

        axiosMockState.requestFulfilled =
          undefined;

        axiosMockState.responseFulfilled =
          undefined;

        axiosMockState.responseRejected =
          undefined;

        vi.resetModules();

        await import(
          "../../services/api"
        );
      }
    );

    it(
      "creates an Axios instance with JSON headers",
      () => {
        expect(
          axiosMockState.create
        ).toHaveBeenCalledWith({
          baseURL:
            "http://localhost:8081",

          headers: {
            Accept:
              "application/json",

            "Content-Type":
              "application/json",
          },
        });
      }
    );

    it(
      "adds the Bearer token to authenticated requests",
      () => {
        axiosMockState.getAccessToken.mockReturnValue(
          "access-token"
        );

        const config: AxiosRequestConfig = {
          headers: {},
        };

        const result =
          axiosMockState.requestFulfilled?.(
            config
          );

        expect(
          axiosMockState.getAccessToken
        ).toHaveBeenCalledTimes(
          1
        );

        expect(
          result?.headers
        ).toEqual(
          expect.objectContaining({
            Authorization:
              "Bearer access-token",
          })
        );
      }
    );

    it(
      "does not add Authorization when token is absent",
      () => {
        axiosMockState.getAccessToken.mockReturnValue(
          null
        );

        const config: AxiosRequestConfig = {
          headers: {},
        };

        const result =
          axiosMockState.requestFulfilled?.(
            config
          );

        expect(
          result?.headers
        ).not.toEqual(
          expect.objectContaining({
            Authorization:
              expect.any(
                String
              ),
          })
        );
      }
    );

    it(
      "returns successful responses unchanged",
      () => {
        const response = {
          data: {
            message:
              "ok",
          },
          status: 200,
          statusText: "OK",
          headers: {},
          config: {
            headers: {},
          },
        } as AxiosResponse;

        expect(
          axiosMockState.responseFulfilled?.(
            response
          )
        ).toBe(
          response
        );
      }
    );

    it(
      "removes token after private 401 response",
      async () => {
        window.history.replaceState(
          {},
          "",
          "/login"
        );

        const error = {
          config: {
            url:
              "/api/management/jobs",
          },

          response: {
            status: 401,
          },
        };

        await expect(
          axiosMockState.responseRejected?.(
            error
          )
        ).rejects.toBe(
          error
        );

        expect(
          axiosMockState.removeAccessToken
        ).toHaveBeenCalledTimes(
          1
        );
      }
    );

    it.each([
      "/auth/login",
      "/auth/register",
      "/auth/email/confirm",
      "/auth/email/resend",
    ])(
      "does not remove token for public endpoint %s",
      async (
        endpoint
      ) => {
        const error = {
          config: {
            url:
              endpoint,
          },

          response: {
            status: 401,
          },
        };

        await expect(
          axiosMockState.responseRejected?.(
            error
          )
        ).rejects.toBe(
          error
        );

        expect(
          axiosMockState.removeAccessToken
        ).not.toHaveBeenCalled();
      }
    );

    it(
      "does not remove token for non-401 responses",
      async () => {
        const error = {
          config: {
            url:
              "/api/jobs",
          },

          response: {
            status: 403,
          },
        };

        await expect(
          axiosMockState.responseRejected?.(
            error
          )
        ).rejects.toBe(
          error
        );

        expect(
          axiosMockState.removeAccessToken
        ).not.toHaveBeenCalled();
      }
    );

    it(
      "handles errors without request URL",
      async () => {
        const error = {
          response: {
            status: 500,
          },
        };

        await expect(
          axiosMockState.responseRejected?.(
            error
          )
        ).rejects.toBe(
          error
        );

        expect(
          axiosMockState.removeAccessToken
        ).not.toHaveBeenCalled();
      }
    );
  }
);