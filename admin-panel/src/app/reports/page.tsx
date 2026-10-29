'use client';

import React, { useCallback, useEffect, useState } from 'react';
import { CheckCircle2, ExternalLink, Flag, RefreshCw, XCircle } from 'lucide-react';
import { adminApi } from '../../lib/api';
import { JobReport } from '../../lib/types';
import { timeAgo } from '../../lib/cn';
import { Alert, Button, Card, Dialog, EmptyState, Field, PageHeader, Segmented, Skeleton, Textarea } from '../../components/ui';
import { StatusBadge } from '../../components/StatusBadge';
import { useToast } from '../../components/Toast';

type Filter = 'OPEN' | 'RESOLVED' | 'DISMISSED' | 'ALL';

export default function ReportsPage() {
  const toast = useToast();
  const [reports, setReports] = useState<JobReport[]>([]);
  const [filter, setFilter] = useState<Filter>('OPEN');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [resolving, setResolving] = useState<JobReport | null>(null);
  const [note, setNote] = useState('');
  const [busy, setBusy] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setReports(await adminApi.getReports(filter));
    } catch (err: any) {
      setError(err?.message || 'Failed to load reports');
    } finally {
      setLoading(false);
    }
  }, [filter]);

  useEffect(() => {
    load();
  }, [load]);

  const act = async (r: JobReport, status: 'RESOLVED' | 'DISMISSED', n?: string) => {
    setBusy(true);
    try {
      await adminApi.resolveReport(r.id, status, n || undefined);
      toast.success(status === 'RESOLVED' ? 'Marked as resolved' : 'Report dismissed');
      setResolving(null);
      setNote('');
      setReports((xs) => (filter === 'OPEN' ? xs.filter((x) => x.id !== r.id) : xs));
      if (filter !== 'OPEN') load();
    } catch (err: any) {
      toast.error('Update failed', err?.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="space-y-6">
      <PageHeader
        title="User reports"
        description="Wrong or outdated information reported by app users."
        actions={<Button variant="outline" icon={<RefreshCw className="h-4 w-4" />} onClick={load} loading={loading}>Refresh</Button>}
      />

      <Segmented
        value={filter}
        onChange={(v) => setFilter(v)}
        options={[
          { value: 'OPEN', label: 'Open' },
          { value: 'RESOLVED', label: 'Resolved' },
          { value: 'DISMISSED', label: 'Dismissed' },
          { value: 'ALL', label: 'All' },
        ]}
      />

      {error && <Alert tone="danger">{error}</Alert>}

      {loading && reports.length === 0 ? (
        <div className="space-y-3">
          {Array.from({ length: 3 }).map((_, i) => <Skeleton key={i} className="h-28 w-full rounded-xl" />)}
        </div>
      ) : reports.length === 0 ? (
        <Card>
          <EmptyState icon={<Flag className="h-5 w-5" />} title={filter === 'OPEN' ? 'No open reports' : 'Nothing here'} description="Reports sent from the app's “Report issue” button show up here." />
        </Card>
      ) : (
        <div className="grid gap-3">
          {reports.map((r) => (
            <Card key={r.id} className="p-5">
              <div className="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
                <div className="min-w-0 space-y-2">
                  <div className="flex flex-wrap items-center gap-2">
                    <StatusBadge status={r.status} />
                    <span className="text-xs text-muted-foreground">{timeAgo(r.created_at)}</span>
                  </div>
                  <p className="font-medium">{r.reason}</p>
                  {r.details && <p className="whitespace-pre-wrap break-words text-sm text-muted-foreground">{r.details}</p>}
                  <div className="flex flex-wrap items-center gap-x-2 text-xs text-muted-foreground">
                    <span className="font-medium text-foreground">{r.job_title || r.job_id}</span>
                    {r.job_organization && <span>· {r.job_organization}</span>}
                    {r.job_url && (
                      <a href={r.job_url} target="_blank" rel="noopener noreferrer" className="inline-flex items-center gap-1 hover:text-primary">
                        · Official notice <ExternalLink className="h-3 w-3" />
                      </a>
                    )}
                  </div>
                  {r.resolution_note && <p className="text-xs text-success">Note: {r.resolution_note}</p>}
                </div>
                {r.status === 'OPEN' && (
                  <div className="flex shrink-0 gap-2">
                    <Button size="sm" variant="outline" icon={<XCircle className="h-3.5 w-3.5" />} onClick={() => act(r, 'DISMISSED')}>Dismiss</Button>
                    <Button size="sm" variant="success" icon={<CheckCircle2 className="h-3.5 w-3.5" />} onClick={() => setResolving(r)}>Resolve</Button>
                  </div>
                )}
              </div>
            </Card>
          ))}
        </div>
      )}

      <Dialog
        open={!!resolving}
        onClose={() => setResolving(null)}
        size="sm"
        title="Resolve report"
        description="Describe what was fixed. This stays in the audit trail."
        footer={
          <>
            <Button variant="outline" onClick={() => setResolving(null)}>Cancel</Button>
            <Button variant="success" loading={busy} onClick={() => resolving && act(resolving, 'RESOLVED', note)}>Mark resolved</Button>
          </>
        }
      >
        <Field label="What was fixed?" hint="Optional">
          <Textarea value={note} onChange={(e) => setNote(e.target.value)} maxLength={500} placeholder="e.g. Corrected the last date" />
        </Field>
      </Dialog>
    </div>
  );
}
