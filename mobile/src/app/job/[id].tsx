import { Stack, useLocalSearchParams, useRouter } from 'expo-router';
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { ActivityIndicator, Platform, Share, StyleSheet, View } from 'react-native';

import { Badge, Button, Card, Divider, EmptyState, IconButton, InlineBanner, Row, ScrollScreen, Text } from '@/components/ui';
import { casteMeta } from '@/constants/catalog';
import { DeadlineText, StatusBadge } from '@/features/jobs/components/JobCard';
import { useIsBookmarked, useToggleBookmark } from '@/features/jobs/hooks';
import { useMentorStore } from '@/features/mentor/mentorStore';
import { checkAge, meetsEducation } from '@/features/profile/eligibility';
import { notificationsSupported } from '@/features/reminders/notifications';
import { formatDate } from '@/lib/dates';
import { formatCount } from '@/lib/format';
import { useOpenLink } from '@/lib/useOpenLink';
import { NOT_SPECIFIED } from '@/services/api/mappers';
import { useContentStore } from '@/store/contentStore';
import { useUserStore } from '@/store/userStore';
import { useTheme } from '@/theme';
import type { Job } from '@/types/domain';

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

  if (loading) {
    return (
      <View style={[styles.center, { backgroundColor: colors.background }]}>
        <ActivityIndicator color={colors.primary} />
      </View>
    );
  }
  if (!job) {
    return (
      <View style={[styles.center, { backgroundColor: colors.background }]}>
        <EmptyState icon="document-outline" title={t('job.notFound')} />
      </View>
    );
  }
  return <JobDetail job={job} />;
}

function Fact({ label, value }: { label: string; value: string }) {
  const { colors, radius } = useTheme();
  return (
    <View style={[styles.fact, { backgroundColor: colors.surfaceMuted, borderRadius: radius.md }]}>
      <Text variant="tiny" tone="muted">
        {label.toUpperCase()}
      </Text>
      <Text variant="subheading" numberOfLines={2}>
        {value}
      </Text>
    </View>
  );
}

function Field({ label, value }: { label: string; value: string }) {
  if (!value || value === NOT_SPECIFIED) return null;
  return (
    <View style={{ gap: 2 }}>
      <Text variant="caption" tone="muted">
        {label}
      </Text>
      <Text>{value}</Text>
    </View>
  );
}

