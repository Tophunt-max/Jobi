import { Ionicons } from '@expo/vector-icons';
import { useLocalSearchParams, useRouter } from 'expo-router';
import { useEffect, useState, type ReactNode } from 'react';
import { useTranslation } from 'react-i18next';
import { ActivityIndicator, Platform, Pressable, ScrollView, Share, StyleSheet, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { Button, EmptyArt, InlineBanner, Page, Pill, ScreenHeader, Surface, TabPills, Text, type IconName } from '@/components/ui';
import { casteMeta } from '@/constants/catalog';
import { JobEmblem } from '@/features/jobs/components/JobCard';
import { useIsBookmarked, useToggleBookmark } from '@/features/jobs/hooks';
import { jobStatus, matchesSector } from '@/features/jobs/logic';
import { useMentorStore } from '@/features/mentor/mentorStore';
import { checkAge, meetsEducation } from '@/features/profile/eligibility';
import { notificationsSupported } from '@/features/reminders/notifications';
import { daysUntil, formatDate, parseDate } from '@/lib/dates';
import { formatCount } from '@/lib/format';
import { useOpenLink } from '@/lib/useOpenLink';
import { NOT_SPECIFIED } from '@/services/api/mappers';
import { useContentStore } from '@/store/contentStore';
import { useUserStore } from '@/store/userStore';
import { useTheme } from '@/theme';
import type { Job } from '@/types/domain';

type Tab = 'overview' | 'vacancy' | 'dates' | 'more';

function useJob(id: string): { job: Job | null; loading: boolean } {
  const local = useContentStore((s) => s.jobs.find((j) => j.id === id || j.slug === id));
  const saved = useUserStore((s) => s.bookmarks[id]);
  const resolveJob = useContentStore((s) => s.resolveJob);
  // Server lookup for jobs that are not cached (e.g. opened from a notification).
  const [remote, setRemote] = useState<{ id: string; job: Job | null } | null>(null);
  const needsFetch = !local && !saved;

  useEffect(() => {
    if (!needsFetch) return;
    let cancelled = false;
    void resolveJob(id).then((job) => {
      if (!cancelled) setRemote({ id, job });
    });
    return () => {
      cancelled = true;
    };
  }, [id, needsFetch, resolveJob]);

  const fetched = remote?.id === id ? remote.job : null;
  return { job: local ?? saved ?? fetched, loading: needsFetch && remote?.id !== id };
}

export default function JobDetailScreen() {
  const { id = '' } = useLocalSearchParams<{ id: string }>();
  const { t } = useTranslation();
  const { colors } = useTheme();
  const { job, loading } = useJob(id);

  if (job) return <JobDetail job={job} />;
  return (
    <View style={{ flex: 1, backgroundColor: colors.background }}>
      <ScreenHeader title={t('job.detailsTitle')} />
      <View style={styles.center}>{loading ? <ActivityIndicator color={colors.primary} /> : <EmptyArt title={t('job.notFound')} />}</View>
    </View>
  );
}

const shown = (v: string) => (v && v !== NOT_SPECIFIED ? v : '');
/** Exam and admit-card dates are free text; format them only when they are real dates. */
const readableDate = (v: string) => (/^\d{4}-\d{2}-\d{2}/.test(v) && parseDate(v) ? formatDate(v) : v);

function JobDetail({ job }: { job: Job }) {
  const { t } = useTranslation();
  const router = useRouter();
  const insets = useSafeAreaInsets();
  const { colors, shadows } = useTheme();
  const openLink = useOpenLink();
  const bookmarked = useIsBookmarked(job.id);
  const toggleBookmark = useToggleBookmark();
  const profile = useUserStore((s) => s.profile);
  const profileCompleted = useUserStore((s) => s.profileCompleted);
  const askEligibility = useMentorStore((s) => s.askEligibility);
  const [tab, setTab] = useState<Tab>('overview');

  const status = jobStatus(job);
  const left = job.lastDate ? daysUntil(job.lastDate) : null;
  const scopeLabel = matchesSector(job, 'state') ? t('sectors.state') : t('sectors.central');
  const ageRange = job.maxAge > 0 ? t('job.ageRangeShort', { min: job.minAge || '–', max: job.maxAge }) : '';
  const fee = [shown(job.feeGeneral) && `${shown(job.feeGeneral)} (${t('job.genObc')})`, shown(job.feeReserved) && `${shown(job.feeReserved)} (${t('job.scStPwd')})`]
    .filter(Boolean)
    .join('\n');

  const share = () => {
    const message = t('job.shareText', {
      title: job.title,
      date: job.lastDate ? formatDate(job.lastDate) : t('common.notAvailable'),
      url: job.notificationUrl || job.websiteUrl,
    });
    void Share.share(Platform.OS === 'ios' ? { message, url: job.notificationUrl || job.websiteUrl } : { message });
  };

  const age = checkAge(job, profile);
  const edu = meetsEducation(job, profile);

  return (
    <View style={{ flex: 1, backgroundColor: colors.background }}>
      <ScreenHeader
        title={t('job.detailsTitle')}
        right={
          <>
            <HeaderIcon icon="share-social-outline" label={t('common.share')} onPress={share} />
            <HeaderIcon
              icon={bookmarked ? 'bookmark' : 'bookmark-outline'}
              color={bookmarked ? colors.primary : undefined}
              label={bookmarked ? t('job.bookmarked') : t('job.bookmark')}
              onPress={() => toggleBookmark(job)}
            />
          </>
        }
      />
      <ScrollView contentContainerStyle={{ paddingBottom: 110 }}>
        <View style={{ backgroundColor: colors.surface, paddingBottom: 16 }}>
          <Page style={{ alignItems: 'center', gap: 8, paddingTop: 8 }}>
            <JobEmblem job={job} size={76} />
            <Text variant="heading" weight="900" align="center" style={{ fontSize: 19, lineHeight: 25 }}>
              {job.title}
            </Text>
            {shown(job.organization) ? (
              <Text tone="muted" align="center">
                {job.organization}
              </Text>
            ) : null}
            <View style={styles.pills}>
              {job.verified ? <Pill tone="green" icon="shield-checkmark" label={t('job.verifiedSource')} /> : null}
              <Pill tone="blue" label={scopeLabel} />
              {status === 'CLOSING_SOON' ? <Pill tone="red" label={t('status.CLOSING_SOON')} /> : null}
              {status === 'UPCOMING' ? <Pill tone="purple" label={t('status.UPCOMING')} /> : null}
            </View>
          </Page>
        </View>

        <Page style={{ gap: 14, paddingTop: 14 }}>
          <TabPills<Tab>
            value={tab}
            onChange={setTab}
            options={[
              { value: 'overview', label: t('job.tabOverview') },
              { value: 'vacancy', label: t('job.tabVacancy') },
              { value: 'dates', label: t('job.tabDates') },
              { value: 'more', label: t('job.tabMore') },
            ]}
          />

          {tab === 'overview' ? (
            <>
              <Surface padded={false}>
                <InfoRow icon="people-outline" label={t('job.totalVacancies')} value={job.totalPosts > 0 ? formatCount(job.totalPosts) : ''} />
                <InfoRow icon="school-outline" label={t('job.qualification')} value={shown(job.qualification)} />
                <InfoRow icon="calendar-outline" label={t('job.ageLimit')} value={ageRange} />
                <InfoRow icon="card-outline" label={t('job.fees')} value={fee} />
                <InfoRow icon="cash-outline" label={t('job.salary')} value={shown(job.salaryScale)} />
                <InfoRow icon="git-network-outline" label={t('job.selection')} value={shown(job.selectionProcess)} last />
              </Surface>

              <Surface style={{ gap: 10 }}>
                <Text weight="800">{t('job.yourEligibility')}</Text>
                {!profileCompleted ? (
                  <InlineBanner tone="info" icon="person-outline" message={t('job.completeProfile')} action={t('common.edit')} onAction={() => router.push('/profile/edit')} />
                ) : (
                  <View style={{ gap: 4 }}>
                    <Text tone={age.kind === 'eligible' ? 'success' : age.kind === 'unknown' ? 'muted' : 'danger'}>
                      {age.kind === 'eligible'
                        ? `✓ ${t('job.ageOk', { limit: age.upperLimit })}`
                        : age.kind === 'too_old'
                          ? `✗ ${t('job.ageTooOld', { limit: age.upperLimit })}`
                          : age.kind === 'too_young'
                            ? `✗ ${t('job.ageTooYoung', { min: age.minAge })}`
                            : `• ${t('job.ageUnknown')}`}
                    </Text>
                    {edu !== null ? <Text tone={edu ? 'success' : 'danger'}>{edu ? `✓ ${t('job.eduOk')}` : `✗ ${t('job.eduLow')}`}</Text> : null}
                    <Text variant="caption" tone="muted">
                      {casteMeta(profile.category).label} · {profile.age}
                    </Text>
                  </View>
                )}
                <View style={{ flexDirection: 'row', gap: 8 }}>
                  <Button
                    compact
                    style={{ flex: 1 }}
                    variant="secondary"
                    icon="sparkles-outline"
                    label={t('job.askAi')}
                    onPress={() => {
                      void askEligibility(job);
                      router.navigate('/mentor');
                    }}
                  />
                  <Button
                    compact
                    style={{ flex: 1 }}
                    variant="ghost"
                    icon="checkmark-done-outline"
                    label={t('eligibility.title')}
                    onPress={() => router.push({ pathname: '/eligibility', params: { job: job.id } })}
                  />
                </View>
              </Surface>
            </>
          ) : null}

          {tab === 'vacancy' ? (
            <Surface padded={false}>
              <InfoRow icon="people-outline" label={t('job.totalVacancies')} value={job.totalPosts > 0 ? formatCount(job.totalPosts) : ''} />
              <InfoRow icon="briefcase-outline" label={t('job.postName')} value={shown(job.postName)} />
              <InfoRow icon="business-outline" label={t('job.department')} value={shown(job.department)} />
              <InfoRow icon="location-outline" label={t('job.scope')} value={shown(job.state)} />
              <InfoRow icon="document-text-outline" label={t('job.advtNo')} value={shown(job.advtNumber)} />
              <InfoRow icon="school-outline" label={t('job.qualification')} value={shown(job.qualification)} last />
            </Surface>
          ) : null}

          {tab === 'dates' ? (
            <Surface padded={false}>
              <InfoRow icon="play-circle-outline" label={t('job.startDate')} value={job.startDate ? formatDate(job.startDate) : ''} />
              <InfoRow
                icon="alarm-outline"
                label={t('job.lastDate')}
                value={job.lastDate ? `${formatDate(job.lastDate)}${left !== null && left >= 0 ? `\n${left === 0 ? t('job.lastDayToday') : t('job.daysLeft', { count: left })}` : ''}` : ''}
                danger
              />
              <InfoRow icon="create-outline" label={t('job.examDate')} value={readableDate(shown(job.examDate))} />
              <InfoRow icon="id-card-outline" label={t('job.admitCardDate')} value={readableDate(shown(job.admitCardDate))} last />
              <View style={{ padding: 14, paddingTop: 0 }}>
                <Text variant="caption" tone="muted">
                  {notificationsSupported ? t('job.reminders') : t('job.remindersWeb')}
                </Text>
              </View>
            </Surface>
          ) : null}

          {tab === 'more' ? (
            <Surface padded={false}>
              {job.websiteUrl ? <LinkRow icon="globe-outline" label={t('job.website')} onPress={() => void openLink(job.websiteUrl, job.organization)} /> : null}
              {job.notificationUrl ? (
                <LinkRow icon="document-text-outline" label={t('job.viewNotification')} onPress={() => void openLink(job.notificationUrl, job.title)} />
              ) : null}
              <LinkRow icon="share-social-outline" label={t('common.share')} onPress={share} />
              <LinkRow icon="flag-outline" label={t('job.report')} danger onPress={() => router.push({ pathname: '/report/[id]', params: { id: job.id } })} />
              <View style={{ padding: 14, gap: 4 }}>
                {job.sourceDomain ? (
                  <Text variant="caption" tone="muted">
                    {t('job.source', { domain: job.sourceDomain })}
                  </Text>
                ) : null}
                <Text variant="caption" tone="muted">
                  {t('common.disclaimer')}
                </Text>
              </View>
            </Surface>
          ) : null}
        </Page>
      </ScrollView>

      <View style={[styles.bottomBar, { backgroundColor: colors.surface, paddingBottom: Math.max(insets.bottom, 12), boxShadow: shadows.bar }]}>
        <Page style={{ flexDirection: 'row', gap: 12 }}>
          <Pressable
            accessibilityRole="button"
            disabled={!job.notificationUrl}
            onPress={() => void openLink(job.notificationUrl, job.title)}
            style={({ pressed }) => [styles.cta, styles.ctaOutline, { borderColor: colors.primary, opacity: !job.notificationUrl ? 0.5 : pressed ? 0.85 : 1 }]}
          >
            <Text weight="800" tone="primary" align="center" numberOfLines={2} style={{ fontSize: 13 }}>
              {t('job.downloadNotification')}
            </Text>
          </Pressable>
          <Pressable
            accessibilityRole="button"
            disabled={!job.applyUrl}
            onPress={() => void openLink(job.applyUrl, job.title)}
            style={({ pressed }) => [styles.cta, { backgroundColor: colors.primary, opacity: !job.applyUrl ? 0.5 : pressed ? 0.85 : 1 }]}
          >
            <Text weight="800" style={{ color: '#fff', fontSize: 14 }}>
              {t('job.applyOnline')}
            </Text>
            <Ionicons name="open-outline" size={16} color="#fff" />
          </Pressable>
        </Page>
      </View>
    </View>
  );
}

function HeaderIcon({ icon, label, onPress, color }: { icon: IconName; label: string; onPress: () => void; color?: string }) {
  const { colors } = useTheme();
  return (
    <Pressable accessibilityRole="button" accessibilityLabel={label} onPress={onPress} hitSlop={8} style={{ padding: 6 }}>
      <Ionicons name={icon} size={22} color={color ?? colors.text} />
    </Pressable>
  );
}

function InfoRow({ icon, label, value, last, danger }: { icon: IconName; label: string; value: string; last?: boolean; danger?: boolean }) {
  const { t } = useTranslation();
  const { colors } = useTheme();
  return (
    <View style={[styles.infoRow, { borderBottomColor: colors.border, borderBottomWidth: last ? 0 : StyleSheet.hairlineWidth }]}>
      <Ionicons name={icon} size={20} color={colors.textMuted} />
      <Text tone="muted" style={{ flex: 1 }}>
        {label}
      </Text>
      <Text weight="800" align="right" style={[{ flex: 1.2 }, danger && value ? { color: colors.danger } : null]}>
        {value || t('common.notAvailable')}
      </Text>
    </View>
  );
}

function LinkRow({ icon, label, onPress, danger }: { icon: IconName; label: string; onPress: () => void; danger?: boolean }): ReactNode {
  const { colors } = useTheme();
  return (
    <Pressable
      accessibilityRole="button"
      onPress={onPress}
      style={({ pressed }) => [styles.infoRow, { borderBottomColor: colors.border, borderBottomWidth: StyleSheet.hairlineWidth, backgroundColor: pressed ? colors.surfaceMuted : 'transparent' }]}
    >
      <Ionicons name={icon} size={20} color={danger ? colors.danger : colors.primary} />
      <Text weight="700" style={{ flex: 1, color: danger ? colors.danger : colors.text }}>
        {label}
      </Text>
      <Ionicons name="chevron-forward" size={16} color={colors.textMuted} />
    </Pressable>
  );
}

const styles = StyleSheet.create({
  center: { flex: 1, alignItems: 'center', justifyContent: 'center' },
  pills: { flexDirection: 'row', flexWrap: 'wrap', gap: 6, justifyContent: 'center' },
  infoRow: { flexDirection: 'row', alignItems: 'center', gap: 12, paddingVertical: 13, paddingHorizontal: 14 },
  bottomBar: { position: 'absolute', left: 0, right: 0, bottom: 0, paddingTop: 12 },
  cta: { flex: 1, minHeight: 50, borderRadius: 12, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 6, paddingHorizontal: 10 },
  ctaOutline: { borderWidth: 1.5 },
});
