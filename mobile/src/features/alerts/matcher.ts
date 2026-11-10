import { jobStatus, matchesSector } from '@/features/jobs/logic';
import type { AlertPreferences, Job } from '@/types/domain';

export type MatchedJob = {
  job: Job;
  score: number;
  reasons: string[];
};

/**
 * Scores a job against the user's alert preferences (same weights as the Kotlin app):
 * sector 40, qualification 30, state 20, vacancies 10. An empty preference list matches all.
 */
export function evaluateMatch(job: Job, prefs: AlertPreferences): MatchedJob {
  let score = 0;
  const reasons: string[] = [];

  const sector = prefs.categories.length === 0 ? null : prefs.categories.find((c) => matchesSector(job, c));
  if (prefs.categories.length === 0 || sector) {
    score += 40;
    if (sector) reasons.push(`sector:${sector}`);
  }

  const qual =
    prefs.qualifications.length === 0
      ? null
      : prefs.qualifications.find((q) => job.qualification.toLowerCase().includes(q.toLowerCase()));
  if (prefs.qualifications.length === 0 || qual) {
    score += 30;
    if (qual) reasons.push(`qualification:${qual}`);
  }

  const allIndia = job.state.toLowerCase() === 'all india';
  const stateMatch =
    allIndia ||
    prefs.states.length === 0 ||
    prefs.states.includes('All India') ||
    prefs.states.some((s) => job.state.toLowerCase().includes(s.toLowerCase()));
  if (stateMatch) {
    score += 20;
    reasons.push(allIndia ? 'state:All India' : `state:${job.state}`);
  }

  if (job.totalPosts >= prefs.minVacancies) {
    score += 10;
    if (prefs.minVacancies > 0) reasons.push(`posts:${job.totalPosts}`);
  }

  return { job, score: Math.min(100, score), reasons };
}

export function matchJobs(jobs: Job[], prefs: AlertPreferences, now: Date = new Date()): MatchedJob[] {
  if (!prefs.enabled) return [];
  return jobs
    .filter((j) => jobStatus(j, now) !== 'EXPIRED')
    .map((j) => evaluateMatch(j, prefs))
    .filter((m) => m.score >= 60)
    .sort((a, b) => b.score - a.score || b.job.publishedAt - a.job.publishedAt);
}
