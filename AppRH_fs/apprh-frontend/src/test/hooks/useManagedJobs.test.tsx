import {
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
  useManagedJobs,
} from "../../hooks/useManagedJobs";

import {
  createQueryWrapper,
} from "../utils/createQueryWrapper";

vi.mock(
  "../../services/api",
  () => ({
    api: {
      get: vi.fn(),
    },
  })
);

const mockedGet =
  vi.mocked(
    api.get
  );

describe(
  "useManagedJobs",
  () => {
    beforeEach(
      () => {
        vi.clearAllMocks();
      }
    );

    it(
      "loads managed jobs",
      async () => {
        const jobs = [
          {
            code: 10,
            name: "Analista ERP",
            description: "Suporte ao ERP",
            expirationDate: "2026-12-20",
            salary: 8500,
          },
        ];

        mockedGet.mockResolvedValue({
          data: jobs,
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
              useManagedJobs(),
            {
              wrapper:
                QueryWrapper,
            }
          );

        await waitFor(
          () => {
            expect(
              result.current.isSuccess
            ).toBe(
              true
            );
          }
        );

        expect(
          mockedGet
        ).toHaveBeenCalledWith(
          "/api/management/jobs"
        );

        expect(
          result.current.data
        ).toEqual(
          jobs
        );
      }
    );

    it(
      "exposes request errors",
      async () => {
        mockedGet.mockRejectedValue(
          new Error(
            "Falha ao carregar vagas"
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
              useManagedJobs(),
            {
              wrapper:
                QueryWrapper,
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