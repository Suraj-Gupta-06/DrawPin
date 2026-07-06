-- ============================================================
-- V1 — Create core users table
-- Matches: com.drawpin.domain.entity.User
-- ============================================================

-- Enable pgcrypto for gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE users
(
    id                  UUID         NOT NULL DEFAULT gen_random_uuid(),
    email               VARCHAR(255) NOT NULL,
    password_hash       VARCHAR(255) NOT NULL,
    name                VARCHAR(80)  NOT NULL,
    handle              VARCHAR(40)  NOT NULL,
    avatar_url          TEXT,
    cover_url           TEXT,
    bio                 VARCHAR(300),
    city                VARCHAR(80),
    website             VARCHAR(255),
    role                VARCHAR(20)  NOT NULL DEFAULT 'COLLECTOR',
    status              VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    is_verified         BOOLEAN      NOT NULL DEFAULT FALSE,
    email_verified      BOOLEAN      NOT NULL DEFAULT FALSE,
    failed_login_count  INTEGER      NOT NULL DEFAULT 0,
    locked_until        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at          TIMESTAMPTZ,

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT uq_users_handle UNIQUE (handle),
    CONSTRAINT chk_users_role CHECK (role IN ('COLLECTOR', 'CREATOR', 'MODERATOR', 'ADMIN')),
    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'DELETED'))
);

-- Indexes for common query patterns
CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_handle ON users (handle);
CREATE INDEX idx_users_role ON users (role);
CREATE INDEX idx_users_status ON users (status);
CREATE INDEX idx_users_created_at ON users (created_at DESC);

-- ============================================================
-- user_settings — one-to-one with users
-- ============================================================
CREATE TABLE user_settings
(
    id                    UUID        NOT NULL DEFAULT gen_random_uuid(),
    user_id               UUID        NOT NULL,
    email_notifications   BOOLEAN     NOT NULL DEFAULT TRUE,
    push_notifications    BOOLEAN     NOT NULL DEFAULT TRUE,
    private_profile       BOOLEAN     NOT NULL DEFAULT FALSE,
    show_online_status    BOOLEAN     NOT NULL DEFAULT TRUE,
    allow_messages        BOOLEAN     NOT NULL DEFAULT TRUE,
    theme                 VARCHAR(10) NOT NULL DEFAULT 'dark',
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT pk_user_settings PRIMARY KEY (id),
    CONSTRAINT uq_user_settings_user_id UNIQUE (user_id),
    CONSTRAINT fk_user_settings_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_user_settings_theme CHECK (theme IN ('dark', 'light', 'system'))
);

CREATE INDEX idx_user_settings_user_id ON user_settings (user_id);

COMMENT ON TABLE users IS 'Core identity table — every person on the platform regardless of role';
COMMENT ON TABLE user_settings IS 'Per-user preference flags for notifications, privacy, and appearance';
