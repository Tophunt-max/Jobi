import { Ionicons } from '@expo/vector-icons';
import { memo } from 'react';
import { useTranslation } from 'react-i18next';
import { Pressable, StyleSheet, View } from 'react-native';

import { emblemArt } from '@/assets/images';
import { ArtIcon, Badge, Pill, Surface, Text, type BadgeTone } from '@/components/ui';
import { daysUntil, formatDate } from '@/lib/dates';
import { formatCount } from '@/lib/format';
import { useTheme } from '@/theme';
import type { Job, JobStatus } from '@/types/domain';

import { useIsBookmarked, useOpenJob, useToggleBookmark } from '../hooks';
import { artForJob, jobStatus, shortQualification } from '../logic';

const STATUS_TONE: Record<JobStatus, BadgeTone> = {
  ACTIVE: 'success',
  CLOSING_SOON: 'danger',
  UPCOMING: 'info',
  EXPIRED: 'neutral',
};

export function StatusBadge({ job }: { job: Job }) {
  const { t } = useTranslation();
  const status = jobStatus(job);
  return <Badge label={t(`status.${status}`)} tone={STATUS_TONE[status]} />;
}

export function DeadlineText({ job }: { job: Job }) {
  const { t } = useTranslation();
  if (!job.lastDate) return null;
  const left = daysUntil(job.lastDate);
  const urgent = left !== null && left >= 0 && left <= 7;
  return (
    <Text variant="caption" tone={urgent ? 'danger' : 'muted'} weight={urgent ? '700' : '500'}>
      {t('job.lastDate')}: {formatDate(job.lastDate)}
      {left !== null && left >= 0 ? ` · ${left === 0 ? t('job.lastDayToday') : t('job.daysLeft', { count: left })}` : ''}
    </Text>
  );
}

/** Organisation emblem for a notice. */
export function JobEmblem({ job, size = 44 }: { job: Job; size?: number }) {
  return <ArtIcon source={emblemArt[artForJob(job)]} size={size} />;
}

function JobCardImpl({ job }: { job: Job; compact?: boolean }) {
  const { t } = useTranslation();
  const { colors } = useTheme();
  const openJob = useOpenJob();
  const bookmarked = useIsBookmarked(job.id);
  const toggleBookmark = useToggleBookmark();
  const qual = shortQualification(job.qualification);
  const left = job.lastDate ? daysUntil(job.lastDate) : null;

  return (
    <Surface onPress={() => openJob(job)} accessibilityLabel={`${job.title}, ${job.organization}`} style={styles.card}>
      <JobEmblem job={job} />
      <View style={styles.body}>
        <Text weight="800" numberOfLines={2} style={styles.title}>
          {job.title}
        </Text>
        <Text variant="caption" tone="muted" numberOfLines={1}>
          {job.organization}
        </Text>
        <View style={styles.pills}>
          {job.totalPosts > 0 ? <Pill tone="green" label={t('job.vacancies', { count: job.totalPosts, formatted: formatCount(job.totalPosts) })} /> : null}
          {qual ? <Pill tone="gray" label={qual} /> : null}
          {job.verified ? <Pill tone="blue" icon="shield-checkmark" label={t('job.verified')} /> : null}
        </View>
        {job.lastDate ? (
          <Text variant="caption" weight="700" tone="danger">
            {t('job.lastDate')}: {formatDate(job.lastDate)}
            {left !== null && left >= 0 && left <= 7 ? ` · ${left === 0 ? t('job.lastDayToday') : t('job.daysLeft', { count: left })}` : ''}
          </Text>
        ) : null}
      </View>
      <Pressable
        accessibilityRole="button"
        accessibilityLabel={bookmarked ? t('job.bookmarked') : t('job.bookmark')}
        onPress={() => toggleBookmark(job)}
        hitSlop={10}
        style={styles.bookmark}
      >
        <Ionicons name={bookmarked ? 'bookmark' : 'bookmark-outline'} size={20} color={bookmarked ? colors.primary : colors.textMuted} />
      </Pressable>
    </Surface>
  );
}

export const JobCard = memo(JobCardImpl);

const styles = StyleSheet.create({
  card: { flexDirection: 'row', gap: 12, alignItems: 'flex-start' },
  body: { flex: 1, gap: 4 },
  title: { fontSize: 14, lineHeight: 19 },
  pills: { flexDirection: 'row', flexWrap: 'wrap', gap: 6, marginTop: 2 },
  bookmark: { paddingLeft: 4 },
});
