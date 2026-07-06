-- ============================================================
-- V2 — Authentication token tables
-- refresh_tokens, password_reset_tokens, email_verifications
-- ============================================================

-- ─────────────────────────────────────────────────────────────
-- refresh_tokens — valid JWT refresh tokens per session/device
-- ─────────────────────────────────────────────────────────────
CREATE TABLE refresh_tokens
(
    id          UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id     UUID         NOT NULL,
    token_hash  VARCHAR(255) NOT NULL,     -- SHA-256 hash of the raw token
    device_info VARCHAR(255),              -- Browser/OS user-agent string
    ip_address  VARCHAR(45),
    expires_at  TIMESTAMPTZ  NOT NULL,
    revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_tokens_token_hash ON refresh_tokens (token_hash);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens (expires_at);

-- ─────────────────────────────────────────────────────────────
-- password_reset_tokens — one-time tokens for forgot password
-- ─────────────────────────────────────────────────────────────
CREATE TABLE password_reset_tokens
(
    id          UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id     UUID         NOT NULL,
    token_hash  VARCHAR(255) NOT NULL,
    expires_at  TIMESTAMPTZ  NOT NULL,
    used        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT pk_password_reset_tokens PRIMARY KEY (id),
    CONSTRAINT fk_password_reset_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_pwd_reset_token_hash ON password_reset_tokens (token_hash);

-- ─────────────────────────────────────────────────────────────
-- email_verifications — tokens sent after registration
-- ─────────────────────────────────────────────────────────────
CREATE TABLE email_verifications
(
    id          UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id     UUID         NOT NULL,
    token_hash  VARCHAR(255) NOT NULL,
    expires_at  TIMESTAMPTZ  NOT NULL,
    verified_at TIMESTAMPTZ,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT pk_email_verifications PRIMARY KEY (id),
    CONSTRAINT fk_email_verification_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_email_verif_token_hash ON email_verifications (token_hash);

COMMENT ON TABLE refresh_tokens IS 'Valid refresh tokens per user device — supports multiple concurrent sessions';
COMMENT ON TABLE password_reset_tokens IS 'Single-use tokens for the forgot-password email flow, expire in 1 hour';
COMMENT ON TABLE email_verifications IS 'Tokens sent during registration to verify email ownership, expire in 24 hours';
