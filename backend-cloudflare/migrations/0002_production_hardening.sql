-- Migration 0002: production hardening.
-- Adds columns the API now uses, database-enforced deduplication, query indexes,
-- and quarantines fabricated rows created by the old placeholder importer.

-- ---------- New columns ----------
ALTER TABLE jobs ADD COLUMN details_json TEXT;              -- category-specific extras (cut-off, exam pattern, ...)
ALTER TABLE jobs ADD COLUMN selection_process TEXT;
ALTER TABLE jobs ADD COLUMN notification_pdf_r2_key TEXT;   -- R2 object key for an uploaded notice PDF
ALTER TABLE jobs ADD COLUMN rejection_reason TEXT;
ALTER TABLE jobs ADD COLUMN reviewed_at TEXT;

ALTER TABLE job_notifications ADD COLUMN type TEXT DEFAULT 'NEW_JOB';
ALTER TABLE job_notifications ADD COLUMN target_slug TEXT;
ALTER TABLE job_notifications ADD COLUMN priority TEXT DEFAULT 'HIGH';

-- ---------- Data clean-up before unique indexes ----------
-- The old importer returned hardcoded, invented notices. Remove the ones that were
-- never approved; approved ones are kept for an admin to review manually.
DELETE FROM jobs WHERE id LIKE 'job-imp-%' AND status = 'pending_review';

-- The old schema.sql seeded illustrative jobs with non-existent PDF links straight
-- into ACTIVE. Move them back to the review queue so nothing unverified is public.
UPDATE jobs
SET status = 'pending_review', verified = 0
WHERE id IN (
    'job-ssc-cgl-2026', 'job-upsc-civil-2026', 'job-rrb-alp-2026', 'job-indiapost-gds-2026',
    'job-opsc-ocs-2026', 'job-ossc-cgl-2026', 'job-osssc-cre-2026', 'job-odishapolice-si-2026',
    'admit-ibps-po-2026', 'admit-ssc-chsl-2026', 'admit-opsc-ocs-prelims-2026',
    'result-rrb-ntpc-2026', 'result-upsc-civil-final-2026', 'result-ossc-cgl-prelims-2026',
    'anskey-ssc-cgl-tier1-2026', 'syllabus-upsc-cse-2026'
);

-- Rejections used to be written into approved_by as "Rejected: <reason>".
UPDATE jobs
SET rejection_reason = substr(approved_by, 11), approved_by = NULL
WHERE status = 'REJECTED' AND approved_by LIKE 'Rejected: %';

-- Blank strings must not collide under the unique indexes below.
UPDATE jobs SET official_notification_url = NULL WHERE trim(coalesce(official_notification_url, '')) = '';
UPDATE jobs SET content_hash = NULL WHERE trim(coalesce(content_hash, '')) = '';

-- If any duplicates already exist, keep the oldest row's value.
UPDATE jobs SET official_notification_url = NULL
WHERE official_notification_url IS NOT NULL
  AND rowid NOT IN (SELECT MIN(rowid) FROM jobs WHERE official_notification_url IS NOT NULL GROUP BY official_notification_url);
UPDATE jobs SET content_hash = NULL
WHERE content_hash IS NOT NULL
  AND rowid NOT IN (SELECT MIN(rowid) FROM jobs WHERE content_hash IS NOT NULL GROUP BY content_hash);

-- Any run left RUNNING by the old code can never finish.
UPDATE job_import_runs SET status = 'FAILED', error_summary = 'Interrupted (pre-migration)'
WHERE status = 'RUNNING';

-- ---------- Indexes ----------
DROP INDEX IF EXISTS idx_jobs_canonical_url;
DROP INDEX IF EXISTS idx_jobs_content_hash;
CREATE UNIQUE INDEX IF NOT EXISTS uq_jobs_notification_url ON jobs(official_notification_url) WHERE official_notification_url IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uq_jobs_content_hash ON jobs(content_hash) WHERE content_hash IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_jobs_status_published ON jobs(status, published_at DESC, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_jobs_status_category_published ON jobs(status, category, published_at DESC);
CREATE INDEX IF NOT EXISTS idx_jobs_pending_created ON jobs(status, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_notifications_created ON job_notifications(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notifications_job ON job_notifications(job_id);
CREATE INDEX IF NOT EXISTS idx_notifications_status ON job_notifications(status);

-- At most one RUNNING import at a time (single-flight lock).
CREATE UNIQUE INDEX IF NOT EXISTS uq_import_runs_single_running ON job_import_runs(status) WHERE status = 'RUNNING';
