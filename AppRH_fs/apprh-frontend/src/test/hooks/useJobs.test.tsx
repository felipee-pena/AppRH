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
  useJobs,
} from "../../hooks/useJobs";

import {
  createQueryWrapper,
} from "../utils/createQueryWrapper";

import type {
  JobData,
} from "../../interfaces/JobData";

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

const jobs: JobData[] = [
  {
    code: 1,
    name: "Analista Java",
    description: "Desenvolvimento backend",
    expirationDate: "2026-12-20",
    createdAt: "2026-10-25T01:00:00-03:00",
    salary: 9000,
    contactEmail: "java@example.com",
  },
  {
    code: 2,
    name: "Analista ERP",
    description: "Sistemas empresariais",
    expirationDate: "2026-12-21",
    createdAt: "2026-10-25T01:00:00-03:00",
    salary: 8500,
    contactEmail: "erp@example.com",
  },
];

describe(
  "useJobs",
  () => {
    beforeEach(
      () => {
        vi.clearAllMocks();
      }
    );

    it(
      "loads all available jobs",
      async () => {
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
              useJobs(),
            {
              wrapper: QueryWrapper,
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
          "/api/jobs"
        );

        expect(
          mockedGet
        ).toHaveBeenCalledTimes(
          1
        );

        expect(
          result.current.data
        ).toEqual(
          jobs
        );
      }
    );

    it(
      "returns an empty list from an empty API response",
      async () => {
        mockedGet.mockResolvedValue({
          data: [],
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
              useJobs(),
            {
              wrapper: QueryWrapper,
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
          result.current.data
        ).toEqual(
          []
        );
      }
    );

    it(
      "exposes request errors",
      async () => {
        const requestError =
          new Error(
            "Não foi possível carregar as vagas"
          );

        mockedGet.mockRejectedValue(
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
              useJobs(),
            {
              wrapper: QueryWrapper,
            }
          );

        await waitFor(
          () => {
            expect(
              result.current.isError
            ).toBe(
              true
            );
          },
          {
            timeout: 8000,
          }
        );

        expect(
          result.current.error
        ).toBe(
          requestError
        );

        expect(
          mockedGet
        ).toHaveBeenCalledWith(
          "/api/jobs"
        );
      }
    );
  }
);