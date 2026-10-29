'use client';

import React, { useState, useEffect, useRef } from 'react';
import { Header } from '../../components/Header';
import { StatusBadge } from '../../components/StatusBadge';
import { adminApi } from '../../lib/api';
import { JobItem } from '../../lib/types';
import { 
  CheckCircle2, 
  XCircle, 
  ExternalLink, 
  Search, 
  Filter, 
  AlertTriangle, 
  FileText, 
  Eye, 
  Calendar,
  Building2,
  DollarSign,
  GraduationCap,
  CheckCheck,
  Loader2
} from 'lucide-react';

export default function PendingReviewPage() {
  const [jobs, setJobs] = useState<JobItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [stateFilter, setStateFilter] = useState('ALL');
  const [categoryFilter, setCategoryFilter] = useState('ALL');
  const [selectedJob, setSelectedJob] = useState<JobItem | null>(null);
  const [rejectingJob, setRejectingJob] = useState<JobItem | null>(null);
  const [rejectReason, setRejectReason] = useState('Incomplete or unverified official notification link');
  const [processingId, setProcessingId] = useState<string | null>(null);
  const [bannerNotice, setBannerNotice] = useState<string | null>(null);

  // Multi-select & Batch Approve state
  const [selectedIds, setSelectedIds] = useState<Set<string>>(new Set());
  const [isBatchApproving, setIsBatchApproving] = useState(false);
  const selectAllCheckboxRef = useRef<HTMLInputElement | null>(null);

  const loadPendingJobs = async () => {
    setLoading(true);
    try {
      const data = await adminApi.getPendingJobs();
      setJobs(data);
    } catch (err: any) {
      setBannerNotice(`Failed to load pending notices: ${err?.message || err}`);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadPendingJobs();
  }, []);

  const handleApprove = async (job: JobItem) => {
    try {
      setProcessingId(job.id);
      await adminApi.approveJob(job.id);
      setBannerNotice(`Approved "${job.title}". Published to the app.`);
      setSelectedJob(null);
      loadPendingJobs();
    } catch (err: any) {
      alert(`Approval failed: ${err.message}`);
    } finally {
      setProcessingId(null);
      setTimeout(() => setBannerNotice(null), 5000);
    }
  };

  const handleReject = async () => {
    if (!rejectingJob) return;
    try {
      setProcessingId(rejectingJob.id);
      await adminApi.rejectJob(rejectingJob.id, rejectReason);
      setBannerNotice(`Notice "${rejectingJob.title}" marked as REJECTED.`);
      setRejectingJob(null);
      setSelectedJob(null);
      loadPendingJobs();
    } catch (err: any) {
      alert(`Rejection failed: ${err.message}`);
    } finally {
      setProcessingId(null);
      setTimeout(() => setBannerNotice(null), 5000);
    }
  };

  const filteredJobs = jobs.filter((j) => {
    const matchesSearch = 
      j.title.toLowerCase().includes(search.toLowerCase()) ||
      j.organization.toLowerCase().includes(search.toLowerCase()) ||
      (j.advertisement_number || '').toLowerCase().includes(search.toLowerCase());
    const matchesState = stateFilter === 'ALL' || j.state === stateFilter || (stateFilter === 'Odisha' && (j.state || '').includes('Odisha'));
    const matchesCategory = categoryFilter === 'ALL' || j.category === categoryFilter;
    return matchesSearch && matchesState && matchesCategory;
  });

  const isAllFilteredSelected = filteredJobs.length > 0 && filteredJobs.every((j) => selectedIds.has(j.id));
  const isSomeFilteredSelected = filteredJobs.some((j) => selectedIds.has(j.id)) && !isAllFilteredSelected;

  useEffect(() => {
    if (selectAllCheckboxRef.current) {
      selectAllCheckboxRef.current.indeterminate = isSomeFilteredSelected;
    }
  }, [isSomeFilteredSelected]);

  const toggleSelectJob = (id: string, e?: React.MouseEvent) => {
    if (e) e.stopPropagation();
    setSelectedIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) {
        next.delete(id);
      } else {
        next.add(id);
      }
      return next;
    });
  };

  const toggleSelectAllFiltered = () => {
    if (isAllFilteredSelected) {
      setSelectedIds((prev) => {
        const next = new Set(prev);
        filteredJobs.forEach((j) => next.delete(j.id));
        return next;
      });
    } else {
      setSelectedIds((prev) => {
        const next = new Set(prev);
        filteredJobs.forEach((j) => next.add(j.id));
        return next;
      });
    }
  };

  const clearSelection = () => {
    setSelectedIds(new Set());
  };

  const handleBatchApprove = async () => {
    const idsToApprove = Array.from(selectedIds);
    if (idsToApprove.length === 0) return;

    if (!confirm(`Are you sure you want to batch approve ${idsToApprove.length} selected notices? They will be immediately published to the mobile application.`)) {
      return;
    }

    setIsBatchApproving(true);
    try {
      const res = await adminApi.batchApproveJobs(idsToApprove);
      const count = res.approvedCount;
      setBannerNotice(`Approved ${count} of ${idsToApprove.length} selected notices.`);
      setSelectedIds(new Set());
      await loadPendingJobs();
    } catch (err: any) {
      alert(`Batch approval failed: ${err.message}`);
    } finally {
      setIsBatchApproving(false);
      setTimeout(() => setBannerNotice(null), 6000);
    }
  };

  return (
    <>
      <Header 
        title="Pending Review & Approval Queue" 
        subtitle="Verify extracted notices before publication to the Android mobile app"
        onRefresh={loadPendingJobs}
        isRefreshing={loading}
      />

      <main className="p-8 space-y-6 flex-1">
        {bannerNotice && (
          <div className="bg-emerald-50 border border-emerald-200 text-emerald-800 px-4 py-3 rounded-xl flex items-center justify-between text-sm font-semibold shadow-sm animate-fade-in">
            <div className="flex items-center gap-2">
              <CheckCircle2 className="w-5 h-5 text-emerald-600" />
              <span>{bannerNotice}</span>
            </div>
          </div>
        )}

        {/* Filter Bar */}
        <div className="bg-white rounded-xl border border-slate-200 p-4 shadow-sm flex flex-col md:flex-row gap-3 items-center justify-between">
          <div className="relative w-full md:w-80">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              placeholder="Search by title, organization, advt no..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full pl-9 pr-3 py-1.5 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500"
            />
          </div>

          <div className="flex items-center gap-2 w-full md:w-auto">
            <select
              value={stateFilter}
              onChange={(e) => setStateFilter(e.target.value)}
              className="text-xs bg-slate-50 border border-slate-200 rounded-lg px-3 py-1.5 focus:outline-none focus:ring-2 focus:ring-blue-500/20"
            >
              <option value="ALL">All States</option>
              <option value="All India">All India</option>
              <option value="Odisha">Odisha</option>
            </select>

            <select
              value={categoryFilter}
              onChange={(e) => setCategoryFilter(e.target.value)}
              className="text-xs bg-slate-50 border border-slate-200 rounded-lg px-3 py-1.5 focus:outline-none focus:ring-2 focus:ring-blue-500/20"
            >
              <option value="ALL">All Categories</option>
              <option value="Latest Job">Latest Job</option>
              <option value="Admit Card">Admit Card</option>
              <option value="Result">Result</option>
              <option value="Answer Key">Answer Key</option>
              <option value="Syllabus">Syllabus</option>
            </select>
          </div>
        </div>

        {/* Table of Pending Notices */}
        <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
          <div className="p-4 border-b border-slate-200 flex flex-wrap items-center justify-between gap-3 bg-slate-50/50">
            <div className="flex items-center gap-3">
              <span className="text-xs font-bold text-slate-700">
                Showing {filteredJobs.length} of {jobs.length} pending review items
              </span>
              {selectedIds.size > 0 && (
                <span className="text-[11px] font-semibold text-blue-700 bg-blue-100 px-2.5 py-0.5 rounded-full">
                  {selectedIds.size} selected
                </span>
              )}
            </div>

            {selectedIds.size > 0 && (
              <div className="flex items-center gap-2">
                <button
                  type="button"
                  onClick={clearSelection}
                  className="px-2.5 py-1 text-slate-600 hover:text-slate-900 hover:bg-slate-200 rounded text-xs font-medium cursor-pointer transition-colors"
                >
                  Clear Selection
                </button>
                <button
                  type="button"
                  onClick={handleBatchApprove}
                  disabled={isBatchApproving}
                  className="inline-flex items-center gap-1.5 px-3.5 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-xs font-bold shadow-sm transition-all cursor-pointer disabled:opacity-50"
                >
                  {isBatchApproving ? (
                    <>
                      <Loader2 className="w-3.5 h-3.5 animate-spin" />
                      <span>Approving {selectedIds.size}...</span>
                    </>
                  ) : (
                    <>
                      <CheckCheck className="w-4 h-4" />
                      <span>Batch Approve ({selectedIds.size})</span>
                    </>
                  )}
                </button>
              </div>
            )}
          </div>

          {loading ? (
            <div className="p-16 text-center text-slate-400 text-sm">Loading pending notices from Cloudflare D1...</div>
          ) : filteredJobs.length === 0 ? (
            <div className="p-16 text-center">
              <CheckCircle2 className="w-10 h-10 text-emerald-500 mx-auto mb-2" />
              <p className="text-sm font-bold text-slate-800">No notices waiting for review</p>
              <p className="text-xs text-slate-500 mt-1">Either all notices are published or none match current filters.</p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse text-xs">
                <thead>
                  <tr className="bg-slate-50 border-b border-slate-200 text-slate-600 font-bold uppercase tracking-wider text-[11px]">
                    <th className="py-3 px-4 w-12 text-center">
                      <input
                        ref={selectAllCheckboxRef}
                        type="checkbox"
                        checked={isAllFilteredSelected}
                        onChange={toggleSelectAllFiltered}
                        title={isAllFilteredSelected ? "Deselect all" : "Select all filtered"}
                        className="w-4 h-4 rounded text-blue-600 border-slate-300 focus:ring-blue-500 cursor-pointer"
                      />
                    </th>
                    <th className="py-3 px-4">Recruitment Details</th>
                    <th className="py-3 px-4">Category</th>
                    <th className="py-3 px-4">State</th>
                    <th className="py-3 px-4">Posts / Qualification</th>
                    <th className="py-3 px-4">Application Window</th>
                    <th className="py-3 px-4">Official Links</th>
                    <th className="py-3 px-4 text-right">Review Action</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {filteredJobs.map((job) => {
                    const isSelected = selectedIds.has(job.id);
                    return (
                      <tr 
                        key={job.id} 
                        className={`transition-colors ${isSelected ? 'bg-blue-50/70 hover:bg-blue-50' : 'hover:bg-slate-50/80'}`}
                      >
                        <td className="py-3.5 px-4 text-center" onClick={(e) => e.stopPropagation()}>
                          <input
                            type="checkbox"
                            checked={isSelected}
                            onChange={(e) => toggleSelectJob(job.id, e as any)}
                            className="w-4 h-4 rounded text-blue-600 border-slate-300 focus:ring-blue-500 cursor-pointer"
                          />
                        </td>
                        <td className="py-3.5 px-4 max-w-xs">
                          <div className="font-bold text-slate-900 line-clamp-1">{job.title}</div>
                          <div className="text-slate-500 text-[11px] mt-0.5">
                            {job.organization}
                          </div>
                          {job.advertisement_number && (
                            <div className="text-[10px] text-blue-600 font-mono mt-0.5">
                              Advt: {job.advertisement_number}
                            </div>
                          )}
                        </td>
                        <td className="py-3.5 px-4">
                          <span className="px-2 py-0.5 rounded font-bold text-[10px] bg-blue-100 text-blue-800">
                            {job.category}
                          </span>
                        </td>
                        <td className="py-3.5 px-4 font-semibold text-slate-700">
                          {job.state}
                        </td>
                        <td className="py-3.5 px-4 text-slate-600">
                          <div className="font-medium text-slate-800">{job.total_posts || '—'}</div>
                          <div className="text-[10px] text-slate-500 line-clamp-1">{job.qualification || '—'}</div>
                        </td>
                        <td className="py-3.5 px-4 font-mono text-slate-700">
                          <div>Start: {job.application_start_date || '—'}</div>
                          <div className="text-rose-600 font-semibold">Last: {job.last_date || '—'}</div>
                        </td>
                        <td className="py-3.5 px-4 space-y-1">
                          {job.official_notification_url && (
                            <a
                              href={job.official_notification_url}
                              target="_blank"
                              rel="noopener noreferrer"
                              className="inline-flex items-center gap-1 text-[11px] text-blue-600 hover:text-blue-800 hover:underline"
                            >
                              <FileText className="w-3 h-3" />
                              <span>PDF Notice</span>
                              <ExternalLink className="w-2.5 h-2.5" />
                            </a>
                          )}
                          {job.official_apply_url && (
                            <div>
                              <a
                                href={job.official_apply_url}
                                target="_blank"
                                rel="noopener noreferrer"
                                className="inline-flex items-center gap-1 text-[11px] text-emerald-600 hover:text-emerald-800 hover:underline"
                              >
                                <span>Apply Portal</span>
                                <ExternalLink className="w-2.5 h-2.5" />
                              </a>
                            </div>
                          )}
                        </td>
                        <td className="py-3.5 px-4 text-right space-x-1.5 whitespace-nowrap">
                          <button
                            onClick={() => setSelectedJob(job)}
                            className="px-2.5 py-1 text-slate-700 hover:text-slate-900 hover:bg-slate-100 rounded border border-slate-200 font-semibold text-[11px] cursor-pointer"
                          >
                            Inspect
                          </button>
                          <button
                            onClick={() => handleApprove(job)}
                            disabled={processingId === job.id || isBatchApproving}
                            className="px-2.5 py-1 bg-emerald-600 hover:bg-emerald-700 text-white rounded font-bold text-[11px] cursor-pointer shadow-sm disabled:opacity-50"
                          >
                            Approve
                          </button>
                          <button
                            onClick={() => setRejectingJob(job)}
                            disabled={processingId === job.id || isBatchApproving}
                            className="px-2.5 py-1 bg-rose-50 text-rose-700 hover:bg-rose-100 border border-rose-200 rounded font-bold text-[11px] cursor-pointer disabled:opacity-50"
                          >
                            Reject
                          </button>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* Detailed Inspection Modal */}
        {selectedJob && (
          <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
            <div className="bg-white rounded-2xl max-w-2xl w-full shadow-2xl border border-slate-200 max-h-[90vh] flex flex-col overflow-hidden">
              <div className="p-6 border-b border-slate-200 flex items-center justify-between bg-slate-50">
                <div>
                  <span className="text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded bg-blue-100 text-blue-800">
                    {selectedJob.category} • {selectedJob.state}
                  </span>
                  <h3 className="text-base font-bold text-slate-900 mt-1">{selectedJob.title}</h3>
                  <p className="text-xs text-slate-500">{selectedJob.organization}</p>
                </div>
                <button
                  onClick={() => setSelectedJob(null)}
                  className="p-1 rounded-lg text-slate-400 hover:text-slate-700 hover:bg-slate-200 cursor-pointer"
                >
                  <XCircle className="w-5 h-5" />
                </button>
              </div>

              <div className="p-6 overflow-y-auto space-y-4 text-xs">
                <div className="grid grid-cols-2 gap-4">
                  <div className="bg-slate-50 p-3 rounded-xl border border-slate-200">
                    <span className="text-slate-400 text-[10px] font-bold uppercase">Total Posts</span>
                    <p className="text-sm font-bold text-slate-900">{selectedJob.total_posts || 'Not stated'}</p>
                  </div>
                  <div className="bg-slate-50 p-3 rounded-xl border border-slate-200">
                    <span className="text-slate-400 text-[10px] font-bold uppercase">Salary Scale</span>
                    <p className="text-sm font-bold text-slate-900">{selectedJob.salary_scale || 'Not stated'}</p>
                  </div>
                  <div className="bg-slate-50 p-3 rounded-xl border border-slate-200">
                    <span className="text-slate-400 text-[10px] font-bold uppercase">Start Date</span>
                    <p className="text-sm font-bold text-slate-900">{selectedJob.application_start_date || 'Not stated'}</p>
                  </div>
                  <div className="bg-slate-50 p-3 rounded-xl border border-slate-200">
                    <span className="text-slate-400 text-[10px] font-bold uppercase">Last Date</span>
                    <p className="text-sm font-bold text-rose-600">{selectedJob.last_date || 'Not stated'}</p>
                  </div>
                </div>

                <div className="bg-slate-50 p-3 rounded-xl border border-slate-200 space-y-1">
                  <span className="text-slate-400 text-[10px] font-bold uppercase">Qualification / Eligibility</span>
                  <p className="text-slate-800 leading-relaxed font-medium">{selectedJob.qualification || 'Not stated. Verify in the official notice before approving.'}</p>
                </div>

                <div className="bg-slate-50 p-3 rounded-xl border border-slate-200 space-y-1">
                  <span className="text-slate-400 text-[10px] font-bold uppercase">Age Limit & Application Fee</span>
                  <p className="text-slate-800">Age: {selectedJob.min_age && selectedJob.max_age ? `${selectedJob.min_age}–${selectedJob.max_age} years` : 'Not stated'} • Fee (General): {selectedJob.fee_general || 'Not stated'} • Fee (SC/ST): {selectedJob.fee_sc_st || 'Not stated'}</p>
                </div>

                <div className="space-y-2">
                  <span className="text-slate-400 text-[10px] font-bold uppercase">Official Links</span>
                  <div className="flex flex-col gap-2">
                    {selectedJob.official_notification_url && (
                      <a
                        href={selectedJob.official_notification_url}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="p-2.5 rounded-lg bg-blue-50 border border-blue-200 text-blue-700 flex items-center justify-between font-semibold"
                      >
                        <span className="flex items-center gap-2">
                          <FileText className="w-4 h-4" />
                          <span>Official Advertisement PDF Link</span>
                        </span>
                        <ExternalLink className="w-3.5 h-3.5" />
                      </a>
                    )}
                    {selectedJob.official_apply_url && (
                      <a
                        href={selectedJob.official_apply_url}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="p-2.5 rounded-lg bg-emerald-50 border border-emerald-200 text-emerald-700 flex items-center justify-between font-semibold"
                      >
                        <span className="flex items-center gap-2">
                          <ExternalLink className="w-4 h-4" />
                          <span>Official Online Application Portal</span>
                        </span>
                        <ExternalLink className="w-3.5 h-3.5" />
                      </a>
                    )}
                  </div>
                </div>
              </div>

              <div className="p-4 border-t border-slate-200 bg-slate-50 flex items-center justify-end gap-2">
                <button
                  onClick={() => setSelectedJob(null)}
                  className="px-4 py-2 text-slate-700 hover:bg-slate-200 rounded-lg text-xs font-semibold cursor-pointer"
                >
                  Close
                </button>
                <button
                  onClick={() => setRejectingJob(selectedJob)}
                  className="px-4 py-2 bg-rose-50 text-rose-700 hover:bg-rose-100 border border-rose-200 rounded-lg text-xs font-bold cursor-pointer"
                >
                  Reject Notice
                </button>
                <button
                  onClick={() => handleApprove(selectedJob)}
                  disabled={processingId === selectedJob.id}
                  className="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-xs font-bold cursor-pointer shadow-sm disabled:opacity-50"
                >
                  Approve & Publish to App
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Reject Confirmation Dialog */}
        {rejectingJob && (
          <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
            <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-2xl border border-slate-200 space-y-4">
              <h3 className="text-base font-bold text-slate-900">Reject Job Notice</h3>
              <p className="text-xs text-slate-600">
                Are you sure you want to reject <span className="font-bold text-slate-800">{rejectingJob.title}</span>? This notice will not appear in the mobile application.
              </p>
              
              <div>
                <label className="text-xs font-bold text-slate-700 block mb-1">Reason for rejection:</label>
                <textarea
                  rows={3}
                  value={rejectReason}
                  onChange={(e) => setRejectReason(e.target.value)}
                  className="w-full text-xs p-2.5 bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-rose-500/20 focus:border-rose-500"
                />
              </div>

              <div className="flex items-center justify-end gap-2 pt-2">
                <button
                  onClick={() => setRejectingJob(null)}
                  className="px-3.5 py-1.5 text-slate-700 hover:bg-slate-100 rounded-lg text-xs font-semibold cursor-pointer"
                >
                  Cancel
                </button>
                <button
                  onClick={handleReject}
                  disabled={processingId === rejectingJob.id}
                  className="px-3.5 py-1.5 bg-rose-600 hover:bg-rose-700 text-white rounded-lg text-xs font-bold cursor-pointer shadow-sm disabled:opacity-50"
                >
                  Confirm Rejection
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Floating Bulk Action Bar */}
        {selectedIds.size > 0 && (
          <div className="fixed bottom-6 left-1/2 -translate-x-1/2 z-40 bg-slate-900/95 backdrop-blur-md text-white px-5 py-3 rounded-2xl shadow-2xl border border-slate-800 flex items-center gap-4 animate-in fade-in slide-in-from-bottom-4">
            <div className="flex items-center gap-2">
              <span className="flex h-2 w-2 rounded-full bg-emerald-400 animate-pulse" />
              <span className="text-xs font-bold tracking-wide">
                {selectedIds.size} {selectedIds.size === 1 ? 'Notice' : 'Notices'} Selected
              </span>
            </div>
            <div className="h-4 w-px bg-slate-700" />
            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={toggleSelectAllFiltered}
                className="px-2.5 py-1 text-slate-300 hover:text-white hover:bg-slate-800 rounded-lg text-xs font-medium cursor-pointer transition-colors"
              >
                {isAllFilteredSelected ? 'Deselect All' : `Select All (${filteredJobs.length})`}
              </button>
              <button
                type="button"
                onClick={clearSelection}
                className="px-2.5 py-1 text-slate-400 hover:text-slate-200 text-xs font-medium cursor-pointer transition-colors"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={handleBatchApprove}
                disabled={isBatchApproving}
                className="inline-flex items-center gap-2 px-4 py-1.5 bg-emerald-500 hover:bg-emerald-600 text-slate-950 font-bold rounded-xl text-xs shadow-lg shadow-emerald-500/20 cursor-pointer transition-all disabled:opacity-50"
              >
                {isBatchApproving ? (
                  <>
                    <Loader2 className="w-4 h-4 animate-spin text-slate-950" />
                    <span>Batch Approving...</span>
                  </>
                ) : (
                  <>
                    <CheckCheck className="w-4 h-4 text-slate-950" />
                    <span>Batch Approve ({selectedIds.size})</span>
                  </>
                )}
              </button>
            </div>
          </div>
        )}
      </main>
    </>
  );
}
