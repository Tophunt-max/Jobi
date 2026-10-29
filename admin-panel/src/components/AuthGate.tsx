'use client';

import React, { useEffect, useState } from 'react';
import { Lock, ShieldCheck } from 'lucide-react';
import { adminApi, getBaseUrl, getSessionKey, setBaseUrl, setSessionKey } from '../lib/api';

/**
 * Blocks the whole panel until the admin signs in with the Worker's ADMIN_API_KEY.
 * The key is verified against the Worker before being kept in sessionStorage.
 */
export function AuthGate({ children }: { children: React.ReactNode }) {
  const [ready, setReady] = useState(false);
  const [signedIn, setSignedIn] = useState(false);
  const [url, setUrl] = useState('');
  const [key, setKey] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    const sync = () => setSignedIn(!!getSessionKey());
    setUrl(getBaseUrl());
    sync();
    setReady(true);
    window.addEventListener('admin-session-changed', sync);
    return () => window.removeEventListener('admin-session-changed', sync);
  }, []);

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setBusy(true);
    try {
      setBaseUrl(url);
      await adminApi.verifyKey(url, key);
      setSessionKey(key);
      setKey('');
      setSignedIn(true);
    } catch (err: any) {
      setError(err?.message || 'Sign-in failed');
    } finally {
      setBusy(false);
    }
  };

  if (!ready) return null;
  if (signedIn) return <>{children}</>;

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-100 p-6">
      <form onSubmit={onSubmit} className="w-full max-w-md bg-white rounded-2xl shadow-lg border border-slate-200 p-8 space-y-5">
        <div className="flex items-center gap-3">
          <div className="w-11 h-11 rounded-xl bg-blue-600 text-white flex items-center justify-center">
            <ShieldCheck className="w-6 h-6" />
          </div>
          <div>
            <h1 className="text-lg font-bold text-slate-900">Sarkari Portal Admin</h1>
            <p className="text-xs text-slate-500">Sign in with the Worker admin key</p>
          </div>
        </div>

        <label className="block text-sm">
          <span className="font-medium text-slate-700">Worker URL</span>
          <input
            type="url"
            required
            value={url}
            onChange={(e) => setUrl(e.target.value)}
            placeholder="https://sarkari-jobs-worker.<account>.workers.dev"
            className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </label>

        <label className="block text-sm">
          <span className="font-medium text-slate-700">Admin key</span>
          <input
            type="password"
            required
            autoComplete="current-password"
            value={key}
            onChange={(e) => setKey(e.target.value)}
            className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </label>

        {error && <p className="text-sm text-rose-600 bg-rose-50 border border-rose-200 rounded-lg px-3 py-2">{error}</p>}

        <button
          type="submit"
          disabled={busy}
          className="w-full inline-flex items-center justify-center gap-2 rounded-lg bg-blue-600 text-white text-sm font-semibold py-2.5 hover:bg-blue-700 disabled:opacity-50"
        >
          <Lock className="w-4 h-4" />
          {busy ? 'Verifying…' : 'Sign in'}
        </button>

        <p className="text-[11px] text-slate-500">
          The key is kept only for this browser tab (sessionStorage) and is sent to the Worker as a Bearer token.
        </p>
      </form>
    </div>
  );
}
