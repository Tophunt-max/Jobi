import { AdConfig, DashboardStats, ImportRun, ImportSummary, JobItem, JobReport, JobSource, ManualJobInput, SourceTestResult } from './types';

/**
 * Admin API client.
 *
 * Security model:
 * - The admin key is NEVER bundled into the JavaScript (no NEXT_PUBLIC_* key, no default).
 * - The admin types it on the login screen; it is kept in sessionStorage only, so it
 *   disappears when the browser tab is closed.
 * - Only the Worker URL may come from build-time config (NEXT_PUBLIC_API_URL) since it is public.
 * - Protect the deployed panel itself with Cloudflare Access (see README).
 */

const URL_STORAGE_KEY = 'sarkari_admin_api_url';
const KEY_STORAGE_KEY = 'sarkari_admin_session_key';

export class ApiError extends Error {
  constructor(public status: number, message: string, public code?: string) {
    super(message);
  }
}

function defaultBaseUrl(): string {
  return (process.env.NEXT_PUBLIC_API_URL || '').replace(/\/+$/, '');
}

export function getBaseUrl(): string {
  if (typeof window === 'undefined') return defaultBaseUrl();
  return (localStorage.getItem(URL_STORAGE_KEY) || defaultBaseUrl()).replace(/\/+$/, '');
}

export function setBaseUrl(url: string) {
  const clean = url.trim().replace(/\/+$/, '');
  const parsed = new URL(clean);
  const isLocal = parsed.hostname === 'localhost' || parsed.hostname === '127.0.0.1';
  if (parsed.protocol !== 'https:' && !isLocal) {
    throw new Error('Worker URL must use https://');
  }
  localStorage.setItem(URL_STORAGE_KEY, clean);
}

export function getSessionKey(): string | null {
  if (typeof window === 'undefined') return null;
  return sessionStorage.getItem(KEY_STORAGE_KEY);
}

export function setSessionKey(key: string) {
  sessionStorage.setItem(KEY_STORAGE_KEY, key.trim());
}

export function clearSession() {
  if (typeof window === 'undefined') return;
  sessionStorage.removeItem(KEY_STORAGE_KEY);
  window.dispatchEvent(new Event('admin-session-changed'));
}

async function request<T>(endpoint: string, options: RequestInit & { auth?: boolean } = {}): Promise<T> {
  const baseUrl = getBaseUrl();
  if (!baseUrl) throw new ApiError(0, 'Worker URL is not configured. Set it on the login screen.');

  const { auth = true, ...init } = options;
  const headers = new Headers(init.headers || {});
  if (init.body && !(init.body instanceof Blob) && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }
  if (auth) {
    const key = getSessionKey();
    if (!key) {
      clearSession();
      throw new ApiError(401, 'Not signed in');
    }
    headers.set('Authorization', `Bearer ${key}`);
  }

  const res = await fetch(`${baseUrl}${endpoint}`, { ...init, headers, cache: 'no-store' });
  const text = await res.text();
  let body: any = null;
  try {
    body = text ? JSON.parse(text) : null;
  } catch {
    body = null;
  }

  if (!res.ok || (body && body.success === false)) {
    if (res.status === 401 && auth) clearSession();
    const message = body?.message || `Request failed (HTTP ${res.status})`;
    throw new ApiError(res.status, message, body?.error);
  }
  return body as T;
}

const enc = encodeURIComponent;

