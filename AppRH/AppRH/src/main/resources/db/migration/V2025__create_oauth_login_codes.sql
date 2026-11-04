CREATE TABLE oauth_login_codes (
    id BIGSERIAL PRIMARY KEY,

    user_id VARCHAR NOT NULL,

    code_hash VARCHAR(64) NOT NULL,

    expires_at TIMESTAMPTZ NOT NULL,

    consumed_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_oauth_login_code_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_oauth_login_code_hash
        UNIQUE (code_hash),

    CONSTRAINT ck_oauth_login_code_expiration
        CHECK (expires_at > created_at)
);

CREATE INDEX idx_oauth_login_code_user_id
    ON oauth_login_codes(user_id);

CREATE INDEX idx_oauth_login_code_expires_at
    ON oauth_login_codes(expires_at);