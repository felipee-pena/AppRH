/*
 * Relaciona cada vaga ao usuário responsável pela criação.
 *
 * A aplicação utiliza public.users.
 * A tabela auth.users pertence ao Supabase Auth
 * e não participa desta relação.
 *
 * As vagas existentes serão atribuídas ao administrador:
 * evomanex
 */

-- 1. Confirma que o administrador existe.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM public.users
        WHERE login = 'evomanex'
          AND role = 'ADMIN'
    ) THEN
        RAISE EXCEPTION
            'Administrador evomanex@admin.local não encontrado em public.users';
    END IF;
END $$;

-- 2. Adiciona a coluna inicialmente aceitando NULL.
ALTER TABLE public.job
    ADD COLUMN created_by_user_id VARCHAR(255);

-- 3. Cria a chave estrangeira para public.users.
ALTER TABLE public.job
    ADD CONSTRAINT fk_job_created_by_user
    FOREIGN KEY (created_by_user_id)
    REFERENCES public.users(id);

-- 4. Atribui todas as vagas existentes ao administrador.
UPDATE public.job
SET created_by_user_id = (
    SELECT id
    FROM public.users
    WHERE login = 'evomanex'
      AND role = 'ADMIN'
    LIMIT 1
)
WHERE created_by_user_id IS NULL;

-- 5. Confirma que nenhuma vaga ficou sem responsável.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM public.job
        WHERE created_by_user_id IS NULL
    ) THEN
        RAISE EXCEPTION
            'Existem vagas sem usuário responsável';
    END IF;
END $$;

-- 6. Torna a coluna obrigatória.
ALTER TABLE public.job
    ALTER COLUMN created_by_user_id SET NOT NULL;