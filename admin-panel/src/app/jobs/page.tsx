'use client';

import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { Briefcase, ExternalLink, Plus, RefreshCw, Search } from 'lucide-react';
import { adminApi } from '../../lib/api';
import { JOB_CATEGORIES, JobCategory, JobItem, ManualJobInput } from '../../lib/types';
import { formatDateTime, hostOf } from '../../lib/cn';
import {
  Alert, Button, Card, Dialog, EmptyState, Field, Input, PageHeader, Segmented, Select, Table, TableSkeleton, Td, Th,
} from '../../components/ui';
import { CategoryBadge, StatusBadge } from '../../components/StatusBadge';
import { useShell } from '../../components/AppShell';
import { useToast } from '../../components/Toast';

type StatusFilter = 'ACTIVE' | 'pending_review' | 'REJECTED' | 'EXPIRED' | 'ALL';

const EMPTY_FORM = {
  title: '', organization: '', category: 'Latest Job' as JobCategory, state: 'All India', advertisement_number: '',
  total_posts: '', qualification: '', salary_scale: '', application_start_date: '', last_date: '',
  official_notification_url: '', official_apply_url: '',
};

function PublishDialog({ open, onClose, onCreated }: { open: boolean; onClose: () => void; onCreated: () => void }) {
  const toast = useToast();
  const [f, setF] = useState(EMPTY_FORM);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const set = (k: keyof typeof EMPTY_FORM) => (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => setF((x) => ({ ...x, [k]: e.target.value }));

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    if (!f.official_notification_url.startsWith('https://')) return setError('The official notice link must start with https://');
    setBusy(true);
    const payload: ManualJobInput = {
      title: f.title.trim(),
      organization: f.organization.trim(),
      category: f.category,
      state: f.state.trim() || 'All India',
      advertisement_number: f.advertisement_number.trim() || undefined,
      total_posts: f.total_posts ? Number(f.total_posts) : undefined,
      qualification: f.qualification.trim() || undefined,
      salary_scale: f.salary_scale.trim() || undefined,
      application_start_date: f.application_start_date || undefined,
      last_date: f.last_date || undefined,
      official_notification_url: f.official_notification_url.trim(),
      official_apply_url: f.official_apply_url.trim() || undefined,
    };
    try {
      await adminApi.createJob(payload);
      toast.success('Notice published', payload.title);
      setF(EMPTY_FORM);
      onCreated();
      onClose();
    } catch (err: any) {
      setError(err?.message || 'Could not publish');
    } finally {
      setBusy(false);
    }
  };

  return (
    <Dialog
      open={open}
      onClose={onClose}
      size="lg"
      title="Publish a notice"
      description="Use this for notices the importer can't reach. It goes live in the app immediately."
      footer={
        <>
          <Button variant="outline" onClick={onClose}>Cancel</Button>
          <Button type="submit" form="publish-form" loading={busy}>Publish</Button>
        </>
      }
    >
      <form id="publish-form" onSubmit={submit} className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <Field label="Title *" className="sm:col-span-2">
          <Input required value={f.title} onChange={set('title')} placeholder="e.g. SSC Combined Graduate Level Examination 2026" />
        </Field>
        <Field label="Organization *">
          <Input required value={f.organization} onChange={set('organization')} placeholder="Staff Selection Commission" />
        </Field>
        <Field label="Advt. number">
          <Input value={f.advertisement_number} onChange={set('advertisement_number')} placeholder="F.No. HQ-PPII03/1/2026" />
        </Field>
        <Field label="Category">
          <Select value={f.category} onChange={set('category')}>
            {JOB_CATEGORIES.map((c) => <option key={c}>{c}</option>)}
          </Select>
        </Field>
        <Field label="State">
          <Input value={f.state} onChange={set('state')} />
        </Field>
        <Field label="Total posts">
          <Input type="number" min={0} value={f.total_posts} onChange={set('total_posts')} />
        </Field>
        <Field label="Salary / pay level">
          <Input value={f.salary_scale} onChange={set('salary_scale')} placeholder="Pay Level 7" />
        </Field>
        <Field label="Qualification" className="sm:col-span-2">
          <Input value={f.qualification} onChange={set('qualification')} placeholder="Bachelor's degree from a recognised university" />
        </Field>
        <Field label="Applications open">
          <Input type="date" value={f.application_start_date} onChange={set('application_start_date')} />
        </Field>
        <Field label="Last date">
          <Input type="date" value={f.last_date} onChange={set('last_date')} />
        </Field>
        <Field label="Official notice link *" hint="PDF or page on the official site (https)" className="sm:col-span-2">
          <Input required type="url" value={f.official_notification_url} onChange={set('official_notification_url')} placeholder="https://ssc.gov.in/…" />
        </Field>
        <Field label="Apply online link" className="sm:col-span-2">
          <Input type="url" value={f.official_apply_url} onChange={set('official_apply_url')} placeholder="https://…" />
        </Field>
        {error && <Alert tone="danger" className="sm:col-span-2">{error}</Alert>}
      </form>
    </Dialog>
  );
}

