'use client';

import React, { useState, useEffect } from 'react';
import { Header } from '../../components/Header';
import { adminApi } from '../../lib/api';
import { JobSource } from '../../lib/types';
import { 
  Globe2, 
  ExternalLink, 
  Play, 
  CheckCircle, 
  XCircle, 
  RefreshCw, 
  Search, 
  Sliders, 
  Terminal,
  Clock,
  ShieldCheck,
  Check
} from 'lucide-react';

export default function SourcesPage() {
  const [sources, setSources] = useState<JobSource[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [stateFilter, setStateFilter] = useState('ALL');
  const [testingSourceId, setTestingSourceId] = useState<string | null>(null);
  const [testResult, setTestResult] = useState<any | null>(null);
  const [runningSourceId, setRunningSourceId] = useState<string | null>(null);
  const [bannerMessage, setBannerMessage] = useState<string | null>(null);

  const loadSources = async () => {
    setLoading(true);
    try {
      const data = await adminApi.getSources();
      setSources(data);
    } catch (err: any) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadSources();
  }, []);

  const handleToggleSource = async (source: JobSource) => {
    const newStatus = source.enabled === 1 ? 0 : 1;
    try {
      await adminApi.updateSource(source.id, newStatus === 1);
      setSources((prev) =>
        prev.map((s) => (s.id === source.id ? { ...s, enabled: newStatus } : s))
      );
      setBannerMessage(`Updated ${source.name} status to ${newStatus === 1 ? 'ENABLED' : 'DISABLED'}`);
      setTimeout(() => setBannerMessage(null), 4000);
    } catch (err: any) {
      alert(`Update failed: ${err.message}`);
    }
  };

  const handleTestSource = async (sourceId: string) => {
    try {
      setTestingSourceId(sourceId);
      setTestResult(null);
      const res = await adminApi.testSource(sourceId);
      setTestResult({
        sourceId,
        ...res,
      });
    } catch (err: any) {
      setTestResult({
        sourceId,
        success: false,
        error: err.message,
      });
    } finally {
      setTestingSourceId(null);
    }
  };

  const handleRunSingleImport = async (sourceId: string, name: string) => {
    try {
      setRunningSourceId(sourceId);
      const res = await adminApi.runImport(sourceId);
      setBannerMessage(`Crawled ${name}: ${res.summary?.inserted_count || 0} inserted, ${res.summary?.duplicate_count || 0} duplicates skipped.`);
      loadSources();
      setTimeout(() => setBannerMessage(null), 5000);
    } catch (err: any) {
      alert(`Import error: ${err.message}`);
    } finally {
      setRunningSourceId(null);
    }
  };

  const filteredSources = sources.filter((s) => {
    const matchesSearch = 
      s.name.toLowerCase().includes(search.toLowerCase()) ||
      s.official_domain.toLowerCase().includes(search.toLowerCase()) ||
      s.id.toLowerCase().includes(search.toLowerCase());
    const matchesState = stateFilter === 'ALL' || s.state === stateFilter || (stateFilter === 'Odisha' && s.state.includes('Odisha'));
    return matchesSearch && matchesState;
  });

  return (
    <>
      <Header 
        title="Official Government Job Sources (30+)" 
        subtitle="Configure, enable, test, and manually invoke recruitment source adapters"
        onRefresh={loadSources}
        isRefreshing={loading}
      />

      <main className="p-8 space-y-6 flex-1">
        {bannerMessage && (
          <div className="bg-emerald-50 border border-emerald-200 text-emerald-800 px-4 py-3 rounded-xl flex items-center gap-2 text-sm font-semibold shadow-sm animate-fade-in">
            <CheckCircle className="w-5 h-5 text-emerald-600" />
            <span>{bannerMessage}</span>
          </div>
        )}

        {/* Filter and Stats Bar */}
        <div className="bg-white rounded-xl border border-slate-200 p-4 shadow-sm flex flex-col md:flex-row gap-3 items-center justify-between">
          <div className="relative w-full md:w-80">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              placeholder="Search by source name, domain..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full pl-9 pr-3 py-1.5 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500"
            />
          </div>

          <div className="flex items-center gap-3">
            <div className="flex items-center gap-1.5 text-xs font-semibold text-slate-600 bg-slate-100 px-3 py-1.5 rounded-lg border border-slate-200">
              <ShieldCheck className="w-4 h-4 text-emerald-600" />
              <span>{sources.filter(s => s.enabled === 1).length} of {sources.length} Enabled</span>
            </div>

            <select
              value={stateFilter}
              onChange={(e) => setStateFilter(e.target.value)}
              className="text-xs bg-slate-50 border border-slate-200 rounded-lg px-3 py-1.5 focus:outline-none focus:ring-2 focus:ring-blue-500/20"
            >
              <option value="ALL">All Jurisdictions</option>
              <option value="All India">All India Portals</option>
              <option value="Odisha">Odisha State Portals</option>
            </select>
          </div>
        </div>

        {/* Sources Table */}
        <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
          {loading ? (
            <div className="p-16 text-center text-slate-400 text-sm">Loading official sources registry...</div>
          ) : filteredSources.length === 0 ? (
            <div className="p-16 text-center text-slate-500 text-sm">No sources found matching current criteria.</div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse text-xs">
                <thead>
                  <tr className="bg-slate-50 border-b border-slate-200 text-slate-600 font-bold uppercase tracking-wider text-[11px]">
                    <th className="py-3 px-5">Official Portal & Domain</th>
                    <th className="py-3 px-4">Jurisdiction</th>
                    <th className="py-3 px-4">Parser Engine</th>
                    <th className="py-3 px-4">Last Scraped</th>
                    <th className="py-3 px-4">Status</th>
                    <th className="py-3 px-4 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {filteredSources.map((source) => (
                    <tr key={source.id} className="hover:bg-slate-50/70 transition-colors">
                      <td className="py-3.5 px-5">
                        <div className="font-bold text-slate-900">{source.name}</div>
                        <div className="flex items-center gap-2 mt-0.5">
                          <a
                            href={source.listing_url}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="text-[11px] text-blue-600 hover:text-blue-800 hover:underline flex items-center gap-1 font-mono"
                          >
                            <span>{source.official_domain}</span>
                            <ExternalLink className="w-2.5 h-2.5" />
                          </a>
                        </div>
                      </td>
                      <td className="py-3.5 px-4 font-semibold text-slate-700">
                        {source.state}
                      </td>
                      <td className="py-3.5 px-4">
                        <span className="font-mono text-[10px] bg-slate-100 text-slate-700 px-2 py-0.5 rounded border border-slate-200">
                          {source.parser_type}
                        </span>
                      </td>
                      <td className="py-3.5 px-4 text-slate-600 font-mono text-[11px]">
                        {source.last_fetched_at ? source.last_fetched_at.slice(0, 16) : 'Scheduled in Cron'}
                      </td>
                      <td className="py-3.5 px-4">
                        <button
                          onClick={() => handleToggleSource(source)}
                          className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full font-bold text-[10px] border transition-colors cursor-pointer ${
                            source.enabled === 1
                              ? 'bg-emerald-50 text-emerald-700 border-emerald-200 hover:bg-emerald-100'
                              : 'bg-slate-100 text-slate-600 border-slate-200 hover:bg-slate-200'
                          }`}
                        >
                          <span className={`w-1.5 h-1.5 rounded-full ${source.enabled === 1 ? 'bg-emerald-500' : 'bg-slate-400'}`} />
                          <span>{source.enabled === 1 ? 'Enabled' : 'Disabled'}</span>
                        </button>
                      </td>
                      <td className="py-3.5 px-4 text-right space-x-1.5 whitespace-nowrap">
                        <button
                          onClick={() => handleTestSource(source.id)}
                          disabled={testingSourceId === source.id}
                          className="px-2.5 py-1 text-slate-700 hover:text-slate-900 hover:bg-slate-100 rounded border border-slate-200 font-semibold text-[11px] cursor-pointer disabled:opacity-50"
                        >
                          {testingSourceId === source.id ? 'Testing...' : 'Test Adapter'}
                        </button>
                        <button
                          onClick={() => handleRunSingleImport(source.id, source.name)}
                          disabled={runningSourceId === source.id || source.enabled === 0}
                          className="px-2.5 py-1 bg-blue-600 hover:bg-blue-700 text-white rounded font-bold text-[11px] cursor-pointer shadow-sm disabled:opacity-50 inline-flex items-center gap-1"
                        >
                          <Play className={`w-3 h-3 ${runningSourceId === source.id ? 'animate-spin' : ''}`} />
                          <span>{runningSourceId === source.id ? 'Crawling...' : 'Crawl Now'}</span>
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* Live Scraper Test Result Modal */}
        {testResult && (
          <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
            <div className="bg-white rounded-2xl max-w-2xl w-full shadow-2xl border border-slate-200 max-h-[85vh] flex flex-col overflow-hidden">
              <div className="p-6 border-b border-slate-200 flex items-center justify-between bg-slate-50">
                <div className="flex items-center gap-2">
                  <Terminal className="w-5 h-5 text-blue-600" />
                  <h3 className="text-base font-bold text-slate-900">
                    Live Adapter Output ({testResult.sourceId})
                  </h3>
                </div>
                <button
                  onClick={() => setTestResult(null)}
                  className="p-1 rounded-lg text-slate-400 hover:text-slate-700 hover:bg-slate-200 cursor-pointer"
                >
                  <XCircle className="w-5 h-5" />
                </button>
              </div>

              <div className="p-6 overflow-y-auto space-y-4">
                <div className="bg-slate-900 text-emerald-400 p-4 rounded-xl font-mono text-xs overflow-x-auto">
                  <pre>{JSON.stringify(testResult, null, 2)}</pre>
                </div>
              </div>

              <div className="p-4 border-t border-slate-200 bg-slate-50 flex justify-end">
                <button
                  onClick={() => setTestResult(null)}
                  className="px-4 py-2 bg-slate-800 text-white rounded-lg text-xs font-bold cursor-pointer hover:bg-slate-700"
                >
                  Close
                </button>
              </div>
            </div>
          </div>
        )}
      </main>
    </>
  );
}
