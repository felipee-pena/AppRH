CREATE TABLE public.email_verification_tokens (
    id UUID PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_email_verification_tokens_user
        FOREIGN KEY (user_id)
        REFERENCES public.users(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_email_verification_tokens_hash
        UNIQUE (token_hash)
);

CREATE INDEX idx_email_verification_tokens_user
ON public.email_verification_tokens(user_id);

CREATE INDEX idx_email_verification_tokens_expiration
ON public.email_verification_tokens(expires_at);

CREATE INDEX idx_email_verification_tokens_active
ON public.email_verification_tokens(user_id, used_at);