import { casteMeta, educationMeta } from '@/constants/catalog';
import type { EligibilityProfile, Job } from '@/types/domain';

export type AgeCheck =
  | { kind: 'unknown' }
  | { kind: 'eligible'; upperLimit: number }
  | { kind: 'too_young'; minAge: number }
  | { kind: 'too_old'; upperLimit: number };

/** Upper age limit plus the category relaxation (PwD and ex-servicemen use their own rows). */
export function effectiveUpperAge(job: Job, profile: EligibilityProfile): number {
  return job.maxAge + casteMeta(profile.category).relaxationYears;
}

export function checkAge(job: Job, profile: EligibilityProfile): AgeCheck {
  if (job.maxAge <= 0) return { kind: 'unknown' };
  const upper = effectiveUpperAge(job, profile);
  if (job.minAge > 0 && profile.age < job.minAge) return { kind: 'too_young', minAge: job.minAge };
  if (profile.age > upper) return { kind: 'too_old', upperLimit: upper };
  return { kind: 'eligible', upperLimit: upper };
}

const QUAL_KEYWORDS: { rank: number; words: string[] }[] = [
  { rank: 5, words: ['post graduate', 'post-graduate', 'master', 'm.sc', 'm.a', 'mba', 'm.tech', 'pg '] },
  { rank: 4, words: ['graduate', 'degree', 'bachelor', 'b.tech', 'b.e', 'b.sc', 'b.com', 'b.a', 'llb', 'mbbs'] },
  { rank: 3, words: ['diploma', 'polytechnic'] },
  { rank: 2, words: ['12th', 'intermediate', '10+2', 'higher secondary', 'iti'] },
  { rank: 1, words: ['10th', 'matric', 'high school', 'sslc'] },
];

/** Lowest education rank mentioned in the notice, or null when it cannot be told. */
export function requiredEducationRank(qualification: string): number | null {
  const text = ` ${qualification.toLowerCase()} `;
  let found: number | null = null;
  for (const q of QUAL_KEYWORDS) {
    if (q.words.some((w) => text.includes(w))) found = found === null ? q.rank : Math.min(found, q.rank);
  }
  return found;
}

export function meetsEducation(job: Job, profile: EligibilityProfile): boolean | null {
  const required = requiredEducationRank(job.qualification);
  if (required === null) return null;
  return educationMeta(profile.qualification).rank >= required;
}

export function isFeeExempt(profile: EligibilityProfile): boolean {
  return casteMeta(profile.category).feeExempt || profile.gender === 'FEMALE' || profile.isPwD;
}
