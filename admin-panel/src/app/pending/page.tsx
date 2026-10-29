'use client';

import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { Check, ExternalLink, FileText, Inbox, RefreshCw, Search, X } from 'lucide-react';
import { adminApi } from '../../lib/api';
import { JOB_CATEGORIES, JobItem } from '../../lib/types';
import { formatDateTime, hostOf, timeAgo } from '../../lib/cn';
import {
  Alert, Button, Card, Checkbox, Dialog, EmptyState, Field, Input, PageHeader, Select, Sheet, Table, TableSkeleton, Td, Textarea, Th,
} from '../../components/ui';
import { CategoryBadge } from '../../components/StatusBadge';
import { useShell } from '../../components/AppShell';
import { useToast } from '../../components/Toast';

const REJECT_REASONS = ['Not a recruitment notice', 'Duplicate of another notice', 'Broken or wrong link', 'Outdated / already closed', 'Other'];

function DetailRow({ label, value }: { label: string; value?: React.ReactNode }) {
  return (
    <div className="grid grid-cols-3 gap-4 py-2.5 text-sm">
      <dt className="text-muted-foreground">{label}</dt>
      <dd className="col-span-2 break-words">{value || <span className="text-muted-foreground">Not stated</span>}</dd>
    </div>
  );
}

