import { parseDate } from '@/lib/dates';
import type { AdmitCardItem, Job } from '@/types/domain';

export type CalendarKind = 'exam' | 'examStart' | 'lastDate' | 'applyStart';

export type CalendarEvent = {
  id: string;
  date: Date;
  title: string;
  kind: CalendarKind;
  jobId?: string;
  url?: string;
};

/**
 * Builds calendar entries from notice dates. Exam dates are often free text
 * ("Tentative: Dec 2026"); entries whose date cannot be parsed are left out.
 */
export function buildCalendar(jobs: Job[], admitCards: AdmitCardItem[]): CalendarEvent[] {
  const out: CalendarEvent[] = [];
  const seen = new Set<string>();
  const add = (e: CalendarEvent) => {
    const key = `${e.title.toLowerCase()}|${e.kind}|${e.date.toDateString()}`;
    if (seen.has(key)) return;
    seen.add(key);
    out.push(e);
  };
  for (const job of jobs) {
    const exam = parseDate(job.examDate);
    if (exam) add({ id: `${job.id}-exam`, date: exam, title: job.title, kind: 'exam', jobId: job.id });
    const last = parseDate(job.lastDate);
    if (last) add({ id: `${job.id}-last`, date: last, title: job.title, kind: 'lastDate', jobId: job.id });
    const start = parseDate(job.startDate);
    if (start) add({ id: `${job.id}-start`, date: start, title: job.title, kind: 'applyStart', jobId: job.id });
  }
  for (const card of admitCards) {
    const exam = parseDate(card.examDate);
    if (exam) add({ id: `${card.id}-exam`, date: exam, title: card.examName, kind: 'examStart', url: card.url });
  }
  return out.sort((a, b) => a.date.getTime() - b.date.getTime());
}
