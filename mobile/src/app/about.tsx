import { Ionicons } from '@expo/vector-icons';
import { useTranslation } from 'react-i18next';
import { Image, ScrollView, StyleSheet, View } from 'react-native';

import { images } from '@/assets/images';
import { Page, ScreenHeader, Surface, Text, type IconName } from '@/components/ui';
import { env } from '@/config/env';
import { useTheme } from '@/theme';

export default function AboutScreen() {
  const { t } = useTranslation();
  const { colors } = useTheme();
  const points: { icon: IconName; text: string }[] = [
    { icon: 'shield-checkmark-outline', text: t('about.point1') },
    { icon: 'phone-portrait-outline', text: t('about.point2') },
    { icon: 'lock-closed-outline', text: t('about.point3') },
    { icon: 'language-outline', text: t('about.point4') },
  ];
  return (
    <View style={{ flex: 1, backgroundColor: colors.background }}>
      <ScreenHeader title={t('about.title')} />
      <ScrollView contentContainerStyle={{ paddingVertical: 20, paddingBottom: 32 }}>
        <Page style={{ gap: 16 }}>
          <View style={styles.brand}>
            <Image source={images.brand.logoMark} style={{ width: 84, height: 84 }} accessibilityIgnoresInvertColors />
            <Text style={{ fontSize: 26, fontWeight: '900' }}>{t('app.title')}</Text>
            <Text tone="muted">{t('splash.tagline')}</Text>
            <Text variant="caption" tone="muted">
              {t('profile.version', { version: env.appVersion })}
            </Text>
          </View>
          <Surface style={{ gap: 12 }}>
            <Text>{t('about.body')}</Text>
            {points.map((p) => (
              <View key={p.text} style={styles.point}>
                <Ionicons name={p.icon} size={20} color={colors.primary} />
                <Text style={{ flex: 1 }}>{p.text}</Text>
              </View>
            ))}
          </Surface>
          <Text variant="caption" tone="muted" align="center">
            {t('common.disclaimer')}
          </Text>
        </Page>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  brand: { alignItems: 'center', gap: 6 },
  point: { flexDirection: 'row', alignItems: 'flex-start', gap: 10 },
});
