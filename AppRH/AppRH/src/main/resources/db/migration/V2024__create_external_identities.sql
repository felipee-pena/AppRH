CREATE TABLE user_external_identities (
    id BIGSERIAL PRIMARY KEY,

    user_id VARCHAR NOT NULL,

    provider VARCHAR(30) NOT NULL,

    provider_user_id VARCHAR(255) NOT NULL,

    provider_email VARCHAR(320),

    provider_username VARCHAR(255),

    email_verified BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    last_login_at TIMESTAMPTZ,

    CONSTRAINT fk_external_identity_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_external_identity_provider_subject
        UNIQUE (provider, provider_user_id),

    CONSTRAINT uk_external_identity_user_provider
        UNIQUE (user_id, provider),

    CONSTRAINT ck_external_identity_provider
        CHECK (
            provider IN (
                'GOOGLE',
                'GITHUB'
            )
        )
);

CREATE INDEX idx_external_identity_user_id
    ON user_external_identities(user_id);

CREATE INDEX idx_external_identity_provider_email
    ON user_external_identities(
        provider,
        provider_email
    );