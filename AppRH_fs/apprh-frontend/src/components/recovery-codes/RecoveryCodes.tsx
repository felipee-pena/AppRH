import "./recovery-codes.css";

interface RecoveryCodesProps {
  codes: string[];
}

export function RecoveryCodes({
  codes,
}: RecoveryCodesProps) {
  if (codes.length === 0) {
    return null;
  }

  async function copyCodes() {
    await navigator.clipboard.writeText(
      codes.join("\n")
    );
  }

  function downloadCodes() {
    const content = [
      "AppRH - Códigos de recuperação",
      "",
      ...codes,
      "",
      "Cada código pode ser utilizado uma única vez.",
    ].join("\n");

    const blob =
      new Blob(
        [content],
        {
          type: "text/plain;charset=utf-8",
        }
      );

    const url =
      URL.createObjectURL(
        blob
      );

    const link =
      document.createElement(
        "a"
      );

    link.href = url;
    link.download =
      "apprh-recovery-codes.txt";

    link.click();

    URL.revokeObjectURL(
      url
    );
  }

  return (
    <section className="recovery-codes">
      <h2>
        Códigos de recuperação
      </h2>

      <p>
        Guarde os códigos em local seguro.
        Cada código poderá ser utilizado
        somente uma vez.
      </p>

      <ul>
        {codes.map(code => (
          <li key={code}>
            <code>{code}</code>
          </li>
        ))}
      </ul>

      <div className="recovery-codes__actions">
        <button
          type="button"
          onClick={
            () => void copyCodes()
          }
        >
          Copiar códigos
        </button>

        <button
          type="button"
          onClick={downloadCodes}
        >
          Baixar arquivo
        </button>
      </div>
    </section>
  );
}