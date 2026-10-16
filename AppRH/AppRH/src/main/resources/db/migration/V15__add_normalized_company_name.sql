/*
 * Adiciona uma representação normalizada do nome
 * para reduzir duplicidade de empresas.
 *
 * Exemplos:
 * Claro        -> claro
 * CLARO        -> claro
 * Claro Brasil -> claro brasil
 */

ALTER TABLE public.companies
    ADD COLUMN normalized_name VARCHAR(150);

UPDATE public.companies
SET normalized_name =
    LOWER(
        REGEXP_REPLACE(
            BTRIM(name),
            '\s+',
            ' ',
            'g'
        )
    );

DO $$
BEGIN
    IF EXISTS (
        SELECT
            normalized_name
        FROM public.companies
        GROUP BY normalized_name
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION
            'Existem empresas duplicadas após normalização';
    END IF;
END $$;

ALTER TABLE public.companies
    ALTER COLUMN normalized_name SET NOT NULL;

ALTER TABLE public.companies
    DROP CONSTRAINT IF EXISTS uk_companies_name;

ALTER TABLE public.companies
    ADD CONSTRAINT uk_companies_normalized_name
    UNIQUE (normalized_name);