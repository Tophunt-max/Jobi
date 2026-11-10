/**
 * Maps backend rows (see backend-cloudflare/src/mappers.js) to app models.
 * Every field is read defensively: a malformed item is dropped, never crashes the app.
 */
import { env } from '@/config/env';
import { normalizeDate, parseServerTimestamp } from '@/lib/dates';
import { hostnameOf, toHttps } from '@/lib/format';
import type {
  AdmissionItem,
  AdmitCardItem,
  AnswerKeyItem,
  AppNotification,
  Job,
  ResultItem,
  SyllabusItem,
} from '@/types/domain';

/** Shown when the backend did not provide a value. Never invent official details. */
export const NOT_SPECIFIED = 'Refer official notification';

type Row = Record<string, unknown>;

function str(v: unknown): string {
  if (typeof v === 'string') return v.trim();
  if (typeof v === 'number' && Number.isFinite(v)) return String(v);
  return '';
}

function num(v: unknown): number {
  const n = typeof v === 'number' ? v : typeof v === 'string' ? Number(v) : NaN;
  return Number.isFinite(n) ? Math.max(0, Math.trunc(n)) : 0;
}

function bool(v: unknown, fallback = false): boolean {
  if (typeof v === 'boolean') return v;
  if (typeof v === 'number') return v === 1;
  return fallback;
}

function orNotSpecified(v: unknown): string {
  return str(v) || NOT_SPECIFIED;
}

function url(v: unknown): string {
  const s = str(v);
  return /^https?:\/\//i.test(s) ? toHttps(s) : '';
}

function isRow(v: unknown): v is Row {
  return typeof v === 'object' && v !== null;
}

function mapList<T>(input: unknown, map: (row: Row) => T | null): T[] {
  if (!Array.isArray(input)) return [];
  const out: T[] = [];
  for (const item of input) {
    if (!isRow(item)) continue;
    const mapped = map(item);
    if (mapped) out.push(mapped);
  }
  return out;
}

export function mapJob(row: Row): Job | null {
  const id = str(row.id);
  const title = str(row.title);
  if (!id || !title) return null;

  const r2Key = str(row.notification_pdf_r2_key);
  const notificationUrl = r2Key
    ? `${env.apiBaseUrl}/api/files/${r2Key}`
    : url(row.official_notification_url) || url(row.official_url);
  const websiteUrl = url(row.official_url) || notificationUrl;

  return {
    id,
    slug: str(row.slug) || id,
    title,
    organization: str(row.organization) || str(row.department) || NOT_SPECIFIED,
    department: str(row.department),
    postName: str(row.post_name),
    advtNumber: str(row.advertisement_number),
    state: str(row.state) || 'All India',
    category: str(row.category) || 'Latest Job',
    totalPosts: num(row.total_posts),
    qualification: orNotSpecified(row.qualification),
    minAge: num(row.min_age),
    maxAge: num(row.max_age),
    salaryScale: orNotSpecified(row.salary_scale),
    feeGeneral: orNotSpecified(row.fee_general),
    feeReserved: orNotSpecified(row.fee_sc_st),
    selectionProcess: orNotSpecified(row.selection_process),
    startDate: normalizeDate(str(row.application_start_date)),
    lastDate: normalizeDate(str(row.last_date)),
    examDate: str(row.exam_date) || NOT_SPECIFIED,
    admitCardDate: str(row.admit_card_date) || NOT_SPECIFIED,
    notificationUrl,
    applyUrl: url(row.official_apply_url) || websiteUrl,
    websiteUrl,
    sourceDomain: str(row.source_domain) || hostnameOf(websiteUrl),
    verified: bool(row.verified),
    featured: bool(row.is_featured),
    publishedAt: parseServerTimestamp(str(row.published_at) || str(row.created_at)),
  };
}

export function mapResult(row: Row): ResultItem | null {
  const id = str(row.id);
  if (!id) return null;
  return {
    id,
    examName: str(row.exam_name),
    recruitingOrg: str(row.recruiting_org),
    advtNumber: str(row.advt_number),
    publicationDate: normalizeDate(str(row.publication_date)) || str(row.publication_date),
    url: url(row.official_result_url),
    cutOffSummary: str(row.cut_off_summary),
    meritListAvailable: bool(row.merit_list_available, true),
    verificationDomain: str(row.verification_domain),
    stage: str(row.stage) || 'Result',
  };
}

