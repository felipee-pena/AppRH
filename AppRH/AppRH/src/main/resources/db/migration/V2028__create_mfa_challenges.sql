CREATE TABLE mfa_challenges (
    id BIGSERIAL PRIMARY KEY,

    user_id VARCHAR NOT NULL,

    challenge_hash VARCHAR(64) NOT NULL,

    expires_at TIMESTAMPTZ NOT NULL,

    consumed_at TIMESTAMPTZ,

    failed_attempts INTEGER NOT NULL DEFAULT 0,

    created_at TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_mfa_challenge_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_mfa_challenge_hash
        UNIQUE (challenge_hash),

    CONSTRAINT ck_mfa_failed_attempts
        CHECK (
            failed_attempts >= 0
            AND failed_attempts <= 5
        ),

    CONSTRAINT ck_mfa_challenge_expiration
        CHECK (expires_at > created_at)
);

CREATE INDEX idx_mfa_challenge_user_id
    ON mfa_challenges(user_id);

CREATE INDEX idx_mfa_challenge_expires_at
    ON mfa_challenges(expires_at);