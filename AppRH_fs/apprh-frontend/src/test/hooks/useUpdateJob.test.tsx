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
  useUpdateJob,
} from "../../hooks/useUpdateJob";

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
      put: vi.fn(),
    },
  })
);

const mockedPut =
  vi.mocked(
    api.put
  );

const request: JobRequestData = {
  name: "Analista Java Pleno",
  description: "Descrição atualizada",
  expirationDate: "2027-01-20",
  salary: 10500,
  contactEmail: "vagas@example.com",
  companyId: 1,
};

const updatedJob: JobData = {
  code: 10,
  name: "Analista Java Pleno",
  description: "Descrição atualizada",
  expirationDate: "2027-01-20",
  salary: 10500,
  contactEmail: "vagas@example.com",
  company: {
    id: 1,
    name: "Empresa Teste",
  },
};

describe(
  "useUpdateJob",
  () => {
    beforeEach(
      () => {
        vi.clearAllMocks();
      }
    );

    it(
      "updates an existing job",
      async () => {
        mockedPut.mockResolvedValue({
          data: updatedJob,
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
              useUpdateJob(),
            {
              wrapper: QueryWrapper,
            }
          );

        let response:
          JobData | undefined;

        await act(
          async () => {
            response =
              await result.current.mutateAsync({
                code: 10,
                job: request,
              });
          }
        );

        expect(
          mockedPut
        ).toHaveBeenCalledWith(
          "/api/management/jobs/10",
          request
        );

        expect(
          response
        ).toEqual(
          updatedJob
        );
      }
    );

    it(
      "invalidates public and managed job queries",
      async () => {
        mockedPut.mockResolvedValue({
          data: updatedJob,
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
              useUpdateJob(),
            {
              wrapper: QueryWrapper,
            }
          );

        await act(
          async () => {
            await result.current.mutateAsync({
              code: 10,
              job: request,
            });
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
      "exposes update errors",
      async () => {
        const requestError =
          new Error(
            "Falha ao atualizar vaga"
          );

        mockedPut.mockRejectedValue(
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
              useUpdateJob(),
            {
              wrapper: QueryWrapper,
            }
          );

        await act(
          async () => {
            await expect(
              result.current.mutateAsync({
                code: 10,
                job: request,
              })
            ).rejects.toThrow(
              "Falha ao atualizar vaga"
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