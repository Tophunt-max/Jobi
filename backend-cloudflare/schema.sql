-- ============================================================
-- Cloudflare D1 Database Schema for Sarkari Job Portal
-- Automated Government Job Importer & Verification Pipeline
-- All India & Odisha Official Recruitment Sources
-- ============================================================

DROP TABLE IF EXISTS jobs;
CREATE TABLE jobs (
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
DROP TABLE IF EXISTS job_sources;
CREATE TABLE job_sources (
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
DROP TABLE IF EXISTS job_import_runs;
CREATE TABLE job_import_runs (
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
DROP TABLE IF EXISTS job_notifications;
CREATE TABLE job_notifications (
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
INSERT INTO job_sources (id, name, official_domain, listing_url, parser_type, state, enabled, polling_interval_hours) VALUES
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
-- Seed Sample Active & Verified Jobs across All Categories
-- ============================================================
INSERT INTO jobs (
    id, title, slug, organization, department, post_name, category, state, total_posts, qualification, 
    min_age, max_age, application_start_date, last_date, exam_date,
    fee_general, fee_sc_st, salary_scale, official_url, official_notification_url, official_apply_url, 
    source_id, source_domain, advertisement_number, status, verified, approved_by, approved_at, published_at, is_featured
) VALUES 
-- LATEST JOBS (ALL INDIA)
(
    'job-ssc-cgl-2026',
    'SSC Combined Graduate Level (CGL) 2026',
    'ssc-cgl-2026',
    'Staff Selection Commission',
    'Ministries & Departments of Govt of India',
    'Assistant Section Officer, Inspector, Sub Inspector, Auditor',
    'Latest Job',
    'All India',
    14850,
    'Bachelor Degree in Any Stream from Recognized University',
    18, 32,
    '2026-06-15', '2026-07-24', 'September 2026',
    'Rs. 100/-', 'Nil (Exempted)', 'Pay Level 4 to Level 8 (Rs. 25,500 - 1,51,100)',
    'https://ssc.gov.in', 'https://ssc.gov.in/notice_cgl_2026.pdf', 'https://ssc.gov.in',
    'src-ssc', 'ssc.gov.in', 'SSC/HQ/CGL-2026/01',
    'ACTIVE', 1, 'Chief Admin', '2026-06-15 10:00:00', '2026-06-15 10:05:00', 1
),
(
    'job-upsc-civil-2026',
    'UPSC Civil Services (IAS / IPS / IFS) 2026',
    'upsc-civil-services-2026',
    'Union Public Service Commission',
    'Department of Personnel & Training (DoPT)',
    'IAS, IPS, IFS, IRS Officers',
    'Latest Job',
    'All India',
    1120,
    'Graduation Degree in any stream from recognized university',
    21, 32,
    '2026-02-14', '2026-03-05', '2026-05-24',
    'Rs. 100/-', 'Nil', 'Rs. 56,100 - 2,50,000 (Level 10)',
    'https://upsc.gov.in', 'https://upsc.gov.in/ExamNotice_CSP_2026.pdf', 'https://upsconline.nic.in',
    'src-upsc', 'upsc.gov.in', '05/2026-CSP',
    'ACTIVE', 1, 'Chief Admin', '2026-02-14 09:00:00', '2026-02-14 09:10:00', 1
),
(
    'job-rrb-alp-2026',
    'RRB Assistant Loco Pilot (ALP) Recruitment 2026',
    'rrb-alp-2026',
    'Railway Recruitment Boards',
    'Ministry of Railways, Govt of India',
    'Assistant Loco Pilot (ALP)',
    'Latest Job',
    'All India',
    18799,
    '10th Pass plus ITI in relevant trades from NCVT/SCVT or Diploma in Engineering',
    18, 30,
    '2026-01-20', '2026-02-19', 'August 2026',
    'Rs. 500/- (Rs. 400 refundable)', 'Rs. 250/-', 'Level 2 (Rs. 19,900/- Initial Pay)',
    'https://indianrailways.gov.in', 'https://rrbcdg.gov.in/CEN_01_2026_ALP.pdf', 'https://www.rrbapply.gov.in',
    'src-rrb', 'indianrailways.gov.in', 'CEN 01/2026',
    'ACTIVE', 1, 'Chief Admin', '2026-01-20 10:00:00', '2026-01-20 10:15:00', 1
),
(
    'job-indiapost-gds-2026',
    'India Post Gramin Dak Sevaks (GDS) Cycle VII Recruitment',
    'indiapost-gds-2026',
    'Department of Posts, Ministry of Communications',
    'All 23 Postal Circles across India',
    'Branch Postmaster (BPM) & Assistant Branch Postmaster (ABPM)',
    'Latest Job',
    'All India',
    44228,
    'Secondary School Examination (10th standard pass) with Mathematics & English',
    18, 40,
    '2026-07-15', '2026-08-05', 'Merit Based (No Exam)',
    'Rs. 100/-', 'Nil (Female/SC/ST Exempted)', 'TRCA Slab: Rs. 10,000 - 29,380',
    'https://indiapostgdsonline.gov.in', 'https://indiapostgdsonline.gov.in/GDS_Notification_2026.pdf', 'https://indiapostgdsonline.gov.in',
    'src-indiapost', 'indiapostgdsonline.gov.in', 'No. 17-21/2026-GDS',
    'ACTIVE', 1, 'Chief Admin', '2026-07-15 08:30:00', '2026-07-15 08:45:00', 1
),

-- LATEST JOBS (ODISHA)
(
    'job-opsc-ocs-2026',
    'Odisha Civil Services (OCS) Examination 2026',
    'opsc-ocs-2026',
    'Odisha Public Service Commission (OPSC)',
    'General Administration & Public Grievance Dept, Govt of Odisha',
    'OAS Group-A, OFS Group-A, OPS Group-A',
    'Latest Job',
    'Odisha',
    399,
    'Bachelor Degree in any discipline with Odia language proficiency',
    21, 38,
    '2026-03-01', '2026-04-15', 'August 2026',
    'Nil (All Categories Exempted as per Odisha Govt Rules)', 'Nil', 'Pay Matrix Level 12 (Rs. 56,100 - Rs. 1,77,500)',
    'https://www.opsc.gov.in', 'https://www.opsc.gov.in/notices/ocs_2026.pdf', 'https://opsconline.gov.in',
    'src-opsc', 'opsc.gov.in', 'Advt. No. 25 of 2025-26',
    'ACTIVE', 1, 'Chief Admin', '2026-03-01 11:00:00', '2026-03-01 11:30:00', 1
),
(
    'job-ossc-cgl-2026',
    'OSSC Combined Graduate Level Recruitment Examination (CGLRE)',
    'ossc-cgl-2026',
    'Odisha Staff Selection Commission (OSSC)',
    'Various Departments & Heads of Department, Odisha',
    'Auditor, Inspector of Supplies, Junior Fishery Technical Assistant',
    'Latest Job',
    'Odisha',
    695,
    'Graduate in Arts/Science/Commerce with basic computer knowledge',
    21, 38,
    '2026-04-10', '2026-05-20', 'July 2026',
    'Nil (No examination fee for any category)', 'Nil', 'Pay Level 9 (Rs. 35,400 - Rs. 1,12,400)',
    'https://www.ossc.gov.in', 'https://www.ossc.gov.in/cgl_notices.pdf', 'https://www.ossc.gov.in',
    'src-ossc', 'ossc.gov.in', 'IIE-64/2026/OSSC',
    'ACTIVE', 1, 'Chief Admin', '2026-04-10 14:00:00', '2026-04-10 14:15:00', 0
),
(
    'job-osssc-cre-2026',
    'OSSSC Combined Recruitment Examination (RI, ARI, Amin, Forest Guard)',
    'osssc-cre-2026',
    'Odisha Sub-ordinate Staff Selection Commission',
    'Revenue & Disaster Management Dept, Odisha',
    'Revenue Inspector (RI), Assistant Revenue Inspector (ARI), Amin',
    'Latest Job',
    'Odisha',
    2895,
    'Higher Secondary (10+2) / Bachelor Degree with Odia proficiency',
    18, 38,
    '2026-05-01', '2026-06-10', 'September 2026',
    'Nil (Exempted)', 'Nil', 'Pay Scale Rs. 19,900 - 63,200 (Level 4/9)',
    'https://www.osssc.gov.in', 'https://www.osssc.gov.in/notice_cre.pdf', 'https://www.osssc.gov.in',
    'src-osssc', 'osssc.gov.in', 'IIE-28/2026-114/OSSSC',
    'ACTIVE', 1, 'Chief Admin', '2026-05-01 09:30:00', '2026-05-01 09:45:00', 1
),
(
    'job-odishapolice-si-2026',
    'Odisha Police Sub Inspector & Constable Recruitment 2026',
    'odishapolice-si-constable-2026',
    'State Police Selection Board, Odisha',
    'Home Department, Government of Odisha',
    'Sub Inspector of Police & Civil Constables',
    'Latest Job',
    'Odisha',
    1920,
    'Graduate for SI, 10+2 Pass for Constable with Physical Fitness Tests',
    18, 28,
    '2026-06-01', '2026-07-05', 'November 2026',
    'Nil (Exempted for all)', 'Nil', 'Pay Level 9 (SI) / Pay Level 3 (Constable)',
    'https://odishapolice.gov.in', 'https://odishapolice.gov.in/si_recruitment_2026.pdf', 'https://odishapolice.gov.in',
    'src-odishapolice', 'odishapolice.gov.in', 'OP-SSB/RECT/2026/02',
    'ACTIVE', 1, 'Chief Admin', '2026-06-01 10:00:00', '2026-06-01 10:10:00', 1
),

-- ADMIT CARDS
(
    'admit-ibps-po-2026',
    'IBPS PO / MT XIV Mains Admit Card 2026',
    'ibps-po-mains-admit-card-2026',
    'Institute of Banking Personnel Selection',
    'Participating Public Sector Banks',
    'Probationary Officer / Management Trainee',
    'Admit Card',
    'All India',
    3955,
    'Graduation in any discipline',
    20, 30,
    '2026-08-01', '2026-08-28', '2026-11-05',
    'Rs. 850/-', 'Rs. 175/-', 'Rs. 36,000 - 63,840',
    'https://ibps.in', 'https://ibps.in/crp-po-xiv.pdf', 'https://ibps.in',
    'src-ibps', 'ibps.in', 'CRP/PO/MT-XIV',
    'ACTIVE', 1, 'Chief Admin', '2026-08-01 10:00:00', '2026-08-01 10:00:00', 0
),
(
    'admit-ssc-chsl-2026',
    'SSC CHSL (10+2) Tier-1 Examination Admit Card 2026',
    'ssc-chsl-tier1-admit-card-2026',
    'Staff Selection Commission',
    'Regional Directorates (ER, NR, CR, WR, SR)',
    'Lower Division Clerk, Data Entry Operator',
    'Admit Card',
    'All India',
    3712,
    '12th Standard Pass',
    18, 27,
    '2026-04-02', '2026-05-01', 'July 2026',
    'Rs. 100/-', 'Nil', 'Pay Level 2 & 4',
    'https://ssc.gov.in', 'https://ssc.gov.in/chsl_admit_card_tier1.pdf', 'https://ssc.gov.in',
    'src-ssc', 'ssc.gov.in', 'HQ-C1201/1/2026-C2',
    'ACTIVE', 1, 'Chief Admin', '2026-07-01 09:00:00', '2026-07-01 09:00:00', 0
),
(
    'admit-opsc-ocs-prelims-2026',
    'OPSC Odisha Civil Services Preliminary Exam Admit Card',
    'opsc-ocs-prelims-admit-card-2026',
    'Odisha Public Service Commission',
    'General Administration Department, Odisha',
    'Odisha Administrative Service (OAS)',
    'Admit Card',
    'Odisha',
    399,
    'Graduate Degree',
    21, 38,
    '2026-03-01', '2026-04-15', 'August 2026',
    'Nil', 'Nil', 'Level 12',
    'https://www.opsc.gov.in', 'https://www.opsc.gov.in/hallticket_ocs.pdf', 'https://opsconline.gov.in',
    'src-opsc', 'opsc.gov.in', 'Advt. 25 of 2025-26',
    'ACTIVE', 1, 'Chief Admin', '2026-07-15 10:00:00', '2026-07-15 10:00:00', 0
),

-- RESULTS
(
    'result-rrb-ntpc-2026',
    'RRB NTPC Graduate Level CBT-1 Result Declared',
    'rrb-ntpc-cbt1-result-2026',
    'Railway Recruitment Board',
    'Ministry of Railways, Govt of India',
    'Station Master, Goods Guard, Senior Clerk',
    'Result',
    'All India',
    8113,
    'Graduate Degree',
    18, 33,
    '2026-09-01', '2026-10-13', '2026-12-10',
    'Rs. 500/-', 'Rs. 250/-', 'Pay Level 5 & 6',
    'https://indianrailways.gov.in', 'https://rrbcdg.gov.in/notice_result.pdf', 'https://rrbcdg.gov.in',
    'src-rrb', 'indianrailways.gov.in', 'CEN-01/2026-NTPC',
    'ACTIVE', 1, 'Chief Admin', '2026-09-01 12:00:00', '2026-09-01 12:00:00', 0
),
(
    'result-upsc-civil-final-2026',
    'UPSC Civil Services Examination Final Merit List & Cutoff',
    'upsc-cse-final-result-2026',
    'Union Public Service Commission',
    'Department of Personnel & Training',
    'IAS, IPS, IFS, Group A & B Officers',
    'Result',
    'All India',
    1016,
    'Graduation in any discipline',
    21, 32,
    '2025-02-14', '2025-03-05', 'Final Interviews Over',
    'Rs. 100/-', 'Nil', 'Level 10 Pay Matrix',
    'https://upsc.gov.in', 'https://upsc.gov.in/FinalResult_CSE_2026.pdf', 'https://upsc.gov.in',
    'src-upsc', 'upsc.gov.in', '05/2025-CSP-Final',
    'ACTIVE', 1, 'Chief Admin', '2026-05-10 16:00:00', '2026-05-10 16:00:00', 0
),
(
    'result-ossc-cgl-prelims-2026',
    'OSSC CGLRE 2026 Preliminary Examination Scorecard & Cutoff',
    'ossc-cglre-prelims-result-2026',
    'Odisha Staff Selection Commission',
    'Various Departments, Government of Odisha',
    'Auditor, Inspector, Junior Fishery Assistant',
    'Result',
    'Odisha',
    695,
    'Bachelor Degree in any discipline',
    21, 38,
    '2026-04-10', '2026-05-20', 'July 2026',
    'Nil', 'Nil', 'Pay Level 9',
    'https://www.ossc.gov.in', 'https://www.ossc.gov.in/cgl_prelims_result_2026.pdf', 'https://www.ossc.gov.in',
    'src-ossc', 'ossc.gov.in', 'IIE-64/2026/OSSC',
    'ACTIVE', 1, 'Chief Admin', '2026-08-20 15:30:00', '2026-08-20 15:30:00', 0
),

-- ANSWER KEYS
(
    'anskey-ssc-cgl-tier1-2026',
    'SSC CGL 2026 Tier-1 Provisional Answer Key with Candidate Response Sheets',
    'ssc-cgl-tier1-answer-key-2026',
    'Staff Selection Commission',
    'Government of India',
    'Combined Graduate Level Examination',
    'Answer Key',
    'All India',
    14850,
    'Graduate Degree',
    18, 32,
    '2026-06-15', '2026-07-24', 'September 2026',
    'Rs. 100/- per challenge', 'Rs. 100/- per challenge', 'Pay Level 4 - 8',
    'https://ssc.gov.in', 'https://ssc.gov.in/cgl_answer_key_tier1.pdf', 'https://ssc.gov.in',
    'src-ssc', 'ssc.gov.in', 'SSC/HQ/CGL-2026/AK',
    'ACTIVE', 1, 'Chief Admin', '2026-09-25 18:00:00', '2026-09-25 18:00:00', 0
),

-- SYLLABUS
(
    'syllabus-upsc-cse-2026',
    'UPSC Civil Services Examination (CSE) Detailed Prelims & Mains Syllabus',
    'upsc-cse-detailed-syllabus-2026',
    'Union Public Service Commission',
    'DoPT, Government of India',
    'Civil Services (Prelims & Mains GS 1-4 + Essay)',
    'Syllabus',
    'All India',
    1120,
    'Degree in any stream',
    21, 32,
    '2026-02-14', '2026-03-05', '2026-05-24',
    'Nil', 'Nil', 'Level 10',
    'https://upsc.gov.in', 'https://upsc.gov.in/CSE_Syllabus_Scheme_2026.pdf', 'https://upsc.gov.in',
    'src-upsc', 'upsc.gov.in', 'UPSC-SYLLABUS-2026',
    'ACTIVE', 1, 'Chief Admin', '2026-02-14 09:00:00', '2026-02-14 09:00:00', 0
);
