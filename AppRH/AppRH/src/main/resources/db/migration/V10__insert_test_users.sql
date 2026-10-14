/*
 * Usuários fictícios para testes de autenticação
 * e autorização no ambiente de desenvolvimento.
 *
 * RH:
 * login: rh.rh@apprh.local
 * senha original: RhTeste@123
 *
 * Candidato:
 * login: candidato.teste@apprh.local
 * senha original: senhasenhaadminrh12
 *
 * As senhas persistidas devem estar em BCrypt.
 */

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM public.users
        WHERE login IN (
            'rh.rh@apprh.local',
            'candidato.teste@apprh.local'
        )
    ) THEN
        RAISE EXCEPTION
            'Um ou mais usuários de teste já existem';
    END IF;
END $$;

INSERT INTO public.users (
    id,
    login,
    password,
    role
)
VALUES
(
    'b20e6612-d087-4fc3-9501-7f85372f4101',
    'rh.rh@apprh.local',
    '$2a$12$fT/vL/kYP18vUmDzgtxGEueCoXNg849VOqis8qI3lllFC6u5M0tS.',
    'USERRH'
),
(
    'c38bc3dd-1352-4dce-aa37-c30d72873c95',
    'candidato.teste@apprh.local',
    '$2a$12$.0u0mnGxEIaXahWF4OK7i.1POjOX9mNT7CgePjtyBCDOqtQcYG772',
    'USER'
);