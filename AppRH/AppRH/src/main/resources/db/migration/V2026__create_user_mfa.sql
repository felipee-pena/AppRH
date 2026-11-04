CREATE TABLE user_mfa (
    id BIGSERIAL PRIMARY KEY,

    user_id VARCHAR NOT NULL,

    method VARCHAR(30) NOT NULL,

    secret_encrypted TEXT NOT NULL,

    enabled BOOLEAN NOT NULL DEFAULT FALSE,

    confirmed_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    last_used_at TIMESTAMPTZ,

    CONSTRAINT fk_user_mfa_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_user_mfa_user_method
        UNIQUE (user_id, method),

    CONSTRAINT ck_user_mfa_method
        CHECK (
            method IN (
                'TOTP'
            )
        ),

    CONSTRAINT ck_user_mfa_confirmation
        CHECK (
            enabled = FALSE
            OR confirmed_at IS NOT NULL
        )
);

CREATE INDEX idx_user_mfa_user_id
    ON user_mfa(user_id);

CREATE INDEX idx_user_mfa_enabled
    ON user_mfa(user_id, enabled);