import { useRouter } from 'expo-router';
import { useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import { ScrollView, StyleSheet, View } from 'react-native';

import { categoryArt } from '@/assets/images';
import { ArtIcon, Page, ScreenHeader, Surface, Text } from '@/components/ui';
import { useJobFilters } from '@/features/jobs/filtersStore';
import { useOpenJobs } from '@/features/jobs/hooks';
import { matchesSector, SECTOR_ART } from '@/features/jobs/logic';
import { useTheme } from '@/theme';

const CATEGORIES: { key: string; tint: string; tintDark: string }[] = [
  { key: 'central', tint: '#FFFBEB', tintDark: '#3A2A0A' },
  { key: 'state', tint: '#F0FDF4', tintDark: '#0B2E1A' },
  { key: 'railways', tint: '#FEF2F2', tintDark: '#3A0E10' },
  { key: 'banking', tint: '#EFF6FF', tintDark: '#0F2547' },
  { key: 'defence', tint: '#F7FEE7', tintDark: '#1C2E07' },
  { key: 'police', tint: '#EEF2FF', tintDark: '#1A1F4A' },
  { key: 'teaching', tint: '#ECFDF5', tintDark: '#062E22' },
  { key: 'medical', tint: '#FDF2F8', tintDark: '#3A0F28' },
  { key: 'psu', tint: '#F0FDFA', tintDark: '#062E2B' },
  { key: 'other', tint: '#F5F3FF', tintDark: '#24124A' },
];

export default function CategoriesScreen() {
  const { t } = useTranslation();
  const router = useRouter();
  const { colors, scheme } = useTheme();
  const jobs = useOpenJobs();
  const reset = useJobFilters((s) => s.reset);
  const counts = useMemo(() => Object.fromEntries(CATEGORIES.map((c) => [c.key, jobs.filter((j) => matchesSector(j, c.key)).length])), [jobs]);

  return (
    <View style={{ flex: 1, backgroundColor: colors.background }}>
      <ScreenHeader title={t('categories.title')} />
      <ScrollView contentContainerStyle={{ paddingVertical: 16 }}>
        <Page>
          <View style={styles.grid}>
            {CATEGORIES.map((c) => (
              <Surface
                key={c.key}
                accessibilityLabel={t(`sectors.${c.key}`)}
                onPress={() => {
                  reset({ sector: c.key });
                  router.navigate('/jobs');
                }}
                style={[styles.card, { backgroundColor: scheme === 'dark' ? c.tintDark : c.tint }]}
              >
                <ArtIcon source={categoryArt[SECTOR_ART[c.key] ?? 'other']} size={46} />
                <Text weight="800" align="center" numberOfLines={1}>
                  {t(`sectors.${c.key}`)}
                </Text>
                <Text variant="caption" tone="muted" align="center" numberOfLines={1}>
                  {t(`categories.${c.key}`)}
                </Text>
                <Text variant="tiny" tone="primary" weight="800">
                  {t('categories.count', { count: counts[c.key] ?? 0 })}
                </Text>
              </Surface>
            ))}
          </View>
        </Page>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  grid: { flexDirection: 'row', flexWrap: 'wrap', gap: 12 },
  card: { flexBasis: '46%', flexGrow: 1, alignItems: 'center', gap: 6, paddingVertical: 18 },
});
