import { Ionicons } from '@expo/vector-icons';
import { useRouter } from 'expo-router';
import { useRef, useState, type ComponentRef } from 'react';
import { useTranslation } from 'react-i18next';
import { Image, Pressable, ScrollView, StyleSheet, useWindowDimensions, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { images } from '@/assets/images';
import { Text } from '@/components/ui';
import { useSettingsStore } from '@/store/settingsStore';
import { MAX_CONTENT_WIDTH, useTheme } from '@/theme';

const SLIDES = [
  { image: images.onboarding.jobs, title: 'onboarding.title1', body: 'onboarding.body1' },
  { image: images.onboarding.eligibility, title: 'onboarding.title2', body: 'onboarding.body2' },
  { image: images.onboarding.alerts, title: 'onboarding.title3', body: 'onboarding.body3' },
] as const;

export default function OnboardingScreen() {
  const { t } = useTranslation();
  const router = useRouter();
  const { colors, shadows } = useTheme();
  const complete = useSettingsStore((s) => s.completeOnboarding);
  const { width: screenWidth } = useWindowDimensions();
  const width = Math.min(screenWidth, MAX_CONTENT_WIDTH);
  const [index, setIndex] = useState(0);
  const scroller = useRef<ComponentRef<typeof ScrollView>>(null);

  const finish = () => {
    complete();
    router.replace('/');
  };
  const isLast = index === SLIDES.length - 1;
  const next = () => {
    if (isLast) return finish();
    scroller.current?.scrollTo({ x: (index + 1) * width, animated: true });
    setIndex(index + 1);
  };

  return (
    <SafeAreaView style={[styles.root, { backgroundColor: colors.surface }]}>
      <View style={[styles.top, { width }]}>
        <Pressable accessibilityRole="button" onPress={finish} hitSlop={12}>
          <Text weight="700" tone="primary">
            {t('onboarding.skip')}
          </Text>
        </Pressable>
      </View>

      <ScrollView
        ref={scroller}
        horizontal
        pagingEnabled
        showsHorizontalScrollIndicator={false}
        style={{ width, flexGrow: 0 }}
        onMomentumScrollEnd={(e) => setIndex(Math.round(e.nativeEvent.contentOffset.x / width))}
      >
        {SLIDES.map((s) => (
          <View key={s.title} style={[styles.slide, { width }]}>
            <Image source={s.image} style={styles.art} resizeMode="contain" accessibilityIgnoresInvertColors />
            <Text style={styles.title} align="center">
              {t(s.title)}
            </Text>
            <Text tone="muted" align="center" style={styles.body}>
              {t(s.body)}
            </Text>
          </View>
        ))}
      </ScrollView>

      <View style={styles.dots} accessibilityLabel={t('onboarding.step', { current: index + 1, total: SLIDES.length })}>
        {SLIDES.map((s, i) => (
          <View key={s.title} style={[styles.dot, { backgroundColor: i === index ? colors.primary : colors.border, width: i === index ? 22 : 8 }]} />
        ))}
      </View>

      <View style={[styles.footer, { width }]}>
        <Pressable
          accessibilityRole="button"
          onPress={next}
          style={({ pressed }) => [styles.cta, { backgroundColor: colors.primary, boxShadow: shadows.raised, opacity: pressed ? 0.9 : 1 }]}
        >
          <Text weight="800" style={styles.ctaText}>
            {isLast ? t('onboarding.getStarted') : t('onboarding.next')}
          </Text>
          <Ionicons name="arrow-forward" size={18} color="#fff" />
        </Pressable>
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, alignItems: 'center' },
  top: { flexDirection: 'row', justifyContent: 'flex-end', paddingHorizontal: 20, paddingTop: 8, height: 40 },
  slide: { alignItems: 'center', paddingHorizontal: 28, paddingTop: 12, gap: 12 },
  art: { width: 280, height: 280 },
  title: { fontSize: 26, lineHeight: 32, fontWeight: '900', marginTop: 8 },
  body: { fontSize: 15, lineHeight: 22, maxWidth: 320 },
  dots: { flexDirection: 'row', gap: 6, marginTop: 24 },
  dot: { height: 8, borderRadius: 4 },
  footer: { marginTop: 'auto', padding: 20 },
  cta: { flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 8, borderRadius: 14, paddingVertical: 16 },
  ctaText: { color: '#fff', fontSize: 16 },
});
