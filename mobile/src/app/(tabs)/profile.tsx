import { Ionicons } from '@expo/vector-icons';
import { useRouter } from 'expo-router';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Image, ScrollView, Share, StyleSheet, View } from 'react-native';
import { useShallow } from 'zustand/react/shallow';

import { images } from '@/assets/images';
import { MenuRow, OptionSheet, Page, ScreenHeader, Surface, Text } from '@/components/ui';
import { casteMeta, educationMeta } from '@/constants/catalog';
import { env } from '@/config/env';
import { useAdRuntime } from '@/features/ads/runtime';
import { showPrivacyOptions } from '@/features/ads/service';
import { buildDossier } from '@/features/profile/dossier';
import { languageMeta } from '@/i18n';
import { relativeTime } from '@/lib/dates';
import { useContentStore } from '@/store/contentStore';
import { useSettingsStore, type ThemePreference } from '@/store/settingsStore';
import { useUserStore } from '@/store/userStore';
import { useTheme } from '@/theme';

export default function ProfileScreen() {
  const { t, i18n } = useTranslation();
  const router = useRouter();
  const { colors } = useTheme();
  const { profile, profileCompleted, locker, exams } = useUserStore(
    useShallow((s) => ({ profile: s.profile, profileCompleted: s.profileCompleted, locker: s.locker, exams: s.appliedExams })),
  );
  const { theme, setTheme } = useSettingsStore(useShallow((s) => ({ theme: s.theme, setTheme: s.setTheme })));
  const { lastSyncedAt, status, sync } = useContentStore(useShallow((s) => ({ lastSyncedAt: s.lastSyncedAt, status: s.status, sync: s.sync })));
  const privacyRequired = useAdRuntime((s) => s.privacyOptionsRequired);
  const [themeOpen, setThemeOpen] = useState(false);

  const themeLabel: Record<ThemePreference, string> = {
    system: t('profileMenu.themeSystem'),
    light: t('profileMenu.themeLight'),
    dark: t('profileMenu.themeDark'),
  };

  return (
    <View style={{ flex: 1, backgroundColor: colors.background }}>
      <ScreenHeader title={t('profileMenu.title')} back={false} />
      <ScrollView contentContainerStyle={{ paddingVertical: 16, paddingBottom: 32 }}>
        <Page style={{ gap: 16 }}>
          <Surface onPress={() => router.push('/profile/edit')} accessibilityLabel={t('profileMenu.editProfile')} style={styles.userCard}>
            <Image source={images.profile.avatar} style={styles.avatar} accessibilityIgnoresInvertColors />
            <View style={{ flex: 1, gap: 2 }}>
              <Text variant="heading" weight="900" numberOfLines={1}>
                {profile.candidateName || t('profileMenu.addName')}
              </Text>
              <Text variant="caption" tone="muted" numberOfLines={1}>
                {profile.email || t('profileMenu.addEmail')}
              </Text>
              {profileCompleted ? (
                <Text variant="tiny" tone="primary" weight="700" numberOfLines={1}>
                  {`${profile.age} · ${casteMeta(profile.category).label} · ${educationMeta(profile.qualification).label}`}
                </Text>
              ) : null}
            </View>
            <Ionicons name="chevron-forward" size={18} color={colors.textMuted} />
          </Surface>

          <Surface padded={false}>
            <MenuRow icon="person-outline" label={t('profileMenu.editProfile')} onPress={() => router.push('/profile/edit')} />
            <MenuRow icon="options-outline" label={t('profileMenu.jobPreferences')} onPress={() => router.push('/alerts')} />
            <MenuRow icon="notifications-outline" label={t('profileMenu.notificationSettings')} onPress={() => router.push('/settings/notifications')} />
            <MenuRow icon="bookmark-outline" label={t('profileMenu.savedJobs')} onPress={() => router.navigate('/saved')} />
            <MenuRow
              icon="clipboard-outline"
              label={t('applications.title')}
              value={exams.length ? String(exams.length) : undefined}
              onPress={() => router.push('/applications')}
            />
            <MenuRow
              icon="globe-outline"
              label={t('profile.language')}
              subtitle={t('profileMenu.languageHint')}
              value={languageMeta(i18n.language).nativeName}
              onPress={() => router.push('/language')}
            />
            <MenuRow icon="contrast-outline" label={t('profileMenu.theme')} value={themeLabel[theme]} onPress={() => setThemeOpen(true)} />
            <MenuRow icon="help-circle-outline" label={t('help.title')} onPress={() => router.push('/help')} />
            <MenuRow icon="information-circle-outline" label={t('about.title')} onPress={() => router.push('/about')} last />
          </Surface>

          <Text variant="caption" weight="800" tone="muted" style={{ marginLeft: 4 }}>
            {t('profileMenu.more').toUpperCase()}
          </Text>
          <Surface padded={false}>
            <MenuRow icon="folder-open-outline" label={t('profile.locker')} onPress={() => router.push('/profile/locker')} />
            <MenuRow icon="checkmark-done-outline" label={t('eligibility.title')} onPress={() => router.push('/eligibility')} />
            <MenuRow icon="calendar-outline" label={t('calendar.title')} onPress={() => router.push('/exam-calendar')} />
            <MenuRow icon="hourglass-outline" label={t('tools.age')} onPress={() => router.push('/tools/age')} />
            <MenuRow icon="cash-outline" label={t('tools.salary')} onPress={() => router.push('/tools/salary')} />
            <MenuRow icon="body-outline" label={t('tools.physical')} onPress={() => router.push('/tools/physical')} />
            <MenuRow icon="share-outline" label={t('profile.shareDossier')} onPress={() => void Share.share({ message: buildDossier(profile, locker, exams) })} />
            {privacyRequired ? <MenuRow icon="shield-outline" label={t('profile.adPrivacy')} onPress={() => void showPrivacyOptions()} /> : null}
            <MenuRow
              icon="refresh-outline"
              label={status === 'syncing' ? t('sync.syncing') : t('profile.refresh')}
              subtitle={t('profile.lastSync', { time: lastSyncedAt ? relativeTime(lastSyncedAt) : t('profile.never') })}
              onPress={() => void sync({ force: true })}
              last
            />
          </Surface>

          <Text variant="caption" tone="muted" align="center">
            {t('profile.version', { version: env.appVersion })}
          </Text>
        </Page>
      </ScrollView>
      <OptionSheet<ThemePreference>
        visible={themeOpen}
        title={t('profileMenu.theme')}
        value={theme}
        options={(['system', 'light', 'dark'] as const).map((v) => ({ value: v, label: themeLabel[v] }))}
        onSelect={setTheme}
        onClose={() => setThemeOpen(false)}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  userCard: { flexDirection: 'row', alignItems: 'center', gap: 14 },
  avatar: { width: 58, height: 58, borderRadius: 29 },
});
