'use client';

import React, { useEffect, useState } from 'react';
import { CheckCircle2, KeyRound, LogOut, Server, ShieldAlert, UserPlus, Users } from 'lucide-react';
import { Header } from '../../components/Header';
import { useSession } from '../../components/AuthGate';
import { adminApi, authApi } from '../../lib/api';
import { AdminUser } from '../../lib/types';

const MIN_PASSWORD = 12;

function Card({ icon, title, subtitle, children }: { icon: React.ReactNode; title: string; subtitle: string; children: React.ReactNode }) {
  return (
    <section className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm space-y-5">
      <div className="flex items-center gap-3 border-b border-slate-100 pb-4">
        <div className="w-10 h-10 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center">{icon}</div>
        <div>
          <h3 className="text-base font-bold text-slate-900">{title}</h3>
          <p className="text-xs text-slate-500">{subtitle}</p>
        </div>
      </div>
      {children}
    </section>
  );
}

function Notice({ kind, text }: { kind: 'ok' | 'error'; text: string }) {
  return (
    <p role={kind === 'error' ? 'alert' : 'status'} className={`text-xs rounded-lg px-3 py-2 border ${
      kind === 'ok' ? 'bg-emerald-50 border-emerald-200 text-emerald-800' : 'bg-rose-50 border-rose-200 text-rose-700'
    }`}>
      {text}
    </p>
  );
}

const inputCls = 'w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500';

function ChangePasswordCard() {
  const [current, setCurrent] = useState('');
  const [next, setNext] = useState('');
  const [confirm, setConfirm] = useState('');
  const [busy, setBusy] = useState(false);
  const [msg, setMsg] = useState<{ kind: 'ok' | 'error'; text: string } | null>(null);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setMsg(null);
    if (next.length < MIN_PASSWORD) return setMsg({ kind: 'error', text: `New password must be at least ${MIN_PASSWORD} characters.` });
    if (next !== confirm) return setMsg({ kind: 'error', text: 'New passwords do not match.' });
    setBusy(true);
    try {
      const res = await authApi.changePassword(current, next);
      setMsg({ kind: 'ok', text: res.message });
      setCurrent(''); setNext(''); setConfirm('');
    } catch (err: any) {
      setMsg({ kind: 'error', text: err?.message || 'Could not change password' });
    } finally {
      setBusy(false);
    }
  };

  return (
    <Card icon={<KeyRound className="w-5 h-5" />} title="Change password" subtitle="Changing it signs out your other devices">
      <form onSubmit={submit} className="grid gap-3 max-w-md">
        <input type="password" autoComplete="current-password" placeholder="Current password" required value={current} onChange={(e) => setCurrent(e.target.value)} className={inputCls} />
        <input type="password" autoComplete="new-password" placeholder={`New password (min ${MIN_PASSWORD} characters)`} required value={next} onChange={(e) => setNext(e.target.value)} className={inputCls} />
        <input type="password" autoComplete="new-password" placeholder="Confirm new password" required value={confirm} onChange={(e) => setConfirm(e.target.value)} className={inputCls} />
        {msg && <Notice {...msg} />}
        <button type="submit" disabled={busy} className="justify-self-start px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-xs font-bold disabled:opacity-50">
          {busy ? 'Saving…' : 'Change password'}
        </button>
      </form>
    </Card>
  );
}

