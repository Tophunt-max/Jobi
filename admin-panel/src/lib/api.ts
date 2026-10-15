import { DashboardStats, ImportRun, JobItem, JobSource } from './types';

export function getApiConfig() {
  if (typeof window !== 'undefined') {
    const savedUrl = localStorage.getItem('CLOUDFLARE_WORKER_URL');
    const savedKey = localStorage.getItem('CLOUDFLARE_ADMIN_KEY');
    return {
      baseUrl: (savedUrl || process.env.NEXT_PUBLIC_DEFAULT_API_URL || 'https://sarkari-jobs-worker.your-subdomain.workers.dev').replace(/\/$/, ''),
      adminKey: savedKey || process.env.NEXT_PUBLIC_DEFAULT_ADMIN_KEY || 'sarkari_admin_secret_key_change_me'
    };
  }
  return {
    baseUrl: (process.env.NEXT_PUBLIC_DEFAULT_API_URL || 'https://sarkari-jobs-worker.your-subdomain.workers.dev').replace(/\/$/, ''),
    adminKey: process.env.NEXT_PUBLIC_DEFAULT_ADMIN_KEY || 'sarkari_admin_secret_key_change_me'
  };
}

export function saveApiConfig(url: string, key: string) {
  if (typeof window !== 'undefined') {
    localStorage.setItem('CLOUDFLARE_WORKER_URL', url.trim().replace(/\/$/, ''));
    localStorage.setItem('CLOUDFLARE_ADMIN_KEY', key.trim());
  }
}

async function request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const { baseUrl, adminKey } = getApiConfig();
  const headers = new Headers(options.headers || {});
  headers.set('X-Admin-Key', adminKey);
  headers.set('Content-Type', 'application/json');

  const fullUrl = `${baseUrl}${endpoint.startsWith('/') ? endpoint : `/${endpoint}`}`;

  try {
    const res = await fetch(fullUrl, {
      ...options,
      headers,
    });

    if (!res.ok) {
      const errText = await res.text();
      let errMsg = `Request failed: HTTP ${res.status}`;
      try {
        const json = JSON.parse(errText);
        errMsg = json.message || errMsg;
      } catch (_) {}
      throw new Error(errMsg);
    }

    return await res.json();
  } catch (err: any) {
    console.error(`API Error on ${endpoint}:`, err);
    throw err;
  }
}

export const adminApi = {
  // Stats
  async getStats(): Promise<DashboardStats> {
    const res = await request<{ success: boolean; data: DashboardStats }>('/api/stats');
    return res.data;
  },

  // Health
  async getHealth(): Promise<{ status: string; database: string; r2: string; timestamp: string }> {
    return await request('/api/health');
  },

  // Pending Jobs
  async getPendingJobs(): Promise<JobItem[]> {
    const res = await request<{ success: boolean; data: JobItem[] }>('/api/admin/jobs/pending');
    return res.data || [];
  },

  // Approve Job
  async approveJob(id: string, approvedBy = 'Admin Panel'): Promise<{ success: boolean; message: string }> {
    return await request(`/api/admin/jobs/${id}/approve`, {
      method: 'POST',
      body: JSON.stringify({ approved_by: approvedBy }),
    });
  },

  // Batch Approve Jobs
  async batchApproveJobs(ids: string[], approvedBy = 'Admin Batch'): Promise<{ success: boolean; message: string; approvedCount?: number }> {
    try {
      return await request(`/api/admin/jobs/batch-approve`, {
        method: 'POST',
        body: JSON.stringify({ ids, approved_by: approvedBy }),
      });
    } catch (err) {
      // Fallback to sequential/parallel individual approves if batch endpoint fails
      let successCount = 0;
      for (const id of ids) {
        try {
          await this.approveJob(id, approvedBy);
          successCount++;
        } catch (_) {}
      }
      return {
        success: successCount > 0,
        message: `Batch approved ${successCount} of ${ids.length} jobs`,
        approvedCount: successCount,
      };
    }
  },

  // Reject Job
  async rejectJob(id: string, reason = 'Rejected by administrator'): Promise<{ success: boolean; message: string }> {
    return await request(`/api/admin/jobs/${id}/reject`, {
      method: 'POST',
      body: JSON.stringify({ reason }),
    });
  },

  // Sources
  async getSources(): Promise<JobSource[]> {
    const res = await request<{ success: boolean; sources: JobSource[] }>('/api/admin/job-import/sources');
    return res.sources || [];
  },

  // Update Source (Toggle enable/disable)
  async updateSource(id: string, enabled: boolean, intervalHours?: number): Promise<{ success: boolean; message: string }> {
    return await request('/api/admin/job-import/sources', {
      method: 'POST',
      body: JSON.stringify({ id, enabled, polling_interval_hours: intervalHours }),
    });
  },

  // Trigger Import Run
  async runImport(sourceId?: string): Promise<{ success: boolean; summary: any }> {
    return await request('/api/admin/job-import/run', {
      method: 'POST',
      body: JSON.stringify({ source_id: sourceId }),
    });
  },

  // Test Source Adapter Live
  async testSource(sourceId: string): Promise<{ success: boolean; data: any; sampleNotices?: any[] }> {
    return await request(`/api/admin/job-import/sources/${sourceId}/test`, {
      method: 'POST',
    });
  },

  // Import Runs Audit Log
  async getImportRuns(): Promise<ImportRun[]> {
    const res = await request<{ success: boolean; runs: ImportRun[] }>('/api/admin/job-import/runs');
    return res.runs || [];
  },

  // All Published Jobs
  async getAllJobs(params?: { status?: string; category?: string; search?: string; limit?: number }): Promise<JobItem[]> {
    const q = new URLSearchParams();
    if (params?.status) q.append('status', params.status);
    if (params?.category) q.append('category', params.category);
    if (params?.search) q.append('search', params.search);
    if (params?.limit) q.append('limit', String(params.limit));

    const res = await request<{ success: boolean; data: JobItem[] }>(`/api/jobs?${q.toString()}`);
    return res.data || [];
  },
};
