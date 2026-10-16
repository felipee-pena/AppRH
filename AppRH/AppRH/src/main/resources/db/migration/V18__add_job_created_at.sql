/*
 * Adiciona a data e hora de criação das vagas.
 *
 * Para as vagas existentes, a migration inicialmente
 * usa CURRENT_TIMESTAMP. A próxima migration ajustará
 * datas históricas de teste coerentes.
 */

ALTER TABLE public.job
    ADD COLUMN created_at
        TIMESTAMP WITH TIME ZONE
        NOT NULL
        DEFAULT CURRENT_TIMESTAMP;