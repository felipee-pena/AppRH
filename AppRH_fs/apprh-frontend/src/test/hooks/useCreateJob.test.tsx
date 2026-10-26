import {
  act,
  renderHook,
  waitFor,
} from "@testing-library/react";

import {
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from "vitest";

import {
  api,
} from "../../services/api";

import {
  useCreateJob,
} from "../../hooks/useCreateJob";

import {
  createQueryWrapper,
} from "../utils/createQueryWrapper";

import type {
  JobData,
  JobRequestData,
} from "../../interfaces/JobData";

vi.mock(
  "../../services/api",
  () => ({
    api: {
      post: vi.fn(),
    },
  })
);

const mockedPost =
  vi.mocked(
    api.post
  );

const request: JobRequestData = {
  name: "Analista Java",
  description: "Desenvolvimento backend",
  expirationDate: "2026-12-20",
  salary: 9500,
  contactEmail: "vagas@example.com",
  companyId: 1,
};

const createdJob: JobData = {
  code: 10,
  name: "Analista Java",
  description: "Desenvolvimento backend",
  expirationDate: "2026-12-20",
  salary: 9500,
  contactEmail: "vagas@example.com",
  company: {
    id: 1,
    name: "Empresa Teste",
  },
};

describe(
  "useCreateJob",
  () => {
    beforeEach(
      () => {
        vi.clearAllMocks();
      }
    );

    it(
      "creates a new job",
      async () => {
        mockedPost.mockResolvedValue({
          data: createdJob,
        });

        const {
          QueryWrapper,
        } =
          createQueryWrapper();

        const {
          result,
        } =
          renderHook(
            () =>
              useCreateJob(),
            {
              wrapper: QueryWrapper,
            }
          );

        let response:
          JobData | undefined;

        await act(
          async () => {
            response =
              await result.current.mutateAsync(
                request
              );
          }
        );

        expect(
          mockedPost
        ).toHaveBeenCalledWith(
          "/api/management/jobs",
          request
        );

        expect(
          response
        ).toEqual(
          createdJob
        );
      }
    );

    it(
      "invalidates public and managed job queries",
      async () => {
        mockedPost.mockResolvedValue({
          data: createdJob,
        });

        const {
          queryClient,
          QueryWrapper,
        } =
          createQueryWrapper();

        const invalidateQueries =
          vi.spyOn(
            queryClient,
            "invalidateQueries"
          );

        const {
          result,
        } =
          renderHook(
            () =>
              useCreateJob(),
            {
              wrapper: QueryWrapper,
            }
          );

        await act(
          async () => {
            await result.current.mutateAsync(
              request
            );
          }
        );

        await waitFor(
          () => {
            expect(
              invalidateQueries
            ).toHaveBeenCalledWith({
              queryKey: [
                "jobs",
              ],
            });
          }
        );

        expect(
          invalidateQueries
        ).toHaveBeenCalledWith({
          queryKey: [
            "managed-jobs",
          ],
        });

        expect(
          invalidateQueries
        ).toHaveBeenCalledTimes(
          2
        );
      }
    );

    it(
      "exposes API errors",
      async () => {
        const requestError =
          new Error(
            "Falha ao criar vaga"
          );

        mockedPost.mockRejectedValue(
          requestError
        );

        const {
          QueryWrapper,
        } =
          createQueryWrapper();

        const {
          result,
        } =
          renderHook(
            () =>
              useCreateJob(),
            {
              wrapper: QueryWrapper,
            }
          );

        await act(
          async () => {
            await expect(
              result.current.mutateAsync(
                request
              )
            ).rejects.toThrow(
              "Falha ao criar vaga"
            );
          }
        );

        await waitFor(
          () => {
            expect(
              result.current.isError
            ).toBe(
              true
            );
          }
        );

        expect(
          result.current.error
        ).toBe(
          requestError
        );
      }
    );
  }
);