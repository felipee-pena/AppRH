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
  useCompanies,
} from "../../hooks/useCompanies";

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
  "useCompanies",
  () => {
    beforeEach(
      () => {
        vi.clearAllMocks();
      }
    );

    it(
      "loads companies",
      async () => {
        const companies = [
          {
            id: 1,
            name: "Empresa A",
          },
          {
            id: 2,
            name: "Empresa B",
          },
        ];

        mockedGet.mockResolvedValue({
          data: companies,
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
              useCompanies(),
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
          "/api/companies"
        );

        expect(
          result.current.data
        ).toEqual(
          companies
        );
      }
    );

    it(
      "returns an empty array for invalid response data",
      async () => {
        mockedGet.mockResolvedValue({
          data: {
            content: [],
          },
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
              useCompanies(),
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
          result.current.data
        ).toEqual(
          []
        );
      }
    );

    it(
      "exposes request errors",
      async () => {
        mockedGet.mockRejectedValue(
          new Error(
            "Falha ao carregar empresas"
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
              useCompanies(),
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
          },
          {
            timeout: 5000,
          }
        );

        expect(
          result.current.error
        ).toBeInstanceOf(
          Error
        );
      }
    );
  }
);