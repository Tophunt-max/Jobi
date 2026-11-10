import * as Notifications from 'expo-notifications';
import { Platform } from 'react-native';

import { createLogger } from '@/lib/logger';
import type { Job } from '@/types/domain';

import { planReminders } from './schedule';

const log = createLogger('reminders');

export const DEADLINE_CHANNEL_ID = 'job_deadline_alerts';
const ID_PREFIX = 'deadline:';
/** Reminders closer than this are skipped; the in-app inbox still shows them. */
const MIN_LEAD_MS = 60_000;

export const notificationsSupported = true;

export async function configureNotifications(): Promise<void> {
  Notifications.setNotificationHandler({
    handleNotification: async () => ({
      shouldShowBanner: true,
      shouldShowList: true,
      shouldPlaySound: false,
      shouldSetBadge: false,
    }),
  });
  if (Platform.OS === 'android') {
    try {
      await Notifications.setNotificationChannelAsync(DEADLINE_CHANNEL_ID, {
        name: 'Application deadline alerts',
        description: 'Reminders before the last date of bookmarked notices',
        // eslint-disable-next-line import/namespace -- enum is re-exported via `export *`, which the plugin cannot follow
        importance: Notifications.AndroidImportance.HIGH,
      });
    } catch (e) {
      log.warn('channel setup failed', e);
    }
  }
}

export async function hasNotificationPermission(): Promise<boolean> {
  try {
    const { granted } = await Notifications.getPermissionsAsync();
    return granted;
  } catch {
    return false;
  }
}

export async function requestNotificationPermission(): Promise<boolean> {
  try {
    const current = await Notifications.getPermissionsAsync();
    if (current.granted) return true;
    if (!current.canAskAgain) return false;
    const { granted } = await Notifications.requestPermissionsAsync();
    return granted;
  } catch (e) {
    log.warn('permission request failed', e);
    return false;
  }
}

/**
 * Makes the OS schedule match the bookmarks: cancels reminders for removed bookmarks
 * or changed deadlines and adds missing ones. Safe to call often. Expo restores
 * scheduled notifications after a reboot on Android.
 */
export async function syncDeadlineReminders(bookmarks: Job[], enabled: boolean): Promise<void> {
  try {
    const scheduled = (await Notifications.getAllScheduledNotificationsAsync()).filter((n) =>
      n.identifier.startsWith(ID_PREFIX),
    );
    const now = Date.now();
    const desired = new Map(
      enabled
        ? bookmarks
            .flatMap(planReminders)
            .filter((r) => r.fireAt.getTime() - now > MIN_LEAD_MS)
            .map((r) => [r.id, r] as const)
        : [],
    );

    const keep = new Set<string>();
    for (const n of scheduled) {
      const want = desired.get(n.identifier);
      const sameTime = want && n.content.data?.fireAt === want.fireAt.getTime();
      if (sameTime) keep.add(n.identifier);
      else await Notifications.cancelScheduledNotificationAsync(n.identifier);
    }

    if (desired.size > keep.size && !(await hasNotificationPermission())) return;

    for (const r of desired.values()) {
      if (keep.has(r.id)) continue;
      await Notifications.scheduleNotificationAsync({
        identifier: r.id,
        content: {
          title: r.title,
          body: r.body,
          data: { jobId: r.jobId, fireAt: r.fireAt.getTime(), url: `sarkari://job/${encodeURIComponent(r.jobId)}` },
        },
        trigger: {
          type: Notifications.SchedulableTriggerInputTypes.DATE,
          date: r.fireAt,
          channelId: DEADLINE_CHANNEL_ID,
        },
      });
    }
  } catch (e) {
    log.warn('reminder sync failed', e);
  }
}

/** Calls `onOpenJob` when the user taps a reminder, including the one that launched the app. */
export function subscribeToReminderTaps(onOpenJob: (jobId: string) => void): () => void {
  const handle = (response: Notifications.NotificationResponse | null) => {
    const jobId = response?.notification.request.content.data?.jobId;
    if (typeof jobId === 'string' && jobId.length > 0 && jobId.length <= 200) onOpenJob(jobId);
  };
  const last = Notifications.getLastNotificationResponse();
  if (last) {
    handle(last);
    Notifications.clearLastNotificationResponse();
  }
  const sub = Notifications.addNotificationResponseReceivedListener((r) => {
    handle(r);
    Notifications.clearLastNotificationResponse();
  });
  return () => sub.remove();
}
