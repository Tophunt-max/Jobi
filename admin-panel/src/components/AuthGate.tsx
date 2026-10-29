'use client';

import React, { createContext, useCallback, useContext, useEffect, useState } from 'react';
import { CheckCircle2, Eye, EyeOff, Loader2, Lock, ShieldCheck } from 'lucide-react';
import { Alert, Button, Field, Input } from './ui';
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
    return (
      <div className="flex min-h-screen items-center justify-center">
        <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
      </div>
    );
  }

  if (state === 'error') {
    return (
      <div className="flex min-h-screen flex-col items-center justify-center gap-3 text-sm text-muted-foreground">
        <p>Could not reach the server.</p>
        <Button onClick={refresh}>Retry</Button>
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
    <div className="grid min-h-screen lg:grid-cols-2">
      {/* Brand panel */}
      <div className="relative hidden overflow-hidden bg-gradient-to-br from-indigo-600 via-violet-600 to-fuchsia-600 p-12 text-white lg:flex lg:flex-col lg:justify-between">
        <div className="absolute -right-24 -top-24 h-96 w-96 rounded-full bg-white/10 blur-3xl" />
        <div className="absolute -bottom-32 -left-16 h-96 w-96 rounded-full bg-fuchsia-400/20 blur-3xl" />
        <div className="relative flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-white/15 backdrop-blur">
            <ShieldCheck className="h-5 w-5" />
          </div>
          <span className="text-lg font-semibold">Sarkari Jobs</span>
        </div>
        <div className="relative max-w-md">
          <h2 className="text-3xl font-semibold leading-tight">Verified government notices, published with care.</h2>
          <p className="mt-4 text-white/80">
            Review what the importer finds on official portals, approve what is real, and keep millions of aspirants informed.
          </p>
        </div>
        <ul className="relative space-y-2 text-sm text-white/85">
          {['Every notice reviewed before it reaches the app', 'Sessions secured with HttpOnly cookies', 'Full audit trail of who approved what'].map((t) => (
            <li key={t} className="flex items-center gap-2">
              <CheckCircle2 className="h-4 w-4" /> {t}
            </li>
          ))}
        </ul>
      </div>

      {/* Form */}
      <div className="flex items-center justify-center bg-background p-6 sm:p-12">
        <form onSubmit={onSubmit} className="w-full max-w-sm space-y-6 animate-slide-up">
          <div className="lg:hidden flex h-11 w-11 items-center justify-center rounded-xl bg-gradient-to-br from-primary to-violet-500 text-white">
            <ShieldCheck className="h-5 w-5" />
          </div>
          <div>
            <h1 className="text-2xl font-semibold tracking-tight">Welcome back</h1>
            <p className="mt-1 text-sm text-muted-foreground">Sign in to the admin console</p>
          </div>

          <div className="space-y-4">
            <Field label="Email">
              <Input type="email" required autoComplete="username" autoFocus value={email} onChange={(e) => setEmail(e.target.value)} placeholder="you@example.com" className="h-10" />
            </Field>
            <Field label="Password">
              <div className="relative">
                <Input
                  type={showPassword ? 'text' : 'password'}
                  required
                  autoComplete="current-password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="h-10 pr-10"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword((v) => !v)}
                  aria-label={showPassword ? 'Hide password' : 'Show password'}
                  className="absolute right-2 top-1/2 -translate-y-1/2 rounded p-1 text-muted-foreground hover:text-foreground"
                >
                  {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                </button>
              </div>
            </Field>
          </div>

          {error && <Alert tone="danger">{error}</Alert>}

          <Button type="submit" size="lg" className="w-full" loading={busy} icon={!busy ? <Lock className="h-4 w-4" /> : undefined}>
            {busy ? 'Signing in…' : 'Sign in'}
          </Button>

          <p className="text-center text-xs text-muted-foreground">Forgot your password? Ask an owner to reset it from Settings › Team.</p>
        </form>
      </div>
    </div>
  );
}
