import {
  type KeyboardEvent,
  type MouseEvent,
} from "react";

import "./confirmation-modal.css";

interface ConfirmationModalProps {
  title: string;
  description: string;
  isPending?: boolean;
  confirmLabel?: string;
  onConfirm(): void;
  onCancel(): void;
}

export function ConfirmationModal({
  title,
  description,
  isPending = false,
  confirmLabel = "Excluir",
  onConfirm,
  onCancel,
}: ConfirmationModalProps) {
  function handleOverlayMouseDown(
    event: MouseEvent<HTMLDivElement>
  ) {
    if (
      event.target !== event.currentTarget
      || isPending
    ) {
      return;
    }

    onCancel();
  }

  function handleOverlayKeyDown(
    event: KeyboardEvent<HTMLDivElement>
  ) {
    if (
      event.target !== event.currentTarget
      || isPending
    ) {
      return;
    }

    if (
      event.key === "Enter"
      || event.key === " "
    ) {
      event.preventDefault();
      onCancel();
    }
  }

  return (
    <div
      className="confirmation-modal__overlay"
      role="button"
      tabIndex={0}
      aria-label="Fechar modal de confirmação"
      onMouseDown={handleOverlayMouseDown}
      onKeyDown={handleOverlayKeyDown}
    >
      <section
        className="confirmation-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="confirmation-title"
        aria-describedby="confirmation-description"
      >
        <div
          className="confirmation-modal__icon"
          aria-hidden="true"
        >
          !
        </div>

        <h2 id="confirmation-title">
          {title}
        </h2>

        <p id="confirmation-description">
          {description}
        </p>

        <div className="confirmation-modal__actions">
          <button
            type="button"
            className="confirmation-modal__button confirmation-modal__button--secondary"
            onClick={onCancel}
            disabled={isPending}
          >
            Cancelar
          </button>

          <button
            type="button"
            className="confirmation-modal__button confirmation-modal__button--danger"
            onClick={onConfirm}
            disabled={isPending}
          >
            {isPending
              ? "Excluindo..."
              : confirmLabel}
          </button>
        </div>
      </section>
    </div>
  );
}
