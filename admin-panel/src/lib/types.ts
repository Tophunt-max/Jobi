// Types mirror the D1 schema in backend-cloudflare/migrations.

export type JobCategory = 'Latest Job' | 'Admit Card' | 'Result' | 'Answer Key' | 'Syllabus' | 'Admission';
export type JobStatus = 'pending_review' | 'ACTIVE' | 'EXPIRED' | 'REJECTED';

export const JOB_CATEGORIES: JobCategory[] = ['Latest Job', 'Admit Card', 'Result', 'Answer Key', 'Syllabus', 'Admission'];

export interface JobItem {
  id: string;
  title: string;
  slug?: string | null;
  organization: string;
  department?: string | null;
  post_name?: string | null;
  category: JobCategory;
  state: string;
  total_posts?: number | null;
  qualification?: string | null;
  min_age?: number | null;
  max_age?: number | null;
  application_start_date?: string | null;
  last_date?: string | null;
  exam_date?: string | null;
  fee_general?: string | null;
  fee_sc_st?: string | null;
  salary_scale?: string | null;
  selection_process?: string | null;
  official_url?: string | null;
  official_notification_url?: string | null;
  official_apply_url?: string | null;
  source_id?: string | null;
  source_domain?: string | null;
  advertisement_number?: string | null;
  status: JobStatus;
  verified: number;
  approved_by?: string | null;
  approved_at?: string | null;
  rejection_reason?: string | null;
  published_at?: string | null;
  created_at?: string | null;
  updated_at?: string | null;
  is_featured?: number;
}

export interface ManualJobInput {
  title: string;
  organization: string;
  category: JobCategory;
  state?: string;
  department?: string;
  post_name?: string;
  total_posts?: number;
  qualification?: string;
  min_age?: number;
  max_age?: number;
  application_start_date?: string;
  last_date?: string;
  exam_date?: string;
  fee_general?: string;
  fee_sc_st?: string;
  salary_scale?: string;
  official_url?: string;
  official_notification_url?: string;
  official_apply_url?: string;
  advertisement_number?: string;
  is_featured?: boolean;
}

export interface JobSource {
  id: string;
  name: string;
  official_domain: string;
  listing_url: string;
  parser_type: string;
  state: string;
  enabled: number;
  polling_interval_hours?: number;
  last_fetched_at?: string | null;
  last_successful_fetch_at?: string | null;
  last_error?: string | null;
  created_at?: string;
}

export interface ImportRun {
  id: string;
  source_id?: string | null;
  triggered_by: string | null;
  status: 'SUCCESS' | 'PARTIAL' | 'FAILED' | 'RUNNING' | string;
  fetched_count: number;
  inserted_count: number;
  duplicate_count: number;
  error_count: number;
  started_at: string;
  finished_at?: string | null;
  error_summary?: string | null;
}

export interface ImportSummary {
  runId: string;
  status: string;
  sourcesProcessed: number;
  totalFetched: number;
  totalInserted: number;
  totalDuplicates: number;
  totalErrors: number;
  errors: string[];
}

export interface ScrapedNotice {
  title: string;
  category: JobCategory;
  official_notification_url: string;
  advertisement_number?: string | null;
}

export interface SourceTestResult {
  source: string;
  count: number;
  sample: ScrapedNotice[];
}

export interface DashboardStats {
  totalJobs: number;
  latestJobs: number;
  admitCards: number;
  results: number;
  pendingReview: number;
  activeSources: number;
}

export interface AdConfig {
  masterEnabled: boolean;
  bannerEnabled: boolean;
  interstitialEnabled: boolean;
  nativeEnabled: boolean;
  rewardedEnabled: boolean;
  bannerId: string;
  interstitialId: string;
  nativeId: string;
  rewardedId: string;
  interstitialClickInterval: number;
  interstitialCooldownSeconds: number;
  customSponsorActive: boolean;
  sponsorName: string;
  sponsorHeadline: string;
  sponsorBody: string;
  sponsorRating: string;
  sponsorCtaText: string;
  sponsorEmoji: string;
  sponsorTargetUrl: string;
  updatedAt?: string;
}

export interface JobReport {
  id: string;
  job_id: string;
  reason: string;
  details?: string | null;
  status: 'OPEN' | 'RESOLVED' | 'DISMISSED';
  created_at: string;
  resolved_at?: string | null;
  resolution_note?: string | null;
  job_title?: string | null;
  job_organization?: string | null;
  job_url?: string | null;
}

export type AdminRole = 'owner' | 'editor';

export interface SessionUser {
  email: string;
  role: AdminRole;
  kind?: 'session' | 'api_key';
}

export interface AdminUser {
  id: string;
  email: string;
  role: AdminRole;
  disabled: boolean;
  locked: boolean;
  last_login_at: string | null;
  created_at: string;
}