function JobDetail({ job }: { job: Job }) {
  const { t } = useTranslation();
  const router = useRouter();
  const { colors } = useTheme();
  const openLink = useOpenLink();
  const bookmarked = useIsBookmarked(job.id);
  const toggleBookmark = useToggleBookmark();
  const profile = useUserStore((s) => s.profile);
  const profileCompleted = useUserStore((s) => s.profileCompleted);
  const askEligibility = useMentorStore((s) => s.askEligibility);

  const age = checkAge(job, profile);
  const edu = meetsEducation(job, profile);
  const ageRange = job.maxAge > 0 ? t('job.ageRange', { min: job.minAge || '–', max: job.maxAge }) : t('common.notAvailable');

  const share = () => {
    const message = t('job.shareText', {
      title: job.title,
      date: job.lastDate ? formatDate(job.lastDate) : t('common.notAvailable'),
      url: job.notificationUrl || job.websiteUrl,
    });
    void Share.share(Platform.OS === 'ios' ? { message, url: job.notificationUrl || job.websiteUrl } : { message });
  };

  return (
    <>
      <Stack.Screen
        options={{
          title: job.organization === NOT_SPECIFIED ? '' : job.organization,
          headerRight: () => (
            <Row gap={2}>
              <IconButton icon="share-social-outline" label={t('common.share')} onPress={share} />
              <IconButton
                icon={bookmarked ? 'bookmark' : 'bookmark-outline'}
                color={bookmarked ? colors.primary : colors.text}
                label={bookmarked ? t('job.bookmarked') : t('job.bookmark')}
                onPress={() => toggleBookmark(job)}
              />
            </Row>
          ),
        }}
      />
      <ScrollScreen>
        <View style={{ gap: 8 }}>
          <Row gap={6} style={{ flexWrap: 'wrap' }}>
            <StatusBadge job={job} />
            {job.verified ? <Badge label={t('job.verifiedSource')} tone="primary" icon="shield-checkmark" /> : null}
            <Badge label={job.category} />
          </Row>
          <Text variant="title">{job.title}</Text>
          <Text tone="muted">{job.organization}</Text>
          <DeadlineText job={job} />
        </View>

        <View style={styles.facts}>
          <Fact label={t('job.lastDate')} value={job.lastDate ? formatDate(job.lastDate) : t('common.notAvailable')} />
          <Fact
            label={t('job.totalPosts')}
            value={job.totalPosts > 0 ? t('job.posts', { count: job.totalPosts, formatted: formatCount(job.totalPosts) }) : t('common.notAvailable')}
          />
          <Fact label={t('job.ageLimit')} value={ageRange} />
          <Fact label={t('job.scope')} value={job.state} />
        </View>

        <Card>
          <Text variant="heading">{t('job.yourEligibility')}</Text>
          {!profileCompleted ? (
            <InlineBanner
              tone="info"
              icon="person-outline"
              message={t('job.completeProfile')}
              action={t('common.edit')}
              onAction={() => router.push('/profile/edit')}
            />
          ) : (
            <View style={{ gap: 6 }}>
              <Text tone={age.kind === 'eligible' ? 'success' : age.kind === 'unknown' ? 'muted' : 'danger'}>
                {age.kind === 'eligible'
                  ? `✓ ${t('job.ageOk', { limit: age.upperLimit })}`
                  : age.kind === 'too_old'
                    ? `✗ ${t('job.ageTooOld', { limit: age.upperLimit })}`
                    : age.kind === 'too_young'
                      ? `✗ ${t('job.ageTooYoung', { min: age.minAge })}`
                      : `• ${t('job.ageUnknown')}`}
              </Text>
              {edu !== null ? (
                <Text tone={edu ? 'success' : 'danger'}>{edu ? `✓ ${t('job.eduOk')}` : `✗ ${t('job.eduLow')}`}</Text>
              ) : null}
              <Text variant="caption" tone="muted">
                {casteMeta(profile.category).label} · {profile.age}
              </Text>
            </View>
          )}
          <Button
            variant="secondary"
            icon="sparkles-outline"
            label={t('job.askAi')}
            onPress={() => {
              void askEligibility(job);
              router.navigate('/mentor');
            }}
          />
        </Card>

        <Card>
          <Text variant="heading">{t('job.importantDates')}</Text>
          <Field label={t('job.startDate')} value={job.startDate ? formatDate(job.startDate) : ''} />
          <Field label={t('job.lastDate')} value={job.lastDate ? formatDate(job.lastDate) : ''} />
          <Field label={t('job.examDate')} value={job.examDate} />
          <Field label={t('job.admitCardDate')} value={job.admitCardDate} />
          <Divider />
          <Text variant="caption" tone="muted">
            {notificationsSupported ? t('job.reminders') : t('job.remindersWeb')}
          </Text>
        </Card>

        <Card>
          <Text variant="heading">{t('job.eligibility')}</Text>
          <Field label={t('job.qualification')} value={job.qualification} />
          <Field label={t('job.ageLimit')} value={job.maxAge > 0 ? ageRange : ''} />
          <Field label={t('job.selection')} value={job.selectionProcess} />
          <Field label={t('job.salary')} value={job.salaryScale} />
        </Card>

        <Card>
          <Text variant="heading">{t('job.fees')}</Text>
          <Field label={t('job.feeGeneral')} value={job.feeGeneral} />
          <Field label={t('job.feeReserved')} value={job.feeReserved} />
          <Field label={t('job.advtNo')} value={job.advtNumber} />
          {job.sourceDomain ? (
            <Text variant="caption" tone="muted">
              {t('job.source', { domain: job.sourceDomain })}
            </Text>
          ) : null}
        </Card>

        <View style={{ gap: 10 }}>
          {job.applyUrl ? (
            <Button icon="open-outline" label={t('job.applyOnline')} onPress={() => void openLink(job.applyUrl, job.title)} />
          ) : null}
          {job.notificationUrl ? (
            <Button
              variant="secondary"
              icon="document-text-outline"
              label={t('job.viewNotification')}
              onPress={() => void openLink(job.notificationUrl, job.title)}
            />
          ) : null}
          {job.websiteUrl && job.websiteUrl !== job.applyUrl ? (
            <Button variant="ghost" icon="globe-outline" label={t('job.website')} onPress={() => void openLink(job.websiteUrl, job.organization)} />
          ) : null}
          <Button
            variant="danger"
            icon="flag-outline"
            label={t('job.report')}
            onPress={() => router.push({ pathname: '/report/[id]', params: { id: job.id } })}
          />
        </View>

        <Text variant="caption" tone="muted" align="center">
          {t('common.disclaimer')}
        </Text>
      </ScrollScreen>
    </>
  );
}

const styles = StyleSheet.create({
  center: { flex: 1, alignItems: 'center', justifyContent: 'center' },
  facts: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },
  fact: { flexGrow: 1, flexBasis: '45%', padding: 12, gap: 4 },
});
