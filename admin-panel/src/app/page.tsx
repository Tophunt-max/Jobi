'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import { Header } from '../components/Header';
import { StatsCard } from '../components/StatsCard';
import { StatusBadge } from '../components/StatusBadge';
import { adminApi } from '../lib/api';
import { DashboardStats, JobItem, JobSource } from '../lib/types';
import { 
  Briefcase, 
  Clock, 
  FileText, 
  Award, 
  Globe2, 
  CheckCircle, 
  ExternalLink, 
  Play, 
  ShieldCheck, 
  ArrowRight,
  Database,
  Cloud,
  Layers,
  Calendar
} from 'lucide-react';

export default function DashboardPage() {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [pendingJobs, setPendingJobs] = useState<JobItem[]>([]);
  const [sources, setSources] = useState<JobSource[]>([]);
  const [loading, setLoading] = useState(true);
  const [actionMessage, setActionMessage] = useState<string | null>(null);

  const loadData = async () => {
    setLoading(true);
    try {
      const [sData, pData, srcData] = await Promise.allSettled([
        adminApi.getStats(),
        adminApi.getPendingJobs(),
        adminApi.getSources(),
      ]);

      if (sData.status === 'fulfilled') setStats(sData.value);
      if (pData.status === 'fulfilled') setPendingJobs(pData.value);
      if (srcData.status === 'fulfilled') setSources(srcData.value);
    } catch (err: any) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleQuickApprove = async (id: string) => {
    try {
      await adminApi.approveJob(id, 'Admin Dashboard');
      setActionMessage('Job approved and published to mobile app! Notification queued.');
      loadData();
      setTimeout(() => setActionMessage(null), 4000);
    } catch (err: any) {
      alert(`Approval error: ${err.message}`);
    }
  };

  return (
    <>
      <Header 
        title="Automated Importer Dashboard" 
        subtitle="Cloudflare Workers + D1 + R2 Pipeline • All India & Odisha Official Recruitment"
        onRefresh={loadData}
        isRefreshing={loading}
      />

      <main className="p-8 space-y-8 flex-1">
        {/* Banner Alert if any */}
        {actionMessage && (
          <div className="bg-emerald-50 border border-emerald-200 text-emerald-800 px-4 py-3 rounded-xl flex items-center justify-between shadow-sm">
            <div className="flex items-center gap-2">
              <CheckCircle className="w-5 h-5 text-emerald-600" />
              <span className="text-sm font-semibold">{actionMessage}</span>
            </div>
          </div>
        )}

        {/* 1. Stats Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-5">
          <StatsCard 
            title="Pending Review Queue" 
            value={stats?.pendingReview ?? pendingJobs.length} 
            subtitle="Scraped notices awaiting admin approval"
            icon={Clock}
            colorScheme="amber"
            trend={stats?.pendingReview ? `${stats.pendingReview} Pending` : 'All Clear'}
          />
          <StatsCard 
            title="Active Published Jobs" 
            value={stats?.latestJobs ?? '...'} 
            subtitle="Visible on Android mobile application"
            icon={Briefcase}
            colorScheme="blue"
          />
          <StatsCard 
            title="Admit Cards & Results" 
            value={(stats?.admitCards ?? 0) + (stats?.results ?? 0)} 
            subtitle={`${stats?.admitCards ?? 0} Cards • ${stats?.results ?? 0} Exam Results`}
            icon={Award}
            colorScheme="emerald"
          />
          <StatsCard 
            title="Official Sources" 
            value={stats?.activeSources ?? sources.length ?? '30+'} 
            subtitle="All India & Odisha verified portals"
            icon={Globe2}
            colorScheme="indigo"
            trend="100% Gov"
          />
        </div>

        {/* 2. Cloudflare Zero-Cost Architecture Highlights */}
        <div className="bg-gradient-to-r from-slate-900 via-indigo-950 to-slate-900 rounded-2xl p-6 text-white shadow-lg border border-slate-800">
          <div className="flex flex-col md:flex-row md:items-center justify-between gap-6">
            <div>
              <div className="flex items-center gap-2">
                <span className="px-2.5 py-0.5 rounded-full bg-blue-500/20 text-blue-300 border border-blue-400/30 text-[11px] font-bold uppercase tracking-wider">
                  Cloudflare Edge Serverless
                </span>
                <span className="text-slate-400 text-xs">Target Budget: ₹0 / month</span>
              </div>
              <h3 className="text-xl font-bold mt-2 text-white">Automated Government Job Ingestion Engine</h3>
              <p className="text-slate-300 text-xs mt-1 max-w-2xl leading-relaxed">
                Runs on Cloudflare Workers with automated Cron Triggers (every 6h), querying official RSS feeds and HTML notices directly. Clean SQLite schema inside Cloudflare D1 with deduplication hashing and zero egress PDF storage in Cloudflare R2.
              </p>
            </div>

            <div className="flex items-center gap-3 shrink-0">
              <Link 
                href="/pending"
                className="px-4 py-2.5 rounded-xl text-xs font-bold bg-amber-500 hover:bg-amber-400 text-slate-950 flex items-center gap-1.5 shadow-md shadow-amber-500/20 transition-all"
              >
                <span>Review Pending Queue</span>
                <ArrowRight className="w-4 h-4" />
              </Link>
              <Link 
                href="/sources"
                className="px-4 py-2.5 rounded-xl text-xs font-bold bg-slate-800 hover:bg-slate-700 text-white border border-slate-700 transition-all"
              >
                Manage 30+ Sources
              </Link>
            </div>
          </div>

          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mt-6 pt-6 border-t border-slate-800/80">
            <div className="flex items-center gap-3">
              <Cloud className="w-5 h-5 text-blue-400" />
              <div>
                <p className="text-[11px] text-slate-400">Execution</p>
                <p className="text-xs font-bold text-slate-200">Worker Cron (6h)</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <Database className="w-5 h-5 text-emerald-400" />
              <div>
                <p className="text-[11px] text-slate-400">Database</p>
                <p className="text-xs font-bold text-slate-200">Cloudflare D1 (SQL)</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <Layers className="w-5 h-5 text-indigo-400" />
              <div>
                <p className="text-[11px] text-slate-400">Object Storage</p>
                <p className="text-xs font-bold text-slate-200">Cloudflare R2 (PDFs)</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <ShieldCheck className="w-5 h-5 text-amber-400" />
              <div>
                <p className="text-[11px] text-slate-400">Publish Flow</p>
                <p className="text-xs font-bold text-slate-200">Admin Approved Only</p>
              </div>
            </div>
          </div>
        </div>

        {/* 3. Pending Review Queue Preview */}
        <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
          <div className="p-6 border-b border-slate-200 flex items-center justify-between">
            <div>
              <h4 className="text-base font-bold text-slate-900">Recent Notices Pending Review</h4>
              <p className="text-xs text-slate-500">Newly fetched notices awaiting verification before mobile publishing</p>
            </div>
            <Link 
              href="/pending"
              className="text-xs font-bold text-blue-600 hover:text-blue-700 flex items-center gap-1"
            >
              <span>View All ({pendingJobs.length})</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </Link>
          </div>

          {loading ? (
            <div className="p-12 text-center text-slate-400 text-sm">Loading pending notices...</div>
          ) : pendingJobs.length === 0 ? (
            <div className="p-12 text-center">
              <div className="w-12 h-12 bg-emerald-50 text-emerald-600 rounded-full flex items-center justify-center mx-auto mb-3">
                <CheckCircle className="w-6 h-6" />
              </div>
              <p className="text-sm font-bold text-slate-800">Pending Queue is Empty</p>
              <p className="text-xs text-slate-500 mt-1">All scraped government notices have been reviewed and published.</p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse text-xs">
                <thead>
                  <tr className="bg-slate-50 border-b border-slate-200 text-slate-600 font-bold uppercase tracking-wider text-[11px]">
                    <th className="py-3 px-5">Organization & Post</th>
                    <th className="py-3 px-4">Category</th>
                    <th className="py-3 px-4">State</th>
                    <th className="py-3 px-4">Posts / Salary</th>
                    <th className="py-3 px-4">Deadline</th>
                    <th className="py-3 px-4 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {pendingJobs.slice(0, 5).map((job) => (
                    <tr key={job.id} className="hover:bg-slate-50/70 transition-colors">
                      <td className="py-3.5 px-5">
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
                        <div>{job.total_posts || 'Check Notice'}</div>
                        <div className="text-[10px] text-slate-400">{job.salary || '7th CPC Scale'}</div>
                      </td>
                      <td className="py-3.5 px-4 text-slate-700 font-mono">
                        {job.last_date || 'N/A'}
                      </td>
                      <td className="py-3.5 px-4 text-right space-x-2">
                        {job.official_notification_url && (
                          <a
                            href={job.official_notification_url}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="p-1.5 text-slate-500 hover:text-slate-800 hover:bg-slate-100 rounded inline-block"
                            title="Open Official Notice"
                          >
                            <ExternalLink className="w-3.5 h-3.5" />
                          </a>
                        )}
                        <button
                          onClick={() => handleQuickApprove(job.id)}
                          className="px-2.5 py-1 bg-emerald-600 hover:bg-emerald-700 text-white rounded font-bold text-[11px] cursor-pointer shadow-sm"
                        >
                          Approve
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* 4. Official Sources Directory Overview */}
        <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h4 className="text-base font-bold text-slate-900">Configured Recruitment Portals</h4>
              <p className="text-xs text-slate-500">Auto-crawled every 6 hours via Cloudflare Workers Cron Triggers</p>
            </div>
            <Link 
              href="/sources"
              className="text-xs font-bold text-blue-600 hover:text-blue-700 flex items-center gap-1"
            >
              <span>Manage All Sources</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </Link>
          </div>

          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3">
            {[
              { name: 'Staff Selection Commission', tag: 'SSC', domain: 'ssc.gov.in' },
              { name: 'Union Public Service Comm.', tag: 'UPSC', domain: 'upsc.gov.in' },
              { name: 'Railway Recruitment Control', tag: 'RRB', domain: 'rrbapply.gov.in' },
              { name: 'Odisha Public Service Comm.', tag: 'OPSC', domain: 'opsc.gov.in' },
              { name: 'Odisha Staff Selection', tag: 'OSSC', domain: 'ossc.gov.in' },
              { name: 'Odisha Sub-ordinate Selection', tag: 'OSSSC', domain: 'osssc.gov.in' },
            ].map((s) => (
              <div key={s.tag} className="p-3 bg-slate-50 rounded-xl border border-slate-200/80">
                <div className="flex items-center justify-between">
                  <span className="font-black text-xs text-blue-700">{s.tag}</span>
                  <span className="w-2 h-2 rounded-full bg-emerald-500" />
                </div>
                <p className="text-[11px] font-bold text-slate-800 line-clamp-1 mt-1">{s.name}</p>
                <p className="text-[10px] text-slate-400 font-mono mt-0.5">{s.domain}</p>
              </div>
            ))}
          </div>
        </div>
      </main>
    </>
  );
}
