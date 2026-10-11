ALTER TABLE public.users
DROP CONSTRAINT IF EXISTS users_role_check;

ALTER TABLE public.users
ALTER COLUMN role DROP DEFAULT;

ALTER TABLE public.users
ALTER COLUMN role TYPE VARCHAR(10)
USING (
    CASE role
        WHEN 0 THEN 'ADMIN'
        WHEN 1 THEN 'USER'
        ELSE NULL
    END
);

ALTER TABLE public.users
ALTER COLUMN role SET NOT NULL;

ALTER TABLE public.users
ADD CONSTRAINT users_role_check
CHECK (role IN ('ADMIN', 'USER'));

ALTER TABLE public.users
ALTER COLUMN role SET DEFAULT 'USER';