import { memo } from 'react';
import { useTranslation } from 'react-i18next';
import { StyleSheet, View } from 'react-native';

import { Badge, Card, IconButton, Row, Text, type BadgeTone } from '@/components/ui';
import { daysUntil, formatDate } from '@/lib/dates';
import { formatCount } from '@/lib/format';
import { useTheme } from '@/theme';
import type { Job, JobStatus } from '@/types/domain';

import { useIsBookmarked, useOpenJob, useToggleBookmark } from '../hooks';
import { jobStatus } from '../logic';

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

function JobCardImpl({ job, compact }: { job: Job; compact?: boolean }) {
  const { t } = useTranslation();
  const { colors } = useTheme();
  const openJob = useOpenJob();
  const bookmarked = useIsBookmarked(job.id);
  const toggleBookmark = useToggleBookmark();

  return (
    <Card onPress={() => openJob(job)} accessibilityLabel={`${job.title}, ${job.organization}`}>
      <Row style={{ alignItems: 'flex-start' }}>
        <View style={{ flex: 1, gap: 4 }}>
          <Row gap={6} style={{ flexWrap: 'wrap' }}>
            <StatusBadge job={job} />
            {job.verified ? <Badge label={t('job.verifiedSource')} tone="primary" icon="shield-checkmark" /> : null}
            {job.state ? <Badge label={job.state} /> : null}
          </Row>
          <Text variant="subheading" numberOfLines={compact ? 2 : 3}>
            {job.title}
          </Text>
          <Text variant="caption" tone="muted" numberOfLines={1}>
            {job.organization}
          </Text>
        </View>
        <IconButton
          icon={bookmarked ? 'bookmark' : 'bookmark-outline'}
          color={bookmarked ? colors.primary : colors.textMuted}
          label={bookmarked ? t('job.bookmarked') : t('job.bookmark')}
          onPress={() => toggleBookmark(job)}
        />
      </Row>
      <View style={styles.footer}>
        <DeadlineText job={job} />
        {job.totalPosts > 0 ? (
          <Text variant="caption" tone="primary" weight="700">
            {t('job.posts', { count: job.totalPosts, formatted: formatCount(job.totalPosts) })}
          </Text>
        ) : null}
      </View>
    </Card>
  );
}

export const JobCard = memo(JobCardImpl);

const styles = StyleSheet.create({
  footer: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', gap: 8, flexWrap: 'wrap' },
});
