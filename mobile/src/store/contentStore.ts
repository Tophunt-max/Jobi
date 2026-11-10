import { create } from 'zustand';
import { persist } from 'zustand/middleware';

import { createLogger } from '@/lib/logger';
import { persistStorage, StorageKeys } from '@/lib/storage';
import { ApiError } from '@/services/api/client';
import { fetchJob, fetchSyncAll } from '@/services/api/endpoints';
import type { ContentSnapshot, Job } from '@/types/domain';

const log = createLogger('sync');

export type SyncStatus = 'idle' | 'syncing' | 'success' | 'offline' | 'not_configured';

type ContentState = ContentSnapshot & {
  lastSyncedAt: number;
  status: SyncStatus;
  errorMessage: string | null;
  sync: (opts?: { force?: boolean }) => Promise<void>;
  /** Looks up a job locally, then on the server. */
  resolveJob: (idOrSlug: string) => Promise<Job | null>;
};

const EMPTY: ContentSnapshot = {
  jobs: [],
  results: [],
  admitCards: [],
  syllabi: [],
  admissions: [],
  answerKeys: [],
  notifications: [],
  serverTimestamp: 0,
};

/** A foreground sync is skipped when the cache is younger than this. */
const MIN_SYNC_INTERVAL_MS = 15 * 60_000;

let inFlight: Promise<void> | null = null;

export const useContentStore = create<ContentState>()(
  persist(
    (set, get) => ({
      ...EMPTY,
      lastSyncedAt: 0,
      status: 'idle',
      errorMessage: null,

      sync: (opts) => {
        if (inFlight) return inFlight;
        if (!opts?.force && Date.now() - get().lastSyncedAt < MIN_SYNC_INTERVAL_MS && get().jobs.length > 0) {
          return Promise.resolve();
        }
        inFlight = (async () => {
          set({ status: 'syncing', errorMessage: null });
          try {
            const snapshot = await fetchSyncAll();
            set({ ...snapshot, lastSyncedAt: Date.now(), status: 'success' });
            log.info(`synced ${snapshot.jobs.length} jobs`);
          } catch (e) {
            const err = e as ApiError;
            set({
              status: err.kind === 'not_configured' ? 'not_configured' : 'offline',
              errorMessage: err.message,
            });
          } finally {
            inFlight = null;
          }
        })();
        return inFlight;
      },

      resolveJob: async (idOrSlug) => {
        const local = get().jobs.find((j) => j.id === idOrSlug || j.slug === idOrSlug);
        if (local) return local;
        try {
          return await fetchJob(idOrSlug);
        } catch {
          return null;
        }
      },
    }),
    {
      name: StorageKeys.content,
      storage: persistStorage,
      version: 1,
      partialize: (s) => ({
        jobs: s.jobs,
        results: s.results,
        admitCards: s.admitCards,
        syllabi: s.syllabi,
        admissions: s.admissions,
        answerKeys: s.answerKeys,
        notifications: s.notifications,
        serverTimestamp: s.serverTimestamp,
        lastSyncedAt: s.lastSyncedAt,
      }),
      // Unknown versions start from an empty cache; the next sync refills it.
      migrate: () => ({ ...EMPTY, lastSyncedAt: 0 }),
    },
  ),
);