export default function JobsPage() {
  const { importVersion } = useShell();
  const [jobs, setJobs] = useState<JobItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [status, setStatus] = useState<StatusFilter>('ACTIVE');
  const [category, setCategory] = useState('ALL');
  const [search, setSearch] = useState('');
  const [publishing, setPublishing] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setJobs(await adminApi.getAllJobs({ status, category: category === 'ALL' ? undefined : category, limit: 200 }));
    } catch (err: any) {
      setError(err?.message || 'Failed to load notices');
    } finally {
      setLoading(false);
    }
  }, [status, category]);

  useEffect(() => {
    load();
  }, [load, importVersion]);

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase();
    if (!q) return jobs;
    return jobs.filter((j) => [j.title, j.organization, j.advertisement_number, j.post_name].some((v) => (v || '').toLowerCase().includes(q)));
  }, [jobs, search]);

  return (
    <div className="space-y-6">
      <PageHeader
        title="Notices"
        description="Everything in the database: published, in review, rejected and expired."
        actions={
          <>
            <Button variant="outline" icon={<RefreshCw className="h-4 w-4" />} onClick={load} loading={loading}>Refresh</Button>
            <Button icon={<Plus className="h-4 w-4" />} onClick={() => setPublishing(true)}>Publish notice</Button>
          </>
        }
      />

      {error && <Alert tone="danger">{error}</Alert>}

      <Card>
        <div className="flex flex-col gap-3 p-4 lg:flex-row lg:items-center">
          <Segmented
            value={status}
            onChange={(v) => setStatus(v)}
            options={[
              { value: 'ACTIVE', label: 'Published' },
              { value: 'pending_review', label: 'In review' },
              { value: 'REJECTED', label: 'Rejected' },
              { value: 'EXPIRED', label: 'Expired' },
              { value: 'ALL', label: 'All' },
            ]}
            className="overflow-x-auto"
          />
          <div className="flex flex-1 gap-2">
            <div className="flex-1">
              <Input leftIcon={<Search className="h-4 w-4" />} placeholder="Search notices" value={search} onChange={(e) => setSearch(e.target.value)} />
            </div>
            <Select value={category} onChange={(e) => setCategory(e.target.value)} className="w-40" aria-label="Category">
              <option value="ALL">All categories</option>
              {JOB_CATEGORIES.map((c) => <option key={c}>{c}</option>)}
            </Select>
          </div>
        </div>

        {loading && jobs.length === 0 ? (
          <TableSkeleton />
        ) : filtered.length === 0 ? (
          <EmptyState
            icon={<Briefcase className="h-5 w-5" />}
            title="No notices here"
            description={status === 'ACTIVE' ? 'Approve notices from the review queue or publish one manually.' : 'Nothing matches these filters.'}
            action={status === 'ACTIVE' ? <Button size="sm" icon={<Plus className="h-4 w-4" />} onClick={() => setPublishing(true)}>Publish notice</Button> : undefined}
          />
        ) : (
          <Table>
            <thead>
              <tr>
                <Th>Notice</Th>
                <Th className="hidden md:table-cell">Category</Th>
                <Th className="hidden lg:table-cell">Last date</Th>
                <Th>Status</Th>
                <Th className="hidden xl:table-cell">Reviewed</Th>
                <Th className="text-right">Link</Th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((j) => (
                <tr key={j.id} className="hover:bg-muted/40">
                  <Td className="max-w-md">
                    <p className="line-clamp-2 font-medium">{j.title}</p>
                    <p className="mt-0.5 truncate text-xs text-muted-foreground">
                      {j.organization} · {j.state}
                      {j.total_posts ? ` · ${j.total_posts} posts` : ''}
                    </p>
                  </Td>
                  <Td className="hidden md:table-cell"><CategoryBadge category={j.category} /></Td>
                  <Td className="hidden whitespace-nowrap text-xs lg:table-cell">{j.last_date || '—'}</Td>
                  <Td><StatusBadge status={j.status} /></Td>
                  <Td className="hidden text-xs text-muted-foreground xl:table-cell">
                    {j.approved_by ? <span title={formatDateTime(j.approved_at)}>{j.approved_by}</span> : j.rejection_reason ? <span className="line-clamp-1" title={j.rejection_reason}>{j.rejection_reason}</span> : '—'}
                  </Td>
                  <Td className="text-right">
                    {j.official_notification_url && (
                      <a href={j.official_notification_url} target="_blank" rel="noopener noreferrer" className="inline-flex items-center gap-1 text-xs text-muted-foreground hover:text-primary">
                        <span className="hidden sm:inline">{hostOf(j.official_notification_url)}</span> <ExternalLink className="h-3.5 w-3.5" />
                      </a>
                    )}
                  </Td>
                </tr>
              ))}
            </tbody>
          </Table>
        )}
        {filtered.length > 0 && <p className="px-5 py-3 text-xs text-muted-foreground">Showing {filtered.length} of {jobs.length}</p>}
      </Card>

      <PublishDialog open={publishing} onClose={() => setPublishing(false)} onCreated={() => (status === 'ACTIVE' ? load() : setStatus('ACTIVE'))} />
    </div>
  );
}
