import { Ionicons } from '@expo/vector-icons';
import { BottomTabBar, Tabs } from 'expo-router/js-tabs';
import type { ComponentProps } from 'react';
import { useTranslation } from 'react-i18next';
import { View, type ColorValue } from 'react-native';

import { HeaderActions } from '@/components/HeaderActions';
import { BannerSlot } from '@/features/ads/BannerSlot';
import { useTheme } from '@/theme';

type IconName = ComponentProps<typeof Ionicons>['name'];

function icon(name: IconName, focusedName: IconName) {
  return function TabBarIcon({ focused, color, size }: { focused: boolean; color: ColorValue; size: number }) {
    return <Ionicons name={focused ? focusedName : name} color={color as string} size={size} />;
  };
}

export default function TabsLayout() {
  const { t } = useTranslation();
  const { colors } = useTheme();

  return (
    <Tabs
      // The banner sits above the tab bar, outside the scrolling content.
      tabBar={(props) => (
        <View>
          <BannerSlot />
          <BottomTabBar {...props} />
        </View>
      )}
      screenOptions={{
        headerStyle: { backgroundColor: colors.surface },
        headerTintColor: colors.text,
        headerTitleStyle: { fontWeight: '800' },
        headerRight: () => <HeaderActions />,
        tabBarActiveTintColor: colors.primary,
        tabBarInactiveTintColor: colors.textMuted,
        tabBarStyle: { backgroundColor: colors.surface, borderTopColor: colors.border },
        tabBarLabelStyle: { fontSize: 11, fontWeight: '600' },
        sceneStyle: { backgroundColor: colors.background },
      }}
    >
      <Tabs.Screen
        name="index"
        options={{ title: t('app.title'), tabBarLabel: t('tabs.home'), tabBarIcon: icon('home-outline', 'home') }}
      />
      <Tabs.Screen
        name="jobs"
        options={{ title: t('jobs.title'), tabBarLabel: t('tabs.jobs'), tabBarIcon: icon('briefcase-outline', 'briefcase') }}
      />
      <Tabs.Screen
        name="results"
        options={{
          title: t('tabs.results'),
          tabBarLabel: t('tabs.results'),
          tabBarIcon: icon('document-text-outline', 'document-text'),
        }}
      />
      <Tabs.Screen
        name="mentor"
        options={{ title: t('mentor.title'), tabBarLabel: t('tabs.mentor'), tabBarIcon: icon('sparkles-outline', 'sparkles') }}
      />
      <Tabs.Screen
        name="profile"
        options={{
          title: t('profile.title'),
          tabBarLabel: t('tabs.profile'),
          tabBarIcon: icon('person-circle-outline', 'person-circle'),
        }}
      />
    </Tabs>
  );
}
