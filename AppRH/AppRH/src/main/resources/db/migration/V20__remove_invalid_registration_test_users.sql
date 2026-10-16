/*
 * Remove usuários criados durante a validação
 * do fluxo antigo de cadastro público.
 *
 * Esses registros foram cadastrados como USER
 * antes da ativação do cadastro público por perfil.
 *
 * O candidato novo.candidato@apprh.local é mantido,
 * pois foi criado corretamente como USER.
 */

DELETE FROM public.users
WHERE login IN (
    'rh.nexa2@apprh.local',
    'rh.claro@apprh.local',
    'admin2@apprh.local'
)
AND role = 'USER';

/*
 * Confirma que os usuários incorretos foram removidos.
 */
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM public.users
        WHERE login IN (
            'rh.nexa2@apprh.local',
            'rh.claro@apprh.local',
            'admin2@apprh.local'
        )
    ) THEN
        RAISE EXCEPTION
            'Não foi possível remover todos os usuários de teste incorretos';
    END IF;
END $$;