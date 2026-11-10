import { clearLegacyData, readLegacyData, type LegacyPayload } from '@modules/legacy-data';

import { CASTE_CATEGORIES, EDUCATION_LEVELS, EXAM_STAGES, QUALIFICATIONS, SECTORS } from '@/constants/catalog';
import { normalizeDate } from '@/lib/dates';
import { hostnameOf } from '@/lib/format';
import { createLogger } from '@/lib/logger';
import { NOT_SPECIFIED } from '@/services/api/mappers';
import { DEFAULT_ALERT_PREFS, DEFAULT_PROFILE, EMPTY_LOCKER } from '@/store/defaults';
import { useSettingsStore } from '@/store/settingsStore';
import { useUserStore } from '@/store/userStore';
import type {
  AlertPreferences,
  AppliedExam,
  CasteCategory,
  DocumentLocker,
  EducationLevel,
  EligibilityProfile,
  ExamStage,
  Job,
} from '@/types/domain';

const log = createLogger('migration');

type Obj = Record<string, unknown>;

function parseObj(raw: string | undefined): Obj | null {
  if (!raw) return null;
  try {
    const v = JSON.parse(raw);
    return v && typeof v === 'object' ? (v as Obj) : null;
  } catch {
    return null;
  }
}

const s = (v: unknown) => (typeof v === 'string' ? v : typeof v === 'number' ? String(v) : '');
const n = (v: unknown, d: number) => (typeof v === 'number' && Number.isFinite(v) ? v : d);
const b = (v: unknown) => v === true || v === 1;

function mapProfile(o: Obj | null): EligibilityProfile | undefined {
  if (!o) return undefined;
  const category = CASTE_CATEGORIES.some((c) => c.key === o.category) ? (o.category as CasteCategory) : DEFAULT_PROFILE.category;
  const qualification = EDUCATION_LEVELS.some((e) => e.key === o.qualification)
    ? (o.qualification as EducationLevel)
    : DEFAULT_PROFILE.qualification;
  const name = s(o.candidateName);
  return {
    // "RozgarOne Aspirant" was the old placeholder, not a real name.
    candidateName: name === 'RozgarOne Aspirant' ? '' : name,
    age: Math.round(n(o.age, DEFAULT_PROFILE.age)),
    gender: o.gender === 'FEMALE' || b(o.isWomenCandidate) ? 'FEMALE' : 'MALE',
    category,
    qualification,
    specificDegree: s(o.specificDegree),
    homeState: s(o.homeState) || DEFAULT_PROFILE.homeState,
    isPwD: b(o.isPwD),
    isExServicemen: b(o.isExServicemen),
    heightCm: n(o.heightCm, DEFAULT_PROFILE.heightCm),
    chestUnexpandedCm: n(o.chestUnexpandedCm, DEFAULT_PROFILE.chestUnexpandedCm),
    chestExpandedCm: n(o.chestExpandedCm, DEFAULT_PROFILE.chestExpandedCm),
  };
}

function mapLocker(o: Obj | null): DocumentLocker | undefined {
  if (!o) return undefined;
  const out = { ...EMPTY_LOCKER };
  for (const key of Object.keys(out) as (keyof DocumentLocker)[]) out[key] = s(o[key]);
  return out;
}

function mapExams(raw: string | undefined): AppliedExam[] | undefined {
  if (!raw) return undefined;
  try {
    const list = JSON.parse(raw);
    if (!Array.isArray(list)) return undefined;
    return list
      .filter((e): e is Obj => !!e && typeof e === 'object' && typeof e.id === 'string')
      .map((e) => ({
        id: s(e.id),
        jobTitle: s(e.jobTitle),
        organization: s(e.organization),
        advtNumber: s(e.advtNumber),
        applicationNo: s(e.applicationNo),
        rollNo: s(e.rollNo),
        examDate: s(e.examDate),
        examCenter: s(e.examCenter),
        stage: EXAM_STAGES.some((st) => st.key === e.stage) ? (e.stage as ExamStage) : 'SUBMITTED',
        appliedDate: s(e.appliedDate),
        notes: s(e.notes),
      }));
  } catch {
    return undefined;
  }
}

