import {
  act,
  renderHook,
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
  useCandidates,
} from "../../hooks/useCandidates";

import {
  createQueryWrapper,
} from "../utils/createQueryWrapper";

import type {
  CandidateData,
} from "../../interfaces/CandidateData";

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
  "useCandidates",
  () => {
    beforeEach(
      () => {
        vi.clearAllMocks();
      }
    );

    it(
      "does not request candidates automatically",
      () => {
        const {
          QueryWrapper,
        } =
          createQueryWrapper();

        const {
          result,
        } =
          renderHook(
            () =>
              useCandidates(),
            {
              wrapper:
                QueryWrapper,
            }
          );

        expect(
          mockedGet
        ).not.toHaveBeenCalled();

        expect(
          result.current.fetchStatus
        ).toBe(
          "idle"
        );

        expect(
          result.current.data
        ).toBeUndefined();
      }
    );

    it(
      "loads candidates after manual refetch",
      async () => {
        const candidates = [
          {
            id: 1,
            name:
              "Candidato Teste",
          },
        ] as unknown as CandidateData[];

        mockedGet.mockResolvedValue({
          data:
            candidates,
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
              useCandidates(),
            {
              wrapper:
                QueryWrapper,
            }
          );

        let refetchResult:
          Awaited<
            ReturnType<
              typeof result.current.refetch
            >
          >;

        await act(
          async () => {
            refetchResult =
              await result.current.refetch();
          }
        );

        expect(
          mockedGet
        ).toHaveBeenCalledWith(
          "/api/candidates"
        );

        expect(
          mockedGet
        ).toHaveBeenCalledTimes(
          1
        );

        expect(
          refetchResult!.isSuccess
        ).toBe(
          true
        );

        expect(
          refetchResult!.data
        ).toEqual(
          candidates
        );

        expect(
          refetchResult!.error
        ).toBeNull();
      }
    );

    it(
      "returns an empty candidate list",
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
              useCandidates(),
            {
              wrapper:
                QueryWrapper,
            }
          );

        let refetchResult:
          Awaited<
            ReturnType<
              typeof result.current.refetch
            >
          >;

        await act(
          async () => {
            refetchResult =
              await result.current.refetch();
          }
        );

        expect(
          refetchResult!.isSuccess
        ).toBe(
          true
        );

        expect(
          refetchResult!.data
        ).toEqual(
          []
        );
      }
    );

    it(
      "exposes errors after manual refetch",
      async () => {
        const requestError =
          new Error(
            "Falha ao obter candidatos"
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
              useCandidates(),
            {
              wrapper:
                QueryWrapper,
            }
          );

        let refetchResult:
          Awaited<
            ReturnType<
              typeof result.current.refetch
            >
          >;

        await act(
          async () => {
            refetchResult =
              await result.current.refetch();
          }
        );

        expect(
          mockedGet
        ).toHaveBeenCalledWith(
          "/api/candidates"
        );

        expect(
          refetchResult!.isError
        ).toBe(
          true
        );

        expect(
          refetchResult!.error
        ).toBe(
          requestError
        );

        expect(
          refetchResult!.data
        ).toBeUndefined();
      }
    );
  }
);