import { Stack, type ErrorBoundaryProps } from 'expo-router';
import * as SplashScreen from 'expo-splash-screen';
import { StatusBar } from 'expo-status-bar';
import { useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { View } from 'react-native';
import { SafeAreaProvider } from 'react-native-safe-area-context';

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
  useAppBootstrap(hydrated);

  useEffect(() => {
    if (hydrated) void SplashScreen.hideAsync().catch(() => {});
  }, [hydrated]);

  if (!hydrated) return <View style={{ flex: 1, backgroundColor: colors.background }} />;

  return (
    <SafeAreaProvider>
      <StatusBar style={scheme === 'dark' ? 'light' : 'dark'} />
      <Stack
        screenOptions={{
          headerStyle: { backgroundColor: colors.surface },
          headerTintColor: colors.text,
          headerTitleStyle: { fontWeight: '700' },
          contentStyle: { backgroundColor: colors.background },
          headerBackButtonDisplayMode: 'minimal',
        }}
      >
        <Stack.Screen name="(tabs)" options={{ headerShown: false, title: t('app.title') }} />
        <Stack.Screen name="job/[id]" options={{ title: '' }} />
        <Stack.Screen name="browser" options={{ title: '' }} />
        <Stack.Screen name="alerts" options={{ title: t('alerts.title') }} />
        <Stack.Screen name="notifications" options={{ title: t('notifications.title'), presentation: 'modal' }} />
        <Stack.Screen name="language" options={{ title: t('language.title'), presentation: 'modal' }} />
        <Stack.Screen name="report/[id]" options={{ title: t('report.title'), presentation: 'modal' }} />
        <Stack.Screen name="tools/age" options={{ title: t('age.title') }} />
        <Stack.Screen name="tools/salary" options={{ title: t('salary.title') }} />
        <Stack.Screen name="tools/physical" options={{ title: t('physical.title') }} />
        <Stack.Screen name="profile/edit" options={{ title: t('profile.eligibility') }} />
        <Stack.Screen name="profile/locker" options={{ title: t('profile.locker') }} />
        <Stack.Screen name="profile/exams" options={{ title: t('exams.title') }} />
        <Stack.Screen name="profile/exam-form" options={{ title: t('exams.add'), presentation: 'modal' }} />
        <Stack.Screen name="+not-found" options={{ title: t('notFound.title') }} />
      </Stack>
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