export function mapAdmitCard(row: Row): AdmitCardItem | null {
  const id = str(row.id);
  if (!id) return null;
  return {
    id,
    examName: str(row.exam_name),
    recruitingOrg: str(row.recruiting_org),
    releaseDate: normalizeDate(str(row.release_date)) || str(row.release_date),
    examDate: str(row.exam_date),
    url: url(row.official_download_url),
    instructions: str(row.instructions),
    citySlipReleased: bool(row.city_slip_released),
    verificationDomain: str(row.verification_domain),
  };
}

function parseSubjects(raw: unknown): { name: string; marks: string }[] {
  let list: unknown = raw;
  if (typeof raw === 'string') {
    try {
      list = JSON.parse(raw);
    } catch {
      return [];
    }
  }
  if (!Array.isArray(list)) return [];
  return list
    .map((s) => {
      if (typeof s === 'string') return { name: s, marks: '' };
      if (isRow(s)) return { name: str(s.name ?? s.subject), marks: str(s.marks ?? s.max_marks) };
      return null;
    })
    .filter((s): s is { name: string; marks: string } => !!s && !!s.name);
}

export function mapSyllabus(row: Row): SyllabusItem | null {
  const id = str(row.id);
  if (!id) return null;
  return {
    id,
    examName: str(row.exam_name),
    recruitingOrg: str(row.recruiting_org),
    examPattern: str(row.exam_pattern),
    subjects: parseSubjects(row.subjects_and_marks_json),
    durationMinutes: num(row.duration_minutes),
    negativeMarkingRule: str(row.negative_marking_rule),
    url: url(row.official_pdf_url),
    verificationDomain: str(row.verification_domain),
  };
}

export function mapAdmission(row: Row): AdmissionItem | null {
  const id = str(row.id);
  if (!id) return null;
  return {
    id,
    name: str(row.exam_or_course_name),
    authority: str(row.conducting_authority),
    eligibility: str(row.eligibility_course),
    startDate: normalizeDate(str(row.application_start_date)),
    closingDate: normalizeDate(str(row.application_closing_date)),
    examDate: str(row.entrance_exam_date),
    applyUrl: url(row.official_apply_url),
    bulletinUrl: url(row.official_information_bulletin_url),
    verificationDomain: str(row.verification_domain),
    status: str(row.status) || 'OPEN',
  };
}

export function mapAnswerKey(row: Row): AnswerKeyItem | null {
  const id = str(row.id);
  if (!id) return null;
  return {
    id,
    examName: str(row.exam_name),
    recruitingOrg: str(row.recruiting_org),
    status: str(row.status) || 'PROVISIONAL',
    releaseDate: normalizeDate(str(row.release_date)) || str(row.release_date),
    objectionDeadline: normalizeDate(str(row.objection_deadline)) || str(row.objection_deadline),
    objectionFee: str(row.objection_fee),
    url: url(row.official_link),
    verificationDomain: str(row.verification_domain),
  };
}

export function mapNotification(row: Row): AppNotification | null {
  const id = str(row.id);
  const title = str(row.title);
  if (!id || !title) return null;
  const ts = typeof row.timestamp === 'number' ? row.timestamp : Date.now();
  return {
    id,
    title,
    body: str(row.body),
    type: str(row.type) || 'NEW_JOB',
    target: str(row.target_slug),
    timestamp: ts,
    priority: str(row.priority) || 'HIGH',
  };
}

export const mapLists = {
  jobs: (v: unknown) => mapList(v, mapJob),
  results: (v: unknown) => mapList(v, mapResult),
  admitCards: (v: unknown) => mapList(v, mapAdmitCard),
  syllabi: (v: unknown) => mapList(v, mapSyllabus),
  admissions: (v: unknown) => mapList(v, mapAdmission),
  answerKeys: (v: unknown) => mapList(v, mapAnswerKey),
  notifications: (v: unknown) => mapList(v, mapNotification),
};
