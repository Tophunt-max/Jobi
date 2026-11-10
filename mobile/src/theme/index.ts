import { useColorScheme } from 'react-native';

import { useSettingsStore } from '@/store/settingsStore';

import { colors, radius, shadows, spacing, typography, type ColorScheme, type ThemeColors } from './tokens';

export * from './tokens';

export type Theme = {
  scheme: ColorScheme;
  colors: ThemeColors;
  spacing: typeof spacing;
  radius: typeof radius;
  typography: typeof typography;
  shadows: (typeof shadows)[ColorScheme];
};

const themes: Record<ColorScheme, Theme> = {
  light: { scheme: 'light', colors: colors.light, spacing, radius, typography, shadows: shadows.light },
  dark: { scheme: 'dark', colors: colors.dark, spacing, radius, typography, shadows: shadows.dark },
};

/** Current theme: the user's choice in Profile, or the device setting. */
export function useTheme(): Theme {
  const device = useColorScheme();
  const preference = useSettingsStore((s) => s.theme);
  const scheme = preference === 'system' ? (device === 'dark' ? 'dark' : 'light') : preference;
  return themes[scheme];
}
