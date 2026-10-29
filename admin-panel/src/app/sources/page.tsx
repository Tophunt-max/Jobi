'use client';

import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { ExternalLink, FlaskConical, Globe2, Play, RefreshCw, Search } from 'lucide-react';
import { adminApi } from '../../lib/api';
import { JobSource, SourceTestResult } from '../../lib/types';
import { timeAgo } from '../../lib/cn';
import {
  Alert, Badge, Button, Card, EmptyState, Input, PageHeader, Segmented, Sheet, Switch, Table, TableSkeleton, Td, Th,
} from '../../components/ui';
import { CategoryBadge } from '../../components/StatusBadge';
import { useShell } from '../../components/AppShell';
import { useToast } from '../../components/Toast';

type Filter = 'ALL' | 'HEALTHY' | 'FAILING' | 'DISABLED';

function health(s: JobSource): { tone: 'success' | 'danger' | 'neutral' | 'warning'; label: string } {
  if (!s.enabled) return { tone: 'neutral', label: 'Disabled' };
  if (s.last_error) return { tone: 'danger', label: 'Failing' };
  if (s.last_successful_fetch_at) return { tone: 'success', label: 'Healthy' };
  return { tone: 'warning', label: 'Not run yet' };
}

export default function SourcesPage() {
  const toast = useToast();
  const { refreshCounts, importVersion } = useShell();
  const [sources, setSources] = useState<JobSource[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState('');
  const [filter, setFilter] = useState<Filter>('ALL');
  const [busy, setBusy] = useState<string | null>(null);
  const [test, setTest] = useState<{ source: JobSource; result?: SourceTestResult; error?: string } | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setSources(await adminApi.getSources());
    } catch (err: any) {
      setError(err?.message || 'Failed to load sources');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load, importVersion]);

  const counts = useMemo(
    () => ({
      ALL: sources.length,
      HEALTHY: sources.filter((s) => health(s).label === 'Healthy').length,
      FAILING: sources.filter((s) => health(s).label === 'Failing').length,
      DISABLED: sources.filter((s) => !s.enabled).length,
    }),
    [sources]
  );

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase();
    return sources.filter((s) => {
      const h = health(s).label;
      if (filter === 'HEALTHY' && h !== 'Healthy') return false;
      if (filter === 'FAILING' && h !== 'Failing') return false;
      if (filter === 'DISABLED' && s.enabled) return false;
      return !q || [s.name, s.official_domain, s.id, s.state].some((v) => v.toLowerCase().includes(q));
    });
  }, [sources, search, filter]);

  const toggle = async (s: JobSource, enabled: boolean) => {
    setSources((xs) => xs.map((x) => (x.id === s.id ? { ...x, enabled: enabled ? 1 : 0 } : x)));
    try {
      await adminApi.updateSource(s.id, enabled);
      toast.success(`${enabled ? 'Enabled' : 'Disabled'} ${s.name.replace(/\s*\(.*\)$/, '')}`);
    } catch (err: any) {
      setSources((xs) => xs.map((x) => (x.id === s.id ? { ...x, enabled: enabled ? 0 : 1 } : x)));
      toast.error('Update failed', err?.message);
    }
  };

  const runTest = async (s: JobSource) => {
    setTest({ source: s });
    try {
      const result = await adminApi.testSource(s.id);
      setTest({ source: s, result });
    } catch (err: any) {
      setTest({ source: s, error: err?.message || 'Test failed' });
    }
  };

  const crawl = async (s: JobSource) => {
    setBusy(s.id);
    try {
      const { summary } = await adminApi.runImport(s.id);
      if (summary.totalErrors) toast.error(`Crawl failed`, summary.errors.join('; '));
      else toast.success(`Crawled ${s.name.replace(/\s*\(.*\)$/, '')}`, `${summary.totalFetched} found · ${summary.totalInserted} new for review`);
      refreshCounts();
      load();
    } catch (err: any) {
      toast.error('Crawl failed', err?.message);
    } finally {
      setBusy(null);
    }
  };

  return (
    <div className="space-y-6">
      <PageHeader
        title="Official sources"
        description="Government portals the importer checks. Each is fetched on its own polling interval."
        actions={<Button variant="outline" icon={<RefreshCw className="h-4 w-4" />} onClick={load} loading={loading}>Refresh</Button>}
      />

      {error && <Alert tone="danger">{error}</Alert>}

      <Card>
        <div className="flex flex-col gap-3 p-4 lg:flex-row lg:items-center">
          <Segmented
            value={filter}
            onChange={(v) => setFilter(v)}
            options={[
              { value: 'ALL', label: 'All', count: counts.ALL },
              { value: 'HEALTHY', label: 'Healthy', count: counts.HEALTHY },
              { value: 'FAILING', label: 'Failing', count: counts.FAILING },
              { value: 'DISABLED', label: 'Disabled', count: counts.DISABLED },
            ]}
            className="overflow-x-auto"
          />
          <div className="flex-1">
            <Input leftIcon={<Search className="h-4 w-4" />} placeholder="Search by name, domain or state" value={search} onChange={(e) => setSearch(e.target.value)} />
          </div>
        </div>

        {loading && sources.length === 0 ? (
          <TableSkeleton rows={8} />
        ) : filtered.length === 0 ? (
          <EmptyState icon={<Globe2 className="h-5 w-5" />} title="No sources match" />
        ) : (
          <Table>
            <thead>
              <tr>
                <Th className="w-12">On</Th>
                <Th>Source</Th>
                <Th className="hidden md:table-cell">State</Th>
                <Th>Health</Th>
                <Th className="hidden lg:table-cell">Last success</Th>
                <Th className="text-right">Actions</Th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((s) => {
                const h = health(s);
                return (
                  <tr key={s.id} className="hover:bg-muted/40">
                    <Td><Switch checked={!!s.enabled} onChange={(v) => toggle(s, v)} label={`Enable ${s.name}`} /></Td>
                    <Td className="max-w-sm">
                      <p className="truncate font-medium">{s.name}</p>
                      <a href={s.listing_url} target="_blank" rel="noopener noreferrer" className="inline-flex items-center gap-1 text-xs text-muted-foreground hover:text-primary">
                        {s.official_domain} <ExternalLink className="h-3 w-3" />
                      </a>
                    </Td>
                    <Td className="hidden md:table-cell text-xs">{s.state}</Td>
                    <Td>
                      <div className="flex flex-col items-start gap-1">
                        <Badge tone={h.tone} dot>{h.label}</Badge>
                        {s.last_error && <span className="max-w-[220px] truncate text-[11px] text-danger" title={s.last_error}>{s.last_error}</span>}
                      </div>
                    </Td>
                    <Td className="hidden whitespace-nowrap text-xs text-muted-foreground lg:table-cell">{timeAgo(s.last_successful_fetch_at)}</Td>
                    <Td className="whitespace-nowrap text-right">
                      <div className="inline-flex gap-1.5">
                        <Button size="sm" variant="ghost" onClick={() => runTest(s)} icon={<FlaskConical className="h-3.5 w-3.5" />}>
                          <span className="hidden sm:inline">Test</span>
                        </Button>
                        <Button size="sm" variant="outline" loading={busy === s.id} disabled={!s.enabled} onClick={() => crawl(s)} icon={<Play className="h-3.5 w-3.5" />}>
                          <span className="hidden sm:inline">Crawl</span>
                        </Button>
                      </div>
                    </Td>
                  </tr>
                );
              })}
            </tbody>
          </Table>
        )}
      </Card>

      <Sheet
        open={!!test}
        onClose={() => setTest(null)}
        title={test ? `Test: ${test.source.name}` : ''}
        description="Dry run. Shows what the scraper would find right now; nothing is saved."
      >
        {test && !test.result && !test.error && (
          <div className="flex items-center gap-3 text-sm text-muted-foreground">
            <RefreshCw className="h-4 w-4 animate-spin" /> Fetching {test.source.official_domain}…
          </div>
        )}
        {test?.error && <Alert tone="danger" title="Fetch failed">{test.error}</Alert>}
        {test?.result && (
          <div className="space-y-4">
            <Alert tone={test.result.count ? 'success' : 'warning'}>
              Found {test.result.count} notice{test.result.count === 1 ? '' : 's'}
              {test.result.count > test.result.sample.length ? ` (showing ${test.result.sample.length})` : ''}.
            </Alert>
            <ul className="space-y-2">
              {test.result.sample.map((n) => (
                <li key={n.official_notification_url} className="rounded-lg border p-3">
                  <div className="flex items-start justify-between gap-3">
                    <p className="text-sm font-medium">{n.title}</p>
                    <CategoryBadge category={n.category} />
                  </div>
                  <a href={n.official_notification_url} target="_blank" rel="noopener noreferrer" className="mt-1 block truncate text-xs text-muted-foreground hover:text-primary">
                    {n.official_notification_url}
                  </a>
                </li>
              ))}
            </ul>
          </div>
        )}
      </Sheet>
    </div>
  );
}
