'use client';

import React, { createContext, useCallback, useContext, useEffect, useState } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import {
  Briefcase, ChevronDown, Clock, Flag, Globe2, History, LayoutDashboard, LogOut, Megaphone, Menu,
  Moon, Play, Settings, ShieldCheck, Sun, X,
} from 'lucide-react';
import { cn } from '../lib/cn';
import { adminApi } from '../lib/api';
import { useSession } from './AuthGate';
import { useToast } from './Toast';
import { Button } from './ui';

/* ------------------------------------------------------------------ */
/* Shared counters (pending badge) so pages can refresh the sidebar     */
/* ------------------------------------------------------------------ */

interface ShellContextValue {
  pendingCount: number | null;
  refreshCounts: () => void;
  importVersion: number;
}
const ShellContext = createContext<ShellContextValue>({ pendingCount: null, refreshCounts: () => {}, importVersion: 0 });
export const useShell = () => useContext(ShellContext);

const NAV = [
  { section: 'Overview', items: [{ name: 'Dashboard', href: '/', icon: LayoutDashboard }] },
  {
    section: 'Content',
    items: [
      { name: 'Review queue', href: '/pending/', icon: Clock, badge: 'pending' as const },
      { name: 'Notices', href: '/jobs/', icon: Briefcase },
      { name: 'User reports', href: '/reports/', icon: Flag },
    ],
  },
  {
    section: 'Pipeline',
    items: [
      { name: 'Sources', href: '/sources/', icon: Globe2 },
      { name: 'Import runs', href: '/runs/', icon: History },
    ],
  },
  {
    section: 'Business',
    items: [{ name: 'Ads & monetization', href: '/ads/', icon: Megaphone }],
  },
  { section: 'Account', items: [{ name: 'Settings & team', href: '/settings/', icon: Settings }] },
];

function isActive(pathname: string, href: string) {
  const p = pathname.endsWith('/') ? pathname : `${pathname}/`;
  return href === '/' ? p === '/' : p.startsWith(href);
}

/* ------------------------------------------------------------------ */
/* Theme toggle                                                        */
/* ------------------------------------------------------------------ */

function ThemeToggle() {
  const [dark, setDark] = useState(false);
  useEffect(() => setDark(document.documentElement.classList.contains('dark')), []);
  const toggle = () => {
    const next = !dark;
    setDark(next);
    document.documentElement.classList.toggle('dark', next);
    try {
      localStorage.setItem('theme', next ? 'dark' : 'light');
    } catch {}
  };
  return (
    <Button variant="ghost" size="icon" onClick={toggle} aria-label={dark ? 'Switch to light mode' : 'Switch to dark mode'}>
      {dark ? <Sun className="h-4 w-4" /> : <Moon className="h-4 w-4" />}
    </Button>
  );
}

/* ------------------------------------------------------------------ */
/* Sidebar                                                             */
/* ------------------------------------------------------------------ */

