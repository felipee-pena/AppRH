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
  useRhCompany,
} from "../../hooks/useRhCompany";

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
  "useRhCompany",
  () => {
    beforeEach(
      () => {
        vi.clearAllMocks();
      }
    );

    it(
      "does not request the company when disabled",
      () => {
        const {
          QueryWrapper,
        } =
          createQueryWrapper();

        renderHook(
          () =>
            useRhCompany(
              false
            ),
          {
            wrapper:
              QueryWrapper,
          }
        );

        expect(
          mockedGet
        ).not.toHaveBeenCalled();
      }
    );

    it(
      "loads the RH company when enabled",
      async () => {
        const company = {
          id: 1,
          name: "Empresa RH",
          created: false,
        };

        mockedGet.mockResolvedValue({
          data: company,
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
              useRhCompany(
                true
              ),
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
          "/api/rh/company"
        );

        expect(
          result.current.data
        ).toEqual(
          company
        );
      }
    );

    it(
      "exposes request errors",
      async () => {
        mockedGet.mockRejectedValue(
          new Error(
            "Empresa não encontrada"
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
              useRhCompany(
                true
              ),
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