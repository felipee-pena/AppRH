ALTER TABLE public.job
ADD COLUMN job_type VARCHAR(30);

UPDATE public.job
SET job_type = CASE
    WHEN MOD(code, 5) = 0 THEN 'CLT'
    WHEN MOD(code, 5) = 1 THEN 'PJ'
    WHEN MOD(code, 5) = 2 THEN 'INTERNSHIP'
    WHEN MOD(code, 5) = 3 THEN 'TEMPORARY'
    ELSE 'OTHER'
END
WHERE job_type IS NULL;

ALTER TABLE public.job
ALTER COLUMN job_type SET NOT NULL;

ALTER TABLE public.job
ADD CONSTRAINT chk_job_type
CHECK (
    job_type IN (
        'CLT',
        'PJ',
        'INTERNSHIP',
        'TEMPORARY',
        'OTHER'
    )
);