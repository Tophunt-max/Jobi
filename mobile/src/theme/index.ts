import { useColorScheme } from 'react-native';

import { colors, radius, spacing, typography, type ColorScheme, type ThemeColors } from './tokens';

export * from './tokens';

export type Theme = {
  scheme: ColorScheme;
  colors: ThemeColors;
  spacing: typeof spacing;
  radius: typeof radius;
  typography: typeof typography;
};

const themes: Record<ColorScheme, Theme> = {
  light: { scheme: 'light', colors: colors.light, spacing, radius, typography },
  dark: { scheme: 'dark', colors: colors.dark, spacing, radius, typography },
};

export function useTheme(): Theme {
  const scheme = useColorScheme() === 'dark' ? 'dark' : 'light';
  return themes[scheme];
}
