'use client';

import React, { useEffect, useState } from 'react';
import { KeyRound, LogOut, MoreHorizontal, Server, UserPlus, Users } from 'lucide-react';
import { adminApi, authApi } from '../../lib/api';
import { AdminUser } from '../../lib/types';
import { timeAgo } from '../../lib/cn';
import {
  Alert, Badge, Button, Card, CardContent, CardHeader, Dialog, Field, Input, PageHeader, Select, Skeleton, Table, Td, Th,
} from '../../components/ui';
import { useSession } from '../../components/AuthGate';
import { useToast } from '../../components/Toast';

const MIN_PASSWORD = 12;

function strength(pw: string): { score: number; label: string } {
  let s = 0;
  if (pw.length >= MIN_PASSWORD) s++;
  if (pw.length >= 16) s++;
  if (/[a-z]/.test(pw) && /[A-Z]/.test(pw)) s++;
  if (/\d/.test(pw)) s++;
  if (/[^A-Za-z0-9]/.test(pw)) s++;
  const label = pw.length < MIN_PASSWORD ? 'Too short' : s <= 2 ? 'Fair' : s <= 3 ? 'Good' : 'Strong';
  return { score: Math.min(s, 4), label };
}

function StrengthMeter({ password }: { password: string }) {
  if (!password) return null;
  const { score, label } = strength(password);
  const color = score <= 1 ? 'bg-danger' : score === 2 ? 'bg-warning' : 'bg-success';
  return (
    <div className="flex items-center gap-3">
      <div className="flex flex-1 gap-1">
        {[0, 1, 2, 3].map((i) => (
          <div key={i} className={`h-1 flex-1 rounded-full ${i < score ? color : 'bg-muted'}`} />
        ))}
      </div>
      <span className="text-[11px] text-muted-foreground">{label}</span>
    </div>
  );
}

function ChangePassword() {
  const toast = useToast();
  const [current, setCurrent] = useState('');
  const [next, setNext] = useState('');
  const [confirm, setConfirm] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    if (next.length < MIN_PASSWORD) return setError(`Use at least ${MIN_PASSWORD} characters.`);
    if (next !== confirm) return setError('The new passwords do not match.');
    setBusy(true);
    try {
      const res = await authApi.changePassword(current, next);
      toast.success('Password changed', res.message);
      setCurrent('');
      setNext('');
      setConfirm('');
    } catch (err: any) {
      setError(err?.message || 'Could not change password');
    } finally {
      setBusy(false);
    }
  };

  return (
    <Card>
      <CardHeader icon={<KeyRound className="h-4 w-4" />} title="Password" description="Changing it signs you out on every other device." />
      <CardContent>
        <form onSubmit={submit} className="grid max-w-md gap-4">
          <Field label="Current password">
            <Input type="password" autoComplete="current-password" required value={current} onChange={(e) => setCurrent(e.target.value)} />
          </Field>
          <Field label="New password" hint={`At least ${MIN_PASSWORD} characters. A passphrase of a few words works well.`}>
            <Input type="password" autoComplete="new-password" required value={next} onChange={(e) => setNext(e.target.value)} />
          </Field>
          <StrengthMeter password={next} />
          <Field label="Confirm new password">
            <Input type="password" autoComplete="new-password" required value={confirm} onChange={(e) => setConfirm(e.target.value)} />
          </Field>
          {error && <Alert tone="danger">{error}</Alert>}
          <div>
            <Button type="submit" loading={busy}>Update password</Button>
          </div>
        </form>
      </CardContent>
    </Card>
  );
}

