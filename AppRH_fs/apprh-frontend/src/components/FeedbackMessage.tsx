import "./feedback-message.css";

interface FeedbackMessageProps {
  type:
    | "success"
    | "error"
    | "info";

  message: string;
}

export function FeedbackMessage({
  type,
  message,
}: FeedbackMessageProps) {
  const role =
    type === "error"
      ? "alert"
      : "status";

  return (
    <div
      className={
        `feedback-message feedback-message--${type}`
      }
      role={role}
      aria-live={
        type === "error"
          ? "assertive"
          : "polite"
      }
    >
      <span
        className="feedback-message__symbol"
        aria-hidden="true"
      >
        {type === "success" && "✓"}
        {type === "error" && "!"}
        {type === "info" && "i"}
      </span>

      <p>
        {message}
      </p>
    </div>
  );
}