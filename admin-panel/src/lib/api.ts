import {
  AdConfig, AdminUser, DashboardStats, ImportRun, ImportSummary, JobItem, JobReport, JobSource,
  ManualJobInput, SessionUser, SourceTestResult,
} from './types';

/**
 * Admin API client.
 *
 * Auth: email + password sign-in creates a server-side session. The browser keeps only an
 * HttpOnly, Secure, SameSite=Strict cookie that JavaScript cannot read. No keys or tokens
 * are stored in the bundle, localStorage or sessionStorage.
 *
 * In production the panel calls its own origin (/api/* is proxied to the backend Worker).
 * For local development, NEXT_PUBLIC_API_URL can point at `wrangler dev`.
 */

const BASE = (process.env.NEXT_PUBLIC_API_URL || '').replace(/\/+$/, '');

export class ApiError extends Error {
  constructor(public status: number, message: string, public code?: string) {
    super(message);
  }
}

export const SESSION_EVENT = 'admin-session-changed';

function notifySignedOut() {
  if (typeof window !== 'undefined') window.dispatchEvent(new Event(SESSION_EVENT));
}

async function request<T>(endpoint: string, init: RequestInit & { quiet401?: boolean } = {}): Promise<T> {
  const { quiet401, ...options } = init;
  const headers = new Headers(options.headers || {});
  if (options.body && !headers.has('Content-Type')) headers.set('Content-Type', 'application/json');

  let res: Response;
  try {
    res = await fetch(`${BASE}${endpoint}`, { ...options, headers, credentials: 'include', cache: 'no-store' });
  } catch {
    throw new ApiError(0, 'Network error. Check your connection.');
  }

  const text = await res.text();
  let body: any = null;
  try {
    body = text ? JSON.parse(text) : null;
  } catch {
    body = null;
  }

  if (!res.ok || (body && body.success === false)) {
    if (res.status === 401 && !quiet401) notifySignedOut();
    throw new ApiError(res.status, body?.message || `Request failed (HTTP ${res.status})`, body?.error);
  }
  return body as T;
}

const enc = encodeURIComponent;
const post = (body?: unknown): RequestInit => ({ method: 'POST', body: body === undefined ? undefined : JSON.stringify(body) });

export const authApi = {
  async me(): Promise<SessionUser | null> {
    try {
      const res = await request<{ user: SessionUser }>('/api/admin/auth/me', { quiet401: true });
      return res.user;
    } catch (err) {
      if (err instanceof ApiError && err.status === 401) return null;
      throw err;
    }
  },

  async login(email: string, password: string): Promise<SessionUser> {
    const res = await request<{ user: SessionUser }>('/api/admin/auth/login', { ...post({ email, password }), quiet401: true });
    return res.user;
  },

  async logout(): Promise<void> {
    try {
      await request('/api/admin/auth/logout', post());
    } finally {
      notifySignedOut();
    }
  },

  async changePassword(currentPassword: string, newPassword: string): Promise<{ message: string }> {
    return request('/api/admin/auth/change-password', post({ current_password: currentPassword, new_password: newPassword }));
  },

  async listUsers(): Promise<AdminUser[]> {
    const res = await request<{ users: AdminUser[] }>('/api/admin/users');
    return res.users || [];
  },

  async createUser(email: string, password: string, role: 'owner' | 'editor'): Promise<AdminUser> {
    const res = await request<{ user: AdminUser }>('/api/admin/users', post({ email, password, role }));
    return res.user;
  },

  async setUserDisabled(id: string, disabled: boolean): Promise<{ message: string }> {
    return request(`/api/admin/users/${enc(id)}/${disabled ? 'disable' : 'enable'}`, post());
  },

  async resetUserPassword(id: string, newPassword: string): Promise<{ message: string }> {
    return request(`/api/admin/users/${enc(id)}/reset-password`, post({ new_password: newPassword }));
  },
};

export const adminApi = {
  async getStats(): Promise<DashboardStats> {
    const res = await request<{ data: DashboardStats }>('/api/stats');
    return res.data;
  },

  async getHealth(): Promise<{ status: string; database: string; storage: string; timestamp: string }> {
    return request('/api/health');
  },

  async getPendingJobs(limit = 200): Promise<JobItem[]> {
    const res = await request<{ data: JobItem[] }>(`/api/admin/jobs/pending?limit=${limit}`);
    return res.data || [];
  },

  async approveJob(id: string): Promise<{ message: string }> {
    return request(`/api/admin/jobs/${enc(id)}/approve`, post({}));
  },

  /** Approves up to 100 jobs per call; larger selections are chunked. */
  async batchApproveJobs(ids: string[]): Promise<{ approvedCount: number }> {
    let approvedCount = 0;
    for (let i = 0; i < ids.length; i += 100) {
      const res = await request<{ approvedCount: number }>('/api/admin/jobs/batch-approve', post({ ids: ids.slice(i, i + 100) }));
      approvedCount += res.approvedCount || 0;
    }
    return { approvedCount };
  },

  async rejectJob(id: string, reason: string): Promise<{ message: string }> {
    return request(`/api/admin/jobs/${enc(id)}/reject`, post({ reason }));
  },

  async getSources(): Promise<JobSource[]> {
    const res = await request<{ sources: JobSource[] }>('/api/admin/job-import/sources');
    return res.sources || [];
  },

  async updateSource(id: string, enabled: boolean, intervalHours?: number): Promise<{ message: string }> {
    return request('/api/admin/job-import/sources', post({ id, enabled, polling_interval_hours: intervalHours ?? null }));
  },

  async runImport(sourceId?: string): Promise<{ summary: ImportSummary }> {
    return request('/api/admin/job-import/run', post(sourceId ? { source_id: sourceId } : {}));
  },

  async testSource(sourceId: string): Promise<SourceTestResult> {
    return request(`/api/admin/job-import/sources/${enc(sourceId)}/test`, post());
  },

  async getImportRuns(limit = 50): Promise<ImportRun[]> {
    const res = await request<{ runs: ImportRun[] }>(`/api/admin/job-import/runs?limit=${limit}`);
    return res.runs || [];
  },

  async createJob(job: ManualJobInput): Promise<{ message: string; jobId: string; slug: string }> {
    return request('/api/admin/jobs', post(job));
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
    return request(`/api/admin/reports/${enc(id)}/resolve`, post({ status, note }));
  },

  async getAdConfig(): Promise<AdConfig> {
    const res = await request<{ data: AdConfig }>('/api/admin/ads');
    return res.data;
  },

  async saveAdConfig(config: AdConfig): Promise<{ message: string; data: AdConfig }> {
    const { updatedAt, ...payload } = config;
    return request('/api/admin/ads', post(payload));
  },
};
