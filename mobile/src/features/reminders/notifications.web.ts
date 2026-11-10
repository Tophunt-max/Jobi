import type { Job } from '@/types/domain';

/**
 * Web build: browsers cannot schedule notifications for a future date while the tab
 * is closed, so system reminders are off. Deadline alerts still appear in the
 * in-app inbox (see schedule.ts).
 */
export const DEADLINE_CHANNEL_ID = 'job_deadline_alerts';
export const notificationsSupported = false;

export async function configureNotifications(): Promise<void> {}

export async function hasNotificationPermission(): Promise<boolean> {
  return false;
}

export async function requestNotificationPermission(): Promise<boolean> {
  return false;
}

export async function syncDeadlineReminders(_bookmarks: Job[], _enabled: boolean): Promise<void> {}

export function subscribeToReminderTaps(_onOpenJob: (jobId: string) => void): () => void {
  return () => {};
}
