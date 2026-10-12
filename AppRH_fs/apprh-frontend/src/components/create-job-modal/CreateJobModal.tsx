import { useEffect, useState, type FormEvent } from "react";
import { useCreateJob } from "../../hooks/useCreateJob";
import type { JobData } from "../../interfaces/JobData";
import "./create-job-modal.css";

interface ModalProps {
  closeModal(): void;
}

export function CreateJobModal({
  closeModal,
}: ModalProps) {
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [date, setDate] = useState("");
  const [salary, setSalary] = useState("");

  const {
    mutate,
    isPending,
    isSuccess,
    isError,
  } = useCreateJob();

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const job: JobData = {
      name,
      description,
      date,
      salary,
    };

    mutate(job);
  }

  useEffect(() => {
    if (isSuccess) {
      closeModal();
    }
  }, [isSuccess, closeModal]);

  return (
    <div className="modal-overlay">
      <div className="modal-body">
        <header className="modal-header">
          <h2>Cadastrar nova vaga</h2>

          <button
            type="button"
            onClick={closeModal}
            aria-label="Fechar"
          >
            ×
          </button>
        </header>

        <form
          className="input-container"
          onSubmit={submit}
        >
          <label htmlFor="name">Nome da vaga</label>
          <input
            id="name"
            value={name}
            onChange={(event) =>
              setName(event.target.value)
            }
            required
          />

          <label htmlFor="description">
            Descrição
          </label>
          <textarea
            id="description"
            value={description}
            onChange={(event) =>
              setDescription(event.target.value)
            }
            required
          />

          <label htmlFor="date">
            Data de expiração
          </label>
          <input
            id="date"
            type="date"
            value={date}
            onChange={(event) =>
              setDate(event.target.value)
            }
            required
          />

          <label htmlFor="salary">Salário</label>
          <input
            id="salary"
            type="number"
            step="0.01"
            value={salary}
            onChange={(event) =>
              setSalary(event.target.value)
            }
            required
          />

          {isError && (
            <p className="error-message">
              Não foi possível cadastrar a vaga.
            </p>
          )}

          <footer className="modal-actions">
            <button
              type="button"
              onClick={closeModal}
            >
              Cancelar
            </button>

            <button
              type="submit"
              disabled={isPending}
            >
              {isPending
                ? "Cadastrando..."
                : "Cadastrar vaga"}
            </button>
          </footer>
        </form>
      </div>
    </div>
  );
}