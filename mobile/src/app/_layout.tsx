import { Stack, type ErrorBoundaryProps } from 'expo-router';
import * as SplashScreen from 'expo-splash-screen';
import { StatusBar } from 'expo-status-bar';
import { useCallback, useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { View } from 'react-native';
import { SafeAreaProvider } from 'react-native-safe-area-context';

import { BrandSplash } from '@/components/BrandSplash';
import { Button, EmptyState } from '@/components/ui';
import { useAppBootstrap } from '@/features/bootstrap/useAppBootstrap';
import { splashMinVisibleMs } from '@/features/launch/onboarding';
import { useLaunchReady } from '@/features/launch/useLaunchReady';
import { configureNotifications } from '@/features/reminders/notifications';
import { initI18n } from '@/i18n';
import { useSettingsStore } from '@/store/settingsStore';
import { useTheme } from '@/theme';

// Runs once, before the first render.
initI18n(null);
void SplashScreen.preventAutoHideAsync().catch(() => {});
SplashScreen.setOptions({ duration: 250, fade: true });

let nativeSplashHidden = false;
/** Called from the paint callback and a safety timer; only the first call does anything. */
function hideNativeSplashOnce() {
  if (nativeSplashHidden) return;
  nativeSplashHidden = true;
  try {
    SplashScreen.hide();
  } catch {
    // Already hidden or not available (e.g. a dev reload); nothing to do.
  }
}
void configureNotifications();

export default function RootLayout() {
  const ready = useLaunchReady();
  const onboardingDone = useSettingsStore((s) => s.onboardingDone);
  const { colors, scheme } = useTheme();
  const { t } = useTranslation();
  const [splashDone, setSplashDone] = useState(false);
  const finishSplash = useCallback(() => setSplashDone(true), []);
  useAppBootstrap(ready);

  // Safety net: never leave the native splash up if the branded one fails to mount.
  useEffect(() => {
    const timer = setTimeout(hideNativeSplashOnce, 3000);
    return () => clearTimeout(timer);
  }, []);

  return (
    <SafeAreaProvider>
      <StatusBar style={!splashDone || scheme === 'dark' ? 'light' : 'dark'} />
      {ready ? (
        <View
          style={{ flex: 1 }}
          // Screen readers skip the app until the splash has gone.
          aria-hidden={!splashDone}
        >
          <Stack
            screenOptions={{
              headerShown: false,
              headerStyle: { backgroundColor: colors.surface },
              headerTintColor: colors.text,
              headerTitleStyle: { fontWeight: '800', fontSize: 18 },
              headerTitleAlign: 'left',
              headerShadowVisible: false,
              contentStyle: { backgroundColor: colors.background },
              headerBackButtonDisplayMode: 'minimal',
            }}
          >
            <Stack.Screen name="(tabs)" options={{ title: t('app.title') }} />
            <Stack.Screen name="onboarding" options={{ animation: 'fade' }} />
            {/* Screens below keep the native header. */}
            <Stack.Screen name="browser" options={{ headerShown: true, title: '' }} />
            <Stack.Screen name="alerts" options={{ headerShown: true, title: t('alerts.title') }} />
            <Stack.Screen name="language" options={{ headerShown: true, title: t('language.title'), presentation: 'modal' }} />
            <Stack.Screen name="report/[id]" options={{ headerShown: true, title: t('report.title'), presentation: 'modal' }} />
            <Stack.Screen name="tools/age" options={{ headerShown: true, title: t('age.title') }} />
            <Stack.Screen name="tools/salary" options={{ headerShown: true, title: t('salary.title') }} />
            <Stack.Screen name="tools/physical" options={{ headerShown: true, title: t('physical.title') }} />
            <Stack.Screen name="profile/edit" options={{ headerShown: true, title: t('profileMenu.editProfile') }} />
            <Stack.Screen name="profile/locker" options={{ headerShown: true, title: t('profile.locker') }} />
            <Stack.Screen name="profile/exam-form" options={{ headerShown: true, title: t('exams.add'), presentation: 'modal' }} />
            <Stack.Screen name="+not-found" options={{ headerShown: true, title: t('notFound.title') }} />
          </Stack>
        </View>
      ) : (
        <View style={{ flex: 1, backgroundColor: colors.background }} />
      )}
      {!splashDone ? (
        <BrandSplash
          ready={ready}
          minVisibleMs={splashMinVisibleMs(ready && onboardingDone)}
          // The branded splash has painted over everything, so the native one can go.
          onPainted={hideNativeSplashOnce}
          onDone={finishSplash}
        />
      ) : null}
    </SafeAreaProvider>
  );
}

/** Shown by Expo Router when a screen throws during render. */
export function ErrorBoundary({ retry }: ErrorBoundaryProps) {
  const { colors } = useTheme();
  const { t } = useTranslation();
  return (
    <View style={{ flex: 1, justifyContent: 'center', backgroundColor: colors.background }}>
      <EmptyState icon="warning-outline" title={t('error.title')} body={t('error.body')}>
        <Button label={t('common.retry')} onPress={retry} icon="refresh" />
      </EmptyState>
    </View>
  );
}
