import { Ionicons } from '@expo/vector-icons';
import { useRouter, type Href } from 'expo-router';
import { useState, type ComponentProps } from 'react';
import { useTranslation } from 'react-i18next';
import { Linking, Pressable, Share, StyleSheet, View } from 'react-native';
import { useShallow } from 'zustand/react/shallow';

import { Card, Divider, InlineBanner, Row, ScrollScreen, Section, Text, ToggleRow } from '@/components/ui';
import { casteMeta, educationMeta } from '@/constants/catalog';
import { env } from '@/config/env';
import { useAdRuntime } from '@/features/ads/runtime';
import { showPrivacyOptions } from '@/features/ads/service';
import { JobCard } from '@/features/jobs/components/JobCard';
import { useBookmarkedJobs } from '@/features/jobs/hooks';
import { buildDossier } from '@/features/profile/dossier';
import { notificationsSupported, requestNotificationPermission } from '@/features/reminders/notifications';
import { languageMeta } from '@/i18n';
import { relativeTime } from '@/lib/dates';
import { useContentStore } from '@/store/contentStore';
import { useSettingsStore } from '@/store/settingsStore';
import { useUserStore } from '@/store/userStore';
import { useTheme } from '@/theme';

type IconName = ComponentProps<typeof Ionicons>['name'];

function LinkRow({ icon, title, subtitle, href, onPress }: { icon: IconName; title: string; subtitle?: string; href?: Href; onPress?: () => void }) {
  const router = useRouter();
  const { colors } = useTheme();
  return (
    <Pressable
      accessibilityRole="button"
      accessibilityLabel={title}
      onPress={onPress ?? (() => href && router.push(href))}
      style={({ pressed }) => [styles.linkRow, { opacity: pressed ? 0.7 : 1 }]}
    >
      <Ionicons name={icon} size={20} color={colors.primary} />
      <View style={{ flex: 1, gap: 2 }}>
        <Text variant="subheading">{title}</Text>
        {subtitle ? (
          <Text variant="caption" tone="muted" numberOfLines={1}>
            {subtitle}
          </Text>
        ) : null}
      </View>
      <Ionicons name="chevron-forward" size={18} color={colors.textMuted} />
    </Pressable>
  );
}

export default function ProfileScreen() {
  const { t, i18n } = useTranslation();
  const { profile, profileCompleted, locker, exams } = useUserStore(
    useShallow((s) => ({ profile: s.profile, profileCompleted: s.profileCompleted, locker: s.locker, exams: s.appliedExams })),
  );
  const saved = useBookmarkedJobs();
  const settings = useSettingsStore();
  const { lastSyncedAt, status, sync } = useContentStore(
    useShallow((s) => ({ lastSyncedAt: s.lastSyncedAt, status: s.status, sync: s.sync })),
  );
  const privacyRequired = useAdRuntime((s) => s.privacyOptionsRequired);
  const [permissionDenied, setPermissionDenied] = useState(false);

  const toggleReminders = async (enabled: boolean) => {
    if (enabled) {
      const granted = await requestNotificationPermission();
      setPermissionDenied(!granted);
    }
    settings.setRemindersEnabled(enabled);
  };

  const shareDossier = () => {
    void Share.share({ message: buildDossier(profile, locker, exams) });
  };

  return (
    <ScrollScreen>
      {settings.legacyNoticePending ? (
        <InlineBanner
          tone="success"
          icon="checkmark-circle-outline"
          message={t('profile.migrated')}
          action={t('common.done')}
          onAction={settings.dismissLegacyNotice}
        />
      ) : null}

      <Card>
        <Row>
          <View style={{ flex: 1, gap: 2 }}>
            <Text variant="heading">{profile.candidateName || t('profile.eligibility')}</Text>
            <Text variant="caption" tone="muted">
              {profileCompleted
                ? `${profile.age} · ${casteMeta(profile.category).label} · ${educationMeta(profile.qualification).label} · ${profile.homeState}`
                : t('profile.eligibilityHint')}
            </Text>
          </View>
        </Row>
        <Divider />
        <LinkRow icon="create-outline" title={t('profile.eligibility')} subtitle={t('profile.eligibilityHint')} href="/profile/edit" />
        <LinkRow icon="folder-open-outline" title={t('profile.locker')} subtitle={t('profile.lockerHint')} href="/profile/locker" />
        <LinkRow icon="clipboard-outline" title={t('profile.exams')} subtitle={t('profile.examsCount', { count: exams.length })} href="/profile/exams" />
        <LinkRow icon="body-outline" title={t('tools.physical')} subtitle={t('tools.physicalDesc')} href="/tools/physical" />
        <LinkRow icon="notifications-outline" title={t('tools.alerts')} subtitle={t('tools.alertsDesc')} href="/alerts" />
        <LinkRow icon="share-outline" title={t('profile.shareDossier')} onPress={shareDossier} />
      </Card>

      <Section title={`${t('profile.bookmarks')} (${saved.length})`}>
        {saved.length === 0 ? (
          <Text variant="caption" tone="muted">
            {t('profile.bookmarksEmpty')}
          </Text>
        ) : (
          saved.map((job) => <JobCard key={job.id} job={job} compact />)
        )}
      </Section>

      <Section title={t('profile.settings')}>
        <Card>
          <LinkRow
            icon="language-outline"
            title={t('profile.language')}
            subtitle={languageMeta(i18n.language).nativeName}
            href="/language"
          />
          {notificationsSupported ? (
            <>
              <Divider />
              <ToggleRow
                label={t('profile.reminders')}
                description={t('profile.remindersDesc')}
                value={settings.remindersEnabled}
                onValueChange={(v) => void toggleReminders(v)}
              />
              {permissionDenied ? (
                <InlineBanner
                  tone="warning"
                  message={t('profile.remindersDenied')}
                  action={t('profile.openSettings')}
                  onAction={() => void Linking.openSettings()}
                />
              ) : null}
            </>
          ) : null}
          {privacyRequired ? (
            <>
              <Divider />
              <LinkRow icon="shield-outline" title={t('profile.adPrivacy')} onPress={() => void showPrivacyOptions()} />
            </>
          ) : null}
        </Card>
      </Section>

      <Section title={t('profile.data')}>
        <Card>
          <LinkRow
            icon="refresh-outline"
            title={status === 'syncing' ? t('sync.syncing') : t('profile.refresh')}
            subtitle={t('profile.lastSync', { time: lastSyncedAt ? relativeTime(lastSyncedAt) : t('profile.never') })}
            onPress={() => void sync({ force: true })}
          />
        </Card>
      </Section>

      <Text variant="caption" tone="muted" align="center">
        {t('profile.version', { version: env.appVersion })}
      </Text>
    </ScrollScreen>
  );
}

const styles = StyleSheet.create({
  linkRow: { flexDirection: 'row', alignItems: 'center', gap: 12, paddingVertical: 10 },
});
