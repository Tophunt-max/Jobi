import { create } from 'zustand';
import { persist } from 'zustand/middleware';

import { persistStorage, StorageKeys } from '@/lib/storage';

export type ThemePreference = 'system' | 'light' | 'dark';

type SettingsState = {
  /** i18n language code; null means "follow the device". */
  language: string | null;
  remindersEnabled: boolean;
  notificationPromptShown: boolean;
  theme: ThemePreference;
  /** True once the user has finished or skipped the intro slides. */
  onboardingDone: boolean;

  setLanguage: (code: string) => void;
  setRemindersEnabled: (enabled: boolean) => void;
  markNotificationPromptShown: () => void;
  setTheme: (theme: ThemePreference) => void;
  completeOnboarding: () => void;
};

export const useSettingsStore = create<SettingsState>()(
  persist(
    (set) => ({
      language: null,
      remindersEnabled: true,
      notificationPromptShown: false,
      theme: 'system',
      onboardingDone: false,

      setLanguage: (language) => set({ language }),
      setRemindersEnabled: (remindersEnabled) => set({ remindersEnabled }),
      markNotificationPromptShown: () => set({ notificationPromptShown: true }),
      setTheme: (theme) => set({ theme }),
      completeOnboarding: () => set({ onboardingDone: true }),
    }),
    {
      name: StorageKeys.settings,
      storage: persistStorage,
      version: 2,
      // v1 had no theme or onboarding flag; existing users skip the intro.
      migrate: (persisted, version) => {
        const state = (persisted ?? {}) as Partial<SettingsState>;
        if (version < 2) return { ...state, theme: 'system', onboardingDone: true };
        return state;
      },
      partialize: ({ language, remindersEnabled, notificationPromptShown, theme, onboardingDone }) => ({
        language,
        remindersEnabled,
        notificationPromptShown,
        theme,
        onboardingDone,
      }),
    },
  ),
);
