/** Design tokens shared by every screen. */
export const palette = {
  navy: '#0B132B',
  blue: '#2563EB',
  blueDark: '#1D4ED8',
  blueSoft: '#DBEAFE',
  saffron: '#EA580C',
  saffronSoft: '#FFEDD5',
  green: '#059669',
  greenSoft: '#D1FAE5',
  red: '#E11D48',
  redSoft: '#FFE4E6',
  sky: '#0284C7',
  skySoft: '#E0F2FE',
  amber: '#D97706',
  amberSoft: '#FEF3C7',
  violet: '#7C3AED',
  violetSoft: '#EDE9FE',
  white: '#FFFFFF',
  black: '#000000',
} as const;

export type ColorScheme = 'light' | 'dark';

export type ThemeColors = {
  background: string;
  surface: string;
  surfaceMuted: string;
  border: string;
  text: string;
  textMuted: string;
  textInverse: string;
  primary: string;
  primarySoft: string;
  onPrimary: string;
  accent: string;
  accentSoft: string;
  success: string;
  successSoft: string;
  danger: string;
  dangerSoft: string;
  warning: string;
  warningSoft: string;
  info: string;
  infoSoft: string;
  overlay: string;
};

export const colors: Record<ColorScheme, ThemeColors> = {
  light: {
    background: '#F8FAFC',
    surface: '#FFFFFF',
    surfaceMuted: '#F1F5F9',
    border: '#E2E8F0',
    text: '#0F172A',
    textMuted: '#64748B',
    textInverse: '#FFFFFF',
    primary: palette.blue,
    primarySoft: palette.blueSoft,
    onPrimary: palette.white,
    accent: palette.saffron,
    accentSoft: palette.saffronSoft,
    success: palette.green,
    successSoft: palette.greenSoft,
    danger: palette.red,
    dangerSoft: palette.redSoft,
    warning: palette.amber,
    warningSoft: palette.amberSoft,
    info: palette.sky,
    infoSoft: palette.skySoft,
    overlay: 'rgba(15, 23, 42, 0.45)',
  },
  dark: {
    background: '#0B1220',
    surface: '#111A2E',
    surfaceMuted: '#1A2540',
    border: '#24314F',
    text: '#E2E8F0',
    textMuted: '#94A3B8',
    textInverse: '#0F172A',
    primary: '#60A5FA',
    primarySoft: '#1E3A8A',
    onPrimary: '#0B1220',
    accent: '#FB923C',
    accentSoft: '#431407',
    success: '#34D399',
    successSoft: '#064E3B',
    danger: '#FB7185',
    dangerSoft: '#4C0519',
    warning: '#FBBF24',
    warningSoft: '#451A03',
    info: '#38BDF8',
    infoSoft: '#082F49',
    overlay: 'rgba(0, 0, 0, 0.6)',
  },
};

export const spacing = { xxs: 2, xs: 4, sm: 8, md: 12, lg: 16, xl: 20, xxl: 28 } as const;

export const radius = { sm: 6, md: 10, lg: 14, xl: 20, pill: 999 } as const;

export const typography = {
  title: { fontSize: 22, fontWeight: '800' as const, lineHeight: 28 },
  heading: { fontSize: 17, fontWeight: '700' as const, lineHeight: 22 },
  subheading: { fontSize: 15, fontWeight: '600' as const, lineHeight: 20 },
  body: { fontSize: 14, fontWeight: '400' as const, lineHeight: 20 },
  caption: { fontSize: 12, fontWeight: '500' as const, lineHeight: 16 },
  tiny: { fontSize: 10, fontWeight: '600' as const, lineHeight: 13 },
};

/** Content never stretches wider than this on tablets and desktop web. */
export const MAX_CONTENT_WIDTH = 760;