function mapBookmark(r: Obj): Job | null {
  const id = s(r.id);
  const title = s(r.title);
  if (!id || !title) return null;
  const website = s(r.officialWebsiteUrl);
  return {
    id,
    slug: s(r.slug) || id,
    title,
    organization: s(r.organization) || NOT_SPECIFIED,
    department: s(r.department),
    postName: '',
    advtNumber: s(r.advtNumber),
    state: s(r.stateOrAllIndia) || 'All India',
    category: s(r.category) || 'Latest Job',
    totalPosts: n(r.totalVacancies, 0),
    qualification: s(r.qualification) || NOT_SPECIFIED,
    minAge: n(r.minAge, 0),
    maxAge: n(r.maxAge, 0),
    salaryScale: s(r.salaryScale) || NOT_SPECIFIED,
    feeGeneral: s(r.feeGeneral) || NOT_SPECIFIED,
    feeReserved: s(r.feeReserved) || NOT_SPECIFIED,
    selectionProcess: s(r.selectionProcess) || NOT_SPECIFIED,
    startDate: normalizeDate(s(r.applicationStartDate)),
    lastDate: normalizeDate(s(r.applicationClosingDate)),
    examDate: s(r.examDate) || NOT_SPECIFIED,
    admitCardDate: s(r.admitCardDate) || NOT_SPECIFIED,
    notificationUrl: s(r.officialNotificationUrl),
    applyUrl: s(r.applyOnlineUrl) || website,
    websiteUrl: website,
    sourceDomain: s(r.verifiedSourceDomain) || hostnameOf(website),
    verified: b(r.isVerified),
    featured: false,
    publishedAt: n(r.publishedAt, 0),
  };
}

const OLD_QUALS: Record<string, string> = {
  '10th Pass': '10th',
  '12th Pass': '12th',
  'ITI / Diploma': 'Diploma',
  Graduate: 'Graduate',
  'B.Tech / BE': 'B.Tech',
  'Post Graduate': 'Post Graduate',
};

function mapAlerts(o: Obj | undefined): AlertPreferences | undefined {
  if (!o) return undefined;
  const list = (v: unknown) => (Array.isArray(v) ? v.filter((x): x is string => typeof x === 'string') : []);
  const sectorKeys = list(o.pref_categories)
    .map((c) => SECTORS.find((sec) => sec.key.toLowerCase() === c.toLowerCase().replace(/\s+/g, '') || (c === 'State PSC' && sec.key === 'statePsc'))?.key)
    .filter((k): k is string => !!k);
  const quals = list(o.pref_quals)
    .map((q) => OLD_QUALS[q])
    .filter((q): q is string => !!q && (QUALIFICATIONS as readonly string[]).includes(q));
  return {
    enabled: o.pref_enabled === undefined ? DEFAULT_ALERT_PREFS.enabled : b(o.pref_enabled),
    states: list(o.pref_states).map((st) => (st === 'Delhi (NCT)' ? 'Delhi' : st)),
    categories: sectorKeys,
    qualifications: quals,
    minVacancies: n(o.pref_min_vacancies, 0),
  };
}

export function mapLegacyPayload(p: LegacyPayload) {
  const bookmarks: Record<string, Job> = {};
  for (const row of p.bookmarked_jobs ?? []) {
    const job = mapBookmark(row as Obj);
    if (job) bookmarks[job.id] = job;
  }
  return {
    profile: mapProfile(parseObj(p.eligibility_profile_v1)),
    locker: mapLocker(parseObj(p.document_locker_v1)),
    appliedExams: mapExams(p.applied_exams_v1),
    bookmarks,
    readNotificationIds: (p.read_notification_ids ?? []).filter((x) => typeof x === 'string'),
    alertPrefs: mapAlerts(p.alert_prefs as Obj | undefined),
  };
}

/**
 * One-time import of the Kotlin app's data (Android only). The old copies are
 * deleted on the following launch, after the new stores have been written to disk.
 */
export async function runLegacyMigration(): Promise<void> {
  const settings = useSettingsStore.getState();
  if (settings.legacyMigrationDone) {
    if (!settings.legacyCleared) {
      await clearLegacyData().catch((e) => log.warn('clear failed', e));
      settings.markLegacyCleared();
    }
    return;
  }
  try {
    const payload = await readLegacyData();
    if (!payload) {
      settings.markLegacyMigrationDone(false);
      return;
    }
    useUserStore.getState().importLegacy(mapLegacyPayload(payload));
    settings.markLegacyMigrationDone(true);
    log.info('legacy data imported');
  } catch (e) {
    // Leave the flag unset so the next launch retries.
    log.warn('legacy import failed', e);
  }
}
