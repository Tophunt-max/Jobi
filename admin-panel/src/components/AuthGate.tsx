'use client';

import React, { createContext, useCallback, useContext, useEffect, useState } from 'react';
import { Eye, EyeOff, Lock, ShieldCheck } from 'lucide-react';
import { authApi, SESSION_EVENT } from '../lib/api';
import { SessionUser } from '../lib/types';

interface SessionContextValue {
  user: SessionUser;
  signOut: () => Promise<void>;
}

const SessionContext = createContext<SessionContextValue | null>(null);

export function useSession(): SessionContextValue {
  const ctx = useContext(SessionContext);
  if (!ctx) throw new Error('useSession must be used inside <AuthGate>');
  return ctx;
}

/**
 * Blocks the whole panel until the user signs in with email and password.
 * The session lives in an HttpOnly cookie; this component only knows who is signed in.
 */
export function AuthGate({ children }: { children: React.ReactNode }) {
  const [state, setState] = useState<'loading' | 'signed-out' | 'signed-in' | 'error'>('loading');
  const [user, setUser] = useState<SessionUser | null>(null);

  const refresh = useCallback(async () => {
    try {
      const me = await authApi.me();
      setUser(me);
      setState(me ? 'signed-in' : 'signed-out');
    } catch {
      setState('error');
    }
  }, []);

  useEffect(() => {
    // Earlier versions kept the Worker URL and the raw admin API key in browser storage.
    // Remove them so a stale copy of the key does not linger on admins' machines.
    try {
      localStorage.removeItem('sarkari_admin_api_url');
      localStorage.removeItem('CLOUDFLARE_ADMIN_KEY');
      localStorage.removeItem('CLOUDFLARE_WORKER_URL');
      sessionStorage.removeItem('sarkari_admin_session_key');
    } catch {
      /* storage may be unavailable (private mode) */
    }
    refresh();
    const onSignedOut = () => {
      setUser(null);
      setState('signed-out');
    };
    window.addEventListener(SESSION_EVENT, onSignedOut);
    return () => window.removeEventListener(SESSION_EVENT, onSignedOut);
  }, [refresh]);

  const signOut = useCallback(async () => {
    await authApi.logout().catch(() => undefined);
    setUser(null);
    setState('signed-out');
  }, []);

  if (state === 'loading') {
    return <div className="min-h-screen flex items-center justify-center text-sm text-slate-400">Loading…</div>;
  }

  if (state === 'error') {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center gap-3 text-sm text-slate-600">
        <p>Could not reach the server.</p>
        <button onClick={refresh} className="px-4 py-2 rounded-lg bg-blue-600 text-white font-semibold">Retry</button>
      </div>
    );
  }

  if (state === 'signed-out' || !user) {
    return <LoginForm onSignedIn={(u) => { setUser(u); setState('signed-in'); }} />;
  }

  return <SessionContext.Provider value={{ user, signOut }}>{children}</SessionContext.Provider>;
}

function LoginForm({ onSignedIn }: { onSignedIn: (u: SessionUser) => void }) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setBusy(true);
    try {
      const u = await authApi.login(email.trim(), password);
      setPassword('');
      onSignedIn(u);
    } catch (err: any) {
      setError(err?.message || 'Sign-in failed');
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-100 p-6">
      <form onSubmit={onSubmit} className="w-full max-w-sm bg-white rounded-2xl shadow-lg border border-slate-200 p-8 space-y-5">
        <div className="flex items-center gap-3">
          <div className="w-11 h-11 rounded-xl bg-blue-600 text-white flex items-center justify-center">
            <ShieldCheck className="w-6 h-6" />
          </div>
          <div>
            <h1 className="text-lg font-bold text-slate-900">Sarkari Portal Admin</h1>
            <p className="text-xs text-slate-500">Sign in to continue</p>
          </div>
        </div>

        <label className="block text-sm">
          <span className="font-medium text-slate-700">Email</span>
          <input
            type="email"
            required
            autoComplete="username"
            autoFocus
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </label>

        <label className="block text-sm">
          <span className="font-medium text-slate-700">Password</span>
          <div className="relative mt-1">
            <input
              type={showPassword ? 'text' : 'password'}
              required
              autoComplete="current-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full rounded-lg border border-slate-300 px-3 py-2 pr-10 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            <button
              type="button"
              onClick={() => setShowPassword((v) => !v)}
              aria-label={showPassword ? 'Hide password' : 'Show password'}
              className="absolute right-2 top-1/2 -translate-y-1/2 p-1 text-slate-400 hover:text-slate-700"
            >
              {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
            </button>
          </div>
        </label>

        {error && (
          <p role="alert" className="text-sm text-rose-600 bg-rose-50 border border-rose-200 rounded-lg px-3 py-2">
            {error}
          </p>
        )}

        <button
          type="submit"
          disabled={busy}
          className="w-full inline-flex items-center justify-center gap-2 rounded-lg bg-blue-600 text-white text-sm font-semibold py-2.5 hover:bg-blue-700 disabled:opacity-50"
        >
          <Lock className="w-4 h-4" />
          {busy ? 'Signing in…' : 'Sign in'}
        </button>

        <p className="text-[11px] text-slate-500">
          Forgot your password? Ask an owner to reset it from Settings &gt; Team.
        </p>
      </form>
    </div>
  );
}
