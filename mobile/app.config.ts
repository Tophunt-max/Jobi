import type { ConfigContext, ExpoConfig } from 'expo/config';

/**
 * Build-time configuration. Values come from environment variables
 * (GitHub Actions variables/secrets in CI, a local .env file, or the shell).
 *
 * APP_VARIANT: development | preview | production
 */
type Variant = 'development' | 'preview' | 'production';

const VARIANT = (process.env.APP_VARIANT ?? 'development') as Variant;
const IS_PROD = VARIANT === 'production';

// Google's public AdMob sample app IDs. Non-production builds always use them.
const TEST_ADMOB_APP_ID = {
  android: 'ca-app-pub-3940256099942544~3347511713',
  ios: 'ca-app-pub-3940256099942544~1458002511',
};
const ADMOB_APP_ID_RE = /^ca-app-pub-\d{16}~\d{10}$/;
const ADMOB_UNIT_ID_RE = /^ca-app-pub-\d{16}\/\d{10}$/;

// Same applicationId as the Kotlin app, so the Play Store treats this as an update.
const ANDROID_PACKAGE = process.env.ANDROID_PACKAGE ?? 'com.aistudio.sarkarijobportal.kxrtzp';
const IOS_BUNDLE_ID = process.env.IOS_BUNDLE_ID ?? 'com.aistudio.sarkarijobportal.kxrtzp';

function env(name: string): string {
  return (process.env[name] ?? '').trim();
}

function admobAppId(platform: 'android' | 'ios'): string {
  const value = env(platform === 'android' ? 'ADMOB_ANDROID_APP_ID' : 'ADMOB_IOS_APP_ID');
  if (!IS_PROD || !value) return TEST_ADMOB_APP_ID[platform];
  if (!ADMOB_APP_ID_RE.test(value)) {
    throw new Error(`Invalid AdMob ${platform} app ID "${value}" (expected ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX)`);
  }
  return value;
}

/** VERSION_CODE is set by CI from the release tag (see .github/workflows/mobile.yml). */
function versionCode(): number {
  const raw = env('VERSION_CODE');
  if (!raw) return 1;
  const n = Number(raw);
  if (!Number.isInteger(n) || n < 1 || n > 2_100_000_000) throw new Error(`Invalid VERSION_CODE "${raw}"`);
  return n;
}

function admobUnit(name: string): string {
  const value = env(name);
  if (!value) return '';
  if (!ADMOB_UNIT_ID_RE.test(value)) {
    throw new Error(`Invalid AdMob unit ID in ${name}: "${value}"`);
  }
  return value;
}

export default ({ config }: ConfigContext): ExpoConfig => {
  const apiBaseUrl = env('EXPO_PUBLIC_API_BASE_URL').replace(/\/+$/, '');
  if (IS_PROD && !apiBaseUrl.startsWith('https://')) {
    throw new Error('EXPO_PUBLIC_API_BASE_URL must be an https:// URL for production builds.');
  }

  const appName = VARIANT === 'production' ? 'Sarkari Jobs' : `Sarkari Jobs (${VARIANT})`;

  return {
    ...config,
    name: appName,
    slug: 'sarkari-jobs',
    version: env('VERSION_NAME') || '2.0.0',
    orientation: 'portrait',
    icon: './assets/icon.png',
    scheme: 'sarkari',
    userInterfaceStyle: 'automatic',
    ios: {
      bundleIdentifier: IOS_BUNDLE_ID,
      buildNumber: String(versionCode()),
      // Lets xcodebuild sign automatically with the App Store Connect API key in CI.
      appleTeamId: env('APPLE_TEAM_ID') || undefined,
      supportsTablet: true,
      infoPlist: {
        ITSAppUsesNonExemptEncryption: false,
      },
    },
    android: {
      package: ANDROID_PACKAGE,
      versionCode: versionCode(),
      // The document locker holds personal data; keep it out of cloud backups (same as the Kotlin app).
      allowBackup: false,
      adaptiveIcon: {
        backgroundColor: '#E6F4FE',
        foregroundImage: './assets/android-icon-foreground.png',
        backgroundImage: './assets/android-icon-background.png',
        monochromeImage: './assets/android-icon-monochrome.png',
      },
      blockedPermissions: [
        'android.permission.RECORD_AUDIO',
        'android.permission.READ_EXTERNAL_STORAGE',
        'android.permission.WRITE_EXTERNAL_STORAGE',
        'android.permission.SYSTEM_ALERT_WINDOW',
      ],
      predictiveBackGestureEnabled: false,
    },
    web: {
      bundler: 'metro',
      // Single-page output: the app reads device storage on start, so it is not pre-rendered.
      output: 'single',
      favicon: './assets/favicon.png',
      name: 'Sarkari Jobs',
      shortName: 'Sarkari Jobs',
      themeColor: '#2563EB',
      backgroundColor: '#F8FAFC',
    },
    plugins: [
      'expo-router',
      'expo-localization',
      'expo-web-browser',
      [
        'expo-splash-screen',
        {
          image: './assets/splash-icon.png',
          imageWidth: 160,
          resizeMode: 'contain',
          backgroundColor: '#F8FAFC',
          dark: { backgroundColor: '#0B1220' },
        },
      ],
      [
        'expo-notifications',
        {
          icon: './assets/android-icon-monochrome.png',
          color: '#2563EB',
          // Only local reminders are used; no APNs entitlement needed.
          enableRemoteNotifications: false,
        },
      ],
      [
        'react-native-google-mobile-ads',
        {
          androidAppId: admobAppId('android'),
          iosAppId: admobAppId('ios'),
          // Wait for the consent form before the SDK collects measurement data.
          delayAppMeasurementInit: true,
        },
      ],
      './plugins/withAndroidReleaseSigning',
    ],
    experiments: {
      typedRoutes: true,
    },
    extra: {
      variant: VARIANT,
      apiBaseUrl,
      admob: {
        // Empty values fall back to Google test units at runtime.
        bannerAndroid: admobUnit('ADMOB_BANNER_ANDROID'),
        bannerIos: admobUnit('ADMOB_BANNER_IOS'),
        interstitialAndroid: admobUnit('ADMOB_INTERSTITIAL_ANDROID'),
        interstitialIos: admobUnit('ADMOB_INTERSTITIAL_IOS'),
        rewardedAndroid: admobUnit('ADMOB_REWARDED_ANDROID'),
        rewardedIos: admobUnit('ADMOB_REWARDED_IOS'),
      },
    },
  };
};
