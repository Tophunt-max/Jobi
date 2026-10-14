'use client';

import React, { useState, useEffect } from 'react';
import { adminApi, getApiConfig } from '../lib/api';
import { RefreshCw, CheckCircle2, AlertTriangle, Play, Sparkles } from 'lucide-react';

interface HeaderProps {
  title: string;
  subtitle?: string;
  onRefresh?: () => void;
  isRefreshing?: boolean;
}

export function Header({ title, subtitle, onRefresh, isRefreshing }: HeaderProps) {
  const [isRunningImport, setIsRunningImport] = useState(false);
  const [importNotice, setImportNotice] = useState<string | null>(null);
  const [healthStatus, setHealthStatus] = useState<'ok' | 'error' | 'loading'>('loading');

  useEffect(() => {
    adminApi.getHealth()
      .then(() => setHealthStatus('ok'))
      .catch(() => setHealthStatus('error'));
  }, []);

  const handleRunAllImports = async () => {
    try {
      setIsRunningImport(true);
      setImportNotice('Running automated crawler on 30+ official sources...');
      const res = await adminApi.runImport();
      setImportNotice(`Import complete: ${res.summary?.inserted_count || 0} inserted, ${res.summary?.duplicate_count || 0} duplicate skipped`);
      if (onRefresh) onRefresh();
    } catch (err: any) {
      setImportNotice(`Import failed: ${err.message}`);
    } finally {
      setIsRunningImport(false);
      setTimeout(() => setImportNotice(null), 6000);
    }
  };

  return (
    <header className="h-16 bg-white border-b border-slate-200 px-8 flex items-center justify-between sticky top-0 z-20 shadow-sm">
      <div>
        <h2 className="text-lg font-bold text-slate-900 tracking-tight">{title}</h2>
        {subtitle && <p className="text-xs text-slate-500">{subtitle}</p>}
      </div>

      <div className="flex items-center gap-3">
        {importNotice && (
          <span className="text-xs px-3 py-1 rounded-full bg-blue-50 text-blue-700 font-medium border border-blue-200 animate-pulse">
            {importNotice}
          </span>
        )}

        {/* Cloudflare Connection Indicator */}
        <div className="flex items-center gap-1.5 px-3 py-1 rounded-full bg-slate-100 text-xs text-slate-600 border border-slate-200">
          <span className={`w-2 h-2 rounded-full ${
            healthStatus === 'ok' ? 'bg-emerald-500' : healthStatus === 'error' ? 'bg-rose-500' : 'bg-amber-400 animate-ping'
          }`} />
          <span className="font-medium text-[11px]">
            {healthStatus === 'ok' ? 'Worker Connected' : healthStatus === 'error' ? 'Check Worker URL' : 'Connecting...'}
          </span>
        </div>

        {/* Run Import Action */}
        <button
          onClick={handleRunAllImports}
          disabled={isRunningImport}
          className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-lg text-xs font-semibold bg-gradient-to-r from-blue-600 to-indigo-600 text-white shadow-sm hover:from-blue-700 hover:to-indigo-700 disabled:opacity-50 transition-all cursor-pointer"
        >
          <Play className={`w-3.5 h-3.5 fill-current ${isRunningImport ? 'animate-spin' : ''}`} />
          <span>{isRunningImport ? 'Crawling...' : 'Run Automated Import'}</span>
        </button>

        {/* Refresh Button */}
        {onRefresh && (
          <button
            onClick={onRefresh}
            disabled={isRefreshing}
            title="Refresh Data"
            className="p-2 text-slate-600 hover:text-slate-900 hover:bg-slate-100 rounded-lg transition-colors border border-slate-200 cursor-pointer"
          >
            <RefreshCw className={`w-4 h-4 ${isRefreshing ? 'animate-spin text-blue-600' : ''}`} />
          </button>
        )}
      </div>
    </header>
  );
}
