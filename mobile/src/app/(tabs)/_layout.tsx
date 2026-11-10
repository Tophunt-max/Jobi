import { Ionicons } from '@expo/vector-icons';
import { Tabs } from 'expo-router/js-tabs';
import type { ComponentProps } from 'react';
import { useTranslation } from 'react-i18next';
import { Pressable, StyleSheet, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { Text } from '@/components/ui';
import { BannerSlot } from '@/features/ads/BannerSlot';
import { useTheme } from '@/theme';

type IconName = ComponentProps<typeof Ionicons>['name'];

const ICONS: Record<string, [IconName, IconName]> = {
  index: ['home-outline', 'home'],
  jobs: ['briefcase-outline', 'briefcase'],
  saved: ['bookmark-outline', 'bookmark'],
  profile: ['person-outline', 'person'],
};

type BarProps = Parameters<NonNullable<ComponentProps<typeof Tabs>['tabBar']>>[0];

/** Bottom bar with a raised AI button in the middle. */
function TabBar({ state, descriptors, emitter, navigateToTab }: BarProps) {
  const { colors, shadows } = useTheme();
  const insets = useSafeAreaInsets();
  return (
    <View>
      <BannerSlot />
      <View
        style={[
          styles.bar,
          { backgroundColor: colors.surface, borderTopColor: colors.border, paddingBottom: Math.max(insets.bottom, 8), boxShadow: shadows.bar },
        ]}
      >
        {state.routes.map((route, index) => {
          const focused = state.index === index;
          const { options } = descriptors[route.key]!;
          const label = typeof options.tabBarLabel === 'string' ? options.tabBarLabel : (options.title ?? route.name);
          const onPress = () => {
            const event = emitter.emit({ type: 'tabPress', target: route.key, canPreventDefault: true });
            if (!focused && !event.defaultPrevented) navigateToTab(route.key);
          };
          if (route.name === 'mentor') {
            return (
              <Pressable
                key={route.key}
                accessibilityRole="tab"
                accessibilityLabel={label}
                accessibilityState={{ selected: focused }}
                onPress={onPress}
                style={styles.centerSlot}
              >
                <View style={[styles.centerButton, { backgroundColor: colors.primary, boxShadow: shadows.raised, borderColor: colors.surface }]}>
                  <Ionicons name="sparkles" size={24} color="#fff" />
                </View>
              </Pressable>
            );
          }
          const [outline, filled] = ICONS[route.name] ?? ['ellipse-outline', 'ellipse'];
          const tint = focused ? colors.primary : colors.textMuted;
          return (
            <Pressable
              key={route.key}
              accessibilityRole="tab"
              accessibilityLabel={label}
              accessibilityState={{ selected: focused }}
              onPress={onPress}
              style={styles.item}
            >
              <Ionicons name={focused ? filled : outline} size={22} color={tint} />
              <Text variant="tiny" weight={focused ? '800' : '600'} style={{ color: tint, fontSize: 11 }} numberOfLines={1}>
                {label}
              </Text>
            </Pressable>
          );
        })}
      </View>
    </View>
  );
}

export default function TabsLayout() {
  const { t } = useTranslation();
  const { colors } = useTheme();

  return (
    <Tabs tabBar={(props) => <TabBar {...props} />} screenOptions={{ headerShown: false, sceneStyle: { backgroundColor: colors.background } }}>
      <Tabs.Screen name="index" options={{ title: t('tabs.home') }} />
      <Tabs.Screen name="jobs" options={{ title: t('tabs.jobs') }} />
      <Tabs.Screen name="mentor" options={{ title: t('tabs.mentor') }} />
      <Tabs.Screen name="saved" options={{ title: t('tabs.saved') }} />
      <Tabs.Screen name="profile" options={{ title: t('tabs.profile') }} />
    </Tabs>
  );
}

const styles = StyleSheet.create({
  bar: { flexDirection: 'row', alignItems: 'flex-end', borderTopWidth: StyleSheet.hairlineWidth, paddingTop: 8 },
  item: { flex: 1, alignItems: 'center', gap: 3, paddingVertical: 2 },
  centerSlot: { flex: 1, alignItems: 'center' },
  centerButton: { width: 56, height: 56, borderRadius: 28, alignItems: 'center', justifyContent: 'center', marginTop: -30, borderWidth: 4 },
});
