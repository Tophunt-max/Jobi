import { Ionicons } from '@expo/vector-icons';
import { FlashList } from '@shopify/flash-list';
import { useRouter } from 'expo-router';
import { useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Pressable, StyleSheet, View, type ImageSourcePropType } from 'react-native';

import { images } from '@/assets/images';
import { ArtIcon, EmptyArt, FilterChips, Page, ScreenHeader, Surface, Text } from '@/components/ui';
import { useInbox, type InboxItem } from '@/features/notifications/inbox';
import { relativeTime } from '@/lib/dates';
import { useUserStore } from '@/store/userStore';
import { useTheme } from '@/theme';

type Filter = 'all' | 'jobs' | 'results' | 'admit';

const TYPE_ICON: Record<string, ImageSourcePropType> = {
  NEW_JOB: images.notifications.newJob,
  DEADLINE_ALERT: images.notifications.deadline,
  RESULT: images.notifications.result,
  ANSWER_KEY: images.notifications.result,
  ADMIT_CARD: images.notifications.admitCard,
};

function group(type: string): Filter {
  if (type === 'NEW_JOB' || type === 'DEADLINE_ALERT') return 'jobs';
  if (type === 'RESULT' || type === 'ANSWER_KEY') return 'results';
  if (type === 'ADMIT_CARD') return 'admit';
  return 'all';
}

export default function NotificationsScreen() {
  const { t } = useTranslation();
  const router = useRouter();
  const { colors } = useTheme();
  const { items } = useInbox();
  const markRead = useUserStore((s) => s.markRead);
  const [filter, setFilter] = useState<Filter>('all');
  const shown = useMemo(() => (filter === 'all' ? items : items.filter((n) => group(n.type) === filter)), [items, filter]);

  const open = (n: InboxItem) => {
    markRead([n.id]);
    if (n.target) router.push({ pathname: '/job/[id]', params: { id: n.target } });
  };

  return (
    <View style={{ flex: 1, backgroundColor: colors.background }}>
      <ScreenHeader
        title={t('notifications.title')}
        right={
          items.some((n) => !n.read) ? (
            <Pressable accessibilityRole="button" accessibilityLabel={t('notifications.markAllRead')} onPress={() => markRead(items.map((n) => n.id))} hitSlop={8} style={{ padding: 6 }}>
              <Ionicons name="checkmark-done-outline" size={22} color={colors.primary} />
            </Pressable>
          ) : null
        }
      />
      <View style={{ backgroundColor: colors.surface, paddingBottom: 10 }}>
        <FilterChips<Filter>
          value={filter}
          onChange={setFilter}
          options={[
            { value: 'all', label: t('jobs.statusAll') },
            { value: 'jobs', label: t('tabs.jobs') },
            { value: 'results', label: t('results.tabResults') },
            { value: 'admit', label: t('notifications.admitCard') },
          ]}
        />
      </View>
      <FlashList
        data={shown}
        keyExtractor={(n) => n.id}
        ListEmptyComponent={<EmptyArt title={t('notifications.empty')} />}
        contentContainerStyle={{ paddingVertical: 12, paddingBottom: 24 }}
        renderItem={({ item }) => (
          <Page style={{ paddingBottom: 10 }}>
            <Surface onPress={() => open(item)} accessibilityLabel={item.title} style={styles.row}>
              <ArtIcon source={TYPE_ICON[item.type] ?? images.notifications.general} size={42} />
              <View style={{ flex: 1, gap: 2 }}>
                <Text weight={item.read ? '700' : '900'} numberOfLines={2}>
                  {item.title}
                </Text>
                {item.body ? (
                  <Text variant="caption" tone="muted" numberOfLines={2}>
                    {item.body}
                  </Text>
                ) : null}
                <Text variant="tiny" tone="muted" style={{ marginTop: 2 }}>
                  {relativeTime(item.timestamp)}
                </Text>
              </View>
              {!item.read ? <View style={[styles.dot, { backgroundColor: colors.primary }]} /> : null}
            </Surface>
          </Page>
        )}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  row: { flexDirection: 'row', gap: 12, alignItems: 'flex-start' },
  dot: { width: 9, height: 9, borderRadius: 5, marginTop: 6 },
});
