-- Migration 0004: email/password accounts and server-side sessions for the admin panel.

CREATE TABLE IF NOT EXISTS admin_users (
    id TEXT PRIMARY KEY,
    email TEXT NOT NULL UNIQUE,              -- stored lower-cased
    password_hash TEXT NOT NULL,             -- base64 PBKDF2-SHA256 output
    password_salt TEXT NOT NULL,             -- base64, 16 random bytes
    password_iterations INTEGER NOT NULL,
    role TEXT NOT NULL DEFAULT 'editor',     -- 'owner' (can manage users) | 'editor'
    disabled INTEGER NOT NULL DEFAULT 0,
    failed_attempts INTEGER NOT NULL DEFAULT 0,
    locked_until TEXT,
    password_changed_at TEXT NOT NULL,
    last_login_at TEXT,
    created_at TEXT NOT NULL,
    created_by TEXT
);

CREATE TABLE IF NOT EXISTS admin_sessions (
    id TEXT PRIMARY KEY,                     -- SHA-256 of the session token (token itself is never stored)
    user_id TEXT NOT NULL,
    created_at TEXT NOT NULL,
    expires_at TEXT NOT NULL,
    last_seen_at TEXT NOT NULL,
    user_agent TEXT,
    ip_hash TEXT
);

CREATE INDEX IF NOT EXISTS idx_admin_sessions_user ON admin_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_admin_sessions_expires ON admin_sessions(expires_at);
