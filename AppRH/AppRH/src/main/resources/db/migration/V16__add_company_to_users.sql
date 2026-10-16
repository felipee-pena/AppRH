/*
 * Permite que usuários USERRH sejam vinculados
 * a uma empresa.
 *
 * ADMIN e USER não precisam de empresa.
 */

ALTER TABLE public.users
    ADD COLUMN company_id BIGINT;

ALTER TABLE public.users
    ADD CONSTRAINT fk_users_company
    FOREIGN KEY (company_id)
    REFERENCES public.companies(id);

CREATE INDEX idx_users_company_id
    ON public.users(company_id);