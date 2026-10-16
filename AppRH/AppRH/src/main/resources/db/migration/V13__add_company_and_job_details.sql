/*
 * Inclui empresa e e-mail de contato na vaga.
 *
 * Também converte:
 * date   VARCHAR(255) -> expiration_date DATE
 * salary VARCHAR(255) -> NUMERIC(12, 2)
 *
 * Os valores antigos possuem três formatos:
 *
 * 9500.00
 * 11.800.00
 * 9.950,00
 */

-- Empresa inicialmente aceita NULL porque as vagas antigas
-- ainda serão associadas na próxima migration.
ALTER TABLE public.job
    ADD COLUMN company_id BIGINT;

-- E-mail é opcional.
ALTER TABLE public.job
    ADD COLUMN contact_email VARCHAR(255);

-- Renomeia a coluna antes da conversão.
ALTER TABLE public.job
    RENAME COLUMN date TO expiration_date;

-- Converte datas no formato ISO YYYY-MM-DD.
ALTER TABLE public.job
    ALTER COLUMN expiration_date TYPE DATE
    USING TO_DATE(
        expiration_date,
        'YYYY-MM-DD'
    );

-- Normaliza salários no formato brasileiro:
-- 9.950,00 -> 9950.00
UPDATE public.job
SET salary =
    REPLACE(
        REPLACE(
            salary,
            '.',
            ''
        ),
        ',',
        '.'
    )
WHERE salary ~ '^[0-9]{1,3}(\.[0-9]{3})+,[0-9]{2}$';

-- Normaliza valores que possuem dois pontos:
-- 11.800.00 -> 11800.00
-- 15.500.00 -> 15500.00
UPDATE public.job
SET salary =
    REGEXP_REPLACE(
        salary,
        '^([0-9]+)\.([0-9]{3})\.([0-9]{2})$',
        '\1\2.\3'
    )
WHERE salary ~ '^[0-9]+\.[0-9]{3}\.[0-9]{2}$';

-- Confirma que todos os valores restantes estão
-- no formato numérico esperado.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM public.job
        WHERE salary !~ '^[0-9]+(\.[0-9]{1,2})?$'
    ) THEN
        RAISE EXCEPTION
            'Existem salários que não podem ser convertidos para NUMERIC(12,2)';
    END IF;
END $$;

-- Converte salário para tipo financeiro.
ALTER TABLE public.job
    ALTER COLUMN salary TYPE NUMERIC(12, 2)
    USING salary::NUMERIC(12, 2);

-- Garante salário não negativo.
ALTER TABLE public.job
    ADD CONSTRAINT chk_job_salary_non_negative
    CHECK (salary >= 0);

-- Cria a relação com empresas.
ALTER TABLE public.job
    ADD CONSTRAINT fk_job_company
    FOREIGN KEY (company_id)
    REFERENCES public.companies(id);