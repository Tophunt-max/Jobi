import { z } from 'zod';

import type { ContentSnapshot, Job } from '@/types/domain';

import { ApiError, apiRequest } from './client';
import { mapJob, mapLists } from './mappers';

const syncEnvelope = z.object({
  success: z.boolean(),
  server_timestamp: z.number().optional(),
  data: z
    .object({
      jobs: z.array(z.unknown()).optional(),
      results: z.array(z.unknown()).optional(),
      admit_cards: z.array(z.unknown()).optional(),
      syllabi: z.array(z.unknown()).optional(),
      admissions: z.array(z.unknown()).optional(),
      answer_keys: z.array(z.unknown()).optional(),
      notifications: z.array(z.unknown()).optional(),
    })
    .optional(),
  message: z.string().optional(),
});

const singleEnvelope = z.object({
  success: z.boolean(),
  data: z.record(z.string(), z.unknown()).nullish(),
});

const actionEnvelope = z.object({
  success: z.boolean(),
  message: z.string().optional(),
});

const aiEnvelope = z.object({
  success: z.boolean(),
  text: z.string().nullish(),
  message: z.string().optional(),
});

function parse<T>(schema: z.ZodType<T>, payload: unknown): T {
  const result = schema.safeParse(payload);
  if (!result.success) throw new ApiError('parse', 'Unexpected response from the server');
  return result.data;
}

/** GET /api/sync/all — every ACTIVE notice, split by category, plus recent notifications. */
export async function fetchSyncAll(signal?: AbortSignal): Promise<ContentSnapshot> {
  const res = parse(syncEnvelope, await apiRequest<unknown>('/api/sync/all', { signal, timeoutMs: 30_000 }));
  if (!res.success || !res.data) throw new ApiError('http', res.message ?? 'Sync failed');
  const d = res.data;
  return {
    jobs: mapLists.jobs(d.jobs),
    results: mapLists.results(d.results),
    admitCards: mapLists.admitCards(d.admit_cards),
    syllabi: mapLists.syllabi(d.syllabi),
    admissions: mapLists.admissions(d.admissions),
    answerKeys: mapLists.answerKeys(d.answer_keys),
    notifications: mapLists.notifications(d.notifications),
    serverTimestamp: res.server_timestamp ?? Date.now(),
  };
}

/** GET /api/jobs/:idOrSlug — used when a notification points to a job not in the local cache. */
export async function fetchJob(idOrSlug: string): Promise<Job | null> {
  const res = parse(singleEnvelope, await apiRequest<unknown>(`/api/jobs/${encodeURIComponent(idOrSlug)}`));
  return res.success && res.data ? mapJob(res.data) : null;
}

export type AiTask = 'eligibility' | 'strategy' | 'query';
export const MAX_AI_PROMPT_CHARS = 8000;

/** POST /api/ai/generate — Gemini proxy; the API key stays on the Worker. */
export async function generateAi(task: AiTask, prompt: string, signal?: AbortSignal): Promise<string> {
  const res = parse(
    aiEnvelope,
    await apiRequest<unknown>('/api/ai/generate', {
      method: 'POST',
      body: { task, prompt: prompt.slice(0, MAX_AI_PROMPT_CHARS) },
      timeoutMs: 90_000,
      signal,
    }),
  );
  const text = res.text?.trim();
  if (!res.success || !text) throw new ApiError('http', res.message ?? 'Empty answer');
  return text;
}

/** POST /api/reports — a user flags wrong or outdated information on a notice. */
export async function submitReport(jobId: string, reason: string, details: string): Promise<void> {
  const res = parse(
    actionEnvelope,
    await apiRequest<unknown>('/api/reports', {
      method: 'POST',
      body: { job_id: jobId, reason: reason.slice(0, 80), details: details.slice(0, 1000) },
    }),
  );
  if (!res.success) throw new ApiError('http', res.message ?? 'Report failed');
}

/** GET /api/ads/config — raw payload; validated in features/ads/config.ts. */
export async function fetchAdConfigRaw(): Promise<unknown> {
  const res = await apiRequest<{ success?: boolean; data?: unknown }>('/api/ads/config', { retries: 1 });
  return res?.data ?? res;
}
