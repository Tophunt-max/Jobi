import { Ionicons } from '@expo/vector-icons';
import { FlashList } from '@shopify/flash-list';
import { useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import { StyleSheet, TextInput, View } from 'react-native';

import { SyncStatus } from '@/components/SyncStatus';
import { Button, Chip, ChipRow, Container, EmptyState, Row, Segmented, SelectField, Text } from '@/components/ui';
import { ALL, INDIAN_STATES, QUALIFICATIONS, SECTORS } from '@/constants/catalog';
import { hasSponsor, useAdConfigStore } from '@/features/ads/config';
import { SponsorCard } from '@/features/ads/SponsorCard';
import { JobCard } from '@/features/jobs/components/JobCard';
import { useJobFilters } from '@/features/jobs/filtersStore';
import { useOpenJobs } from '@/features/jobs/hooks';
import { DEFAULT_FILTERS, filterJobs, type SortOrder, type StatusFilter } from '@/features/jobs/logic';
import { useContentStore } from '@/store/contentStore';
import { useTheme } from '@/theme';
import type { Job } from '@/types/domain';

type ListRow = { kind: 'job'; job: Job } | { kind: 'sponsor' };

const SPONSOR_POSITION = 5;

export default function JobsScreen() {
  const { colors, spacing } = useTheme();
  const jobs = useOpenJobs();
  const filters = useJobFilters((s) => s.filters);
  const status = useContentStore((s) => s.status);
  const sync = useContentStore((s) => s.sync);
  const showSponsor = useAdConfigStore((s) => hasSponsor(s.config));

  const filtered = useMemo(() => filterJobs(jobs, filters), [jobs, filters]);
  const rows = useMemo<ListRow[]>(() => {
    const list: ListRow[] = filtered.map((job) => ({ kind: 'job', job }));
    if (showSponsor && list.length >= SPONSOR_POSITION) list.splice(SPONSOR_POSITION, 0, { kind: 'sponsor' });
    return list;
  }, [filtered, showSponsor]);

  return (
    <View style={{ flex: 1, backgroundColor: colors.background }}>
      <FlashList
        data={rows}
        keyExtractor={(r) => (r.kind === 'job' ? r.job.id : 'sponsor')}
        getItemType={(r) => r.kind}
        renderItem={({ item }) => (
          <Container style={{ paddingHorizontal: spacing.lg, paddingBottom: spacing.md }}>
            {item.kind === 'job' ? <JobCard job={item.job} /> : <SponsorCard />}
          </Container>
        )}
        ListHeaderComponent={<JobsHeader count={filtered.length} />}
        ListEmptyComponent={<JobsEmpty />}
        refreshing={status === 'syncing'}
        onRefresh={() => void sync({ force: true })}
        keyboardShouldPersistTaps="handled"
        contentContainerStyle={{ paddingBottom: spacing.xxl }}
      />
    </View>
  );
}

function JobsHeader({ count }: { count: number }) {
  const { t } = useTranslation();
  const { colors, radius, spacing } = useTheme();
  const filters = useJobFilters((s) => s.filters);
  const setFilters = useJobFilters((s) => s.set);

  const statusOptions: { value: StatusFilter; label: string }[] = [
    { value: ALL, label: t('jobs.statusAll') },
    { value: 'ACTIVE', label: t('jobs.statusActive') },
    { value: 'CLOSING_SOON', label: t('jobs.statusClosing') },
    { value: 'UPCOMING', label: t('jobs.statusUpcoming') },
  ];

  const isFiltered =
    filters.query !== '' ||
    filters.state !== ALL ||
    filters.sector !== ALL ||
    filters.qualification !== ALL ||
    filters.status !== ALL;

  return (
    <Container style={{ padding: spacing.lg, gap: spacing.md }}>
      <View style={[styles.search, { backgroundColor: colors.surface, borderColor: colors.border, borderRadius: radius.lg }]}>
        <Ionicons name="search" size={18} color={colors.textMuted} />
        <TextInput
          accessibilityLabel={t('home.searchPlaceholder')}
          value={filters.query}
          onChangeText={(query) => setFilters({ query })}
          placeholder={t('home.searchPlaceholder')}
          placeholderTextColor={colors.textMuted}
          returnKeyType="search"
          clearButtonMode="while-editing"
          style={{ flex: 1, color: colors.text, paddingVertical: 12, fontSize: 14 }}
        />
      </View>

      <SyncStatus />

      <ChipRow>
        {statusOptions.map((o) => (
          <Chip key={o.value} label={o.label} selected={filters.status === o.value} onPress={() => setFilters({ status: o.value })} />
        ))}
      </ChipRow>

      <View style={styles.selects}>
        <View style={styles.select}>
          <SelectField
            label={t('jobs.state')}
            value={filters.state}
            searchable
            options={[{ value: ALL, label: t('jobs.anyState') }, ...INDIAN_STATES.map((s) => ({ value: s, label: s }))]}
            onChange={(state) => setFilters({ state })}
          />
        </View>
        <View style={styles.select}>
          <SelectField
            label={t('jobs.sector')}
            value={filters.sector}
            options={[
              { value: ALL, label: t('jobs.anySector') },
              ...SECTORS.map((s) => ({ value: s.key, label: `${s.icon} ${t(`sectors.${s.key}`)}` })),
            ]}
            onChange={(sector) => setFilters({ sector })}
          />
        </View>
        <View style={styles.select}>
          <SelectField
            label={t('jobs.qualification')}
            value={filters.qualification}
            options={[{ value: ALL, label: t('jobs.anyQualification') }, ...QUALIFICATIONS.map((q) => ({ value: q, label: q }))]}
            onChange={(qualification) => setFilters({ qualification })}
          />
        </View>
      </View>

      <Row style={{ justifyContent: 'space-between' }}>
        <Text variant="caption" tone="muted" weight="700">
          {t('jobs.count', { count })}
        </Text>
        <View style={{ width: 200 }}>
          <Segmented<SortOrder>
            value={filters.sort}
            onChange={(sort) => setFilters({ sort })}
            options={[
              { value: 'latest', label: t('jobs.sortLatest') },
              { value: 'deadline', label: t('jobs.sortDeadline') },
            ]}
          />
        </View>
      </Row>
      {isFiltered ? (
        <Button
          compact
          variant="ghost"
          icon="close-circle-outline"
          label={t('jobs.clearFilters')}
          onPress={() => setFilters({ ...DEFAULT_FILTERS, sort: filters.sort })}
        />
      ) : null}
    </Container>
  );
}

function JobsEmpty() {
  const { t } = useTranslation();
  const reset = useJobFilters((s) => s.reset);
  return (
    <EmptyState icon="search-outline" title={t('jobs.empty')} body={t('jobs.emptyHint')}>
      <Button variant="secondary" label={t('jobs.clearFilters')} onPress={() => reset()} />
    </EmptyState>
  );
}

const styles = StyleSheet.create({
  search: { flexDirection: 'row', alignItems: 'center', gap: 8, paddingHorizontal: 14, borderWidth: 1 },
  selects: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },
  select: { flexGrow: 1, flexBasis: 150 },
});
