/*
 * Cria usuários USERRH de demonstração para todas
 * as empresas que ainda não possuem um responsável.
 *
 * Também:
 *
 * 1. relaciona cada usuário de RH à sua empresa;
 * 2. relaciona todas as vagas ao RH da empresa;
 * 3. define datas de criação históricas para vagas antigas;
 * 4. valida se não existem vagas sem empresa ou responsável.
 *
 * ATENÇÃO:
 * As credenciais são apenas para ambiente local.
 */

CREATE EXTENSION IF NOT EXISTS pgcrypto;

/*
 * AgileWorks Consulting
 */
INSERT INTO public.users (
    id,
    login,
    password,
    role,
    company_id
)
SELECT
    '10000000-0000-4000-8000-000000000008',
    'rh.agileworks@apprh.local',
    crypt(
        'AgileWorks@2026',
        gen_salt('bf', 10)
    ),
    'USERRH',
    c.id
FROM public.companies c
WHERE c.normalized_name =
      'agileworks consulting'
  AND NOT EXISTS (
      SELECT 1
      FROM public.users u
      WHERE u.login =
            'rh.agileworks@apprh.local'
  );

/*
 * BluePeak Technologies
 */
INSERT INTO public.users (
    id,
    login,
    password,
    role,
    company_id
)
SELECT
    '10000000-0000-4000-8000-000000000005',
    'rh.bluepeak@apprh.local',
    crypt(
        'BluePeak@2026',
        gen_salt('bf', 10)
    ),
    'USERRH',
    c.id
FROM public.companies c
WHERE c.normalized_name =
      'bluepeak technologies'
  AND NOT EXISTS (
      SELECT 1
      FROM public.users u
      WHERE u.login =
            'rh.bluepeak@apprh.local'
  );

/*
 * CloudBridge Services
 */
INSERT INTO public.users (
    id,
    login,
    password,
    role,
    company_id
)
SELECT
    '10000000-0000-4000-8000-000000000012',
    'rh.cloudbridge@apprh.local',
    crypt(
        'CloudBridge@2026',
        gen_salt('bf', 10)
    ),
    'USERRH',
    c.id
FROM public.companies c
WHERE c.normalized_name =
      'cloudbridge services'
  AND NOT EXISTS (
      SELECT 1
      FROM public.users u
      WHERE u.login =
            'rh.cloudbridge@apprh.local'
  );

/*
 * DevCore Software
 */
INSERT INTO public.users (
    id,
    login,
    password,
    role,
    company_id
)
SELECT
    '10000000-0000-4000-8000-000000000009',
    'rh.devcore@apprh.local',
    crypt(
        'DevCore@2026',
        gen_salt('bf', 10)
    ),
    'USERRH',
    c.id
FROM public.companies c
WHERE c.normalized_name =
      'devcore software'
  AND NOT EXISTS (
      SELECT 1
      FROM public.users u
      WHERE u.login =
            'rh.devcore@apprh.local'
  );

/*
 * Fusion Product Studio
 */
INSERT INTO public.users (
    id,
    login,
    password,
    role,
    company_id
)
SELECT
    '10000000-0000-4000-8000-000000000011',
    'rh.fusionproduct@apprh.local',
    crypt(
        'FusionProduct@2026',
        gen_salt('bf', 10)
    ),
    'USERRH',
    c.id
FROM public.companies c
WHERE c.normalized_name =
      'fusion product studio'
  AND NOT EXISTS (
      SELECT 1
      FROM public.users u
      WHERE u.login =
            'rh.fusionproduct@apprh.local'
  );

/*
 * Lumina Data Labs
 */
INSERT INTO public.users (
    id,
    login,
    password,
    role,
    company_id
)
SELECT
    '10000000-0000-4000-8000-000000000003',
    'rh.lumina@apprh.local',
    crypt(
        'LuminaData@2026',
        gen_salt('bf', 10)
    ),
    'USERRH',
    c.id
FROM public.companies c
WHERE c.normalized_name =
      'lumina data labs'
  AND NOT EXISTS (
      SELECT 1
      FROM public.users u
      WHERE u.login =
            'rh.lumina@apprh.local'
  );

/*
 * Orbit Cloud Solutions
 */
INSERT INTO public.users (
    id,
    login,
    password,
    role,
    company_id
)
SELECT
    '10000000-0000-4000-8000-000000000002',
    'rh.orbitcloud@apprh.local',
    crypt(
        'OrbitCloud@2026',
        gen_salt('bf', 10)
    ),
    'USERRH',
    c.id
FROM public.companies c
WHERE c.normalized_name =
      'orbit cloud solutions'
  AND NOT EXISTS (
      SELECT 1
      FROM public.users u
      WHERE u.login =
            'rh.orbitcloud@apprh.local'
  );

/*
 * Postgres Prime
 */
INSERT INTO public.users (
    id,
    login,
    password,
    role,
    company_id
)
SELECT
    '10000000-0000-4000-8000-000000000010',
    'rh.postgresprime@apprh.local',
    crypt(
        'PostgresPrime@2026',
        gen_salt('bf', 10)
    ),
    'USERRH',
    c.id
