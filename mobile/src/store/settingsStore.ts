import { create } from 'zustand';
import { persist } from 'zustand/middleware';

import { persistStorage, StorageKeys } from '@/lib/storage';

type SettingsState = {
  /** i18n language code; null means "follow the device". */
  language: string | null;
  remindersEnabled: boolean;
  notificationPromptShown: boolean;

  setLanguage: (code: string) => void;
  setRemindersEnabled: (enabled: boolean) => void;
  markNotificationPromptShown: () => void;
};

export const useSettingsStore = create<SettingsState>()(
  persist(
    (set) => ({
      language: null,
      remindersEnabled: true,
      notificationPromptShown: false,

      setLanguage: (language) => set({ language }),
      setRemindersEnabled: (remindersEnabled) => set({ remindersEnabled }),
      markNotificationPromptShown: () => set({ notificationPromptShown: true }),
    }),
    {
      name: StorageKeys.settings,
      storage: persistStorage,
      version: 1,
      partialize: ({ language, remindersEnabled, notificationPromptShown }) => ({
        language,
        remindersEnabled,
        notificationPromptShown,
      }),
    },
  ),
);
