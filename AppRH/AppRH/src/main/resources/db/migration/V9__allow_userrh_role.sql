/*
 * Atualiza os perfis permitidos pela aplicação.
 *
 * Perfis:
 * ADMIN  - acesso administrativo
 * USERRH - criação e gestão das próprias vagas
 * USER   - usuário candidato
 */

ALTER TABLE public.users
    DROP CONSTRAINT IF EXISTS users_role_check;

ALTER TABLE public.users
    ADD CONSTRAINT users_role_check
    CHECK (
        role IN (
            'ADMIN',
            'USERRH',
            'USER'
        )
    );