export default function PendingPage() {
  const toast = useToast();
  const { refreshCounts, importVersion } = useShell();
  const [jobs, setJobs] = useState<JobItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState('');
  const [category, setCategory] = useState('ALL');
  const [state, setState] = useState('ALL');
  const [selected, setSelected] = useState<Set<string>>(new Set());
  const [inspect, setInspect] = useState<JobItem | null>(null);
  const [rejecting, setRejecting] = useState<JobItem[] | null>(null);
  const [rejectReason, setRejectReason] = useState(REJECT_REASONS[0]);
  const [rejectNote, setRejectNote] = useState('');
  const [busy, setBusy] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setJobs(await adminApi.getPendingJobs(200));
    } catch (err: any) {
      setError(err?.message || 'Failed to load the review queue');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load, importVersion]);

  const states = useMemo(() => Array.from(new Set(jobs.map((j) => j.state).filter(Boolean))).sort(), [jobs]);

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase();
    return jobs.filter((j) => {
      if (category !== 'ALL' && j.category !== category) return false;
      if (state !== 'ALL' && j.state !== state) return false;
      if (!q) return true;
      return [j.title, j.organization, j.advertisement_number, j.source_domain].some((v) => (v || '').toLowerCase().includes(q));
    });
  }, [jobs, search, category, state]);

  const allSelected = filtered.length > 0 && filtered.every((j) => selected.has(j.id));
  const someSelected = filtered.some((j) => selected.has(j.id)) && !allSelected;

  const toggle = (id: string) =>
    setSelected((s) => {
      const n = new Set(s);
      n.has(id) ? n.delete(id) : n.add(id);
      return n;
    });

  const toggleAll = () =>
    setSelected((s) => {
      const n = new Set(s);
      filtered.forEach((j) => (allSelected ? n.delete(j.id) : n.add(j.id)));
      return n;
    });

  const removeLocally = (ids: string[]) => {
    const set = new Set(ids);
    setJobs((xs) => xs.filter((j) => !set.has(j.id)));
    setSelected((s) => {
      const n = new Set(s);
      ids.forEach((id) => n.delete(id));
      return n;
    });
    refreshCounts();
  };

  const approve = async (ids: string[]) => {
    setBusy(ids.length === 1 ? ids[0] : 'bulk');
    try {
      if (ids.length === 1) {
        await adminApi.approveJob(ids[0]);
        toast.success('Published to the app');
      } else {
        const { approvedCount } = await adminApi.batchApproveJobs(ids);
        toast.success(`${approvedCount} notices published`);
      }
      removeLocally(ids);
      if (inspect && ids.includes(inspect.id)) setInspect(null);
    } catch (err: any) {
      toast.error('Approval failed', err?.message);
    } finally {
      setBusy(null);
    }
  };

  const confirmReject = async () => {
    if (!rejecting) return;
    const reason = rejectReason === 'Other' ? rejectNote.trim() || 'Other' : rejectNote.trim() ? `${rejectReason}: ${rejectNote.trim()}` : rejectReason;
    setBusy('reject');
    const done: string[] = [];
    try {
      for (const j of rejecting) {
        await adminApi.rejectJob(j.id, reason);
        done.push(j.id);
      }
      toast.success(done.length === 1 ? 'Notice rejected' : `${done.length} notices rejected`);
    } catch (err: any) {
      toast.error('Reject failed', err?.message);
    } finally {
      removeLocally(done);
      if (inspect && done.includes(inspect.id)) setInspect(null);
      setRejecting(null);
      setRejectNote('');
      setRejectReason(REJECT_REASONS[0]);
      setBusy(null);
    }
  };

  const selectedIds = Array.from(selected);

  return (
    <div className="space-y-6">
      <PageHeader
        title="Review queue"
        description="Notices found on official portals. Nothing here is visible in the app until you approve it."
        actions={
          <Button variant="outline" icon={<RefreshCw className="h-4 w-4" />} onClick={load} loading={loading}>
            Refresh
          </Button>
        }
      />

      {error && <Alert tone="danger">{error}</Alert>}

      <Card>
        <div className="flex flex-col gap-3 p-4 md:flex-row md:items-center">
          <div className="flex-1">
            <Input leftIcon={<Search className="h-4 w-4" />} placeholder="Search title, organization, advt. no. or domain" value={search} onChange={(e) => setSearch(e.target.value)} />
          </div>
          <div className="flex gap-2">
            <Select value={category} onChange={(e) => setCategory(e.target.value)} className="md:w-40" aria-label="Category">
              <option value="ALL">All categories</option>
              {JOB_CATEGORIES.map((c) => (
                <option key={c}>{c}</option>
              ))}
            </Select>
            <Select value={state} onChange={(e) => setState(e.target.value)} className="md:w-36" aria-label="State">
              <option value="ALL">All states</option>
              {states.map((s) => (
                <option key={s}>{s}</option>
              ))}
            </Select>
          </div>
        </div>

        {loading && jobs.length === 0 ? (
          <TableSkeleton />
        ) : filtered.length === 0 ? (
          <EmptyState
            icon={<Inbox className="h-5 w-5" />}
            title={jobs.length === 0 ? 'Nothing to review' : 'No matches'}
            description={jobs.length === 0 ? 'Run an import to fetch new notices from official sources.' : 'Try a different search or filter.'}
          />
        ) : (
          <>
          {/* Mobile: cards with actions always visible */}
          <ul className="divide-y border-t md:hidden">
            {filtered.map((job) => (
              <li key={job.id} className={selected.has(job.id) ? 'bg-accent/40 px-4 py-4' : 'px-4 py-4'}>
                <div className="flex items-start gap-3">
                  <div className="pt-0.5">
                    <Checkbox checked={selected.has(job.id)} onChange={() => toggle(job.id)} label={`Select ${job.title}`} />
                  </div>
                  <button onClick={() => setInspect(job)} className="min-w-0 flex-1 text-left">
                    <p className="line-clamp-3 text-sm font-medium">{job.title}</p>
                    <p className="mt-1 truncate text-xs text-muted-foreground">{job.organization}</p>
                    <div className="mt-2 flex flex-wrap items-center gap-2 text-[11px] text-muted-foreground">
                      <CategoryBadge category={job.category} />
                      <span>{hostOf(job.official_notification_url)}</span>
                      <span>· {timeAgo(job.created_at)}</span>
                    </div>
                  </button>
                </div>
                <div className="mt-3 flex gap-2 pl-7">
                  <Button size="sm" variant="outline" className="flex-1 text-danger" onClick={() => setRejecting([job])} icon={<X className="h-3.5 w-3.5" />}>
                    Reject
                  </Button>
                  <Button size="sm" variant="outline" className="flex-1" loading={busy === job.id} onClick={() => approve([job.id])} icon={<Check className="h-3.5 w-3.5" />}>
                    Approve
                  </Button>
                </div>
              </li>
            ))}
          </ul>

          <div className="hidden md:block">
          <Table>
            <thead>
              <tr>
                <Th className="w-10">
                  <Checkbox checked={allSelected} indeterminate={someSelected} onChange={toggleAll} label="Select all" />
                </Th>
                <Th>Notice</Th>
                <Th>Category</Th>
                <Th className="hidden lg:table-cell">Source</Th>
                <Th className="hidden sm:table-cell">Found</Th>
                <Th className="text-right">Actions</Th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((job) => (
                <tr key={job.id} className={selected.has(job.id) ? 'bg-accent/40' : 'hover:bg-muted/40'}>
                  <Td>
                    <Checkbox checked={selected.has(job.id)} onChange={() => toggle(job.id)} label={`Select ${job.title}`} />
                  </Td>
                  <Td className="max-w-md">
                    <button onClick={() => setInspect(job)} className="text-left">
                      <p className="line-clamp-2 font-medium hover:text-primary">{job.title}</p>
                      <p className="mt-0.5 truncate text-xs text-muted-foreground">
                        {job.organization}
                        {job.advertisement_number ? ` · Advt. ${job.advertisement_number}` : ''}
                      </p>
                    </button>
                  </Td>
                  <Td>
                    <CategoryBadge category={job.category} />
                  </Td>
                  <Td className="hidden lg:table-cell">
                    {job.official_notification_url ? (
                      <a href={job.official_notification_url} target="_blank" rel="noopener noreferrer" className="inline-flex items-center gap-1 text-xs text-muted-foreground hover:text-primary">
                        {hostOf(job.official_notification_url)} <ExternalLink className="h-3 w-3" />
                      </a>
                    ) : (
                      '—'
                    )}
                  </Td>
                  <Td className="hidden whitespace-nowrap text-xs text-muted-foreground sm:table-cell">{timeAgo(job.created_at)}</Td>
                  <Td className="whitespace-nowrap text-right">
                    <div className="inline-flex gap-1.5">
                      <Button size="sm" variant="ghost" onClick={() => setRejecting([job])} aria-label="Reject" className="text-danger hover:bg-danger-soft hover:text-danger">
                        <X className="h-3.5 w-3.5" />
                      </Button>
                      <Button size="sm" variant="outline" loading={busy === job.id} onClick={() => approve([job.id])} icon={<Check className="h-3.5 w-3.5" />}>
                        Approve
                      </Button>
                    </div>
                  </Td>
                </tr>
              ))}
            </tbody>
          </Table>
          </div>
          </>
        )}
        {filtered.length > 0 && (
          <div className="flex items-center justify-between px-5 py-3 text-xs text-muted-foreground">
            <span>
              Showing {filtered.length} of {jobs.length}
            </span>
          </div>
        )}
      </Card>

      {/* Floating bulk action bar */}
      {selectedIds.length > 0 && (
        <div className="fixed inset-x-0 bottom-6 z-30 flex justify-center px-4 lg:pl-64">
          <div className="flex items-center gap-3 rounded-2xl border bg-card px-4 py-3 shadow-pop animate-slide-up">
            <span className="text-sm font-medium tabular-nums">{selectedIds.length} selected</span>
            <div className="h-5 w-px bg-border" />
            <Button size="sm" variant="ghost" onClick={() => setSelected(new Set())}>
              Clear
            </Button>
            <Button size="sm" variant="outline" className="text-danger" onClick={() => setRejecting(jobs.filter((j) => selected.has(j.id)))}>
              Reject
            </Button>
            <Button size="sm" variant="success" loading={busy === 'bulk'} onClick={() => approve(selectedIds)} icon={<Check className="h-3.5 w-3.5" />}>
              Approve all
            </Button>
          </div>
        </div>
      )}

      {/* Inspect sheet */}
      <Sheet
        open={!!inspect}
        onClose={() => setInspect(null)}
        title={inspect?.title}
        description={inspect && <span className="flex flex-wrap items-center gap-2"><CategoryBadge category={inspect.category} /> {inspect.organization}</span>}
        footer={
          inspect && (
            <>
              <Button variant="outline" className="text-danger" onClick={() => setRejecting([inspect])}>
                Reject
              </Button>
              <Button variant="success" loading={busy === inspect.id} onClick={() => approve([inspect.id])} icon={<Check className="h-4 w-4" />}>
                Approve & publish
              </Button>
            </>
          )
        }
      >
        {inspect && (
          <div className="space-y-6">
            <Alert tone="warning" title="Verify before publishing">
              Open the official link and confirm the notice is genuine. The fields below were read automatically and may be incomplete.
            </Alert>

            <div className="flex flex-col gap-2">
              {inspect.official_notification_url && (
                <a href={inspect.official_notification_url} target="_blank" rel="noopener noreferrer">
                  <Button variant="outline" className="w-full justify-between">
                    <span className="inline-flex items-center gap-2"><FileText className="h-4 w-4" /> Official notice</span>
                    <span className="truncate text-xs text-muted-foreground">{hostOf(inspect.official_notification_url)}</span>
                  </Button>
                </a>
              )}
              {inspect.official_apply_url && (
                <a href={inspect.official_apply_url} target="_blank" rel="noopener noreferrer">
                  <Button variant="outline" className="w-full justify-between">
                    <span className="inline-flex items-center gap-2"><ExternalLink className="h-4 w-4" /> Apply portal</span>
                    <span className="truncate text-xs text-muted-foreground">{hostOf(inspect.official_apply_url)}</span>
                  </Button>
                </a>
              )}
            </div>

            <dl className="divide-y">
              <DetailRow label="State" value={inspect.state} />
              <DetailRow label="Advt. number" value={inspect.advertisement_number} />
              <DetailRow label="Total posts" value={inspect.total_posts || undefined} />
              <DetailRow label="Qualification" value={inspect.qualification} />
              <DetailRow label="Age limit" value={inspect.min_age && inspect.max_age ? `${inspect.min_age}–${inspect.max_age} years` : undefined} />
              <DetailRow label="Fee" value={inspect.fee_general ? `General ${inspect.fee_general}${inspect.fee_sc_st ? ` · SC/ST ${inspect.fee_sc_st}` : ''}` : undefined} />
              <DetailRow label="Salary" value={inspect.salary_scale} />
              <DetailRow label="Apply window" value={inspect.application_start_date || inspect.last_date ? `${inspect.application_start_date || '?'} → ${inspect.last_date || '?'}` : undefined} />
              <DetailRow label="Source" value={inspect.source_domain} />
              <DetailRow label="Found at" value={formatDateTime(inspect.created_at)} />
            </dl>
          </div>
        )}
      </Sheet>

      {/* Reject dialog */}
      <Dialog
        open={!!rejecting}
        onClose={() => setRejecting(null)}
        size="sm"
        title={rejecting && rejecting.length > 1 ? `Reject ${rejecting.length} notices?` : 'Reject this notice?'}
        description="Rejected notices never appear in the app. The reason is saved for the audit trail."
        footer={
          <>
            <Button variant="outline" onClick={() => setRejecting(null)}>
              Cancel
            </Button>
            <Button variant="danger" loading={busy === 'reject'} onClick={confirmReject}>
              Reject
            </Button>
          </>
        }
      >
        <div className="space-y-4">
          <Field label="Reason">
            <Select value={rejectReason} onChange={(e) => setRejectReason(e.target.value)}>
              {REJECT_REASONS.map((r) => (
                <option key={r}>{r}</option>
              ))}
            </Select>
          </Field>
          <Field label="Note" hint="Optional">
            <Textarea value={rejectNote} onChange={(e) => setRejectNote(e.target.value)} placeholder="Add details for your team" maxLength={400} />
          </Field>
        </div>
      </Dialog>
    </div>
  );
}
