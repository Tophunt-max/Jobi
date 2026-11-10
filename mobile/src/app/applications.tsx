import { Ionicons } from '@expo/vector-icons';
import { useLocalSearchParams, useRouter } from 'expo-router';
import { useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Pressable, ScrollView, StyleSheet, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { emblemArt } from '@/assets/images';
import { ArtIcon, EmptyArt, Page, Pill, ScreenHeader, Surface, TabPills, Text, type PillTone } from '@/components/ui';
import { stageMeta } from '@/constants/catalog';
import { matchJobs } from '@/features/alerts/matcher';
import { JobCard } from '@/features/jobs/components/JobCard';
import { useBookmarkedJobs, useOpenJobs } from '@/features/jobs/hooks';
import { artForText } from '@/features/jobs/logic';
import { formatDate, parseDate } from '@/lib/dates';
import { useUserStore } from '@/store/userStore';
import { useTheme } from '@/theme';
import type { ExamStage } from '@/types/domain';

type Tab = 'applied' | 'saved' | 'alerts';

const STAGE_TONE: Record<ExamStage, PillTone> = {
  SUBMITTED: 'blue',
  ADMIT_CARD_OUT: 'orange',
  EXAM_SCHEDULED: 'purple',
  RESULT_AWAITED: 'amber',
  SELECTED: 'green',
  REJECTED: 'red',
};

export default function ApplicationsScreen() {
  const { t } = useTranslation();
  const router = useRouter();
  const insets = useSafeAreaInsets();
  const { colors, shadows } = useTheme();
  const { tab: initial } = useLocalSearchParams<{ tab?: Tab }>();
  const [tab, setTab] = useState<Tab>(initial === 'saved' || initial === 'alerts' ? initial : 'applied');
  const exams = useUserStore((s) => s.appliedExams);
  const alertPrefs = useUserStore((s) => s.alertPrefs);
  const saved = useBookmarkedJobs();
  const jobs = useOpenJobs();
  const matched = useMemo(() => (alertPrefs.enabled ? matchJobs(jobs, alertPrefs) : []), [jobs, alertPrefs]);

  return (
    <View style={{ flex: 1, backgroundColor: colors.background }}>
      <ScreenHeader title={t('applications.title')} />
      <View style={{ backgroundColor: colors.surface, paddingBottom: 12 }}>
        <Page>
          <TabPills<Tab>
            value={tab}
            onChange={setTab}
            options={[
              { value: 'applied', label: t('applications.applied') },
              { value: 'saved', label: t('applications.saved') },
              { value: 'alerts', label: t('applications.alerts') },
            ]}
          />
        </Page>
      </View>
      <ScrollView contentContainerStyle={{ paddingVertical: 14, paddingBottom: 110 }}>
        <Page style={{ gap: 10 }}>
          {tab === 'applied' ? (
            exams.length === 0 ? (
              <EmptyArt title={t('applications.emptyApplied')} body={t('exams.empty')} />
            ) : (
              exams.map((e) => {
                const stage = stageMeta(e.stage);
                const applied = parseDate(e.appliedDate);
                return (
                  <Surface
                    key={e.id}
                    accessibilityLabel={e.jobTitle}
                    onPress={() => router.push({ pathname: '/profile/exam-form', params: { id: e.id } })}
                    style={styles.row}
                  >
                    <ArtIcon source={emblemArt[artForText(`${e.jobTitle} ${e.organization}`)]} size={44} />
                    <View style={{ flex: 1, gap: 4 }}>
                      <Text weight="800" numberOfLines={2}>
                        {e.jobTitle}
                      </Text>
                      <Text variant="caption" tone="muted">
                        {applied
                          ? t('applications.appliedOn', { date: formatDate(e.appliedDate) })
                          : e.organization || t('applications.noDate')}
                      </Text>
                      <Pill tone={STAGE_TONE[e.stage]} label={t('applications.status', { stage: stage.label })} />
                    </View>
                    <Ionicons name="chevron-forward" size={16} color={colors.textMuted} />
                  </Surface>
                );
              })
            )
          ) : null}

          {tab === 'saved' ? (
            saved.length === 0 ? (
              <EmptyArt title={t('saved.empty')} body={t('profile.bookmarksEmpty')} />
            ) : (
              saved.map((job) => <JobCard key={job.id} job={job} />)
            )
          ) : null}

          {tab === 'alerts' ? (
            <>
              <Surface onPress={() => router.push('/alerts')} style={styles.row} accessibilityLabel={t('profileMenu.jobPreferences')}>
                <Ionicons name="options-outline" size={22} color={colors.primary} />
                <View style={{ flex: 1 }}>
                  <Text weight="800">{t('profileMenu.jobPreferences')}</Text>
                  <Text variant="caption" tone="muted">
                    {alertPrefs.enabled ? t('alerts.matches', { count: matched.length }) : t('alerts.enabledDesc')}
                  </Text>
                </View>
                <Ionicons name="chevron-forward" size={16} color={colors.textMuted} />
              </Surface>
              {matched.length === 0 ? (
                <EmptyArt title={alertPrefs.enabled ? t('alerts.none') : t('applications.alertsOff')} />
              ) : (
                matched.slice(0, 30).map((m) => <JobCard key={m.job.id} job={m.job} />)
              )}
            </>
          ) : null}
        </Page>
      </ScrollView>

      {tab === 'applied' ? (
        <View style={[styles.bottom, { paddingBottom: Math.max(insets.bottom, 14) }]}>
          <Page>
            <Pressable
              accessibilityRole="button"
              onPress={() => router.push('/profile/exam-form')}
              style={({ pressed }) => [styles.add, { backgroundColor: colors.primary, boxShadow: shadows.raised, opacity: pressed ? 0.9 : 1 }]}
            >
              <Ionicons name="add" size={20} color="#fff" />
              <Text weight="800" style={{ color: '#fff' }}>
                {t('exams.add')}
              </Text>
            </Pressable>
          </Page>
        </View>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  row: { flexDirection: 'row', alignItems: 'center', gap: 12 },
  bottom: { position: 'absolute', left: 0, right: 0, bottom: 0, paddingTop: 10 },
  add: { flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 6, borderRadius: 12, paddingVertical: 14 },
});
