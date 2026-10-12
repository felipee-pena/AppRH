ALTER TABLE public.users
DROP CONSTRAINT IF EXISTS users_role_check;

ALTER TABLE public.users
ALTER COLUMN role DROP DEFAULT;

UPDATE public.users
SET role = CASE
    WHEN role = '0' THEN 'ADMIN'
    WHEN role = '1' THEN 'USER'
    WHEN UPPER(role) = 'ADMIN' THEN 'ADMIN'
    WHEN UPPER(role) = 'USER' THEN 'USER'
    ELSE role
END;

ALTER TABLE public.users
ALTER COLUMN role TYPE VARCHAR(10)
USING role::VARCHAR(10);

ALTER TABLE public.users
ADD CONSTRAINT users_role_check
CHECK (role IN ('ADMIN', 'USER'));

ALTER TABLE public.users
ALTER COLUMN role SET DEFAULT 'USER';

ALTER TABLE public.users
ALTER COLUMN role SET NOT NULL;