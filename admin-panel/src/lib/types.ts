export interface JobItem {
  id: string;
  title: string;
  slug?: string;
  organization: string;
  post_name?: string;
  total_posts?: string;
  qualification?: string;
  age_limit?: string;
  salary?: string;
  application_fee?: string;
  start_date?: string;
  last_date?: string;
  exam_date?: string;
  category: 'Latest Job' | 'Admit Card' | 'Result' | 'Answer Key' | 'Syllabus';
  state: string;
  official_notification_url?: string;
  official_apply_url?: string;
  source_id?: string;
  source_domain?: string;
  advertisement_number?: string;
  status: 'pending_review' | 'ACTIVE' | 'EXPIRED' | 'REJECTED';
  verified: number;
  approved_by?: string;
  published_at?: string;
  created_at?: string;
  updated_at?: string;
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
  last_fetched_at?: string;
  last_successful_fetch_at?: string;
  last_error?: string;
  created_at?: string;
}

export interface ImportRun {
  id: string;
  source_id?: string;
  trigger_type: string;
  status: string;
  fetched_count: number;
  inserted_count: number;
  duplicate_count: number;
  error_count: number;
  started_at: string;
  finished_at?: string;
  error_summary?: string;
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
