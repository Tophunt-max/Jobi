'use client';

import React, { useState, useEffect } from 'react';
import { Header } from '../../components/Header';
import { getApiConfig, saveApiConfig, adminApi } from '../../lib/api';
import { 
  Settings, 
  Save, 
  CheckCircle2, 
  AlertTriangle, 
  Server, 
  Key, 
  Globe, 
  Terminal, 
  Copy, 
  Check
} from 'lucide-react';

export default function SettingsPage() {
  const [url, setUrl] = useState('');
  const [key, setKey] = useState('');
  const [testResult, setTestResult] = useState<any | null>(null);
  const [testing, setTesting] = useState(false);
  const [savedMessage, setSavedMessage] = useState(false);
  const [copiedCmd, setCopiedCmd] = useState<string | null>(null);

  useEffect(() => {
    const config = getApiConfig();
    setUrl(config.baseUrl);
    setKey(config.adminKey);
  }, []);

  const handleSave = () => {
    saveApiConfig(url, key);
    setSavedMessage(true);
    setTimeout(() => setSavedMessage(false), 3000);
  };

  const handleTestConnection = async () => {
    setTesting(true);
    setTestResult(null);
    try {
      saveApiConfig(url, key);
      const res = await adminApi.getHealth();
      setTestResult({
        success: true,
        data: res,
      });
    } catch (err: any) {
      setTestResult({
        success: false,
        error: err.message,
      });
    } finally {
      setTesting(false);
    }
  };

  const copyToClipboard = (text: string, id: string) => {
    navigator.clipboard.writeText(text);
    setCopiedCmd(id);
    setTimeout(() => setCopiedCmd(null), 2000);
  };

  return (
    <>
      <Header 
        title="Settings & Cloudflare Configuration" 
        subtitle="Manage Worker endpoint, Admin Secret Key, and review deployment commands"
      />

      <main className="p-8 space-y-8 flex-1 max-w-4xl">
        {/* Connection Settings Card */}
        <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm space-y-6">
          <div className="flex items-center gap-3 border-b border-slate-100 pb-4">
            <div className="w-10 h-10 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center font-bold">
              <Server className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-bold text-slate-900">Cloudflare Worker API Connection</h3>
              <p className="text-xs text-slate-500">Configure the worker URL and secret authorization header</p>
            </div>
          </div>

          <div className="space-y-4">
            <div>
              <label className="text-xs font-bold text-slate-700 block mb-1">
                Cloudflare Worker Base URL:
              </label>
              <div className="relative">
                <Globe className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
                <input
                  type="text"
                  value={url}
                  onChange={(e) => setUrl(e.target.value)}
                  placeholder="https://sarkari-jobs-worker.your-subdomain.workers.dev"
                  className="w-full pl-9 pr-3 py-2 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20 font-mono"
                />
              </div>
              <p className="text-[11px] text-slate-400 mt-1">
                The deployed Cloudflare Worker URL serving `/api/jobs`, `/api/admin/jobs/pending`, and `/api/stats`.
              </p>
            </div>

            <div>
              <label className="text-xs font-bold text-slate-700 block mb-1">
                Admin Secret Key (X-Admin-Key):
              </label>
              <div className="relative">
                <Key className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
                <input
                  type="password"
                  value={key}
                  onChange={(e) => setKey(e.target.value)}
                  placeholder="sarkari_admin_secret_key_change_me"
                  className="w-full pl-9 pr-3 py-2 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20 font-mono"
                />
              </div>
              <p className="text-[11px] text-slate-400 mt-1">
                Must match the `ADMIN_API_KEY` defined in `backend-cloudflare/wrangler.toml`.
              </p>
            </div>

            <div className="flex items-center gap-3 pt-2">
              <button
                onClick={handleSave}
                className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-xs font-bold cursor-pointer inline-flex items-center gap-1.5 shadow-sm"
              >
                <Save className="w-3.5 h-3.5" />
                <span>Save Settings</span>
              </button>

              <button
                onClick={handleTestConnection}
                disabled={testing}
                className="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-800 rounded-lg text-xs font-semibold cursor-pointer border border-slate-200 disabled:opacity-50"
              >
                {testing ? 'Testing...' : 'Test Connection'}
              </button>

              {savedMessage && (
                <span className="text-xs text-emerald-600 font-bold flex items-center gap-1">
                  <CheckCircle2 className="w-4 h-4" />
                  <span>Saved to browser memory!</span>
                </span>
              )}
            </div>

            {/* Test result status */}
            {testResult && (
              <div className={`p-4 rounded-xl border text-xs ${
                testResult.success
                  ? 'bg-emerald-50 border-emerald-200 text-emerald-900'
                  : 'bg-rose-50 border-rose-200 text-rose-900'
              }`}>
                <div className="flex items-center gap-2 font-bold mb-1">
                  {testResult.success ? (
                    <>
                      <CheckCircle2 className="w-4 h-4 text-emerald-600" />
                      <span>Connection Successful!</span>
                    </>
                  ) : (
                    <>
                      <AlertTriangle className="w-4 h-4 text-rose-600" />
                      <span>Connection Failed</span>
                    </>
                  )}
                </div>
                <div className="font-mono text-[11px] mt-1">
                  {testResult.success ? JSON.stringify(testResult.data, null, 2) : testResult.error}
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Cloudflare Deployment Guide Card */}
        <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm space-y-4">
          <div className="flex items-center gap-3 border-b border-slate-100 pb-4">
            <div className="w-10 h-10 rounded-xl bg-orange-50 text-orange-600 flex items-center justify-center font-bold">
              <Terminal className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-bold text-slate-900">How to Deploy this Next.js Admin Panel to Cloudflare</h3>
              <p className="text-xs text-slate-500">Deploy for ₹0 on Cloudflare Pages or Cloudflare Workers</p>
            </div>
          </div>

          <div className="space-y-4 text-xs text-slate-700">
            <div>
              <h4 className="font-bold text-slate-900 mb-1">Method 1: Direct Cloudflare Pages Deploy (Recommended - 1 Command)</h4>
              <p className="text-slate-500 mb-2">Build static output and upload to Cloudflare Pages:</p>
              <div className="bg-slate-900 text-slate-200 p-3 rounded-xl font-mono text-[11px] flex items-center justify-between">
                <code>cd admin-panel && npm run build && npx wrangler pages deploy out --project-name=sarkari-jobs-admin</code>
                <button
                  onClick={() => copyToClipboard('cd admin-panel && npm run build && npx wrangler pages deploy out --project-name=sarkari-jobs-admin', 'cmd1')}
                  className="p-1 hover:text-white text-slate-400"
                >
                  {copiedCmd === 'cmd1' ? <Check className="w-4 h-4 text-emerald-400" /> : <Copy className="w-4 h-4" />}
                </button>
              </div>
            </div>

            <div>
              <h4 className="font-bold text-slate-900 mb-1">Method 2: Git Repository Connect via Cloudflare Dashboard</h4>
              <ol className="list-decimal list-inside space-y-1 text-slate-600 leading-relaxed">
                <li>Log in to <span className="font-semibold text-slate-800">Cloudflare Dashboard</span> &gt; <span className="font-semibold text-slate-800">Workers & Pages</span> &gt; <span className="font-semibold text-slate-800">Create Application</span> &gt; <span className="font-semibold text-slate-800">Pages</span>.</li>
                <li>Connect your GitHub/GitLab repository.</li>
                <li>Set <strong>Root directory</strong>: <code className="bg-slate-100 px-1 py-0.5 rounded font-mono">admin-panel</code></li>
                <li>Set <strong>Framework preset</strong>: <code className="bg-slate-100 px-1 py-0.5 rounded font-mono">Next.js (Static HTML Export)</code></li>
                <li>Set <strong>Build command</strong>: <code className="bg-slate-100 px-1 py-0.5 rounded font-mono">npm run build</code></li>
                <li>Set <strong>Build output directory</strong>: <code className="bg-slate-100 px-1 py-0.5 rounded font-mono">out</code></li>
                <li>Click <strong>Save and Deploy</strong>!</li>
              </ol>
            </div>
          </div>
        </div>
      </main>
    </>
  );
}