function SidebarContent({ pendingCount, onNavigate }: { pendingCount: number | null; onNavigate?: () => void }) {
  const pathname = usePathname() || '/';
  return (
    <div className="flex h-full flex-col">
      <div className="flex h-16 items-center gap-3 px-5">
        <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-gradient-to-br from-primary to-violet-500 text-white shadow-sm">
          <ShieldCheck className="h-5 w-5" />
        </div>
        <div className="leading-tight">
          <p className="text-sm font-semibold">Sarkari Jobs</p>
          <p className="text-[11px] text-sidebar-muted">Admin console</p>
        </div>
      </div>

      <nav className="flex-1 overflow-y-auto px-3 py-2 scrollbar-thin">
        {NAV.map((group) => (
          <div key={group.section} className="mb-5">
            <p className="px-3 pb-1.5 text-[10px] font-semibold uppercase tracking-wider text-sidebar-muted/80">{group.section}</p>
            <ul className="space-y-0.5">
              {group.items.map((item) => {
                const active = isActive(pathname, item.href);
                const Icon = item.icon;
                const count = 'badge' in item && item.badge === 'pending' ? pendingCount : null;
                return (
                  <li key={item.href}>
                    <Link
                      href={item.href}
                      onClick={onNavigate}
                      aria-current={active ? 'page' : undefined}
                      className={cn(
                        'group flex items-center gap-3 rounded-lg px-3 py-2 text-sm transition-colors',
                        active ? 'bg-accent text-accent-foreground font-medium' : 'text-sidebar-muted hover:bg-muted hover:text-sidebar-foreground'
                      )}
                    >
                      <Icon className={cn('h-4 w-4 shrink-0', active ? 'text-accent-foreground' : 'text-sidebar-muted group-hover:text-sidebar-foreground')} />
                      <span className="flex-1 truncate">{item.name}</span>
                      {count !== null && count > 0 && (
                        <span className="rounded-full bg-warning-soft px-2 py-0.5 text-[10px] font-semibold tabular-nums text-warning">{count}</span>
                      )}
                    </Link>
                  </li>
                );
              })}
            </ul>
          </div>
        ))}
      </nav>

      <div className="border-t border-sidebar-border px-5 py-4 text-[11px] text-sidebar-muted">
        <div className="flex items-center justify-between">
          <span>Auto-import</span>
          <span className="font-medium text-success">every 3 h</span>
        </div>
      </div>
    </div>
  );
}

/* ------------------------------------------------------------------ */
/* User menu                                                           */
/* ------------------------------------------------------------------ */

function UserMenu() {
  const { user, signOut } = useSession();
  const [open, setOpen] = useState(false);
  const initial = user.email.charAt(0).toUpperCase();

  useEffect(() => {
    if (!open) return;
    const close = () => setOpen(false);
    document.addEventListener('click', close);
    return () => document.removeEventListener('click', close);
  }, [open]);

  return (
    <div className="relative">
      <button
        onClick={(e) => {
          e.stopPropagation();
          setOpen((v) => !v);
        }}
        className="flex items-center gap-2 rounded-lg p-1 pr-2 hover:bg-muted"
        aria-haspopup="menu"
        aria-expanded={open}
      >
        <span className="flex h-7 w-7 items-center justify-center rounded-full bg-gradient-to-br from-primary to-violet-500 text-xs font-semibold text-white">
          {initial}
        </span>
        <ChevronDown className="hidden h-3.5 w-3.5 text-muted-foreground sm:block" />
      </button>
      {open && (
        <div role="menu" className="absolute right-0 top-11 z-40 w-64 rounded-xl border bg-card p-1.5 shadow-pop animate-slide-up" onClick={(e) => e.stopPropagation()}>
          <div className="px-3 py-2.5">
            <p className="truncate text-sm font-medium">{user.email}</p>
            <p className="text-xs capitalize text-muted-foreground">{user.role}</p>
          </div>
          <div className="my-1 h-px bg-border" />
          <Link href="/settings/" onClick={() => setOpen(false)} className="flex items-center gap-2 rounded-lg px-3 py-2 text-sm hover:bg-muted" role="menuitem">
            <Settings className="h-4 w-4 text-muted-foreground" /> Settings & team
          </Link>
          <button onClick={signOut} className="flex w-full items-center gap-2 rounded-lg px-3 py-2 text-sm text-danger hover:bg-danger-soft" role="menuitem">
            <LogOut className="h-4 w-4" /> Sign out
          </button>
        </div>
      )}
    </div>
  );
}

/* ------------------------------------------------------------------ */
/* Shell                                                               */
/* ------------------------------------------------------------------ */

