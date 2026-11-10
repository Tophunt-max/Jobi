import { FlashList } from '@shopify/flash-list';
import { useRouter } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { View } from 'react-native';

import { Button, EmptyArt, Page, ScreenHeader, Text } from '@/components/ui';
import { JobCard } from '@/features/jobs/components/JobCard';
import { useBookmarkedJobs } from '@/features/jobs/hooks';
import { useTheme } from '@/theme';

export default function SavedScreen() {
  const { t } = useTranslation();
  const router = useRouter();
  const { colors } = useTheme();
  const saved = useBookmarkedJobs();

  return (
    <View style={{ flex: 1, backgroundColor: colors.background }}>
      <ScreenHeader title={t('saved.title')} back={false} />
      <FlashList
        data={saved}
        keyExtractor={(j) => j.id}
        renderItem={({ item }) => (
          <Page style={{ paddingBottom: 10 }}>
            <JobCard job={item} />
          </Page>
        )}
        ListHeaderComponent={
          saved.length > 0 ? (
            <Page style={{ paddingTop: 14, paddingBottom: 10 }}>
              <Text weight="800">{t('saved.count', { count: saved.length })}</Text>
              <Text variant="caption" tone="muted">
                {t('saved.hint')}
              </Text>
            </Page>
          ) : null
        }
        ListEmptyComponent={
          <EmptyArt title={t('saved.empty')} body={t('profile.bookmarksEmpty')}>
            <Button label={t('home.quickLatest')} icon="briefcase-outline" onPress={() => router.navigate('/jobs')} />
          </EmptyArt>
        }
        contentContainerStyle={{ paddingBottom: 24 }}
      />
    </View>
  );
}
