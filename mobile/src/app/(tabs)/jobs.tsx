import { Ionicons } from '@expo/vector-icons';
import { FlashList } from '@shopify/flash-list';
import { useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Modal, Pressable, StyleSheet, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { SyncStatus } from '@/components/SyncStatus';
import { Button, EmptyArt, FilterChips, Page, ScreenHeader, SearchInput, SelectField, Segmented, Text } from '@/components/ui';
import { ALL, INDIAN_STATES, QUALIFICATIONS } from '@/constants/catalog';
import { hasSponsor, useAdConfigStore } from '@/features/ads/config';
import { SponsorCard } from '@/features/ads/SponsorCard';
import { JobCard } from '@/features/jobs/components/JobCard';
import { useJobFilters } from '@/features/jobs/filtersStore';
import { useOpenJobs } from '@/features/jobs/hooks';
import { DEFAULT_FILTERS, filterJobs, type SortOrder, type StatusFilter } from '@/features/jobs/logic';
import { formatCount } from '@/lib/format';
import { useContentStore } from '@/store/contentStore';
import { useTheme } from '@/theme';
import type { Job } from '@/types/domain';

type ListRow = { kind: 'job'; job: Job } | { kind: 'sponsor' };

const SPONSOR_POSITION = 5;
const JOB_SECTOR_CHIPS = [ALL, 'central', 'state', 'railways', 'banking', 'defence', 'police', 'teaching', 'ssc', 'upsc', 'medical', 'psu', 'engineering', 'other'];

export default function JobsScreen() {
  const { t } = useTranslation();
  const { colors } = useTheme();
  const jobs = useOpenJobs();
  const filters = useJobFilters((s) => s.filters);
  const setFilters = useJobFilters((s) => s.set);
  const status = useContentStore((s) => s.status);
  const sync = useContentStore((s) => s.sync);
  const showSponsor = useAdConfigStore((s) => hasSponsor(s.config));
  const [sheet, setSheet] = useState(false);

  const filtered = useMemo(() => filterJobs(jobs, filters), [jobs, filters]);
  const rows = useMemo<ListRow[]>(() => {
    const list: ListRow[] = filtered.map((job) => ({ kind: 'job', job }));
    if (showSponsor && list.length >= SPONSOR_POSITION) list.splice(SPONSOR_POSITION, 0, { kind: 'sponsor' });
    return list;
  }, [filtered, showSponsor]);
  const extraFilters = [filters.state !== ALL, filters.qualification !== ALL, filters.status !== ALL].filter(Boolean).length;

  return (
    <View style={{ flex: 1, backgroundColor: colors.background }}>
      <ScreenHeader
        title={t('jobs.latestTitle')}
        right={
          <Pressable accessibilityRole="button" accessibilityLabel={t('jobs.filters')} onPress={() => setSheet(true)} hitSlop={8} style={{ padding: 6 }}>
            <Ionicons name="options-outline" size={22} color={extraFilters ? colors.primary : colors.text} />
            {extraFilters ? <View style={[styles.dot, { backgroundColor: colors.primary }]} /> : null}
          </Pressable>
        }
      />
      <View style={{ backgroundColor: colors.surface, paddingBottom: 10, gap: 10 }}>
        <Page>
          <SearchInput value={filters.query} onChangeText={(query) => setFilters({ query })} placeholder={t('home.searchPlaceholder')} mic />
        </Page>
        <FilterChips
          options={JOB_SECTOR_CHIPS.map((k) => ({ value: k, label: k === ALL ? t('jobs.statusAll') : t(`sectors.${k}`) }))}
          value={filters.sector}
          onChange={(sector) => setFilters({ sector })}
        />
      </View>
      <FlashList
        data={rows}
        keyExtractor={(r) => (r.kind === 'job' ? r.job.id : 'sponsor')}
        getItemType={(r) => r.kind}
        renderItem={({ item }) => (
          <Page style={{ paddingBottom: 10 }}>{item.kind === 'job' ? <JobCard job={item.job} /> : <SponsorCard />}</Page>
        )}
        ListHeaderComponent={
          <Page style={{ paddingTop: 12, paddingBottom: 10, gap: 10 }}>
            <SyncStatus />
            <View style={styles.countRow}>
              <Text weight="800">{t('jobs.found', { count: filtered.length, formatted: formatCount(filtered.length) })}</Text>
              <Pressable
                accessibilityRole="button"
                onPress={() => setFilters({ sort: filters.sort === 'latest' ? 'deadline' : 'latest' })}
                style={styles.sort}
                hitSlop={6}
              >
                <Ionicons name="swap-vertical" size={14} color={colors.primary} />
                <Text variant="caption" weight="700" tone="primary">
                  {filters.sort === 'latest' ? t('jobs.sortLatest') : t('jobs.sortDeadline')}
                </Text>
              </Pressable>
            </View>
          </Page>
        }
        ListEmptyComponent={
          <EmptyArt title={t('jobs.empty')} body={t('jobs.emptyHint')}>
            <Button variant="secondary" label={t('jobs.clearFilters')} onPress={() => setFilters({ ...DEFAULT_FILTERS, sort: filters.sort })} />
          </EmptyArt>
        }
        refreshing={status === 'syncing'}
        onRefresh={() => void sync({ force: true })}
        keyboardShouldPersistTaps="handled"
        contentContainerStyle={{ paddingBottom: 24 }}
      />
      <FilterSheet visible={sheet} onClose={() => setSheet(false)} />
    </View>
  );
}

function FilterSheet({ visible, onClose }: { visible: boolean; onClose: () => void }) {
  const { t } = useTranslation();
  const { colors } = useTheme();
  const filters = useJobFilters((s) => s.filters);
  const setFilters = useJobFilters((s) => s.set);
  const statusOptions: { value: StatusFilter; label: string }[] = [
    { value: ALL, label: t('jobs.statusAll') },
    { value: 'ACTIVE', label: t('jobs.statusActive') },
    { value: 'CLOSING_SOON', label: t('jobs.statusClosing') },
    { value: 'UPCOMING', label: t('jobs.statusUpcoming') },
  ];
  return (
    <Modal visible={visible} transparent animationType="slide" onRequestClose={onClose}>
      <Pressable style={{ flex: 1, backgroundColor: colors.overlay }} onPress={onClose} accessibilityLabel={t('common.close')} />
      <SafeAreaView edges={['bottom']} style={[styles.sheet, { backgroundColor: colors.surface }]}>
        <View style={styles.sheetHead}>
          <Text variant="heading" weight="800">
            {t('jobs.filters')}
          </Text>
          <Pressable accessibilityRole="button" onPress={onClose} hitSlop={8}>
            <Ionicons name="close" size={22} color={colors.text} />
          </Pressable>
        </View>
        <View style={{ gap: 14, paddingHorizontal: 16 }}>
          <Segmented<StatusFilter> value={filters.status} onChange={(status) => setFilters({ status })} options={statusOptions} />
          <SelectField
            label={t('jobs.state')}
            value={filters.state}
            searchable
            options={[{ value: ALL, label: t('jobs.anyState') }, ...INDIAN_STATES.map((s) => ({ value: s, label: s }))]}
            onChange={(state) => setFilters({ state })}
          />
          <SelectField
            label={t('jobs.qualification')}
            value={filters.qualification}
            options={[{ value: ALL, label: t('jobs.anyQualification') }, ...QUALIFICATIONS.map((q) => ({ value: q, label: q }))]}
            onChange={(qualification) => setFilters({ qualification })}
          />
          <Segmented<SortOrder>
            value={filters.sort}
            onChange={(sort) => setFilters({ sort })}
            options={[
              { value: 'latest', label: t('jobs.sortLatest') },
              { value: 'deadline', label: t('jobs.sortDeadline') },
            ]}
          />
          <View style={{ flexDirection: 'row', gap: 10 }}>
            <Button style={{ flex: 1 }} variant="ghost" label={t('jobs.clearFilters')} onPress={() => setFilters({ ...DEFAULT_FILTERS, query: filters.query })} />
            <Button style={{ flex: 1 }} label={t('common.done')} onPress={onClose} />
          </View>
        </View>
      </SafeAreaView>
    </Modal>
  );
}

const styles = StyleSheet.create({
  countRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  sort: { flexDirection: 'row', alignItems: 'center', gap: 4 },
  dot: { position: 'absolute', top: 4, right: 4, width: 7, height: 7, borderRadius: 4 },
  sheet: { position: 'absolute', left: 0, right: 0, bottom: 0, borderTopLeftRadius: 20, borderTopRightRadius: 20, paddingBottom: 16 },
  sheetHead: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', padding: 16 },
});
