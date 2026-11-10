import { create } from 'zustand';
import { persist } from 'zustand/middleware';

import { persistStorage, StorageKeys } from '@/lib/storage';
import type {
  AlertPreferences,
  AppliedExam,
  DocumentLocker,
  EligibilityProfile,
  Job,
} from '@/types/domain';

import { DEFAULT_ALERT_PREFS, DEFAULT_PROFILE, EMPTY_LOCKER } from './defaults';

/**
 * Data that belongs to the user and never leaves the device unless they share it:
 * eligibility profile, document locker, applied exams, bookmarks, read state.
 */
type UserState = {
  profile: EligibilityProfile;
  /** True once the user has saved the profile at least once. */
  profileCompleted: boolean;
  locker: DocumentLocker;
  appliedExams: AppliedExam[];
  /** Bookmarked jobs, kept as snapshots so they survive when the server drops them. */
  bookmarks: Record<string, Job>;
  readNotificationIds: string[];
  alertPrefs: AlertPreferences;

  setProfile: (profile: EligibilityProfile) => void;
  setLocker: (locker: DocumentLocker) => void;
  upsertExam: (exam: AppliedExam) => void;
  deleteExam: (id: string) => void;
  toggleBookmark: (job: Job) => boolean;
  /** Refreshes bookmark snapshots with the latest server copy. */
  refreshBookmarks: (jobs: Job[]) => void;
  markRead: (ids: string[]) => void;
  setAlertPrefs: (prefs: AlertPreferences) => void;
  importLegacy: (data: Partial<Pick<UserState, 'profile' | 'locker' | 'appliedExams' | 'bookmarks' | 'readNotificationIds' | 'alertPrefs'>>) => void;
};

const MAX_READ_IDS = 500;

export const useUserStore = create<UserState>()(
  persist(
    (set, get) => ({
      profile: DEFAULT_PROFILE,
      profileCompleted: false,
      locker: EMPTY_LOCKER,
      appliedExams: [],
      bookmarks: {},
      readNotificationIds: [],
      alertPrefs: DEFAULT_ALERT_PREFS,

      setProfile: (profile) => set({ profile, profileCompleted: true }),
      setLocker: (locker) => set({ locker }),

      upsertExam: (exam) =>
        set((s) => ({ appliedExams: [exam, ...s.appliedExams.filter((e) => e.id !== exam.id)] })),
      deleteExam: (id) => set((s) => ({ appliedExams: s.appliedExams.filter((e) => e.id !== id) })),

      toggleBookmark: (job) => {
        const exists = !!get().bookmarks[job.id];
        set((s) => {
          const next = { ...s.bookmarks };
          if (exists) delete next[job.id];
          else next[job.id] = job;
          return { bookmarks: next };
        });
        return !exists;
      },

      refreshBookmarks: (jobs) => {
        const current = get().bookmarks;
        const ids = Object.keys(current);
        if (ids.length === 0) return;
        const byId = new Map(jobs.map((j) => [j.id, j]));
        let changed = false;
        const next: Record<string, Job> = {};
        for (const id of ids) {
          const fresh = byId.get(id);
          if (fresh && fresh !== current[id]) changed = true;
          next[id] = fresh ?? current[id]!;
        }
        if (changed) set({ bookmarks: next });
      },

      markRead: (ids) =>
        set((s) => {
          const merged = Array.from(new Set([...ids, ...s.readNotificationIds]));
          return { readNotificationIds: merged.slice(0, MAX_READ_IDS) };
        }),

      setAlertPrefs: (alertPrefs) => set({ alertPrefs }),

      importLegacy: (data) =>
        set((s) => ({
          profile: data.profile ? { ...s.profile, ...data.profile } : s.profile,
          profileCompleted: s.profileCompleted || !!data.profile,
          locker: data.locker ? { ...s.locker, ...data.locker } : s.locker,
          appliedExams: data.appliedExams?.length ? data.appliedExams : s.appliedExams,
          bookmarks: { ...(data.bookmarks ?? {}), ...s.bookmarks },
          readNotificationIds: Array.from(new Set([...(data.readNotificationIds ?? []), ...s.readNotificationIds])).slice(0, MAX_READ_IDS),
          alertPrefs: data.alertPrefs ?? s.alertPrefs,
        })),
    }),
    {
      name: StorageKeys.user,
      storage: persistStorage,
      version: 1,
      partialize: ({ profile, profileCompleted, locker, appliedExams, bookmarks, readNotificationIds, alertPrefs }) => ({
        profile,
        profileCompleted,
        locker,
        appliedExams,
        bookmarks,
        readNotificationIds,
        alertPrefs,
      }),
    },
  ),
);
