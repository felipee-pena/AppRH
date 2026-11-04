ALTER TABLE user_external_identities
    ALTER COLUMN user_id TYPE VARCHAR(255);

ALTER TABLE oauth_login_codes
    ALTER COLUMN user_id TYPE VARCHAR(255);

ALTER TABLE user_mfa
    ALTER COLUMN user_id TYPE VARCHAR(255);

ALTER TABLE user_mfa_recovery_codes
    ALTER COLUMN user_id TYPE VARCHAR(255);

ALTER TABLE mfa_challenges
    ALTER COLUMN user_id TYPE VARCHAR(255);
