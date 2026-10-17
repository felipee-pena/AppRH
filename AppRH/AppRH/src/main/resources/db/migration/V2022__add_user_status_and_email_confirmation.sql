ALTER TABLE public.users
ADD COLUMN status VARCHAR(40);

ALTER TABLE public.users
ADD COLUMN email_verified_at TIMESTAMP WITH TIME ZONE;

UPDATE public.users
SET status = 'ACTIVE'
WHERE status IS NULL;

ALTER TABLE public.users
ALTER COLUMN status SET NOT NULL;

ALTER TABLE public.users
ALTER COLUMN status SET DEFAULT 'PENDING_EMAIL_CONFIRMATION';

ALTER TABLE public.users
ADD CONSTRAINT chk_users_status
CHECK (
    status IN (
        'PENDING_EMAIL_CONFIRMATION',
        'PENDING_APPROVAL',
        'ACTIVE',
        'BLOCKED',
        'REJECTED',
        'DELETED'
    )
);

CREATE INDEX idx_users_status
ON public.users(status);