function Team({ currentEmail }: { currentEmail: string }) {
  const toast = useToast();
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [loading, setLoading] = useState(true);
  const [inviting, setInviting] = useState(false);
  const [resetting, setResetting] = useState<AdminUser | null>(null);
  const [menu, setMenu] = useState<string | null>(null);
  const [form, setForm] = useState({ email: '', password: '', role: 'editor' as 'editor' | 'owner' });
  const [resetPw, setResetPw] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const load = async () => {
    setLoading(true);
    try {
      setUsers(await authApi.listUsers());
    } catch (err: any) {
      toast.error('Could not load team', err?.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (!menu) return;
    const close = () => setMenu(null);
    document.addEventListener('click', close);
    return () => document.removeEventListener('click', close);
  }, [menu]);

  const invite = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    if (form.password.length < MIN_PASSWORD) return setError(`Temporary password must be at least ${MIN_PASSWORD} characters.`);
    setBusy(true);
    try {
      const u = await authApi.createUser(form.email.trim(), form.password, form.role);
      toast.success('Member added', `Share the temporary password with ${u.email} privately.`);
      setInviting(false);
      setForm({ email: '', password: '', role: 'editor' });
      load();
    } catch (err: any) {
      setError(err?.message || 'Could not add member');
    } finally {
      setBusy(false);
    }
  };

  const toggleDisabled = async (u: AdminUser) => {
    setMenu(null);
    if (!u.disabled && !window.confirm(`Disable ${u.email}? They will be signed out immediately.`)) return;
    try {
      const res = await authApi.setUserDisabled(u.id, !u.disabled);
      toast.success(res.message);
      load();
    } catch (err: any) {
      toast.error('Update failed', err?.message);
    }
  };

  const doReset = async () => {
    if (!resetting) return;
    setError(null);
    if (resetPw.length < MIN_PASSWORD) return setError(`Use at least ${MIN_PASSWORD} characters.`);
    setBusy(true);
    try {
      await authApi.resetUserPassword(resetting.id, resetPw);
      toast.success('Password reset', `${resetting.email} was signed out everywhere.`);
      setResetting(null);
      setResetPw('');
      load();
    } catch (err: any) {
      setError(err?.message || 'Reset failed');
    } finally {
      setBusy(false);
    }
  };

  return (
    <Card>
      <CardHeader
        icon={<Users className="h-4 w-4" />}
        title="Team"
        description="Owners manage members. Editors review, publish and configure everything else."
        actions={<Button size="sm" icon={<UserPlus className="h-3.5 w-3.5" />} onClick={() => { setError(null); setInviting(true); }}>Add member</Button>}
      />
      {loading ? (
        <div className="space-y-2 px-5 pb-5">{[0, 1].map((i) => <Skeleton key={i} className="h-12 w-full" />)}</div>
      ) : (
        <Table>
          <thead>
            <tr>
              <Th>Member</Th>
              <Th>Role</Th>
              <Th>Status</Th>
              <Th className="hidden sm:table-cell">Last sign-in</Th>
              <Th className="w-10" />
            </tr>
          </thead>
          <tbody>
            {users.map((u) => (
              <tr key={u.id}>
                <Td>
                  <div className="flex items-center gap-3">
                    <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-accent text-xs font-semibold text-accent-foreground">{u.email[0].toUpperCase()}</span>
                    <span className="truncate font-medium">
                      {u.email}
                      {u.email === currentEmail && <span className="ml-1.5 text-xs font-normal text-muted-foreground">(you)</span>}
                    </span>
                  </div>
                </Td>
                <Td><Badge tone={u.role === 'owner' ? 'primary' : 'neutral'}>{u.role === 'owner' ? 'Owner' : 'Editor'}</Badge></Td>
                <Td>
                  {u.disabled ? <Badge tone="danger" dot>Disabled</Badge> : u.locked ? <Badge tone="warning" dot>Locked</Badge> : <Badge tone="success" dot>Active</Badge>}
                </Td>
                <Td className="hidden text-xs text-muted-foreground sm:table-cell">{timeAgo(u.last_login_at)}</Td>
                <Td className="relative">
                  {u.email !== currentEmail && (
                    <>
                      <Button variant="ghost" size="icon" aria-label={`Actions for ${u.email}`} onClick={(e) => { e.stopPropagation(); setMenu(menu === u.id ? null : u.id); }}>
                        <MoreHorizontal className="h-4 w-4" />
                      </Button>
                      {menu === u.id && (
                        <div className="absolute right-4 top-11 z-20 w-44 rounded-xl border bg-card p-1 shadow-pop animate-slide-up" onClick={(e) => e.stopPropagation()}>
                          <button className="w-full rounded-lg px-3 py-2 text-left text-sm hover:bg-muted" onClick={() => { setMenu(null); setError(null); setResetting(u); }}>Reset password</button>
                          <button className={`w-full rounded-lg px-3 py-2 text-left text-sm ${u.disabled ? 'hover:bg-muted' : 'text-danger hover:bg-danger-soft'}`} onClick={() => toggleDisabled(u)}>
                            {u.disabled ? 'Enable account' : 'Disable account'}
                          </button>
                        </div>
                      )}
                    </>
                  )}
                </Td>
              </tr>
            ))}
          </tbody>
        </Table>
      )}

      <Dialog
        open={inviting}
        onClose={() => setInviting(false)}
        size="sm"
        title="Add a team member"
        description="They sign in with this email and the temporary password, then change it in Settings."
        footer={<><Button variant="outline" onClick={() => setInviting(false)}>Cancel</Button><Button type="submit" form="invite-form" loading={busy}>Add member</Button></>}
      >
        <form id="invite-form" onSubmit={invite} className="space-y-4">
          <Field label="Email"><Input type="email" required value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} placeholder="name@example.com" /></Field>
          <Field label="Temporary password" hint={`At least ${MIN_PASSWORD} characters`}>
            <Input type="text" required autoComplete="off" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} />
          </Field>
          <StrengthMeter password={form.password} />
          <Field label="Role">
            <Select value={form.role} onChange={(e) => setForm({ ...form, role: e.target.value as 'editor' | 'owner' })}>
              <option value="editor">Editor: review and publish</option>
              <option value="owner">Owner: also manages the team</option>
            </Select>
          </Field>
          {error && <Alert tone="danger">{error}</Alert>}
        </form>
      </Dialog>

      <Dialog
        open={!!resetting}
        onClose={() => setResetting(null)}
        size="sm"
        title={`Reset password for ${resetting?.email ?? ''}`}
        description="They will be signed out everywhere and must use the new password."
        footer={<><Button variant="outline" onClick={() => setResetting(null)}>Cancel</Button><Button loading={busy} onClick={doReset}>Reset password</Button></>}
      >
        <div className="space-y-4">
          <Field label="New temporary password"><Input type="text" autoComplete="off" value={resetPw} onChange={(e) => setResetPw(e.target.value)} /></Field>
          <StrengthMeter password={resetPw} />
          {error && <Alert tone="danger">{error}</Alert>}
        </div>
      </Dialog>
    </Card>
  );
}

