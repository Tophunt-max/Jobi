import { Ionicons } from '@expo/vector-icons';
import { FlashList } from '@shopify/flash-list';
import { Stack, useRouter } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { Pressable, StyleSheet, View } from 'react-native';

import { Container, EmptyState, IconButton, Text } from '@/components/ui';
import { useInbox, type InboxItem } from '@/features/notifications/inbox';
import { relativeTime } from '@/lib/dates';
import { useUserStore } from '@/store/userStore';
import { useTheme } from '@/theme';

const TYPE_ICON: Record<string, keyof typeof Ionicons.glyphMap> = {
  DEADLINE_ALERT: 'alarm-outline',
  NEW_JOB: 'briefcase-outline',
  RESULT: 'trophy-outline',
  ADMIT_CARD: 'id-card-outline',
};

export default function NotificationsScreen() {
  const { t } = useTranslation();
  const router = useRouter();
  const { colors, spacing } = useTheme();
  const { items } = useInbox();
  const markRead = useUserStore((s) => s.markRead);

  const open = (n: InboxItem) => {
    markRead([n.id]);
    if (n.target) router.push({ pathname: '/job/[id]', params: { id: n.target } });
  };

  return (
    <View style={{ flex: 1, backgroundColor: colors.background }}>
      <Stack.Screen
        options={{
          headerRight: () =>
            items.some((n) => !n.read) ? (
              <IconButton icon="checkmark-done-outline" label={t('notifications.markAllRead')} onPress={() => markRead(items.map((n) => n.id))} />
            ) : null,
        }}
      />
      <FlashList
        data={items}
        keyExtractor={(n) => n.id}
        ListEmptyComponent={<EmptyState icon="notifications-off-outline" title={t('notifications.empty')} />}
        contentContainerStyle={{ paddingVertical: spacing.sm }}
        renderItem={({ item }) => (
          <Container>
            <Pressable
              accessibilityRole="button"
              onPress={() => open(item)}
              style={({ pressed }) => [
                styles.row,
                { borderBottomColor: colors.border, backgroundColor: pressed ? colors.surfaceMuted : 'transparent' },
              ]}
            >
              <View style={[styles.icon, { backgroundColor: item.type === 'DEADLINE_ALERT' ? colors.dangerSoft : colors.primarySoft }]}>
                <Ionicons
                  name={TYPE_ICON[item.type] ?? 'megaphone-outline'}
                  size={18}
                  color={item.type === 'DEADLINE_ALERT' ? colors.danger : colors.primary}
                />
              </View>
              <View style={{ flex: 1, gap: 2 }}>
                <Text variant="subheading" weight={item.read ? '500' : '800'} numberOfLines={2}>
                  {item.title}
                </Text>
                {item.body ? (
                  <Text variant="caption" tone="muted" numberOfLines={2}>
                    {item.body}
                  </Text>
                ) : null}
                <Text variant="tiny" tone="muted">
                  {relativeTime(item.timestamp)}
                </Text>
              </View>
              {!item.read ? <View style={[styles.dot, { backgroundColor: colors.primary }]} /> : null}
            </Pressable>
          </Container>
        )}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  row: { flexDirection: 'row', gap: 12, padding: 16, borderBottomWidth: StyleSheet.hairlineWidth, alignItems: 'flex-start' },
  icon: { width: 36, height: 36, borderRadius: 18, alignItems: 'center', justifyContent: 'center' },
  dot: { width: 8, height: 8, borderRadius: 4, marginTop: 6 },
});
