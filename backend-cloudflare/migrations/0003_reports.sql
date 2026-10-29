-- Migration 0003: user reports about wrong or outdated notice information.
CREATE TABLE IF NOT EXISTS job_reports (
    id TEXT PRIMARY KEY,
    job_id TEXT NOT NULL,
    reason TEXT NOT NULL,
    details TEXT,
    status TEXT NOT NULL DEFAULT 'OPEN',   -- 'OPEN', 'RESOLVED', 'DISMISSED'
    reporter_hash TEXT,                    -- salted hash of client IP, for abuse throttling only
    created_at TEXT NOT NULL,
    resolved_at TEXT,
    resolution_note TEXT
);

CREATE INDEX IF NOT EXISTS idx_reports_status_created ON job_reports(status, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_reports_job ON job_reports(job_id);
CREATE INDEX IF NOT EXISTS idx_reports_reporter_created ON job_reports(reporter_hash, created_at);
