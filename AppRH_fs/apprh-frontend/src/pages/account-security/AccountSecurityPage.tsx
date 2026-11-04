import {
  useEffect,
  useState,
  type FormEvent,
} from "react";

import {
  QRCodeSVG,
} from "qrcode.react";

import axios from "axios";

import {
  RecoveryCodes,
} from "../../components/recovery-codes/RecoveryCodes";

import {
  useMfaManagement,
} from "../../hooks/useMfaManagement";

import type {
  ApiErrorResponse,
} from "../../interfaces/ApiErrorResponse";

import type {
  MfaSetupResponse,
} from "../../interfaces/MfaSetupResponse";

import "./account-security-page.css";

function extractErrorMessage(
  error: unknown
): string {
  if (
    axios.isAxiosError<ApiErrorResponse>(
      error
    )
  ) {
    return (
      error.response?.data?.message
      ?? "Não foi possível concluir a operação."
    );
  }

  if (error instanceof Error) {
    return error.message;
  }

  return (
    "Não foi possível concluir "
    + "a operação."
  );
}

export function AccountSecurityPage() {
  const {
    statusQuery,
    setupMutation,
    confirmMutation,
    regenerateMutation,
    disableMutation,
  } =
    useMfaManagement();

  const [
    setupConfiguration,
    setSetupConfiguration,
  ] =
    useState<
      MfaSetupResponse | null
    >(null);

  const [
    confirmationCode,
    setConfirmationCode,
  ] = useState("");

  const [
    password,
    setPassword,
  ] = useState("");

  const [
    disableCode,
    setDisableCode,
  ] = useState("");

  const [
    recoveryCodes,
    setRecoveryCodes,
  ] = useState<string[]>([]);

  const [
    successMessage,
    setSuccessMessage,
  ] = useState("");

  const [
    localError,
    setLocalError,
  ] = useState("");

  const mfaEnabled =
    statusQuery.data
      ?.mfa_enabled
    ?? false;

  useEffect(
    () => {
      if (
        setupMutation.isSuccess
        && setupMutation.data
      ) {
        setSetupConfiguration(
          setupMutation.data
        );

        setRecoveryCodes(
          setupMutation.data
            .recovery_codes
        );
      }
    },
    [
      setupMutation.isSuccess,
      setupMutation.data,
    ]
  );

  useEffect(
    () => {
      if (
        regenerateMutation.isSuccess
        && regenerateMutation.data
      ) {
        setRecoveryCodes(
          regenerateMutation.data
            .recovery_codes
        );

        setSuccessMessage(
          "Novos códigos de recuperação "
          + "foram gerados."
        );
      }
    },
    [
      regenerateMutation.isSuccess,
      regenerateMutation.data,
    ]
  );

  const currentError =
    localError
    || (
      statusQuery.error
        ? extractErrorMessage(
            statusQuery.error
          )
        : ""
    )
    || (
      setupMutation.error
        ? extractErrorMessage(
            setupMutation.error
          )
        : ""
    )
    || (
      confirmMutation.error
        ? extractErrorMessage(
            confirmMutation.error
          )
        : ""
    )
    || (
      regenerateMutation.error
        ? extractErrorMessage(
            regenerateMutation.error
          )
        : ""
    )
    || (
      disableMutation.error
        ? extractErrorMessage(
            disableMutation.error
          )
        : ""
    );

  const isSubmitting =
    setupMutation.isPending
    || confirmMutation.isPending
    || regenerateMutation.isPending
    || disableMutation.isPending;

  function handleBeginSetup() {
    setLocalError("");
    setSuccessMessage("");
    setRecoveryCodes([]);

    setupMutation.mutate();
  }

  function handleConfirmSetup(
    event: FormEvent<HTMLFormElement>
  ) {
    event.preventDefault();

    setLocalError("");
    setSuccessMessage("");

    const normalizedCode =
      confirmationCode
        .replace(
          /\D/g,
          ""
        )
        .slice(
          0,
          6
        );

    if (
      !/^\d{6}$/.test(
        normalizedCode
      )
    ) {
      setLocalError(
        "O código MFA deve possuir 6 dígitos."
      );

      return;
    }

    confirmMutation.mutate(
      normalizedCode,
      {
        onSuccess:
          response => {
            setSuccessMessage(
              response.message
            );

            setSetupConfiguration(
              null
            );

            setConfirmationCode(
              ""
            );
          },
      }
    );
  }

  function handleRegenerateCodes() {
    setLocalError("");
    setSuccessMessage("");

    regenerateMutation.mutate();
  }

  function handleDisableMfa(
    event: FormEvent<HTMLFormElement>
  ) {
    event.preventDefault();

    setLocalError("");
    setSuccessMessage("");

    if (!password.trim()) {
      setLocalError(
        "Informe a senha atual."
      );

      return;
    }

    if (
      !/^\d{6}$/.test(
        disableCode
      )
    ) {
      setLocalError(
        "O código MFA deve possuir 6 dígitos."
      );

      return;
    }

    disableMutation.mutate(
      {
        password,
        code:
          disableCode,
      },
      {
        onSuccess:
          response => {
            setSuccessMessage(
              response.message
            );

            setPassword("");
            setDisableCode("");
            setRecoveryCodes([]);
            setSetupConfiguration(
              null
            );
          },
      }
    );
  }

  if (statusQuery.isPending) {
    return (
      <main className="account-security-page">
        <section className="account-security-card">
          <p>
            Carregando segurança da conta...
          </p>
        </section>
      </main>
    );
  }

  return (
    <main className="account-security-page">
      <section className="account-security-card">
        <header className="account-security-header">
          <span>
            Segurança
          </span>

          <h1>
            Autenticação em duas etapas
          </h1>

          <p>
            Proteja a conta com um código
            temporário gerado pelo aplicativo
            autenticador.
          </p>
        </header>

        {successMessage && (
          <div
            className="account-security-message account-security-message--success"
            role="status"
          >
            {successMessage}
          </div>
        )}

        {currentError && (
          <div
            className="account-security-message account-security-message--error"
            role="alert"
          >
            {currentError}
          </div>
        )}

        <div className="account-security-status">
          <div>
            <strong>
              Estado atual
            </strong>

            <span>
              {mfaEnabled
                ? "A autenticação em duas etapas está habilitada."
                : "A autenticação em duas etapas está desabilitada."}
            </span>
          </div>

          <span
            className={
              mfaEnabled
                ? "account-security-badge account-security-badge--enabled"
                : "account-security-badge account-security-badge--disabled"
            }
          >
            {mfaEnabled
              ? "Habilitado"
              : "Desabilitado"}
          </span>
        </div>

        {!mfaEnabled
          && !setupConfiguration && (
          <button
            type="button"
            className="account-security-primary-button"
            disabled={isSubmitting}
            onClick={
              handleBeginSetup
            }
          >
            {setupMutation.isPending
              ? "Preparando configuração..."
              : "Habilitar MFA"}
          </button>
        )}

        {setupConfiguration && (
          <section className="account-security-setup">
            <h2>
              Configure o aplicativo autenticador
            </h2>

            <p>
              Escaneie o QR Code usando Google
              Authenticator, Microsoft Authenticator
              ou outro aplicativo compatível com TOTP.
            </p>

            <div className="account-security-qr">
              <QRCodeSVG
                value={
                  setupConfiguration
                    .otpauth_uri
                }
                size={220}
                level="M"
                includeMargin
              />
            </div>

            <details className="account-security-secret">
              <summary>
                Inserir chave manualmente
              </summary>

              <code>
                {
                  setupConfiguration
                    .secret
                }
              </code>
            </details>

            <form
              className="account-security-form"
              onSubmit={
                handleConfirmSetup
              }
            >
              <label htmlFor="mfa-confirmation-code">
                Código atual do autenticador
              </label>

              <input
                id="mfa-confirmation-code"
                inputMode="numeric"
                autoComplete="one-time-code"
                placeholder="000000"
                maxLength={6}
                value={
                  confirmationCode
                }
                disabled={isSubmitting}
                onChange={
                  event => {
                    setConfirmationCode(
                      event.target.value
                        .replace(
                          /\D/g,
                          ""
                        )
                        .slice(
                          0,
                          6
                        )
                    );

                    setLocalError("");
                  }
                }
              />

              <button
                type="submit"
                className="account-security-primary-button"
                disabled={
                  isSubmitting
                  || confirmationCode.length
                    !== 6
                }
              >
                {confirmMutation.isPending
                  ? "Confirmando..."
                  : "Confirmar configuração"}
              </button>
            </form>
          </section>
        )}

        <RecoveryCodes
          codes={
            recoveryCodes
          }
        />

        {mfaEnabled && (
          <>
            <section className="account-security-section">
              <h2>
                Códigos de recuperação
              </h2>

              <p>
                Gere novos códigos se os anteriores
                tiverem sido perdidos ou expostos.
              </p>

              <button
                type="button"
                className="account-security-secondary-button"
                disabled={isSubmitting}
                onClick={
                  handleRegenerateCodes
                }
              >
                {regenerateMutation.isPending
                  ? "Gerando..."
                  : "Regenerar códigos"}
              </button>
            </section>

            <section className="account-security-section account-security-danger-zone">
              <h2>
                Desabilitar MFA
              </h2>

              <p>
                Essa ação reduzirá a proteção da conta.
                Informe a senha atual e o código do
                autenticador.
              </p>

              <form
                className="account-security-form"
                onSubmit={
                  handleDisableMfa
                }
              >
                <label htmlFor="mfa-disable-password">
                  Senha atual
                </label>

                <input
                  id="mfa-disable-password"
                  type="password"
                  autoComplete="current-password"
                  value={password}
                  disabled={isSubmitting}
                  onChange={
                    event => {
                      setPassword(
                        event.target.value
                      );

                      setLocalError("");
                    }
                  }
                />

                <label htmlFor="mfa-disable-code">
                  Código atual do autenticador
                </label>

                <input
                  id="mfa-disable-code"
                  inputMode="numeric"
                  autoComplete="one-time-code"
                  placeholder="000000"
                  maxLength={6}
                  value={
                    disableCode
                  }
                  disabled={isSubmitting}
                  onChange={
                    event => {
                      setDisableCode(
                        event.target.value
                          .replace(
                            /\D/g,
                            ""
                          )
                          .slice(
                            0,
                            6
                          )
                      );

                      setLocalError("");
                    }
                  }
                />

                <button
                  type="submit"
                  className="account-security-danger-button"
                  disabled={
                    isSubmitting
                    || !password
                    || disableCode.length
                      !== 6
                  }
                >
                  {disableMutation.isPending
                    ? "Desabilitando..."
                    : "Desabilitar MFA"}
                </button>
              </form>
            </section>
          </>
        )}
      </section>
    </main>
  );
}