'use client';

import React, { useEffect, useState } from 'react';
import { CheckCircle2, ExternalLink, Flag, XCircle } from 'lucide-react';
import { Header } from '../../components/Header';
import { adminApi } from '../../lib/api';
import { JobReport } from '../../lib/types';

type Filter = 'OPEN' | 'RESOLVED' | 'DISMISSED' | 'ALL';

export default function ReportsPage() {
  const [reports, setReports] = useState<JobReport[]>([]);
  const [filter, setFilter] = useState<Filter>('OPEN');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [busyId, setBusyId] = useState<string | null>(null);

  const load = async () => {
    setLoading(true);
    setError(null);
    try {
      setReports(await adminApi.getReports(filter));
    } catch (err: any) {
      setError(err?.message || 'Failed to load reports');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filter]);

  const resolve = async (report: JobReport, status: 'RESOLVED' | 'DISMISSED') => {
    const note = status === 'RESOLVED' ? window.prompt('What was fixed? (optional)') ?? undefined : undefined;
    setBusyId(report.id);
    try {
      await adminApi.resolveReport(report.id, status, note || undefined);
      await load();
    } catch (err: any) {
      setError(err?.message || 'Update failed');
    } finally {
      setBusyId(null);
    }
  };

  return (
    <>
      <Header
        title="User Reports"
        subtitle="Wrong or outdated information reported from the Android app"
        onRefresh={load}
        isRefreshing={loading}
      />
      <main className="p-8 space-y-4 flex-1">
        <div className="flex items-center gap-2">
          {(['OPEN', 'RESOLVED', 'DISMISSED', 'ALL'] as Filter[]).map((f) => (
            <button
              key={f}
              onClick={() => setFilter(f)}
              className={`px-3 py-1.5 rounded-lg text-xs font-semibold border ${
                filter === f ? 'bg-blue-600 text-white border-blue-600' : 'bg-white text-slate-700 border-slate-200 hover:bg-slate-50'
              }`}
            >
              {f}
            </button>
          ))}
        </div>

        {error && <div className="p-3 text-xs text-rose-700 bg-rose-50 border border-rose-200 rounded-lg">{error}</div>}

        <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
          {loading ? (
            <div className="p-16 text-center text-slate-400 text-sm">Loading reports…</div>
          ) : reports.length === 0 ? (
            <div className="p-16 text-center text-slate-500 text-sm flex flex-col items-center gap-2">
              <Flag className="w-6 h-6 text-slate-300" />
              No {filter === 'ALL' ? '' : filter.toLowerCase()} reports.
            </div>
          ) : (
            <table className="w-full text-left text-xs">
              <thead>
                <tr className="bg-slate-50 border-b border-slate-200 text-slate-600 font-bold uppercase tracking-wider text-[11px]">
                  <th className="py-3 px-5">Notice</th>
                  <th className="py-3 px-4">Reason</th>
                  <th className="py-3 px-4">Reported</th>
                  <th className="py-3 px-4">Status</th>
                  <th className="py-3 px-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {reports.map((r) => (
                  <tr key={r.id} className="align-top">
                    <td className="py-3.5 px-5 max-w-sm">
                      <div className="font-bold text-slate-900">{r.job_title || r.job_id}</div>
                      <div className="text-slate-500 text-[11px]">{r.job_organization || ''}</div>
                      {r.job_url && (
                        <a href={r.job_url} target="_blank" rel="noopener noreferrer" className="inline-flex items-center gap-1 text-[11px] text-blue-600 hover:underline mt-1">
                          Official notice <ExternalLink className="w-3 h-3" />
                        </a>
                      )}
                    </td>
                    <td className="py-3.5 px-4 max-w-xs">
                      <div className="font-semibold text-slate-800">{r.reason}</div>
                      {r.details && <div className="text-slate-600 mt-0.5 whitespace-pre-wrap break-words">{r.details}</div>}
                      {r.resolution_note && <div className="text-emerald-700 mt-1">Note: {r.resolution_note}</div>}
                    </td>
                    <td className="py-3.5 px-4 font-mono text-[11px] text-slate-600">{r.created_at.replace('T', ' ').slice(0, 16)}</td>
                    <td className="py-3.5 px-4 font-bold text-[11px]">{r.status}</td>
                    <td className="py-3.5 px-4 text-right whitespace-nowrap space-x-1.5">
                      {r.status === 'OPEN' && (
                        <>
                          <button
                            disabled={busyId === r.id}
                            onClick={() => resolve(r, 'RESOLVED')}
                            className="inline-flex items-center gap-1 px-2.5 py-1 bg-emerald-600 hover:bg-emerald-700 text-white rounded font-bold text-[11px] disabled:opacity-50"
                          >
                            <CheckCircle2 className="w-3.5 h-3.5" /> Resolve
                          </button>
                          <button
                            disabled={busyId === r.id}
                            onClick={() => resolve(r, 'DISMISSED')}
                            className="inline-flex items-center gap-1 px-2.5 py-1 bg-slate-100 hover:bg-slate-200 text-slate-700 border border-slate-200 rounded font-bold text-[11px] disabled:opacity-50"
                          >
                            <XCircle className="w-3.5 h-3.5" /> Dismiss
                          </button>
                        </>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </main>
    </>
  );
}