FROM public.companies c
WHERE c.normalized_name =
      'postgres prime'
  AND NOT EXISTS (
      SELECT 1
      FROM public.users u
      WHERE u.login =
            'rh.postgresprime@apprh.local'
  );

/*
 * Quantum AI Solutions
 */
INSERT INTO public.users (
    id,
    login,
    password,
    role,
    company_id
)
SELECT
    '10000000-0000-4000-8000-000000000007',
    'rh.quantumai@apprh.local',
    crypt(
        'QuantumAI@2026',
        gen_salt('bf', 10)
    ),
    'USERRH',
    c.id
FROM public.companies c
WHERE c.normalized_name =
      'quantum ai solutions'
  AND NOT EXISTS (
      SELECT 1
      FROM public.users u
      WHERE u.login =
            'rh.quantumai@apprh.local'
  );

/*
 * SecureWave Cybersecurity
 */
INSERT INTO public.users (
    id,
    login,
    password,
    role,
    company_id
)
SELECT
    '10000000-0000-4000-8000-000000000006',
    'rh.securewave@apprh.local',
    crypt(
        'SecureWave@2026',
        gen_salt('bf', 10)
    ),
    'USERRH',
    c.id
FROM public.companies c
WHERE c.normalized_name =
      'securewave cybersecurity'
  AND NOT EXISTS (
      SELECT 1
      FROM public.users u
      WHERE u.login =
            'rh.securewave@apprh.local'
  );

/*
 * Vertex Digital
 */
INSERT INTO public.users (
    id,
    login,
    password,
    role,
    company_id
)
SELECT
    '10000000-0000-4000-8000-000000000004',
    'rh.vertexdigital@apprh.local',
    crypt(
        'VertexDigital@2026',
        gen_salt('bf', 10)
    ),
    'USERRH',
    c.id
FROM public.companies c
WHERE c.normalized_name =
      'vertex digital'
  AND NOT EXISTS (
      SELECT 1
      FROM public.users u
      WHERE u.login =
            'rh.vertexdigital@apprh.local'
  );

/*
 * Associa todas as vagas ao usuário RH responsável
 * pela empresa da vaga.
 *
 * Nexa Sistemas utiliza o usuário RH já existente.
 */
UPDATE public.job j
SET created_by_user_id =
    CASE j.company_id

        WHEN 1 THEN
            'b20e6612-d087-4fc3-9501-7f85372f4101'

        WHEN 2 THEN
            '10000000-0000-4000-8000-000000000002'

        WHEN 3 THEN
            '10000000-0000-4000-8000-000000000003'

        WHEN 4 THEN
            '10000000-0000-4000-8000-000000000004'

        WHEN 5 THEN
            '10000000-0000-4000-8000-000000000005'

        WHEN 6 THEN
            '10000000-0000-4000-8000-000000000006'

        WHEN 7 THEN
            '10000000-0000-4000-8000-000000000007'

        WHEN 8 THEN
            '10000000-0000-4000-8000-000000000008'

        WHEN 9 THEN
            '10000000-0000-4000-8000-000000000009'

        WHEN 10 THEN
            '10000000-0000-4000-8000-000000000010'

        WHEN 11 THEN
            '10000000-0000-4000-8000-000000000011'

        WHEN 12 THEN
            '10000000-0000-4000-8000-000000000012'

        ELSE j.created_by_user_id
    END;

/*
 * Cria datas históricas variadas.
 *
 * O cálculo usa o código da vaga para distribuir
 * as datas de criação entre agosto e outubro de 2026.
 */
UPDATE public.job
SET created_at =
        TIMESTAMPTZ '2026-08-15 09:00:00-03'
        + (
            code * INTERVAL '1 day'
        )
        + (
            MOD(code, 8) * INTERVAL '1 hour'
        );

/*
 * Garante que a criação sempre seja anterior
 * ou igual à data de expiração.
 */
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM public.job
        WHERE created_at::DATE
              > expiration_date
    ) THEN
        RAISE EXCEPTION
            'Existem vagas com criação posterior à expiração';
    END IF;
END $$;

/*
 * Confirma que todos os usuários RH possuem empresa.
 */
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

/*
 * Confirma que todas as vagas possuem empresa
 * e usuário responsável.
 */
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM public.job
        WHERE company_id IS NULL
           OR created_by_user_id IS NULL
    ) THEN
        RAISE EXCEPTION
            'Existem vagas sem empresa ou responsável';
    END IF;
END $$;

/*
 * Confirma que vaga, empresa e usuário RH
 * pertencem à mesma empresa.
 */
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM public.job j
        JOIN public.users u
          ON u.id = j.created_by_user_id
        WHERE u.role <> 'USERRH'
           OR u.company_id <> j.company_id
    ) THEN
        RAISE EXCEPTION
            'Existem vagas vinculadas a RH de outra empresa';
    END IF;
END $$;