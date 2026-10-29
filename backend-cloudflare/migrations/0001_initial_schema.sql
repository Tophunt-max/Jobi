-- Migration 0001: initial schema (idempotent).
-- Safe to apply on a database that was previously created from the old schema.sql:
-- every statement uses IF NOT EXISTS / INSERT OR IGNORE and nothing is dropped.

CREATE TABLE IF NOT EXISTS jobs (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    slug TEXT UNIQUE,
    organization TEXT NOT NULL,
    department TEXT,
    post_name TEXT,
    category TEXT NOT NULL, -- 'Latest Job', 'Admit Card', 'Result', 'Answer Key', 'Syllabus', 'Admission'
    state TEXT DEFAULT 'All India', -- 'All India', 'Odisha', etc.
    total_posts INTEGER DEFAULT 0,
    qualification TEXT,
    min_age INTEGER,
    max_age INTEGER,
    application_start_date TEXT,
    last_date TEXT,
    exam_date TEXT,
    admit_card_date TEXT,
    result_date TEXT,
    answer_key_date TEXT,
    fee_general TEXT,
    fee_sc_st TEXT,
    salary_scale TEXT,
    official_url TEXT,
    official_notification_url TEXT,
    official_apply_url TEXT,
    source_id TEXT,
    source_domain TEXT,
    advertisement_number TEXT,
    source_published_at TEXT,
    fetched_at TEXT,
    status TEXT DEFAULT 'ACTIVE', -- 'ACTIVE', 'pending_review', 'REJECTED', 'EXPIRED'
    verified INTEGER DEFAULT 1,
    content_hash TEXT,
    approved_by TEXT,
    approved_at TEXT,
    published_at TEXT,
    is_featured INTEGER DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Indices for rapid queries, category filtering & deduplication
CREATE INDEX IF NOT EXISTS idx_jobs_category ON jobs(category);
CREATE INDEX IF NOT EXISTS idx_jobs_status ON jobs(status);
CREATE INDEX IF NOT EXISTS idx_jobs_state ON jobs(state);
CREATE INDEX IF NOT EXISTS idx_jobs_last_date ON jobs(last_date);
CREATE INDEX IF NOT EXISTS idx_jobs_source_advt ON jobs(source_id, advertisement_number);
CREATE INDEX IF NOT EXISTS idx_jobs_content_hash ON jobs(content_hash);
CREATE INDEX IF NOT EXISTS idx_jobs_canonical_url ON jobs(official_notification_url);
CREATE INDEX IF NOT EXISTS idx_jobs_published ON jobs(published_at);

-- ============================================================
-- Official Job Sources Registry Table
-- Supports All India & Odisha verified sources
-- ============================================================
CREATE TABLE IF NOT EXISTS job_sources (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    official_domain TEXT NOT NULL,
    listing_url TEXT NOT NULL,
    parser_type TEXT NOT NULL, -- 'API', 'RSS', 'HTML_TABLE', 'STATIC_REGISTRY'
    state TEXT DEFAULT 'All India', -- 'All India', 'Odisha', etc.
    enabled INTEGER DEFAULT 1,
    polling_interval_hours INTEGER DEFAULT 6,
    last_fetched_at TEXT,
    last_successful_fetch_at TEXT,
    last_error TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- Import Execution Runs Audit Table
-- ============================================================
CREATE TABLE IF NOT EXISTS job_import_runs (
    id TEXT PRIMARY KEY,
    source_id TEXT,
    started_at TEXT NOT NULL,
    finished_at TEXT,
    fetched_count INTEGER DEFAULT 0,
    inserted_count INTEGER DEFAULT 0,
    duplicate_count INTEGER DEFAULT 0,
    error_count INTEGER DEFAULT 0,
    status TEXT DEFAULT 'RUNNING', -- 'SUCCESS', 'PARTIAL', 'FAILED', 'RUNNING'
    error_summary TEXT,
    triggered_by TEXT DEFAULT 'CRON_TRIGGER' -- 'CRON_TRIGGER', 'ADMIN_MANUAL'
);

CREATE INDEX IF NOT EXISTS idx_import_runs_source ON job_import_runs(source_id);
CREATE INDEX IF NOT EXISTS idx_import_runs_started ON job_import_runs(started_at);

-- ============================================================
-- FCM Push Notifications Queue Table
-- ============================================================
CREATE TABLE IF NOT EXISTS job_notifications (
    id TEXT PRIMARY KEY,
    job_id TEXT NOT NULL,
    title TEXT NOT NULL,
    body TEXT NOT NULL,
    status TEXT DEFAULT 'PENDING', -- 'PENDING', 'SENT', 'FAILED'
    attempt_count INTEGER DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    sent_at TEXT,
    last_error TEXT
);

-- ============================================================
-- Seed Official Sources Registry (All India + Odisha)
-- Over 25+ verified government recruitment authorities
-- ============================================================
INSERT OR IGNORE INTO job_sources (id, name, official_domain, listing_url, parser_type, state, enabled, polling_interval_hours) VALUES
-- ALL INDIA CENTRAL AUTHORITIES
('src-ssc', 'Staff Selection Commission (SSC)', 'ssc.gov.in', 'https://ssc.gov.in/', 'API', 'All India', 1, 6),
('src-upsc', 'Union Public Service Commission (UPSC)', 'upsc.gov.in', 'https://www.upsc.gov.in/', 'HTML_TABLE', 'All India', 1, 6),
('src-rrb', 'Railway Recruitment Control Board (RRB)', 'indianrailways.gov.in', 'https://indianrailways.gov.in/', 'HTML_TABLE', 'All India', 1, 6),
('src-ibps', 'Institute of Banking Personnel Selection (IBPS)', 'ibps.in', 'https://www.ibps.in/', 'HTML_TABLE', 'All India', 1, 6),
('src-sbi', 'State Bank of India Careers (SBI)', 'sbi.co.in', 'https://sbi.co.in/web/careers', 'API', 'All India', 1, 6),
('src-rbi', 'Reserve Bank of India Opportunities (RBI)', 'rbi.org.in', 'https://opportunities.rbi.org.in/', 'HTML_TABLE', 'All India', 1, 12),
('src-nabard', 'National Bank for Agriculture and Rural Development (NABARD)', 'nabard.org', 'https://www.nabard.org/careers-notices.aspx', 'HTML_TABLE', 'All India', 1, 12),
('src-kvs', 'Kendriya Vidyalaya Sangathan (KVS)', 'kvsangathan.nic.in', 'https://kvsangathan.nic.in/employment-notice', 'HTML_TABLE', 'All India', 1, 12),
('src-nvs', 'Navodaya Vidyalaya Samiti (NVS)', 'navodaya.gov.in', 'https://navodaya.gov.in/nvs/en/Recruitment/Recruitment-Notices/', 'HTML_TABLE', 'All India', 1, 12),
('src-indiapost', 'India Post GDS Recruitment Portal', 'indiapostgdsonline.gov.in', 'https://indiapostgdsonline.gov.in/', 'API', 'All India', 1, 6),
('src-drdo', 'Defence Research & Development Organisation (DRDO / CEPTAM)', 'drdo.gov.in', 'https://www.drdo.gov.in/careers', 'HTML_TABLE', 'All India', 1, 12),
('src-isro', 'Indian Space Research Organisation (ISRO / ICRB)', 'isro.gov.in', 'https://www.isro.gov.in/Careers.html', 'HTML_TABLE', 'All India', 1, 12),
('src-lic', 'Life Insurance Corporation of India (LIC Careers)', 'licindia.in', 'https://licindia.in/careers', 'HTML_TABLE', 'All India', 1, 12),
('src-dsssb', 'Delhi Subordinate Services Selection Board (DSSSB)', 'dsssb.delhi.gov.in', 'https://dsssb.delhi.gov.in/current-vacancies', 'HTML_TABLE', 'All India', 1, 12),
('src-iaf', 'Indian Air Force Careers (AFCAT & Agniveervayu)', 'careerindianairforce.cdac.in', 'https://careerindianairforce.cdac.in/', 'HTML_TABLE', 'All India', 1, 6),
('src-army', 'Join Indian Army Official Recruitment', 'joinindianarmy.nic.in', 'https://joinindianarmy.nic.in/', 'HTML_TABLE', 'All India', 1, 6),
('src-navy', 'Join Indian Navy Careers (Nausena Bharti)', 'joinindiannavy.gov.in', 'https://www.joinindiannavy.gov.in/', 'HTML_TABLE', 'All India', 1, 6),
('src-capf', 'Central Armed Police Forces (BSF/CISF/CRPF/ITBP/SSB)', 'recruitment.itbpolice.nic.in', 'https://recruitment.itbpolice.nic.in/', 'HTML_TABLE', 'All India', 1, 6),
('src-fci', 'Food Corporation of India (FCI Recruitment)', 'fci.gov.in', 'https://fci.gov.in/current-vacancies.php', 'HTML_TABLE', 'All India', 1, 12),
('src-nta', 'National Testing Agency (NTA Exam Portals)', 'nta.ac.in', 'https://nta.ac.in/', 'HTML_TABLE', 'All India', 1, 6),

-- ODISHA STATE AUTHORITIES
('src-opsc', 'Odisha Public Service Commission (OPSC)', 'opsc.gov.in', 'https://www.opsc.gov.in/', 'HTML_TABLE', 'Odisha', 1, 6),
('src-ossc', 'Odisha Staff Selection Commission (OSSC)', 'ossc.gov.in', 'https://www.ossc.gov.in/', 'HTML_TABLE', 'Odisha', 1, 6),
('src-osssc', 'Odisha Sub-ordinate Staff Selection Commission (OSSSC)', 'osssc.gov.in', 'https://www.osssc.gov.in/', 'HTML_TABLE', 'Odisha', 1, 6),
('src-odishapolice', 'Odisha Police State Selection Board', 'odishapolice.gov.in', 'https://odishapolice.gov.in/recruitment', 'HTML_TABLE', 'Odisha', 1, 6),
('src-ojee', 'Odisha Joint Entrance Examination (OJEE)', 'ojee.nic.in', 'https://ojee.nic.in/', 'HTML_TABLE', 'Odisha', 1, 12),
('src-chseodisha', 'Council of Higher Secondary Education (CHSE Odisha)', 'chseodisha.nic.in', 'https://chseodisha.nic.in/', 'HTML_TABLE', 'Odisha', 1, 12),
('src-bseodisha', 'Board of Secondary Education Odisha (OTET / BSE)', 'bseodisha.ac.in', 'https://www.bseodisha.ac.in/', 'HTML_TABLE', 'Odisha', 1, 12),
('src-optcl', 'Odisha Power Transmission Corporation Ltd (OPTCL)', 'optcl.co.in', 'https://www.optcl.co.in/Career.aspx', 'HTML_TABLE', 'Odisha', 1, 12),
('src-dnhodisha', 'Health & Family Welfare / OSMCL Odisha', 'dnhodisha.gov.in', 'https://health.odisha.gov.in/', 'HTML_TABLE', 'Odisha', 1, 12),
('src-ohpc', 'Odisha Hydro Power Corporation / GRIDCO', 'ohpcltd.com', 'https://www.ohpcltd.com/recruitment/', 'HTML_TABLE', 'Odisha', 1, 12);

-- ============================================================
-- Application Settings & Monetization Configuration Table
-- ============================================================
CREATE TABLE IF NOT EXISTS app_settings (
    key TEXT PRIMARY KEY,
    value TEXT NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Seed default safe ad settings
INSERT OR IGNORE INTO app_settings (key, value) VALUES (
    'ad_config',
    '{"masterEnabled":true,"bannerEnabled":true,"interstitialEnabled":true,"nativeEnabled":true,"rewardedEnabled":true,"bannerId":"","interstitialId":"","nativeId":"","rewardedId":"","interstitialClickInterval":3,"interstitialCooldownSeconds":60,"customSponsorActive":false,"sponsorName":"Sarkari Pariksha Partner","sponsorHeadline":"🎯 All India Mock Test Series 2026","sponsorBody":"500+ Sectional & Full-length tests with live All-India rank predictor & video solutions.","sponsorRating":"4.9 ★","sponsorCtaText":"Start Free Test","sponsorEmoji":"🎯","sponsorTargetUrl":"https://play.google.com/store/search?q=sarkari+mock+test&c=apps"}'
);