export default function SettingsPage() {
  const { user, signOut } = useSession();
  const [health, setHealth] = useState<{ status: string; database: string; storage: string } | null>(null);

  useEffect(() => {
    adminApi.getHealth().then(setHealth).catch(() => setHealth({ status: 'down', database: 'unknown', storage: 'unknown' }));
  }, []);

  return (
    <div className="space-y-6">
      <PageHeader title="Settings" description="Your account, your team and the backend." />

      <div className="grid gap-6 lg:grid-cols-3">
        <div className="space-y-6 lg:col-span-2">
          <ChangePassword />
          {user.role === 'owner' && <Team currentEmail={user.email} />}
        </div>

        <div className="space-y-6">
          <Card>
            <CardContent className="pt-5">
              <div className="flex items-center gap-3">
                <span className="flex h-11 w-11 items-center justify-center rounded-full bg-gradient-to-br from-primary to-violet-500 text-base font-semibold text-white">{user.email[0].toUpperCase()}</span>
                <div className="min-w-0">
                  <p className="truncate font-medium">{user.email}</p>
                  <p className="text-xs capitalize text-muted-foreground">{user.role}</p>
                </div>
              </div>
              <p className="mt-4 text-xs text-muted-foreground">Sessions end after 12 hours, or after 2 hours without activity.</p>
              <Button variant="outline" className="mt-4 w-full" icon={<LogOut className="h-4 w-4" />} onClick={signOut}>Sign out</Button>
            </CardContent>
          </Card>

          <Card>
            <CardHeader icon={<Server className="h-4 w-4" />} title="Backend" description="Cloudflare Worker + D1" />
            <CardContent className="space-y-2 text-sm">
              {!health ? (
                <Skeleton className="h-16 w-full" />
              ) : (
                [
                  ['API', health.status === 'ok' ? 'Operational' : 'Unreachable', health.status === 'ok'],
                  ['Database', health.database, health.database === 'connected'],
                  ['File storage (R2)', health.storage === 'connected' ? 'connected' : 'not configured', health.storage === 'connected'],
                ].map(([k, v, ok]) => (
                  <div key={k as string} className="flex items-center justify-between">
                    <span className="text-muted-foreground">{k}</span>
                    <Badge tone={ok ? 'success' : 'warning'} dot>{v as string}</Badge>
                  </div>
                ))
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
