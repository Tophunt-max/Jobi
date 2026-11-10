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
import { configureNotifications } from '@/features/reminders/notifications';
import { initI18n } from '@/i18n';
import { useStoresHydrated } from '@/store/hydration';
import { useTheme } from '@/theme';

// Runs once, before the first render.
initI18n(null);
void SplashScreen.preventAutoHideAsync().catch(() => {});
void configureNotifications();

export default function RootLayout() {
  const hydrated = useStoresHydrated();
  const { colors, scheme } = useTheme();
  const { t } = useTranslation();
  const [splashDone, setSplashDone] = useState(false);
  const finishSplash = useCallback(() => setSplashDone(true), []);
  useAppBootstrap(hydrated);

  useEffect(() => {
    // The branded JS splash covers the screen, so the native one can go as soon as it mounts.
    void SplashScreen.hideAsync().catch(() => {});
  }, []);

  return (
    <SafeAreaProvider>
      <StatusBar style={!splashDone || scheme === 'dark' ? 'light' : 'dark'} />
      {hydrated ? (
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
      ) : (
        <View style={{ flex: 1, backgroundColor: colors.background }} />
      )}
      {!splashDone ? <BrandSplash ready={hydrated} onDone={finishSplash} /> : null}
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
