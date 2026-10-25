import {
  useCallback,
  useEffect,
  type CSSProperties,
} from "react";

import {
  createPortal,
} from "react-dom";

import type {
  JobData,
} from "../../interfaces/JobData";

interface JobDetailsModalProps {
  job: JobData;
  onClose(): void;
}

const overlayStyle: CSSProperties = {
  position: "fixed",
  inset: 0,
  zIndex: 2147483647,

  display: "flex",
  alignItems: "center",
  justifyContent: "center",

  padding: "24px",

  overflowY: "auto",

  backgroundColor: "rgba(2, 6, 23, 0.82)",
  backdropFilter: "blur(4px)",
};

const modalStyle: CSSProperties = {
  position: "relative",

  width: "min(720px, 100%)",
  maxHeight: "calc(100vh - 48px)",

  overflowY: "auto",

  border: "1px solid #334155",
  borderRadius: "20px",

  color: "#f8fafc",
  backgroundColor: "#111c31",

  boxShadow:
    "0 30px 80px rgba(0, 0, 0, 0.55)",
};

const headerStyle: CSSProperties = {
  display: "flex",
  alignItems: "flex-start",
  justifyContent: "space-between",
  gap: "24px",

  padding: "28px 32px",

  borderBottom: "1px solid #334155",
};

const closeButtonStyle: CSSProperties = {
  width: "42px",
  height: "42px",

  flex: "0 0 42px",

  border: "1px solid #475569",
  borderRadius: "50%",

  color: "#ffffff",
  backgroundColor: "#1e293b",

  cursor: "pointer",

  fontSize: "24px",
};

const bodyStyle: CSSProperties = {
  display: "grid",
  gap: "28px",

  padding: "32px",
};

const informationStyle: CSSProperties = {
  display: "grid",
  gridTemplateColumns:
    "repeat(2, minmax(0, 1fr))",

  gap: "16px",

  margin: 0,
};

const informationItemStyle: CSSProperties = {
  padding: "18px",

  border: "1px solid #334155",
  borderRadius: "14px",

  backgroundColor: "#16233a",
};

const footerStyle: CSSProperties = {
  display: "flex",
  justifyContent: "flex-end",

  padding: "20px 32px",

  borderTop: "1px solid #334155",
};

function formatCurrency(
  value: number
): string {
  return new Intl.NumberFormat(
    "pt-BR",
    {
      style: "currency",
      currency: "BRL",
    }
  ).format(value);
}

function formatDate(
  value?: string
): string {
  if (!value) {
    return "Não informada";
  }

  const datePart =
    value.includes("T")
      ? value.split("T")[0]
      : value;

  const parts =
    datePart.split("-");

  if (parts.length !== 3) {
    return value;
  }

  const [
    year,
    month,
    day,
  ] = parts;

  return `${day}/${month}/${year}`;
}

export function JobDetailsModal({
  job,
  onClose,
}: JobDetailsModalProps) {
  const handleClose =
    useCallback(
      () => {
        onClose();
      },
      [
        onClose,
      ]
    );

  useEffect(
    () => {
      const previousOverflow =
        document.body.style.overflow;

      document.body.style.overflow =
        "hidden";

      function handleEscape(
        event: KeyboardEvent
      ) {
        if (event.key === "Escape") {
          handleClose();
        }
      }

      window.addEventListener(
        "keydown",
        handleEscape
      );

      return () => {
        document.body.style.overflow =
          previousOverflow;

        document.body.classList.remove(
          "modal-open"
        );

        window.removeEventListener(
          "keydown",
          handleEscape
        );
      };
    },
    [
      handleClose,
    ]
  );

  function handleContact() {
    if (!job.contactEmail) {
      return;
    }

    window.location.assign(
      `mailto:${job.contactEmail}`
    );
  }

  return createPortal(
    <div
      style={overlayStyle}
      role="presentation"
      data-testid="job-details-overlay"
      onMouseDown={handleClose}
    >
      <section
        style={modalStyle}
        role="dialog"
        aria-modal="true"
        aria-labelledby="job-details-title"
        onMouseDown={
          (event) => {
            event.stopPropagation();
          }
        }
      >
        <header style={headerStyle}>
          <div>
            <span
              style={{
                display: "block",

                marginBottom: "8px",

                color: "#4ade80",

                fontSize: "12px",
                fontWeight: 800,
                letterSpacing: "0.08em",
                textTransform: "uppercase",
              }}
            >
              Detalhes da oportunidade
            </span>

            <h2
              id="job-details-title"
              style={{
                margin: 0,

                color: "#ffffff",

                fontSize: "28px",
              }}
            >
              {job.name}
            </h2>

            <p
              style={{
                margin: "10px 0 0",

                color: "#a5b4fc",

                fontWeight: 700,
              }}
            >
              {job.company?.name
                ?? "Empresa não informada"}
            </p>
          </div>

          <button
            type="button"
            style={closeButtonStyle}
            aria-label="Fechar detalhes da vaga"
            onClick={handleClose}
          >
            X
          </button>
        </header>

        <div style={bodyStyle}>
          <section>
            <h3
              style={{
                margin: "0 0 12px",

                color: "#ffffff",
              }}
            >
              Descrição
            </h3>

            <p
              style={{
                margin: 0,

                color: "#cbd5e1",

                lineHeight: 1.75,
                whiteSpace: "pre-wrap",
              }}
            >
              {job.description}
            </p>
          </section>

          <dl style={informationStyle}>
            <div style={informationItemStyle}>
              <dt>
                Salário
              </dt>

              <dd
                style={{
                  margin: "8px 0 0",
                  fontWeight: 700,
                }}
              >
                {formatCurrency(
                  job.salary
                )}
              </dd>
            </div>

            <div style={informationItemStyle}>
              <dt>
                Publicada em
              </dt>

              <dd
                style={{
                  margin: "8px 0 0",
                  fontWeight: 700,
                }}
              >
                {formatDate(
                  job.createdAt
                )}
              </dd>
            </div>

            <div style={informationItemStyle}>
              <dt>
                Expira em
              </dt>

              <dd
                style={{
                  margin: "8px 0 0",
                  fontWeight: 700,
                }}
              >
                {formatDate(
                  job.expirationDate
                )}
              </dd>
            </div>

            <div style={informationItemStyle}>
              <dt>
                Empresa
              </dt>

              <dd
                style={{
                  margin: "8px 0 0",
                  fontWeight: 700,
                }}
              >
                {job.company?.name
                  ?? "Não informada"}
              </dd>
            </div>
          </dl>

          {job.contactEmail && (
            <section>
              <h3
                style={{
                  margin: "0 0 12px",
                }}
              >
                Contato
              </h3>

              <button
                type="button"
                onClick={handleContact}
                style={{
                  padding: 0,

                  border: 0,

                  color: "#a5b4fc",
                  background: "transparent",

                  cursor: "pointer",

                  font: "inherit",
                  fontWeight: 700,
                }}
              >
                {job.contactEmail}
              </button>
            </section>
          )}
        </div>

        <footer style={footerStyle}>
          <button
            type="button"
            onClick={handleClose}
            style={{
              minWidth: "110px",

              padding: "12px 20px",

              border: "1px solid #475569",
              borderRadius: "10px",

              color: "#ffffff",
              backgroundColor: "#1e293b",

              cursor: "pointer",

              fontWeight: 700,
            }}
          >
            Fechar
          </button>
        </footer>
      </section>
    </div>,
    document.body
  );
}