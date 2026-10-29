'use client';

import React, { useState, useEffect } from 'react';
import { Header } from '../../components/Header';
import { adminApi } from '../../lib/api';
import { ImportRun } from '../../lib/types';
import { 
  History, 
  CheckCircle, 
  AlertCircle, 
  Clock, 
  RefreshCw,
  FileText
} from 'lucide-react';

export default function RunsPage() {
  const [runs, setRuns] = useState<ImportRun[]>([]);
  const [loading, setLoading] = useState(true);

  const [error, setError] = useState<string | null>(null);

  const loadRuns = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await adminApi.getImportRuns();
      setRuns(data);
    } catch (err: any) {
      setError(err?.message || 'Failed to load import runs');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadRuns();
  }, []);

  return (
    <>
      <Header 
        title="Crawler Runs & Ingestion Audit Log" 
        subtitle="Historical records of Cloudflare Workers scheduled cron triggers and manual imports"
        onRefresh={loadRuns}
        isRefreshing={loading}
      />

      <main className="p-8 space-y-6 flex-1">
        <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
          <div className="p-4 border-b border-slate-200 flex items-center justify-between bg-slate-50/50">
            <span className="text-xs font-bold text-slate-700">
              Showing recent execution runs ({runs.length})
            </span>
          </div>

          {error && (
            <div className="p-4 text-xs text-rose-700 bg-rose-50 border-b border-rose-200">{error}</div>
          )}
          {loading ? (
            <div className="p-16 text-center text-slate-400 text-sm">Loading execution logs...</div>
          ) : runs.length === 0 ? (
            <div className="p-16 text-center text-slate-500 text-sm">
              No import runs logged yet. Click "Run Automated Import" in the header to trigger one.
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse text-xs">
                <thead>
                  <tr className="bg-slate-50 border-b border-slate-200 text-slate-600 font-bold uppercase tracking-wider text-[11px]">
                    <th className="py-3 px-5">Execution Time</th>
                    <th className="py-3 px-4">Trigger Mode</th>
                    <th className="py-3 px-4">Target Source</th>
                    <th className="py-3 px-4">Notices Fetched</th>
                    <th className="py-3 px-4">New Inserted</th>
                    <th className="py-3 px-4">Duplicates Skipped</th>
                    <th className="py-3 px-4">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 font-mono text-[11px]">
                  {runs.map((run) => (
                    <tr key={run.id} className="hover:bg-slate-50/70 transition-colors">
                      <td className="py-3.5 px-5 font-semibold text-slate-900">
                        {run.started_at ? run.started_at.replace('T', ' ').slice(0, 19) : run.id}
                      </td>
                      <td className="py-3.5 px-4">
                        <span className={`px-2 py-0.5 rounded font-bold text-[10px] ${
                          (run.triggered_by || '').includes('CRON')
                            ? 'bg-purple-100 text-purple-800'
                            : 'bg-blue-100 text-blue-800'
                        }`}>
                          {run.triggered_by || 'UNKNOWN'}
                        </span>
                      </td>
                      <td className="py-3.5 px-4 font-sans text-slate-700 font-medium">
                        {!run.source_id || run.source_id === 'ALL_ENABLED' ? 'All enabled sources' : run.source_id}
                      </td>
                      <td className="py-3.5 px-4 text-slate-700">
                        {run.fetched_count}
                      </td>
                      <td className="py-3.5 px-4 text-emerald-600 font-bold">
                        +{run.inserted_count}
                      </td>
                      <td className="py-3.5 px-4 text-amber-600">
                        {run.duplicate_count} skipped
                      </td>
                      <td className="py-3.5 px-4 font-sans">
                        {run.status === 'SUCCESS' ? (
                          <span className="inline-flex items-center gap-1 text-emerald-700 font-bold text-[10px]">
                            <CheckCircle className="w-3.5 h-3.5" />
                            <span>SUCCESS</span>
                          </span>
                        ) : run.status === 'RUNNING' ? (
                          <span className="inline-flex items-center gap-1 text-blue-700 font-bold text-[10px]">
                            <Clock className="w-3.5 h-3.5 animate-spin" />
                            <span>RUNNING</span>
                          </span>
                        ) : (
                          <span
                            title={run.error_summary || ''}
                            className={`inline-flex items-center gap-1 font-bold text-[10px] ${run.status === 'PARTIAL' ? 'text-amber-700' : 'text-rose-700'}`}
                          >
                            <AlertCircle className="w-3.5 h-3.5" />
                            <span>{run.status}</span>
                            {run.error_count > 0 && <span className="font-normal">({run.error_count} errors)</span>}
                          </span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </main>
    </>
  );
}