export const adminApi = {
  /** Verifies a key without storing it. */
  async verifyKey(baseUrl: string, key: string): Promise<void> {
    const res = await fetch(`${baseUrl.replace(/\/+$/, '')}/api/admin/jobs/pending?limit=1`, {
      headers: { Authorization: `Bearer ${key.trim()}` },
      cache: 'no-store',
    });
    if (res.status === 401) throw new ApiError(401, 'Invalid admin key');
    if (res.status === 503) throw new ApiError(503, 'Admin API is not configured on the Worker (ADMIN_API_KEY secret missing)');
    if (!res.ok) throw new ApiError(res.status, `Worker responded with HTTP ${res.status}`);
  },

  async getStats(): Promise<DashboardStats> {
    const res = await request<{ data: DashboardStats }>('/api/stats', { auth: false });
    return res.data;
  },

  async getHealth(): Promise<{ status: string; database: string; storage: string; timestamp: string }> {
    return request('/api/health', { auth: false });
  },

  async getPendingJobs(limit = 100): Promise<JobItem[]> {
    const res = await request<{ data: JobItem[] }>(`/api/admin/jobs/pending?limit=${limit}`);
    return res.data || [];
  },

  async approveJob(id: string, approvedBy = 'admin-panel'): Promise<{ message: string }> {
    return request(`/api/admin/jobs/${enc(id)}/approve`, {
      method: 'POST',
      body: JSON.stringify({ approved_by: approvedBy }),
    });
  },

  /** Approves up to 100 jobs per call; larger selections are chunked. */
  async batchApproveJobs(ids: string[], approvedBy = 'admin-panel'): Promise<{ approvedCount: number }> {
    let approvedCount = 0;
    for (let i = 0; i < ids.length; i += 100) {
      const res = await request<{ approvedCount: number }>('/api/admin/jobs/batch-approve', {
        method: 'POST',
        body: JSON.stringify({ ids: ids.slice(i, i + 100), approved_by: approvedBy }),
      });
      approvedCount += res.approvedCount || 0;
    }
    return { approvedCount };
  },

  async rejectJob(id: string, reason: string): Promise<{ message: string }> {
    return request(`/api/admin/jobs/${enc(id)}/reject`, {
      method: 'POST',
      body: JSON.stringify({ reason }),
    });
  },

  async getSources(): Promise<JobSource[]> {
    const res = await request<{ sources: JobSource[] }>('/api/admin/job-import/sources');
    return res.sources || [];
  },

  async updateSource(id: string, enabled: boolean, intervalHours?: number): Promise<{ message: string }> {
    return request('/api/admin/job-import/sources', {
      method: 'POST',
      body: JSON.stringify({ id, enabled, polling_interval_hours: intervalHours ?? null }),
    });
  },

  async runImport(sourceId?: string): Promise<{ summary: ImportSummary }> {
    return request('/api/admin/job-import/run', {
      method: 'POST',
      body: JSON.stringify(sourceId ? { source_id: sourceId } : {}),
    });
  },

  async testSource(sourceId: string): Promise<SourceTestResult> {
    return request(`/api/admin/job-import/sources/${enc(sourceId)}/test`, { method: 'POST' });
  },

  async getImportRuns(limit = 50): Promise<ImportRun[]> {
    const res = await request<{ runs: ImportRun[] }>(`/api/admin/job-import/runs?limit=${limit}`);
    return res.runs || [];
  },

  async createJob(job: ManualJobInput): Promise<{ message: string; jobId: string; slug: string }> {
    return request('/api/admin/jobs', { method: 'POST', body: JSON.stringify(job) });
  },

  async getAllJobs(params?: { status?: string; category?: string; limit?: number }): Promise<JobItem[]> {
    const q = new URLSearchParams();
    if (params?.status) q.set('status', params.status);
    if (params?.category) q.set('category', params.category);
    q.set('limit', String(params?.limit ?? 100));
    const res = await request<{ data: JobItem[] }>(`/api/admin/jobs?${q.toString()}`);
    return res.data || [];
  },

  async getReports(status: 'OPEN' | 'RESOLVED' | 'DISMISSED' | 'ALL' = 'OPEN'): Promise<JobReport[]> {
    const res = await request<{ data: JobReport[] }>(`/api/admin/reports?status=${status}&limit=200`);
    return res.data || [];
  },

  async resolveReport(id: string, status: 'RESOLVED' | 'DISMISSED', note?: string): Promise<{ message: string }> {
    return request(`/api/admin/reports/${enc(id)}/resolve`, {
      method: 'POST',
      body: JSON.stringify({ status, note }),
    });
  },

  async getAdConfig(): Promise<AdConfig> {
    const res = await request<{ data: AdConfig }>('/api/admin/ads');
    return res.data;
  },

  async saveAdConfig(config: AdConfig): Promise<{ message: string; data: AdConfig }> {
    const { updatedAt, ...payload } = config;
    return request('/api/admin/ads', { method: 'POST', body: JSON.stringify(payload) });
  },
};
