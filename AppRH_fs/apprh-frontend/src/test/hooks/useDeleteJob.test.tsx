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
  useDeleteJob,
} from "../../hooks/useDeleteJob";

import {
  createQueryWrapper,
} from "../utils/createQueryWrapper";

vi.mock(
  "../../services/api",
  () => ({
    api: {
      delete: vi.fn(),
    },
  })
);

const mockedDelete =
  vi.mocked(
    api.delete
  );

describe(
  "useDeleteJob",
  () => {
    beforeEach(
      () => {
        vi.clearAllMocks();
      }
    );

    it(
      "deletes a job by code",
      async () => {
        mockedDelete.mockResolvedValue({
          data: undefined,
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
              useDeleteJob(),
            {
              wrapper:
                QueryWrapper,
            }
          );

        await act(
          async () => {
            await result.current.mutateAsync(
              10
            );
          }
        );

        expect(
          mockedDelete
        ).toHaveBeenCalledWith(
          "/api/management/jobs/10"
        );
      }
    );

    it(
      "invalidates job queries after deletion",
      async () => {
        mockedDelete.mockResolvedValue({
          data: undefined,
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
              useDeleteJob(),
            {
              wrapper:
                QueryWrapper,
            }
          );

        await act(
          async () => {
            await result.current.mutateAsync(
              10
            );
          }
        );

        await waitFor(
          () => {
            expect(
              invalidateQueries
            ).toHaveBeenCalledWith({
              queryKey: [
                "managed-jobs",
              ],
            });
          }
        );

        expect(
          invalidateQueries
        ).toHaveBeenCalledWith({
          queryKey: [
            "jobs",
          ],
        });
      }
    );

    it(
      "exposes deletion errors",
      async () => {
        mockedDelete.mockRejectedValue(
          new Error(
            "Falha ao excluir vaga"
          )
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
              useDeleteJob(),
            {
              wrapper:
                QueryWrapper,
            }
          );

        await act(
          async () => {
            await expect(
              result.current.mutateAsync(
                10
              )
            ).rejects.toThrow(
              "Falha ao excluir vaga"
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
      }
    );
  }
);