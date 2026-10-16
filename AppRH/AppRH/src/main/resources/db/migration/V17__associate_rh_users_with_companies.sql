/*
 * Vincula o usuário USERRH existente à Nexa Sistemas
 * e alinha as vagas criadas por esse usuário.
 */

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM public.companies
        WHERE normalized_name = 'nexa sistemas'
    ) THEN
        RAISE EXCEPTION
            'Empresa Nexa Sistemas não encontrada';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM public.users
        WHERE id =
              'b20e6612-d087-4fc3-9501-7f85372f4101'
          AND role = 'USERRH'
    ) THEN
        RAISE EXCEPTION
            'Usuário USERRH de teste não encontrado';
    END IF;
END $$;

UPDATE public.users
SET company_id = (
    SELECT id
    FROM public.companies
    WHERE normalized_name = 'nexa sistemas'
)
WHERE id =
      'b20e6612-d087-4fc3-9501-7f85372f4101'
  AND role = 'USERRH';

UPDATE public.job
SET company_id = (
    SELECT id
    FROM public.companies
    WHERE normalized_name = 'nexa sistemas'
)
WHERE created_by_user_id =
      'b20e6612-d087-4fc3-9501-7f85372f4101';

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM public.users
        WHERE role = 'USERRH'
          AND company_id IS NULL
    ) THEN
        RAISE EXCEPTION
            'Existem usuários USERRH sem empresa';
    END IF;
END $$;

ALTER TABLE public.users
    ADD CONSTRAINT chk_userrh_requires_company
    CHECK (
        role <> 'USERRH'
        OR company_id IS NOT NULL
    );