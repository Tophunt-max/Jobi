-- ============================================================
-- Cloudflare D1 Database Schema for Sarkari Job Portal
-- ============================================================

DROP TABLE IF EXISTS jobs;
CREATE TABLE jobs (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    department TEXT NOT NULL,
    post_name TEXT,
    category TEXT NOT NULL, -- 'Latest Job', 'Admit Card', 'Result', 'Answer Key', 'Syllabus'
    total_posts INTEGER DEFAULT 0,
    qualification TEXT,
    age_limit TEXT,
    min_age INTEGER DEFAULT 18,
    max_age INTEGER DEFAULT 40,
    application_start_date TEXT,
    last_date TEXT,
    exam_date TEXT,
    result_date TEXT,
    fee_general TEXT,
    fee_sc_st TEXT,
    salary_scale TEXT,
    selection_process TEXT,
    official_url TEXT,
    apply_url TEXT,
    admit_card_url TEXT,
    result_url TEXT,
    notification_pdf_r2_key TEXT, -- Cloudflare R2 object key (e.g. notifications/ssc_cgl_2026.pdf)
    status TEXT DEFAULT 'ACTIVE', -- 'ACTIVE', 'EXPIRED', 'UPCOMING'
    is_featured INTEGER DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Indices for rapid edge queries
CREATE INDEX idx_jobs_category ON jobs(category);
CREATE INDEX idx_jobs_status ON jobs(status);
CREATE INDEX idx_jobs_last_date ON jobs(last_date);
CREATE INDEX idx_jobs_department ON jobs(department);

-- Initial seed data
INSERT INTO jobs (
    id, title, department, post_name, category, total_posts, qualification, 
    min_age, max_age, application_start_date, last_date, exam_date,
    fee_general, fee_sc_st, salary_scale, official_url, apply_url, status, is_featured
) VALUES 
(
    'job-ssc-cgl-2026',
    'SSC Combined Graduate Level (CGL) 2026',
    'Staff Selection Commission',
    'Assistant Audit Officer, Inspector, Sub Inspector',
    'Latest Job',
    14582,
    'Bachelor Degree in Any Stream from Recognized University',
    18, 32,
    '2026-06-15', '2026-07-24', 'September 2026',
    'Rs. 100/-', 'Nil (Exempted)', 'Level 4 to Level 8 (Rs. 25,500 - 1,51,100)',
    'https://ssc.gov.in', 'https://ssc.gov.in',
    'ACTIVE', 1
),
(
    'job-upsc-civil-2026',
    'UPSC Civil Services (IAS / IPS / IFS) 2026',
    'Union Public Service Commission',
    'IAS, IPS, IFS, IRS Officers',
    'Latest Job',
    1056,
    'Graduation Degree in any stream',
    21, 32,
    '2026-02-14', '2026-03-05', '2026-05-26',
    'Rs. 100/-', 'Nil', 'Rs. 56,100 - 2,50,000 (Level 10)',
    'https://upsc.gov.in', 'https://upsconline.nic.in',
    'ACTIVE', 1
),
(
    'admit-ibps-po-2026',
    'IBPS PO / MT XIV Mains Admit Card 2026',
    'Institute of Banking Personnel Selection',
    'Probationary Officer / Management Trainee',
    'Admit Card',
    3955,
    'Graduation in any discipline',
    20, 30,
    '2026-08-01', '2026-08-28', '2026-11-05',
    'Rs. 850/-', 'Rs. 175/-', 'Rs. 36,000 - 63,840',
    'https://ibps.in', 'https://ibps.in',
    'ACTIVE', 0
),
(
    'result-rrb-ntpc-2026',
    'RRB NTPC Graduate Level CBT-1 Result Declared',
    'Railway Recruitment Board',
    'Station Master, Goods Guard, Senior Clerk',
    'Result',
    8113,
    'Graduate Degree',
    18, 33,
    '2026-09-01', '2026-10-13', '2026-12-10',
    'Rs. 500/-', 'Rs. 250/-', 'Pay Level 5 & 6',
    'https://rrbcdg.gov.in', 'https://rrbcdg.gov.in',
    'ACTIVE', 0
);
