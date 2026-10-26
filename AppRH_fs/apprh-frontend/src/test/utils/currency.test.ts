import {
  describe,
  expect,
  it,
} from "vitest";

import {
  normalizeBrazilianCurrency,
} from "../../utils/currency";

describe(
  "normalizeBrazilianCurrency",
  () => {
    it(
      "normalizes Brazilian decimal currency",
      () => {
        expect(
          normalizeBrazilianCurrency(
            "9.500,50"
          )
        ).toBe(
          "9500.50"
        );
      }
    );

    it(
      "removes the currency prefix",
      () => {
        expect(
          normalizeBrazilianCurrency(
            "R$9.500,50"
          )
        ).toBe(
          "9500.50"
        );
      }
    );

    it(
      "removes spaces",
      () => {
        expect(
          normalizeBrazilianCurrency(
            "R$ 9.500,50"
          )
        ).toBe(
          "9500.50"
        );
      }
    );

    it(
      "normalizes multiple thousand separators",
      () => {
        expect(
          normalizeBrazilianCurrency(
            "1.234.567,89"
          )
        ).toBe(
          "1234567.89"
        );
      }
    );

    it(
      "treats a point followed by three digits as thousand separator",
      () => {
        expect(
          normalizeBrazilianCurrency(
            "100.800"
          )
        ).toBe(
          "100800"
        );
      }
    );

    it(
      "preserves decimal point notation",
      () => {
        expect(
          normalizeBrazilianCurrency(
            "5500.50"
          )
        ).toBe(
          "5500.50"
        );
      }
    );

    it(
      "preserves integer notation",
      () => {
        expect(
          normalizeBrazilianCurrency(
            "8500"
          )
        ).toBe(
          "8500"
        );
      }
    );

    it(
      "returns an empty value for blank input",
      () => {
        expect(
          normalizeBrazilianCurrency(
            "   "
          )
        ).toBe(
          ""
        );
      }
    );
  }
);