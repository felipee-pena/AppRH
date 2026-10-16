/*
 * Associa as vagas existentes às empresas fictícias
 * inseridas pela migration V12.
 */

-- Nexa Sistemas:
-- vagas de sistemas, ERP, suporte e PL/SQL.
UPDATE public.job
SET company_id = (
    SELECT id
    FROM public.companies
    WHERE name = 'Nexa Sistemas'
)
WHERE code IN (
    1,
    3,
    4,
    5,
    7,
    10,
    28,
    29
);

-- DevCore Software:
-- desenvolvimento Java e arquitetura.
UPDATE public.job
SET company_id = (
    SELECT id
    FROM public.companies
    WHERE name = 'DevCore Software'
)
WHERE code IN (
    8,
    9,
    12
);

-- Orbit Cloud Solutions:
-- cloud, DevOps e confiabilidade.
UPDATE public.job
SET company_id = (
    SELECT id
    FROM public.companies
    WHERE name = 'Orbit Cloud Solutions'
)
WHERE code IN (
    11,
    18,
    22
);

-- Vertex Digital:
-- frontend e full stack.
UPDATE public.job
SET company_id = (
    SELECT id
    FROM public.companies
    WHERE name = 'Vertex Digital'
)
WHERE code IN (
    13,
    14
);

-- Lumina Data Labs:
-- análise e ciência de dados.
UPDATE public.job
SET company_id = (
    SELECT id
    FROM public.companies
    WHERE name = 'Lumina Data Labs'
)
WHERE code IN (
    15,
    17
);

-- BluePeak Technologies:
-- engenharia de dados.
UPDATE public.job
SET company_id = (
    SELECT id
    FROM public.companies
    WHERE name = 'BluePeak Technologies'
)
WHERE code IN (
    16,
    32
);

-- SecureWave Cybersecurity:
-- segurança e cibersegurança.
UPDATE public.job
SET company_id = (
    SELECT id
    FROM public.companies
    WHERE name = 'SecureWave Cybersecurity'
)
WHERE code IN (
    19,
    20
);

-- AgileWorks Consulting:
-- qualidade e métodos ágeis.
UPDATE public.job
SET company_id = (
    SELECT id
    FROM public.companies
    WHERE name = 'AgileWorks Consulting'
)
WHERE code IN (
    21,
    25
);

-- Postgres Prime:
-- banco de dados.
UPDATE public.job
SET company_id = (
    SELECT id
    FROM public.companies
    WHERE name = 'Postgres Prime'
)
WHERE code IN (
    23
);

-- Fusion Product Studio:
-- produto.
UPDATE public.job
SET company_id = (
    SELECT id
    FROM public.companies
    WHERE name = 'Fusion Product Studio'
)
WHERE code IN (
    24
);

-- Quantum AI Solutions:
-- inteligência artificial.
UPDATE public.job
SET company_id = (
    SELECT id
    FROM public.companies
    WHERE name = 'Quantum AI Solutions'
)
WHERE code IN (
    26
);

-- CloudBridge Services:
-- suporte cloud.
UPDATE public.job
SET company_id = (
    SELECT id
    FROM public.companies
    WHERE name = 'CloudBridge Services'
)
WHERE code IN (
    27
);

-- Valida se alguma vaga ficou sem empresa.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM public.job
        WHERE company_id IS NULL
    ) THEN
        RAISE EXCEPTION
            'Existem vagas sem empresa associada';
    END IF;
END $$;

-- Depois da migração dos dados, a empresa passa
-- a ser obrigatória.
ALTER TABLE public.job
    ALTER COLUMN company_id SET NOT NULL;