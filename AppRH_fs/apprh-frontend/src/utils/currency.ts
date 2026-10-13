export function normalizeBrazilianCurrency(
  value: string
): string {
  const sanitizedValue = value
    .trim()
    .replace(/\s/g, "")
    .replace(/^R\$/, "");

  if (!sanitizedValue) {
    return "";
  }

  /*
   * Formato brasileiro:
   * 100.800,50 -> 100800.50
   */
  if (sanitizedValue.includes(",")) {
    return sanitizedValue
      .replace(/\./g, "")
      .replace(",", ".");
  }

  /*
   * Somente ponto com três casas:
   * 100.800 -> 100800
   *
   * Nesse caso, consideramos o ponto como
   * separador de milhar.
   */
  if (/^\d{1,3}(\.\d{3})+$/.test(sanitizedValue)) {
    return sanitizedValue.replace(/\./g, "");
  }

  /*
   * Formato decimal:
   * 5500.50 -> 5500.50
   */
  return sanitizedValue;
}