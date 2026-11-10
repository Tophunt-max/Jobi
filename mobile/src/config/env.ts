import Constants from 'expo-constants';

type Extra = {
  variant?: 'development' | 'preview' | 'production';
  apiBaseUrl?: string;
  admob?: Partial<Record<
    | 'bannerAndroid'
    | 'bannerIos'
    | 'interstitialAndroid'
    | 'interstitialIos'
    | 'rewardedAndroid'
    | 'rewardedIos',
    string
  >>;
};

const extra = (Constants.expoConfig?.extra ?? {}) as Extra;

const apiBaseUrl = (extra.apiBaseUrl ?? '').replace(/\/+$/, '');

export const env = {
  variant: extra.variant ?? 'development',
  isProduction: extra.variant === 'production',
  apiBaseUrl,
  /** The backend URL must be https; anything else is treated as "not configured". */
  isApiConfigured: apiBaseUrl.startsWith('https://'),
  admob: extra.admob ?? {},
  appVersion: Constants.expoConfig?.version ?? '0.0.0',
} as const;
