'use client';

import React, { useState, useEffect } from 'react';
import { Header } from '../../components/Header';
import { StatusBadge } from '../../components/StatusBadge';
import { adminApi } from '../../lib/api';
import { JobItem } from '../../lib/types';
import { 
  Briefcase, 
  Search, 
  ExternalLink, 
  FileText, 
  Calendar, 
  Building2,
  CheckCircle2,
  Filter
} from 'lucide-react';

export default function AllJobsPage() {
  const [jobs, setJobs] = useState<JobItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [categoryFilter, setCategoryFilter] = useState('ALL');

  const loadJobs = async () => {
    setLoading(true);
    try {
      const data = await adminApi.getAllJobs({
        status: statusFilter,
        category: categoryFilter !== 'ALL' ? categoryFilter : undefined,
        search: search || undefined,
        limit: 100,
      });
      setJobs(data);
    } catch (err: any) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadJobs();
  }, [statusFilter, categoryFilter]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    loadJobs();
  };

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
              placeholder="Search title, organization, advt... (Press Enter)"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full pl-9 pr-3 py-1.5 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500"
            />
          </form>

          <div className="flex items-center gap-2">
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
              <option value="Latest Job">Latest Job</option>
              <option value="Admit Card">Admit Card</option>
              <option value="Result">Result</option>
              <option value="Answer Key">Answer Key</option>
              <option value="Syllabus">Syllabus</option>
            </select>
          </div>
        </div>

        {/* Database Table */}
        <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
          <div className="p-4 border-b border-slate-200 flex items-center justify-between bg-slate-50/50">
            <span className="text-xs font-bold text-slate-700">
              Found {jobs.length} records in Cloudflare D1
            </span>
          </div>

          {loading ? (
            <div className="p-16 text-center text-slate-400 text-sm">Querying Cloudflare D1 database...</div>
          ) : jobs.length === 0 ? (
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
                  {jobs.map((job) => (
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
                        <div className="font-medium text-slate-800">{job.total_posts || 'Check Notice'}</div>
                        <div className="text-[10px] text-slate-400 line-clamp-1">{job.qualification || 'As per norms'}</div>
                      </td>
                      <td className="py-3.5 px-4 font-mono text-slate-700 text-[11px]">
                        <div>Start: {job.start_date || 'Active'}</div>
                        <div className="text-slate-500">Last: {job.last_date || 'N/A'}</div>
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
      </main>
    </>
  );
}
