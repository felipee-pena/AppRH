CREATE TABLE user_mfa_recovery_codes (
    id BIGSERIAL PRIMARY KEY,

    user_id VARCHAR NOT NULL,

    code_hash VARCHAR(64) NOT NULL,

    used_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_mfa_recovery_code_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_mfa_recovery_code_hash
        UNIQUE (code_hash)
);

CREATE INDEX idx_mfa_recovery_code_user_id
    ON user_mfa_recovery_codes(user_id);