'use client';

import React, { useState, useEffect } from 'react';
import { Header } from '../../components/Header';
import { StatusBadge } from '../../components/StatusBadge';
import { adminApi } from '../../lib/api';
import { JobCategory, JobItem, JOB_CATEGORIES } from '../../lib/types';
import { 
  Briefcase, 
  Search, 
  ExternalLink, 
  FileText, 
  Calendar, 
  Building2,
  CheckCircle2,
  Filter,
  Plus,
  X
} from 'lucide-react';

export default function AllJobsPage() {
  const [jobs, setJobs] = useState<JobItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [categoryFilter, setCategoryFilter] = useState('ALL');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [alertMsg, setAlertMsg] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  // Form State
  const [formTitle, setFormTitle] = useState('');
  const [formOrg, setFormOrg] = useState('');
  const [formAdvtNo, setFormAdvtNo] = useState('');
  const [formCategory, setFormCategory] = useState<JobCategory>('Latest Job');
  const [formState, setFormState] = useState('All India');
  const [formVacancies, setFormVacancies] = useState('');
  const [formQual, setFormQual] = useState('');
  const [formSalary, setFormSalary] = useState('');
  const [formClosingDate, setFormClosingDate] = useState('');
  const [formPdfUrl, setFormPdfUrl] = useState('');
  const [formApplyUrl, setFormApplyUrl] = useState('');

  const resetForm = () => {
    setFormTitle(''); setFormOrg(''); setFormAdvtNo(''); setFormCategory('Latest Job'); setFormState('All India');
    setFormVacancies(''); setFormQual(''); setFormSalary(''); setFormClosingDate(''); setFormPdfUrl(''); setFormApplyUrl('');
  };

  const handleCreateJobSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formTitle.trim() || !formOrg.trim() || !formPdfUrl.trim()) {
      setAlertMsg({ type: 'error', text: 'Title, organization and official notice URL are required.' });
      return;
    }
    setIsSubmitting(true);
    setAlertMsg(null);
    try {
      const res = await adminApi.createJob({
        title: formTitle.trim(),
        organization: formOrg.trim(),
        advertisement_number: formAdvtNo.trim() || undefined,
        category: formCategory,
        state: formState.trim() || 'All India',
        total_posts: formVacancies ? parseInt(formVacancies, 10) : undefined,
        qualification: formQual.trim() || undefined,
        salary_scale: formSalary.trim() || undefined,
        last_date: formClosingDate || undefined,
        official_notification_url: formPdfUrl.trim(),
        official_apply_url: formApplyUrl.trim() || undefined,
      });
      setAlertMsg({ type: 'success', text: res.message || 'Job published successfully!' });
      setShowCreateModal(false);
      resetForm();
      loadJobs();
    } catch (err: any) {
      setAlertMsg({ type: 'error', text: err.message || 'Failed to publish job' });
    } finally {
      setIsSubmitting(false);
    }
  };

  const loadJobs = async () => {
    setLoading(true);
    try {
      const data = await adminApi.getAllJobs({
        status: statusFilter,
        category: categoryFilter !== 'ALL' ? categoryFilter : undefined,
        limit: 200,
      });
      setJobs(data);
    } catch (err: any) {
      setAlertMsg({ type: 'error', text: err?.message || 'Failed to load jobs' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadJobs();
  }, [statusFilter, categoryFilter]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
  };

  const term = search.trim().toLowerCase();
  const visibleJobs = term
    ? jobs.filter((j) =>
        [j.title, j.organization, j.advertisement_number, j.post_name]
          .some((v) => (v || '').toLowerCase().includes(term)))
    : jobs;

  return (
    <>
      <Header 
        title="Active Recruitment Notices & Database" 
        subtitle="All live jobs, admit cards, syllabus, and results stored in Cloudflare D1"
        onRefresh={loadJobs}
        isRefreshing={loading}
      />

      <main className="p-8 space-y-6 flex-1">
        {/* Filters */}
        <div className="bg-white rounded-xl border border-slate-200 p-4 shadow-sm flex flex-col md:flex-row gap-3 items-center justify-between">
          <form onSubmit={handleSearchSubmit} className="relative w-full md:w-80">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              placeholder="Filter by title, organization, advt..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full pl-9 pr-3 py-1.5 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500"
            />
          </form>

          <div className="flex items-center gap-2">
            <button
              onClick={() => setShowCreateModal(true)}
              className="flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition shadow-sm"
            >
              <Plus className="w-3.5 h-3.5" />
              <span>Publish Job Notice</span>
            </button>
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="text-xs bg-slate-50 border border-slate-200 rounded-lg px-3 py-1.5 focus:outline-none focus:ring-2 focus:ring-blue-500/20"
            >
              <option value="ALL">All Statuses</option>
              <option value="ACTIVE">ACTIVE (Published)</option>
              <option value="pending_review">Pending Review</option>
              <option value="REJECTED">REJECTED</option>
              <option value="EXPIRED">EXPIRED</option>
            </select>

            <select
              value={categoryFilter}
              onChange={(e) => setCategoryFilter(e.target.value)}
              className="text-xs bg-slate-50 border border-slate-200 rounded-lg px-3 py-1.5 focus:outline-none focus:ring-2 focus:ring-blue-500/20"
            >
              <option value="ALL">All Categories</option>
              {JOB_CATEGORIES.map((c) => <option key={c} value={c}>{c}</option>)}
            </select>
          </div>
        </div>

        {/* Database Table */}
        <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
          <div className="p-4 border-b border-slate-200 flex items-center justify-between bg-slate-50/50">
            <span className="text-xs font-bold text-slate-700">
              Showing {visibleJobs.length} of {jobs.length} records
            </span>
          </div>

          {loading ? (
            <div className="p-16 text-center text-slate-400 text-sm">Querying Cloudflare D1 database...</div>
          ) : visibleJobs.length === 0 ? (
            <div className="p-16 text-center text-slate-500 text-sm">No records match the current filters.</div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse text-xs">
                <thead>
                  <tr className="bg-slate-50 border-b border-slate-200 text-slate-600 font-bold uppercase tracking-wider text-[11px]">
                    <th className="py-3 px-5">Title & Organization</th>
                    <th className="py-3 px-4">Category</th>
                    <th className="py-3 px-4">State</th>
                    <th className="py-3 px-4">Posts / Qual.</th>
                    <th className="py-3 px-4">Application Window</th>
                    <th className="py-3 px-4">Status</th>
                    <th className="py-3 px-4 text-right">Links</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {visibleJobs.map((job) => (
                    <tr key={job.id} className="hover:bg-slate-50/70 transition-colors">
                      <td className="py-3.5 px-5 max-w-sm">
                        <div className="font-bold text-slate-900 line-clamp-1">{job.title}</div>
                        <div className="text-slate-500 text-[11px] mt-0.5">
                          {job.organization} {job.advertisement_number ? `• Advt: ${job.advertisement_number}` : ''}
                        </div>
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
                        <div className="text-[10px] text-slate-400 line-clamp-1">{job.qualification || '—'}</div>
                      </td>
                      <td className="py-3.5 px-4 font-mono text-slate-700 text-[11px]">
                        <div>Start: {job.application_start_date || '—'}</div>
                        <div className="text-slate-500">Last: {job.last_date || '—'}</div>
                      </td>
                      <td className="py-3.5 px-4">
                        <StatusBadge status={job.status} />
                      </td>
                      <td className="py-3.5 px-4 text-right space-x-1.5 whitespace-nowrap">
                        {job.official_notification_url && (
                          <a
                            href={job.official_notification_url}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="p-1.5 text-blue-600 hover:text-blue-800 hover:bg-blue-50 rounded inline-block"
                            title="Official PDF Notice"
                          >
                            <FileText className="w-4 h-4" />
                          </a>
                        )}
                        {job.official_apply_url && (
                          <a
                            href={job.official_apply_url}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="p-1.5 text-emerald-600 hover:text-emerald-800 hover:bg-emerald-50 rounded inline-block"
                            title="Apply Portal"
                          >
                            <ExternalLink className="w-4 h-4" />
                          </a>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
        {alertMsg && (
          <div className={`p-4 rounded-xl text-xs font-semibold ${alertMsg.type === 'success' ? 'bg-emerald-50 text-emerald-800 border border-emerald-200' : 'bg-red-50 text-red-800 border border-red-200'}`}>
            {alertMsg.text}
          </div>
        )}

        {/* Modal for Manual Job Publishing */}
        {showCreateModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm animate-in fade-in">
            <div className="bg-white rounded-2xl max-w-xl w-full p-6 shadow-2xl border border-slate-200 space-y-4 max-h-[90vh] overflow-y-auto">
              <div className="flex items-center justify-between pb-3 border-b border-slate-100">
                <div className="flex items-center gap-2">
                  <div className="p-2 bg-blue-50 text-blue-600 rounded-lg">
                    <Briefcase className="w-5 h-5" />
                  </div>
                  <div>
                    <h3 className="text-sm font-bold text-slate-900">Publish New Recruitment Notice</h3>
                    <p className="text-xs text-slate-500">Notice will be saved directly into Cloudflare D1 database</p>
                  </div>
                </div>
                <button
                  onClick={() => setShowCreateModal(false)}
                  className="p-1 text-slate-400 hover:text-slate-600 rounded-lg"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>

              <form onSubmit={handleCreateJobSubmit} className="space-y-3 text-xs">
                <div>
                  <label className="block font-semibold text-slate-700 mb-1">Recruitment Title *</label>
                  <input
                    type="text"
                    required
                    placeholder="e.g., SSC CGL Examination 2026"
                    value={formTitle}
                    onChange={(e) => setFormTitle(e.target.value)}
                    className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20"
                  />
                </div>

                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="block font-semibold text-slate-700 mb-1">Organization / Department *</label>
                    <input
                      type="text"
                      required
                      placeholder="e.g., Staff Selection Commission"
                      value={formOrg}
                      onChange={(e) => setFormOrg(e.target.value)}
                      className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20"
                    />
                  </div>
                  <div>
                    <label className="block font-semibold text-slate-700 mb-1">Advt / Notice Number</label>
                    <input
                      type="text"
                      placeholder="e.g., F.No. HQ-C1201/2026"
                      value={formAdvtNo}
                      onChange={(e) => setFormAdvtNo(e.target.value)}
                      className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20"
                    />
                  </div>
                </div>

                <div className="grid grid-cols-3 gap-3">
                  <div>
                    <label className="block font-semibold text-slate-700 mb-1">Category</label>
                    <select
                      value={formCategory}
                      onChange={(e) => setFormCategory(e.target.value as JobCategory)}
                      className="w-full px-2 py-2 bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20"
                    >
                      {JOB_CATEGORIES.map((c) => <option key={c} value={c}>{c}</option>)}
                    </select>
                  </div>
                  <div>
                    <label className="block font-semibold text-slate-700 mb-1">State / Scope</label>
                    <input
                      type="text"
                      value={formState}
                      onChange={(e) => setFormState(e.target.value)}
                      className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20"
                    />
                  </div>
                  <div>
                    <label className="block font-semibold text-slate-700 mb-1">Total Vacancies</label>
                    <input
                      type="number"
                      value={formVacancies}
                      onChange={(e) => setFormVacancies(e.target.value)}
                      className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20"
                    />
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="block font-semibold text-slate-700 mb-1">Qualification</label>
                    <input
                      type="text"
                      value={formQual}
                      onChange={(e) => setFormQual(e.target.value)}
                      className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20"
                    />
                  </div>
                  <div>
                    <label className="block font-semibold text-slate-700 mb-1">Salary / Pay Level</label>
                    <input
                      type="text"
                      value={formSalary}
                      onChange={(e) => setFormSalary(e.target.value)}
                      className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20"
                    />
                  </div>
                </div>

                <div className="grid grid-cols-3 gap-3">
                  <div>
                    <label className="block font-semibold text-slate-700 mb-1">Closing Date</label>
                    <input
                      type="date"
                      value={formClosingDate}
                      onChange={(e) => setFormClosingDate(e.target.value)}
                      className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20"
                    />
                  </div>
                  <div>
                    <label className="block font-semibold text-slate-700 mb-1">Official Notice URL (https) *</label>
                    <input
                      type="url"
                      required
                      placeholder="https://…gov.in/notice.pdf"
                      value={formPdfUrl}
                      onChange={(e) => setFormPdfUrl(e.target.value)}
                      className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20"
                    />
                  </div>
                  <div>
                    <label className="block font-semibold text-slate-700 mb-1">Apply Online URL</label>
                    <input
                      type="url"
                      value={formApplyUrl}
                      onChange={(e) => setFormApplyUrl(e.target.value)}
                      className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20"
                    />
                  </div>
                </div>

                <div className="pt-3 border-t border-slate-100 flex items-center justify-end gap-2">
                  <button
                    type="button"
                    onClick={() => setShowCreateModal(false)}
                    className="px-4 py-2 text-slate-600 bg-slate-100 rounded-lg hover:bg-slate-200 font-semibold"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={isSubmitting}
                    className="px-4 py-2 text-white bg-blue-600 rounded-lg hover:bg-blue-700 font-semibold shadow-sm flex items-center gap-1.5"
                  >
                    {isSubmitting ? "Publishing..." : "Publish"}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </main>
    </>
  );
}
