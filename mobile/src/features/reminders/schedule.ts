import { parseDate } from '@/lib/dates';
import type { AppNotification, Job } from '@/types/domain';

/** Reminders fire at 09:00 local time, 3 days and 1 day before the last date. */
export const REMINDER_OFFSETS_DAYS = [3, 1] as const;
export const REMINDER_HOUR = 9;
const INBOX_WINDOW_MS = 60 * 86_400_000;

export type PlannedReminder = {
  id: string;
  jobId: string;
  fireAt: Date;
  daysLeft: number;
  title: string;
  body: string;
};

export function reminderId(jobId: string, offset: number): string {
  return `deadline:${jobId}:${offset}`;
}

export function planReminders(job: Job): PlannedReminder[] {
  const last = parseDate(job.lastDate);
  if (!last) return [];
  return REMINDER_OFFSETS_DAYS.map((offset) => {
    const fireAt = new Date(last.getFullYear(), last.getMonth(), last.getDate() - offset, REMINDER_HOUR, 0, 0);
    return {
      id: reminderId(job.id, offset),
      jobId: job.id,
      fireAt,
      daysLeft: offset,
      title: offset === 1 ? `Last day tomorrow: ${job.title}` : `${offset} days left: ${job.title}`,
      body: `${job.organization}. Apply before ${job.lastDate}.`,
    };
  });
}

/**
 * Deadline alerts for the in-app inbox. They are derived from bookmarks, so they show up
 * on every platform (including web) without needing a background task.
 */
export function deadlineInboxItems(bookmarks: Job[], now: number = Date.now()): AppNotification[] {
  const items: AppNotification[] = [];
  for (const job of bookmarks) {
    for (const r of planReminders(job)) {
      const t = r.fireAt.getTime();
      if (t <= now && now - t <= INBOX_WINDOW_MS) {
        items.push({
          id: r.id,
          title: r.title,
          body: r.body,
          type: 'DEADLINE_ALERT',
          target: job.id,
          timestamp: t,
          priority: 'HIGH',
        });
      }
    }
  }
  return items;
}
