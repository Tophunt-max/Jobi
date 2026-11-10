import type { ArtKey } from '@/assets/images';
import { ALL, SECTORS } from '@/constants/catalog';
import { requiredEducationRank } from '@/features/profile/eligibility';
import { daysUntil } from '@/lib/dates';
import type { Job, JobStatus } from '@/types/domain';

export const CLOSING_SOON_DAYS = 7;

/** Status is computed from dates at render time, so it never goes stale in the cache. */
export function jobStatus(job: Job, now: Date = new Date()): JobStatus {
  const left = job.lastDate ? daysUntil(job.lastDate, now) : null;
  if (left !== null && left < 0) return 'EXPIRED';
  const toStart = job.startDate ? daysUntil(job.startDate, now) : null;
  if (toStart !== null && toStart > 0) return 'UPCOMING';
  if (left !== null && left <= CLOSING_SOON_DAYS) return 'CLOSING_SOON';
  return 'ACTIVE';
}

export type StatusFilter = typeof ALL | 'ACTIVE' | 'CLOSING_SOON' | 'UPCOMING';
export type SortOrder = 'latest' | 'deadline';

export type JobFilters = {
  query: string;
  state: string;
  sector: string;
  qualification: string;
  status: StatusFilter;
  sort: SortOrder;
};

export const DEFAULT_FILTERS: JobFilters = {
  query: '',
  state: ALL,
  sector: ALL,
  qualification: ALL,
  status: ALL,
  sort: 'latest',
};

function haystack(job: Job): string {
  return `${job.title} ${job.organization} ${job.department} ${job.postName} ${job.category}`.toLowerCase();
}

