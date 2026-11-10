import { useRouter } from 'expo-router';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Linking, ScrollView, View } from 'react-native';

import { InlineBanner, MenuRow, Page, ScreenHeader, Surface, Text, ToggleRow } from '@/components/ui';
import { notificationsSupported, requestNotificationPermission } from '@/features/reminders/notifications';
import { useSettingsStore } from '@/store/settingsStore';
import { useUserStore } from '@/store/userStore';
import { useTheme } from '@/theme';

export default function NotificationSettingsScreen() {
  const { t } = useTranslation();
  const router = useRouter();
  const { colors } = useTheme();
  const remindersEnabled = useSettingsStore((s) => s.remindersEnabled);
  const setRemindersEnabled = useSettingsStore((s) => s.setRemindersEnabled);
  const alertPrefs = useUserStore((s) => s.alertPrefs);
  const setAlertPrefs = useUserStore((s) => s.setAlertPrefs);
  const [denied, setDenied] = useState(false);

  const toggleReminders = async (enabled: boolean) => {
    if (enabled) {
      const granted = await requestNotificationPermission();
      setDenied(!granted);
    }
    setRemindersEnabled(enabled);
  };

  return (
    <View style={{ flex: 1, backgroundColor: colors.background }}>
      <ScreenHeader title={t('profileMenu.notificationSettings')} />
      <ScrollView contentContainerStyle={{ paddingVertical: 16 }}>
        <Page style={{ gap: 14 }}>
          <Surface style={{ gap: 6 }}>
            {notificationsSupported ? (
              <ToggleRow
                label={t('profile.reminders')}
                description={t('profile.remindersDesc')}
                value={remindersEnabled}
                onValueChange={(v) => void toggleReminders(v)}
              />
            ) : (
              <Text variant="caption" tone="muted">
                {t('job.remindersWeb')}
              </Text>
            )}
            {denied ? (
              <InlineBanner tone="warning" message={t('profile.remindersDenied')} action={t('profile.openSettings')} onAction={() => void Linking.openSettings()} />
            ) : null}
            <ToggleRow
              label={t('alerts.enabled')}
              description={t('alerts.enabledDesc')}
              value={alertPrefs.enabled}
              onValueChange={(enabled) => setAlertPrefs({ ...alertPrefs, enabled })}
            />
          </Surface>
          <Surface padded={false}>
            <MenuRow icon="options-outline" label={t('profileMenu.jobPreferences')} onPress={() => router.push('/alerts')} />
            <MenuRow icon="notifications-outline" label={t('notifications.title')} onPress={() => router.push('/notifications')} last />
          </Surface>
        </Page>
      </ScrollView>
    </View>
  );
}
