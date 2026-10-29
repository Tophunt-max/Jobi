'use client';

import React, { useCallback, useEffect, useState } from 'react';
import Link from 'next/link';
import { AlertTriangle, ArrowRight, Briefcase, CheckCircle2, Clock, Globe2, GraduationCap, Inbox, Trophy } from 'lucide-react';
import { adminApi } from '../lib/api';
import { DashboardStats, ImportRun, JobItem, JobSource } from '../lib/types';
import { hostOf, timeAgo } from '../lib/cn';
import { Badge, Button, Card, CardHeader, EmptyState, PageHeader, Skeleton, StatCard } from '../components/ui';
import { CategoryBadge, StatusBadge } from '../components/StatusBadge';
import { useSession } from '../components/AuthGate';
import { useShell } from '../components/AppShell';
import { useToast } from '../components/Toast';

function greeting() {
  const h = new Date().getHours();
  return h < 12 ? 'Good morning' : h < 17 ? 'Good afternoon' : 'Good evening';
}

export default function DashboardPage() {
  const { user } = useSession();
  const { refreshCounts, importVersion } = useShell();
  const toast = useToast();
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [pending, setPending] = useState<JobItem[]>([]);
  const [sources, setSources] = useState<JobSource[]>([]);
  const [lastRun, setLastRun] = useState<ImportRun | null>(null);
  const [loading, setLoading] = useState(true);
  const [approving, setApproving] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    const [s, p, src, runs] = await Promise.allSettled([
      adminApi.getStats(),
      adminApi.getPendingJobs(6),
      adminApi.getSources(),
      adminApi.getImportRuns(1),
    ]);
    if (s.status === 'fulfilled') setStats(s.value);
    if (p.status === 'fulfilled') setPending(p.value);
    if (src.status === 'fulfilled') setSources(src.value);
    if (runs.status === 'fulfilled') setLastRun(runs.value[0] || null);
    setLoading(false);
  }, []);

  useEffect(() => {
    load();
  }, [load, importVersion]);

  const approve = async (job: JobItem) => {
    setApproving(job.id);
    try {
      await adminApi.approveJob(job.id);
      toast.success('Published', job.title);
      setPending((xs) => xs.filter((j) => j.id !== job.id));
      refreshCounts();
      adminApi.getStats().then(setStats).catch(() => undefined);
    } catch (err: any) {
      toast.error('Could not approve', err?.message);
    } finally {
      setApproving(null);
    }
  };

  const failing = sources.filter((s) => s.enabled && s.last_error);
  const healthy = sources.filter((s) => s.enabled && !s.last_error && s.last_successful_fetch_at).length;
  const enabled = sources.filter((s) => s.enabled).length;

  return (
    <div className="space-y-8">
      <PageHeader
        title={`${greeting()}, ${user.email.split('@')[0]}`}
        description="Here's what needs your attention today."
        actions={
          <Link href="/pending/">
            <Button icon={<Clock className="h-4 w-4" />}>Open review queue</Button>
          </Link>
        }
      />

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard label="Waiting for review" value={stats?.pendingReview ?? 0} icon={<Inbox className="h-4 w-4" />} tone="warning" loading={loading && !stats} hint="Scraped notices not yet public" />
        <StatCard label="Published notices" value={stats?.totalJobs ?? 0} icon={<CheckCircle2 className="h-4 w-4" />} tone="success" loading={loading && !stats} hint="Visible in the app" />
        <StatCard label="Latest jobs" value={stats?.latestJobs ?? 0} icon={<Briefcase className="h-4 w-4" />} tone="primary" loading={loading && !stats} hint={`${stats?.admitCards ?? 0} admit cards`} />
        <StatCard label="Results" value={stats?.results ?? 0} icon={<Trophy className="h-4 w-4" />} tone="info" loading={loading && !stats} hint={`${stats?.activeSources ?? 0} official sources`} />
      </div>

      <div className="grid grid-cols-1 gap-6 xl:grid-cols-3">
        {/* Review queue preview */}
        <Card className="xl:col-span-2">
          <CardHeader
            title="Latest in the review queue"
            description="Approve a notice only after checking its official link."
            icon={<Clock className="h-4 w-4" />}
            actions={
              <Link href="/pending/" className="inline-flex items-center gap-1 text-xs font-medium text-primary hover:underline">
                View all <ArrowRight className="h-3.5 w-3.5" />
              </Link>
            }
          />
          {loading && pending.length === 0 ? (
            <div className="space-y-3 px-5 pb-5">
              {Array.from({ length: 4 }).map((_, i) => (
                <Skeleton key={i} className="h-14 w-full" />
              ))}
            </div>
          ) : pending.length === 0 ? (
            <EmptyState icon={<GraduationCap className="h-5 w-5" />} title="Queue is clear" description="New notices appear here after the next import run." />
          ) : (
            <ul className="divide-y border-t">
              {pending.map((job) => (
                <li key={job.id} className="flex items-center gap-4 px-5 py-3.5 hover:bg-muted/40">
                  <div className="min-w-0 flex-1">
                    <p className="truncate text-sm font-medium">{job.title}</p>
                    <div className="mt-1 flex flex-wrap items-center gap-2 text-xs text-muted-foreground">
                      <CategoryBadge category={job.category} />
                      <span className="truncate">{job.organization}</span>
                      <span>·</span>
                      <span>{timeAgo(job.created_at)}</span>
                    </div>
                  </div>
                  {job.official_notification_url && (
                    <a href={job.official_notification_url} target="_blank" rel="noopener noreferrer" className="hidden text-xs text-muted-foreground hover:text-foreground md:block">
                      {hostOf(job.official_notification_url)}
                    </a>
                  )}
                  <Button size="sm" variant="outline" loading={approving === job.id} onClick={() => approve(job)}>
                    Approve
                  </Button>
                </li>
              ))}
            </ul>
          )}
        </Card>

        {/* Pipeline health */}
        <Card>
          <CardHeader title="Import pipeline" description="Official sources health" icon={<Globe2 className="h-4 w-4" />} />
          <div className="space-y-5 px-5 pb-5">
            {loading && sources.length === 0 ? (
              <Skeleton className="h-24 w-full" />
            ) : (
              <>
                <div>
                  <div className="flex items-baseline justify-between">
                    <span className="text-2xl font-semibold tabular-nums">
                      {healthy}
                      <span className="text-base text-muted-foreground">/{enabled}</span>
                    </span>
                    <span className="text-xs text-muted-foreground">sources healthy</span>
                  </div>
                  <div className="mt-2 h-2 overflow-hidden rounded-full bg-muted">
                    <div className="h-full rounded-full bg-success transition-all" style={{ width: `${enabled ? (healthy / enabled) * 100 : 0}%` }} />
                  </div>
                </div>

                {lastRun && (
                  <div className="flex items-center justify-between rounded-lg bg-muted/60 px-3 py-2.5 text-xs">
                    <div>
                      <p className="font-medium">Last run</p>
                      <p className="text-muted-foreground">
                        {timeAgo(lastRun.started_at)} · +{lastRun.inserted_count} new
                      </p>
                    </div>
                    <StatusBadge status={lastRun.status} />
                  </div>
                )}

                {failing.length > 0 && (
                  <div>
                    <p className="mb-2 flex items-center gap-1.5 text-xs font-medium text-warning">
                      <AlertTriangle className="h-3.5 w-3.5" /> {failing.length} sources need attention
                    </p>
                    <ul className="space-y-1.5">
                      {failing.slice(0, 5).map((s) => (
                        <li key={s.id} className="flex items-center justify-between gap-2 text-xs">
                          <span className="truncate">{s.name.replace(/\s*\(.*\)$/, '')}</span>
                          <Badge tone="danger" className="shrink-0 max-w-[45%] truncate">
                            {(s.last_error || '').replace(/^HTTP /, '')}
                          </Badge>
                        </li>
                      ))}
                    </ul>
                    <Link href="/sources/" className="mt-3 inline-flex items-center gap-1 text-xs font-medium text-primary hover:underline">
                      Manage sources <ArrowRight className="h-3.5 w-3.5" />
                    </Link>
                  </div>
                )}
              </>
            )}
          </div>
        </Card>
      </div>
    </div>
  );
}
