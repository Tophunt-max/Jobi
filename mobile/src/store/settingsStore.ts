import { create } from 'zustand';
import { persist } from 'zustand/middleware';

import { persistStorage, StorageKeys } from '@/lib/storage';

type SettingsState = {
  /** i18n language code; null means "follow the device". */
  language: string | null;
  remindersEnabled: boolean;
  notificationPromptShown: boolean;
  /** Data from the Kotlin app was imported. */
  legacyMigrationDone: boolean;
  /** The Kotlin app's copies were deleted (one launch after import, once the new copy is on disk). */
  legacyCleared: boolean;
  /** Shown once after a successful import. */
  legacyNoticePending: boolean;

  setLanguage: (code: string) => void;
  setRemindersEnabled: (enabled: boolean) => void;
  markNotificationPromptShown: () => void;
  markLegacyMigrationDone: (imported: boolean) => void;
  markLegacyCleared: () => void;
  dismissLegacyNotice: () => void;
};

export const useSettingsStore = create<SettingsState>()(
  persist(
    (set) => ({
      language: null,
      remindersEnabled: true,
      notificationPromptShown: false,
      legacyMigrationDone: false,
      legacyCleared: false,
      legacyNoticePending: false,

      setLanguage: (language) => set({ language }),
      setRemindersEnabled: (remindersEnabled) => set({ remindersEnabled }),
      markNotificationPromptShown: () => set({ notificationPromptShown: true }),
      markLegacyMigrationDone: (imported) =>
        set({ legacyMigrationDone: true, legacyNoticePending: imported, legacyCleared: !imported }),
      markLegacyCleared: () => set({ legacyCleared: true }),
      dismissLegacyNotice: () => set({ legacyNoticePending: false }),
    }),
    {
      name: StorageKeys.settings,
      storage: persistStorage,
      version: 1,
      partialize: ({
        language,
        remindersEnabled,
        notificationPromptShown,
        legacyMigrationDone,
        legacyCleared,
        legacyNoticePending,
      }) => ({
        language,
        remindersEnabled,
        notificationPromptShown,
        legacyMigrationDone,
        legacyCleared,
        legacyNoticePending,
      }),
    },
  ),
);
