import { getLocales } from 'expo-localization';
import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import { I18nManager, Platform } from 'react-native';

import as from './locales/as.json';
import bn from './locales/bn.json';
import en from './locales/en.json';
import gu from './locales/gu.json';
import hi from './locales/hi.json';
import kn from './locales/kn.json';
import ml from './locales/ml.json';
import mr from './locales/mr.json';
import or from './locales/or.json';
import pa from './locales/pa.json';
import ta from './locales/ta.json';
import te from './locales/te.json';
import ur from './locales/ur.json';

export type AppLanguage = {
  code: string;
  nativeName: string;
  englishName: string;
  rtl?: boolean;
  /** Fully translated; other languages fall back to English for missing strings. */
  complete?: boolean;
};

export const LANGUAGES: AppLanguage[] = [
  { code: 'en', nativeName: 'English', englishName: 'English', complete: true },
  { code: 'hi', nativeName: 'हिन्दी', englishName: 'Hindi', complete: true },
  { code: 'or', nativeName: 'ଓଡ଼ିଆ', englishName: 'Odia' },
  { code: 'bn', nativeName: 'বাংলা', englishName: 'Bengali' },
  { code: 'mr', nativeName: 'मराठी', englishName: 'Marathi' },
  { code: 'ta', nativeName: 'தமிழ்', englishName: 'Tamil' },
  { code: 'te', nativeName: 'తెలుగు', englishName: 'Telugu' },
  { code: 'gu', nativeName: 'ગુજરાતી', englishName: 'Gujarati' },
  { code: 'pa', nativeName: 'ਪੰਜਾਬੀ', englishName: 'Punjabi' },
  { code: 'ur', nativeName: 'اردو', englishName: 'Urdu', rtl: true },
  { code: 'kn', nativeName: 'ಕನ್ನಡ', englishName: 'Kannada' },
  { code: 'ml', nativeName: 'മലയാളം', englishName: 'Malayalam' },
  { code: 'as', nativeName: 'অসমীয়া', englishName: 'Assamese' },
];

const resources = {
  en: { translation: en },
  hi: { translation: hi },
  or: { translation: or },
  bn: { translation: bn },
  mr: { translation: mr },
  ta: { translation: ta },
  te: { translation: te },
  gu: { translation: gu },
  pa: { translation: pa },
  ur: { translation: ur },
  kn: { translation: kn },
  ml: { translation: ml },
  as: { translation: as },
} as const;

export function deviceLanguage(): string {
  const code = getLocales()[0]?.languageCode ?? 'en';
  return LANGUAGES.some((l) => l.code === code) ? code : 'en';
}

export function languageMeta(code: string): AppLanguage {
  return LANGUAGES.find((l) => l.code === code) ?? LANGUAGES[0]!;
}

let initialised = false;

export function initI18n(preferred: string | null): typeof i18n {
  const lng = preferred && LANGUAGES.some((l) => l.code === preferred) ? preferred : deviceLanguage();
  if (!initialised) {
    initialised = true;
    // eslint-disable-next-line import/no-named-as-default-member -- the default instance is used on purpose
    void i18n.use(initReactI18next).init({
      resources,
      lng,
      fallbackLng: 'en',
      interpolation: { escapeValue: false },
      returnNull: false,
    });
  } else if (i18n.language !== lng) {
    // eslint-disable-next-line import/no-named-as-default-member -- the default instance is used on purpose
    void i18n.changeLanguage(lng);
  }
  applyDirection(lng);
  return i18n;
}

/**
 * Returns true when the layout direction must change. Native RTL only takes effect
 * after a restart, so the caller tells the user to reopen the app.
 */
export function applyDirection(code: string): boolean {
  const rtl = !!languageMeta(code).rtl;
  if (Platform.OS === 'web') {
    if (typeof document !== 'undefined') document.documentElement.dir = rtl ? 'rtl' : 'ltr';
    return false;
  }
  if (I18nManager.isRTL === rtl) return false;
  I18nManager.allowRTL(rtl);
  I18nManager.forceRTL(rtl);
  return true;
}

export { i18n };
