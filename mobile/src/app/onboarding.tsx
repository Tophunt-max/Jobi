import { Ionicons } from '@expo/vector-icons';
import { Redirect, useFocusEffect, useRouter } from 'expo-router';
import { useCallback, useEffect, useRef, useState, type ComponentRef } from 'react';
import { useTranslation } from 'react-i18next';
import {
  BackHandler,
  Image,
  Pressable,
  ScrollView,
  StyleSheet,
  useWindowDimensions,
  View,
  type NativeScrollEvent,
  type NativeSyntheticEvent,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { Text } from '@/components/ui';
import { ONBOARDING_SLIDES, onboardingArtSize, slideIndexForOffset } from '@/features/launch/onboarding';
import { useSettingsStore } from '@/store/settingsStore';
import { MAX_CONTENT_WIDTH, useTheme } from '@/theme';

const SLIDES = ONBOARDING_SLIDES;
const LAST = SLIDES.length - 1;

export default function OnboardingRoute() {
  const done = useSettingsStore((s) => s.onboardingDone);
  // Reached again after finishing (web URL, history, old link): go to the app instead.
  if (done) return <Redirect href="/" />;
  return <OnboardingScreen />;
}

function OnboardingScreen() {
  const { t } = useTranslation();
  const router = useRouter();
  const { colors, shadows } = useTheme();
  const complete = useSettingsStore((s) => s.completeOnboarding);
  const { width: screenWidth, height: screenHeight } = useWindowDimensions();
  const width = Math.min(screenWidth, MAX_CONTENT_WIDTH);
  const art = onboardingArtSize(screenHeight);
  const [index, setIndex] = useState(0);
  const scroller = useRef<ComponentRef<typeof ScrollView>>(null);
  const finished = useRef(false);

  const goTo = useCallback(
    (i: number, animated = true) => {
      const target = Math.min(LAST, Math.max(0, i));
      scroller.current?.scrollTo({ x: target * width, animated });
      setIndex(target);
    },
    [width],
  );

  // Keep the current slide in view when the width changes (rotation, split screen, web resize).
  const indexRef = useRef(index);
  useEffect(() => {
    indexRef.current = index;
  }, [index]);
  useEffect(() => {
    scroller.current?.scrollTo({ x: indexRef.current * width, animated: false });
  }, [width]);

  const finish = useCallback(() => {
    // Guards double taps on Skip / Get Started.
    if (finished.current) return;
    finished.current = true;
    complete();
    router.replace('/');
  }, [complete, router]);

  // Android back steps through the slides before leaving the app.
  useFocusEffect(
    useCallback(() => {
      const sub = BackHandler.addEventListener('hardwareBackPress', () => {
        if (indexRef.current === 0) return false;
        goTo(indexRef.current - 1);
        return true;
      });
      return () => sub.remove();
    }, [goTo]),
  );

  // onScroll (not onMomentumScrollEnd) so the dots follow drags on web and programmatic scrolls.
  const onScroll = (e: NativeSyntheticEvent<NativeScrollEvent>) => {
    // Same value is a no-op render.
    setIndex(slideIndexForOffset(e.nativeEvent.contentOffset.x, width));
  };

  const isLast = index === LAST;
  const onPrimary = () => (isLast ? finish() : goTo(index + 1));

  return (
    <SafeAreaView style={[styles.root, { backgroundColor: colors.surface }]}>
      <View style={[styles.top, { width }]}>
        {/* Kept in the layout on the last slide so nothing jumps; Get Started does the same job. */}
        <Pressable
          accessibilityRole="button"
          accessibilityLabel={t('onboarding.skip')}
          aria-hidden={isLast}
          disabled={isLast}
          onPress={finish}
          hitSlop={12}
          style={{ opacity: isLast ? 0 : 1 }}
        >
          <Text weight="700" tone="primary">
            {t('onboarding.skip')}
          </Text>
        </Pressable>
      </View>

      <ScrollView
        ref={scroller}
        horizontal
        pagingEnabled
        bounces={false}
        showsHorizontalScrollIndicator={false}
        scrollEventThrottle={16}
        onScroll={onScroll}
        // Pages are laid out left to right in every language; offsets stay simple in RTL.
        style={[styles.pager, { width }]}
        contentContainerStyle={styles.pagerContent}
      >
        {SLIDES.map((s, i) => {
          const current = i === index;
          return (
            <View
              key={s.key}
              style={[styles.slide, { width }]}
              // Off-screen slides are skipped by screen readers.
              aria-hidden={!current}
            >
              <Image source={s.image} style={{ width: art, height: art }} resizeMode="contain" accessibilityIgnoresInvertColors />
              <Text style={styles.title} align="center" accessibilityRole="header">
                {t(s.title)}
              </Text>
              <Text tone="muted" align="center" style={styles.body}>
                {t(s.body)}
              </Text>
            </View>
          );
        })}
      </ScrollView>

      <View style={styles.dots} role="tablist">
        {SLIDES.map((s, i) => {
          const current = i === index;
          return (
            <Pressable
              key={s.key}
              role="tab"
              aria-selected={current}
              aria-label={t('onboarding.step', { current: i + 1, total: SLIDES.length })}
              onPress={() => goTo(i)}
              hitSlop={8}
              style={styles.dotHit}
            >
              <View style={[styles.dot, { backgroundColor: current ? colors.primary : colors.border, width: current ? 22 : 8 }]} />
            </Pressable>
          );
        })}
      </View>

      <View style={[styles.footer, { width }]}>
        <Pressable
          accessibilityRole="button"
          accessibilityHint={isLast ? undefined : t('onboarding.step', { current: index + 2, total: SLIDES.length })}
          onPress={onPrimary}
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
  pager: { flexGrow: 0, direction: 'ltr' },
  pagerContent: { direction: 'ltr' },
  slide: { alignItems: 'center', paddingHorizontal: 28, paddingTop: 12, gap: 12 },
  title: { fontSize: 26, lineHeight: 32, fontWeight: '900', marginTop: 8 },
  body: { fontSize: 15, lineHeight: 22, maxWidth: 320 },
  dots: { flexDirection: 'row', gap: 2, marginTop: 20 },
  dotHit: { paddingVertical: 6, paddingHorizontal: 2 },
  dot: { height: 8, borderRadius: 4 },
  footer: { marginTop: 'auto', padding: 20 },
  cta: { flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 8, borderRadius: 14, paddingVertical: 16 },
  ctaText: { color: '#fff', fontSize: 16 },
});
