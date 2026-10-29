'use client';

import React, { useCallback, useEffect, useState } from 'react';
import { ChevronDown, History, RefreshCw } from 'lucide-react';
import { adminApi } from '../../lib/api';
import { ImportRun } from '../../lib/types';
import { cn, formatDateTime } from '../../lib/cn';
import { Alert, Badge, Button, Card, EmptyState, PageHeader, Table, TableSkeleton, Td, Th } from '../../components/ui';
import { StatusBadge } from '../../components/StatusBadge';
import { useShell } from '../../components/AppShell';

function duration(run: ImportRun) {
  if (!run.finished_at) return '—';
  const ms = new Date(run.finished_at).getTime() - new Date(run.started_at).getTime();
  if (!Number.isFinite(ms) || ms < 0) return '—';
  return ms < 1000 ? `${ms} ms` : ms < 60000 ? `${(ms / 1000).toFixed(1)} s` : `${Math.round(ms / 60000)} min`;
}

export default function RunsPage() {
  const { importVersion } = useShell();
  const [runs, setRuns] = useState<ImportRun[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [open, setOpen] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setRuns(await adminApi.getImportRuns(100));
    } catch (err: any) {
      setError(err?.message || 'Failed to load import runs');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load, importVersion]);

  return (
    <div className="space-y-6">
      <PageHeader
        title="Import runs"
        description="History of scheduled and manual imports, with per-source errors."
        actions={<Button variant="outline" icon={<RefreshCw className="h-4 w-4" />} onClick={load} loading={loading}>Refresh</Button>}
      />

      {error && <Alert tone="danger">{error}</Alert>}

      <Card>
        {loading && runs.length === 0 ? (
          <TableSkeleton />
        ) : runs.length === 0 ? (
          <EmptyState icon={<History className="h-5 w-5" />} title="No runs yet" description="Use “Run import” in the top bar, or wait for the next scheduled run." />
        ) : (
          <Table>
            <thead>
              <tr>
                <Th>Started</Th>
                <Th className="hidden sm:table-cell">Trigger</Th>
                <Th className="hidden md:table-cell">Scope</Th>
                <Th className="text-right">Found</Th>
                <Th className="text-right">New</Th>
                <Th className="hidden md:table-cell text-right">Errors</Th>
                <Th className="hidden lg:table-cell">Duration</Th>
                <Th>Status</Th>
                <Th className="w-8" />
              </tr>
            </thead>
            <tbody>
              {runs.map((r) => {
                const expandable = !!r.error_summary;
                const isOpen = open === r.id;
                return (
                  <React.Fragment key={r.id}>
                    <tr className={cn('hover:bg-muted/40', expandable && 'cursor-pointer')} onClick={() => expandable && setOpen(isOpen ? null : r.id)}>
                      <Td className="whitespace-nowrap text-xs">{formatDateTime(r.started_at)}</Td>
                      <Td className="hidden sm:table-cell">
                        <Badge tone={(r.triggered_by || '').includes('CRON') ? 'info' : 'primary'}>{(r.triggered_by || '').includes('CRON') ? 'Scheduled' : 'Manual'}</Badge>
                      </Td>
                      <Td className="hidden md:table-cell text-xs text-muted-foreground">{!r.source_id || r.source_id === 'ALL_ENABLED' ? 'All sources' : r.source_id.replace(/^src-/, '')}</Td>
                      <Td className="text-right tabular-nums">{r.fetched_count}</Td>
                      <Td className="text-right tabular-nums font-medium text-success">+{r.inserted_count}</Td>
                      <Td className={cn('hidden md:table-cell text-right tabular-nums', r.error_count ? 'text-danger' : 'text-muted-foreground')}>{r.error_count}</Td>
                      <Td className="hidden lg:table-cell text-xs text-muted-foreground">{duration(r)}</Td>
                      <Td><StatusBadge status={r.status} /></Td>
                      <Td>{expandable && <ChevronDown className={cn('h-4 w-4 text-muted-foreground transition-transform', isOpen && 'rotate-180')} />}</Td>
                    </tr>
                    {isOpen && (
                      <tr>
                        <td colSpan={9} className="border-b bg-muted/30 px-5 py-4">
                          <ul className="space-y-1 font-mono text-xs">
                            {(r.error_summary || '').split('; ').map((e, i) => (
                              <li key={i} className="text-danger">{e}</li>
                            ))}
                          </ul>
                        </td>
                      </tr>
                    )}
                  </React.Fragment>
                );
              })}
            </tbody>
          </Table>
        )}
      </Card>
    </div>
  );
}
