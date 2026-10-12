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
  return (
    <div
      className="confirmation-modal__overlay"
      role="dialog"
      aria-modal="true"
      aria-labelledby="confirmation-title"
      onMouseDown={onCancel}
    >
      <section
        className="confirmation-modal"
        onMouseDown={(event) => event.stopPropagation()}
      >
        <div className="confirmation-modal__icon">
          !
        </div>

        <h2 id="confirmation-title">{title}</h2>

        <p>{description}</p>

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
            {isPending ? "Excluindo..." : confirmLabel}
          </button>
        </div>
      </section>
    </div>
  );
}