function TeamCard({ currentEmail }: { currentEmail: string }) {
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [loading, setLoading] = useState(true);
  const [msg, setMsg] = useState<{ kind: 'ok' | 'error'; text: string } | null>(null);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState<'editor' | 'owner'>('editor');
  const [busy, setBusy] = useState(false);

  const load = async () => {
    setLoading(true);
    try {
      setUsers(await authApi.listUsers());
    } catch (err: any) {
      setMsg({ kind: 'error', text: err?.message || 'Could not load users' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const create = async (e: React.FormEvent) => {
    e.preventDefault();
    setMsg(null);
    if (password.length < MIN_PASSWORD) return setMsg({ kind: 'error', text: `Password must be at least ${MIN_PASSWORD} characters.` });
    setBusy(true);
    try {
      const u = await authApi.createUser(email.trim(), password, role);
      setMsg({ kind: 'ok', text: `Created ${u.email}. Share the password with them privately; they can change it after signing in.` });
      setEmail(''); setPassword(''); setRole('editor');
      await load();
    } catch (err: any) {
      setMsg({ kind: 'error', text: err?.message || 'Could not create user' });
    } finally {
      setBusy(false);
    }
  };

  const toggle = async (u: AdminUser) => {
    if (!u.disabled && !confirm(`Disable ${u.email}? They will be signed out immediately.`)) return;
    try {
      const res = await authApi.setUserDisabled(u.id, !u.disabled);
      setMsg({ kind: 'ok', text: res.message });
      await load();
    } catch (err: any) {
      setMsg({ kind: 'error', text: err?.message || 'Update failed' });
    }
  };

  const reset = async (u: AdminUser) => {
    const pw = window.prompt(`New password for ${u.email} (min ${MIN_PASSWORD} characters):`);
    if (!pw) return;
    try {
      const res = await authApi.resetUserPassword(u.id, pw);
      setMsg({ kind: 'ok', text: `${res.message}. They have been signed out everywhere.` });
      await load();
    } catch (err: any) {
      setMsg({ kind: 'error', text: err?.message || 'Reset failed' });
    }
  };

  return (
    <Card icon={<Users className="w-5 h-5" />} title="Team" subtitle="Owners manage accounts; editors review and publish notices">
      {msg && <Notice {...msg} />}
      {loading ? (
        <p className="text-xs text-slate-400">Loading…</p>
      ) : (
        <table className="w-full text-left text-xs">
          <thead>
            <tr className="text-slate-500 uppercase text-[10px] tracking-wider border-b border-slate-200">
              <th className="py-2">Email</th><th>Role</th><th>Status</th><th>Last sign-in</th><th className="text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {users.map((u) => (
              <tr key={u.id}>
                <td className="py-2.5 font-semibold text-slate-800">{u.email}{u.email === currentEmail && <span className="ml-1 text-slate-400 font-normal">(you)</span>}</td>
                <td className="capitalize">{u.role}</td>
                <td>{u.disabled ? <span className="text-rose-600 font-bold">Disabled</span> : u.locked ? <span className="text-amber-600 font-bold">Locked</span> : <span className="text-emerald-600 font-bold">Active</span>}</td>
                <td className="font-mono text-[11px] text-slate-500">{u.last_login_at ? u.last_login_at.replace('T', ' ').slice(0, 16) : '—'}</td>
                <td className="text-right space-x-1.5 whitespace-nowrap">
                  {u.email !== currentEmail && (
                    <>
                      <button onClick={() => reset(u)} className="px-2 py-1 rounded border border-slate-200 hover:bg-slate-50 font-semibold">Reset password</button>
                      <button onClick={() => toggle(u)} className={`px-2 py-1 rounded font-semibold ${u.disabled ? 'bg-emerald-600 text-white' : 'bg-rose-50 text-rose-700 border border-rose-200'}`}>
                        {u.disabled ? 'Enable' : 'Disable'}
                      </button>
                    </>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      <form onSubmit={create} className="grid gap-3 sm:grid-cols-[1fr_1fr_auto_auto] items-end pt-2 border-t border-slate-100">
        <input type="email" required placeholder="new.member@example.com" value={email} onChange={(e) => setEmail(e.target.value)} className={inputCls} />
        <input type="password" required autoComplete="new-password" placeholder={`Temporary password (min ${MIN_PASSWORD})`} value={password} onChange={(e) => setPassword(e.target.value)} className={inputCls} />
        <select value={role} onChange={(e) => setRole(e.target.value as 'editor' | 'owner')} className={inputCls}>
          <option value="editor">Editor</option>
          <option value="owner">Owner</option>
        </select>
        <button type="submit" disabled={busy} className="inline-flex items-center gap-1.5 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-xs font-bold disabled:opacity-50">
          <UserPlus className="w-3.5 h-3.5" /> {busy ? 'Adding…' : 'Add member'}
        </button>
      </form>
    </Card>
  );
}

export default function SettingsPage() {
  const { user, signOut } = useSession();
  const [health, setHealth] = useState<string | null>(null);

  useEffect(() => {
    adminApi.getHealth()
      .then((h) => setHealth(`Backend ${h.status} • database ${h.database} • file storage ${h.storage}`))
      .catch((e) => setHealth(`Backend unreachable: ${e.message}`));
  }, []);

  return (
    <>
      <Header title="Settings" subtitle="Your account, team access and backend status" />
      <main className="p-8 space-y-6 flex-1 max-w-4xl">
        <Card icon={<ShieldAlert className="w-5 h-5" />} title="Signed in" subtitle="Sessions expire after 12 hours, or after 2 hours of inactivity">
          <div className="flex items-center justify-between gap-4 text-sm">
            <div>
              <div className="font-bold text-slate-900">{user.email}</div>
              <div className="text-xs text-slate-500 capitalize">{user.role}</div>
            </div>
            <button onClick={signOut} className="inline-flex items-center gap-1.5 px-3 py-2 rounded-lg border border-slate-300 text-xs font-semibold hover:bg-slate-50">
              <LogOut className="w-3.5 h-3.5" /> Sign out
            </button>
          </div>
        </Card>

        <ChangePasswordCard />

        {user.role === 'owner' && <TeamCard currentEmail={user.email} />}

        <Card icon={<Server className="w-5 h-5" />} title="Backend" subtitle="Cloudflare Worker + D1">
          <p className="text-xs text-slate-600 flex items-center gap-2">
            <CheckCircle2 className="w-4 h-4 text-emerald-500" /> {health || 'Checking…'}
          </p>
        </Card>
      </main>
    </>
  );
}