function escapeRegExp(s: string): string {
  return s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

/**
 * Keywords match at the start of a word ("bank" matches "banking"). Short ones
 * (3 letters or fewer, e.g. "ssc", "je") must be a whole word, so "OSSC" is not "SSC".
 */
const SECTOR_PATTERNS = new Map(
  SECTORS.filter((s) => s.keywords.length > 0).map((s) => {
    const parts = s.keywords.map((k) => {
      const kw = escapeRegExp(k.trim());
      return k.trim().length <= 3 ? `${kw}(?![a-z0-9])` : kw;
    });
    return [s.key, new RegExp(`(?<![a-z0-9])(?:${parts.join('|')})`, 'i')] as const;
  }),
);

/** Keyword sector test on any text (used for results, admit cards and other lists). */
export function textMatchesSector(text: string, sectorKey: string, scope = ''): boolean {
  if (sectorKey === ALL) return true;
  const isAllIndia = scope === '' || scope.toLowerCase() === 'all india';
  const statePsc = SECTOR_PATTERNS.get('statePsc')?.test(text) ?? false;
  switch (sectorKey) {
    case 'central':
      return isAllIndia && !statePsc && !(SECTOR_PATTERNS.get('psu')?.test(text) ?? false);
    case 'state':
      return !isAllIndia || statePsc;
    case 'other':
      return ![...SECTOR_PATTERNS.values()].some((p) => p.test(text));
    default: {
      const pattern = SECTOR_PATTERNS.get(sectorKey);
      return pattern ? pattern.test(text) : true;
    }
  }
}

export function matchesSector(job: Job, sectorKey: string): boolean {
  return textMatchesSector(haystack(job), sectorKey, job.state);
}

/** Order used to pick one artwork for a notice; the first match wins. */
const ART_ORDER: { sector: string; art: ArtKey }[] = [
  { sector: 'railways', art: 'railway' },
  { sector: 'banking', art: 'banking' },
  { sector: 'defence', art: 'defence' },
  { sector: 'police', art: 'police' },
  { sector: 'teaching', art: 'teaching' },
  { sector: 'medical', art: 'medical' },
  { sector: 'psu', art: 'psu' },
  { sector: 'upsc', art: 'upsc' },
  { sector: 'ssc', art: 'ssc' },
  { sector: 'engineering', art: 'engineering' },
  { sector: 'statePsc', art: 'state-govt' },
];

/** Picks the emblem artwork for any text (organisation, exam name, title). */
export function artForText(text: string, scope = ''): ArtKey {
  for (const { sector, art } of ART_ORDER) {
    if (SECTOR_PATTERNS.get(sector)?.test(text)) return art;
  }
  return scope && scope.toLowerCase() !== 'all india' ? 'state-govt' : 'central-govt';
}

export function artForJob(job: Job): ArtKey {
  return artForText(haystack(job), job.state);
}

/** Artwork for a sector filter key. */
export const SECTOR_ART: Record<string, ArtKey> = {
  central: 'central-govt',
  state: 'state-govt',
  statePsc: 'state-govt',
  railways: 'railway',
  banking: 'banking',
  defence: 'defence',
  police: 'police',
  teaching: 'teaching',
  medical: 'medical',
  psu: 'psu',
  other: 'other',
  ssc: 'ssc',
  upsc: 'upsc',
  engineering: 'engineering',
};

/** Short qualification label for list pills ("Graduate", "12th Pass"). */
export function shortQualification(qualification: string): string {
  const rank = requiredEducationRank(qualification);
  switch (rank) {
    case 1:
      return '10th Pass';
    case 2:
      return '12th Pass';
    case 3:
      return 'Diploma';
    case 4:
      return 'Graduate';
    case 5:
      return 'Post Graduate';
    default:
      return '';
  }
}

export function matchesState(job: Job, state: string): boolean {
  if (state === ALL) return true;
  if (state === 'All India') return job.state.toLowerCase() === 'all india';
  // A state filter also shows All-India notices, which residents of any state can apply for.
  return job.state.toLowerCase() === 'all india' || job.state.toLowerCase().includes(state.toLowerCase());
}

function matchesQuery(job: Job, query: string): boolean {
  const terms = query.toLowerCase().split(/\s+/).filter(Boolean);
  if (terms.length === 0) return true;
  const text = `${haystack(job)} ${job.advtNumber.toLowerCase()} ${job.state.toLowerCase()}`;
  return terms.every((t) => text.includes(t));
}

function deadlineKey(job: Job): number {
  // Unknown deadlines sort last.
  return job.lastDate ? Date.parse(job.lastDate) : Number.MAX_SAFE_INTEGER;
}

export function filterJobs(jobs: Job[], f: JobFilters, now: Date = new Date()): Job[] {
  const out = jobs.filter((job) => {
    const status = jobStatus(job, now);
    if (status === 'EXPIRED') return false;
    if (f.status !== ALL) {
      if (f.status === 'ACTIVE' && status !== 'ACTIVE' && status !== 'CLOSING_SOON') return false;
      if (f.status !== 'ACTIVE' && status !== f.status) return false;
    }
    return (
      matchesState(job, f.state) &&
      matchesSector(job, f.sector) &&
      (f.qualification === ALL || job.qualification.toLowerCase().includes(f.qualification.toLowerCase())) &&
      matchesQuery(job, f.query)
    );
  });
  return out.sort((a, b) =>
    f.sort === 'deadline' ? deadlineKey(a) - deadlineKey(b) : b.publishedAt - a.publishedAt,
  );
}

/** Open notices whose last date is within the next `days` days, soonest first. */
export function closingSoon(jobs: Job[], days = CLOSING_SOON_DAYS, now: Date = new Date()): Job[] {
  return jobs
    .filter((j) => {
      const left = j.lastDate ? daysUntil(j.lastDate, now) : null;
      return left !== null && left >= 0 && left <= days;
    })
    .sort((a, b) => deadlineKey(a) - deadlineKey(b));
}

export function countBy<T extends string>(jobs: Job[], keys: T[], test: (job: Job, key: T) => boolean): Record<T, number> {
  const out = {} as Record<T, number>;
  for (const k of keys) out[k] = jobs.filter((j) => test(j, k)).length;
  return out;
}
