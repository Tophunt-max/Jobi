import { useRouter } from 'expo-router';
import { useEffect, useRef } from 'react';
import { AppState } from 'react-native';

import { useAdConfigStore } from '@/features/ads/config';
import { initAds } from '@/features/ads/service';
import { useBookmarkedJobs } from '@/features/jobs/hooks';
import { runLegacyMigration } from '@/features/migration/legacy';
import { subscribeToReminderTaps, syncDeadlineReminders } from '@/features/reminders/notifications';
import { initI18n } from '@/i18n';
import { useContentStore } from '@/store/contentStore';
import { useSettingsStore } from '@/store/settingsStore';
import { useUserStore } from '@/store/userStore';

/**
 * Start-up work that needs the persisted stores:
 * language, legacy import, content sync, ads, reminders and notification taps.
 */
export function useAppBootstrap(hydrated: boolean) {
  const router = useRouter();
  const language = useSettingsStore((s) => s.language);
  const remindersEnabled = useSettingsStore((s) => s.remindersEnabled);
  const bookmarks = useBookmarkedJobs();
  const jobs = useContentStore((s) => s.jobs);
  const started = useRef(false);

  // Language follows the saved choice (or the device).
  useEffect(() => {
    if (hydrated) initI18n(language);
  }, [hydrated, language]);

  // One-time start-up tasks.
  useEffect(() => {
    if (!hydrated || started.current) return;
    started.current = true;
    void (async () => {
      await runLegacyMigration();
      await useContentStore.getState().sync({ force: true });
    })();
    void useAdConfigStore
      .getState()
      .refresh()
      .finally(() => initAds());
  }, [hydrated]);

  // Refresh when the app returns to the foreground (throttled inside sync()).
  useEffect(() => {
    if (!hydrated) return;
    const sub = AppState.addEventListener('change', (state) => {
      if (state === 'active') void useContentStore.getState().sync();
    });
    return () => sub.remove();
  }, [hydrated]);

  // Keep saved notices up to date with the latest server copy.
  useEffect(() => {
    if (hydrated && jobs.length > 0) useUserStore.getState().refreshBookmarks(jobs);
  }, [hydrated, jobs]);

  // Keep OS reminders in step with bookmarks and their deadlines.
  const reminderKey = bookmarks.map((j) => `${j.id}@${j.lastDate}`).join('|');
  useEffect(() => {
    if (!hydrated) return;
    const t = setTimeout(() => void syncDeadlineReminders(bookmarks, remindersEnabled), 500);
    return () => clearTimeout(t);
    // bookmarks is represented by reminderKey
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [hydrated, reminderKey, remindersEnabled]);

  // Reminder tap opens the job.
  useEffect(() => {
    if (!hydrated) return;
    return subscribeToReminderTaps((jobId) => router.push({ pathname: '/job/[id]', params: { id: jobId } }));
  }, [hydrated, router]);
}
