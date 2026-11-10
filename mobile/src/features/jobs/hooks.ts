import { useRouter } from 'expo-router';
import { useCallback, useMemo } from 'react';
import { useShallow } from 'zustand/react/shallow';

import { runWithInterstitial } from '@/features/ads/service';
import { requestNotificationPermission } from '@/features/reminders/notifications';
import { useContentStore } from '@/store/contentStore';
import { useSettingsStore } from '@/store/settingsStore';
import { useUserStore } from '@/store/userStore';
import type { Job } from '@/types/domain';

import { jobStatus } from './logic';

/** Opens a job detail screen. Every entry point goes through here so ad frequency is consistent. */
export function useOpenJob() {
  const router = useRouter();
  return useCallback(
    (job: Pick<Job, 'id'>) => {
      runWithInterstitial(() => router.push({ pathname: '/job/[id]', params: { id: job.id } }));
    },
    [router],
  );
}

export function useOpenJobs(): Job[] {
  const jobs = useContentStore((s) => s.jobs);
  return useMemo(() => {
    const now = new Date();
    return jobs.filter((j) => jobStatus(j, now) !== 'EXPIRED');
  }, [jobs]);
}

export function useBookmarkedJobs(): Job[] {
  const bookmarks = useUserStore((s) => s.bookmarks);
  return useMemo(
    () =>
      Object.values(bookmarks).sort((a, b) => {
        const da = a.lastDate ? Date.parse(a.lastDate) : Number.MAX_SAFE_INTEGER;
        const db = b.lastDate ? Date.parse(b.lastDate) : Number.MAX_SAFE_INTEGER;
        return da - db;
      }),
    [bookmarks],
  );
}

export function useIsBookmarked(id: string): boolean {
  return useUserStore((s) => !!s.bookmarks[id]);
}

/** Toggles a bookmark. The first save asks for notification permission so reminders can fire. */
export function useToggleBookmark() {
  const toggle = useUserStore((s) => s.toggleBookmark);
  const { prompted, markPrompted } = useSettingsStore(
    useShallow((s) => ({ prompted: s.notificationPromptShown, markPrompted: s.markNotificationPromptShown })),
  );
  return useCallback(
    (job: Job) => {
      const added = toggle(job);
      if (added && !prompted) {
        markPrompted();
        void requestNotificationPermission();
      }
      return added;
    },
    [toggle, prompted, markPrompted],
  );
}