export function AppShell({ children }: { children: React.ReactNode }) {
  const toast = useToast();
  const pathname = usePathname();
  const [mobileOpen, setMobileOpen] = useState(false);
  const [pendingCount, setPendingCount] = useState<number | null>(null);
  const [health, setHealth] = useState<'ok' | 'down' | 'checking'>('checking');
  const [importing, setImporting] = useState(false);
  const [importVersion, setImportVersion] = useState(0);

  const refreshCounts = useCallback(() => {
    adminApi.getStats().then((s) => setPendingCount(s.pendingReview)).catch(() => undefined);
  }, []);

  useEffect(() => {
    refreshCounts();
    adminApi.getHealth().then((h) => setHealth(h.status === 'ok' ? 'ok' : 'down')).catch(() => setHealth('down'));
  }, [refreshCounts]);

  useEffect(() => setMobileOpen(false), [pathname]);

  const runImport = async () => {
    setImporting(true);
    toast.info('Import started', 'Crawling enabled official sources. This can take a minute.');
    try {
      const { summary } = await adminApi.runImport();
      const msg = `${summary.totalInserted} new · ${summary.totalDuplicates} already known · ${summary.totalErrors} source errors`;
      if (summary.totalErrors > 0 && summary.totalInserted === 0) toast.error('Import finished with errors', msg);
      else toast.success('Import finished', msg);
      refreshCounts();
      setImportVersion((v) => v + 1);
    } catch (err: any) {
      toast.error('Import failed', err?.message);
    } finally {
      setImporting(false);
    }
  };

  return (
    <ShellContext.Provider value={{ pendingCount, refreshCounts, importVersion }}>
      <div className="min-h-screen">
        {/* Desktop sidebar */}
        <aside className="fixed inset-y-0 left-0 z-30 hidden w-64 border-r border-sidebar-border bg-sidebar lg:block">
          <SidebarContent pendingCount={pendingCount} />
        </aside>

        {/* Mobile drawer */}
        {mobileOpen && (
          <div className="fixed inset-0 z-40 lg:hidden">
            <div className="absolute inset-0 bg-black/40 animate-fade-in" onClick={() => setMobileOpen(false)} />
            <aside className="absolute inset-y-0 left-0 w-72 bg-sidebar shadow-pop animate-slide-up">
              <button onClick={() => setMobileOpen(false)} aria-label="Close menu" className="absolute right-3 top-4 rounded-lg p-2 text-muted-foreground hover:bg-muted">
                <X className="h-4 w-4" />
              </button>
              <SidebarContent pendingCount={pendingCount} onNavigate={() => setMobileOpen(false)} />
            </aside>
          </div>
        )}

        <div className="lg:pl-64">
          <header className="sticky top-0 z-20 flex h-16 items-center gap-3 border-b bg-background/80 px-4 backdrop-blur-md sm:px-6">
            <Button variant="ghost" size="icon" className="lg:hidden" onClick={() => setMobileOpen(true)} aria-label="Open menu">
              <Menu className="h-5 w-5" />
            </Button>

            <div className="flex-1" />

            <span
              className={cn(
                'hidden items-center gap-2 rounded-full border px-3 py-1 text-xs sm:inline-flex',
                health === 'ok' ? 'text-success' : health === 'down' ? 'text-danger' : 'text-muted-foreground'
              )}
              title="Backend health"
            >
              <span className={cn('h-1.5 w-1.5 rounded-full', health === 'ok' ? 'bg-success' : health === 'down' ? 'bg-danger' : 'bg-muted-foreground animate-pulse')} />
              {health === 'ok' ? 'All systems normal' : health === 'down' ? 'Backend unreachable' : 'Checking…'}
            </span>

            <Button size="sm" onClick={runImport} loading={importing} icon={<Play className="h-3.5 w-3.5" />}>
              <span className="hidden sm:inline">{importing ? 'Importing…' : 'Run import'}</span>
            </Button>
            <ThemeToggle />
            <UserMenu />
          </header>

          <main className="mx-auto w-full max-w-7xl px-4 py-6 sm:px-6 lg:px-8 lg:py-8 animate-fade-in">{children}</main>
        </div>
      </div>
    </ShellContext.Provider>
  );
}
