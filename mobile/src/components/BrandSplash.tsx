import { useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { AccessibilityInfo, Animated, Image, Platform, StyleSheet, useWindowDimensions, View } from 'react-native';

import { images } from '@/assets/images';
import { Text } from '@/components/ui';

/** Same blue as the native splash in app.config.ts, so the hand-off is seamless. */
export const SPLASH_BLUE = '#1D4ED8';
const FADE_MS = 350;
const LIFT_MS = 500;
/** Native splash is hidden after this long even if the background image never reports a load. */
const PAINT_FALLBACK_MS = 800;
// The splash only shows on cold start, so the module load time is when it appeared.
const SHOWN_AT = Date.now();
const NATIVE_DRIVER = Platform.OS !== 'web';

type Props = {
  /** True once the app underneath can be shown. */
  ready: boolean;
  /** Shortest total time on screen, counted from app start. */
  minVisibleMs: number;
  /** Called once the splash has painted, so the native splash can be hidden without a blank frame. */
  onPainted: () => void;
  /** Called after the fade-out; the parent unmounts the splash. */
  onDone: () => void;
};

/**
 * Branded splash shown over the app on cold start. The native splash (same blue and logo)
 * stays up until this view has painted; this view then fades out once `ready` is true.
 */
export function BrandSplash({ ready, minVisibleMs, onPainted, onDone }: Props) {
  const { t } = useTranslation();
  const [opacity] = useState(() => new Animated.Value(1));
  const [lift] = useState(() => new Animated.Value(12));
  const [leaving, setLeaving] = useState(false);
  const reduceMotion = useReduceMotion();
  const { height: screenHeight } = useWindowDimensions();
  // Building and wave take the bottom ~30% at most, so they never reach the text on short screens.
  const art = Math.min(1, Math.max(0.55, (screenHeight * 0.3) / 280));
  const painted = useRef(false);
  const callbacks = useRef({ onPainted, onDone });
  useEffect(() => {
    callbacks.current = { onPainted, onDone };
  }, [onPainted, onDone]);

  const markPainted = () => {
    if (painted.current) return;
    painted.current = true;
    callbacks.current.onPainted();
  };

  // Hand-off fallback: some platforms never fire onLoad for cached or failed images.
  useEffect(() => {
    const timer = setTimeout(markPainted, PAINT_FALLBACK_MS);
    return () => clearTimeout(timer);
  }, []);

  useEffect(() => {
    if (reduceMotion === null) return;
    if (reduceMotion) lift.setValue(0);
    else Animated.timing(lift, { toValue: 0, duration: LIFT_MS, useNativeDriver: NATIVE_DRIVER }).start();
  }, [lift, reduceMotion]);

  useEffect(() => {
    // Does not wait for the reduce-motion setting: leaving must never depend on it.
    if (!ready) return;
    const wait = Math.max(0, minVisibleMs - (Date.now() - SHOWN_AT));
    const timer = setTimeout(() => {
      setLeaving(true);
      const finish = () => callbacks.current.onDone();
      if (reduceMotion === true) return finish();
      Animated.timing(opacity, { toValue: 0, duration: FADE_MS, useNativeDriver: NATIVE_DRIVER }).start(finish);
    }, wait);
    return () => clearTimeout(timer);
  }, [ready, minVisibleMs, opacity, reduceMotion]);

  return (
    <Animated.View
      style={[StyleSheet.absoluteFill, styles.root, { opacity }]}
      // Blocks touches on the app underneath until the fade starts.
      pointerEvents={leaving ? 'none' : 'auto'}
      accessible
      role="img"
      aria-label={`${t('app.title')}, ${t('splash.tagline')}`}
    >
      <Image
        source={images.splash.background}
        style={styles.fillImage}
        resizeMode="cover"
        onLoadEnd={markPainted}
        accessibilityIgnoresInvertColors
      />
      {/* Building and tricolour sit on the bottom edge, behind the text. */}
      <View style={styles.bottom} pointerEvents="none">
        <Image
          source={images.splash.building}
          style={[styles.building, { width: 300 * art, height: 174 * art, marginBottom: -46 * art }]}
          resizeMode="contain"
          accessibilityIgnoresInvertColors
        />
        <Image source={images.splash.wave} style={[styles.wave, { height: 150 * art }]} resizeMode="stretch" accessibilityIgnoresInvertColors />
      </View>
      <Animated.View style={[styles.center, { transform: [{ translateY: lift }] }]}>
        <View style={styles.logoCard}>
          <Image source={images.splash.logo} style={styles.logo} resizeMode="contain" accessibilityIgnoresInvertColors />
        </View>
        <View style={styles.titleRow}>
          <Image source={images.splash.laurelLeft} style={styles.laurel} resizeMode="contain" accessibilityIgnoresInvertColors />
          <View style={styles.titleText}>
            <Text style={styles.title} numberOfLines={1} adjustsFontSizeToFit>
              {t('app.title')}
            </Text>
            <Text style={styles.subtitle} numberOfLines={1} adjustsFontSizeToFit>
              {t('splash.tagline')}
            </Text>
          </View>
          <Image source={images.splash.laurelRight} style={styles.laurel} resizeMode="contain" accessibilityIgnoresInvertColors />
        </View>
        <View style={styles.features}>
          <Text style={styles.feature}>{t('splash.line1')}</Text>
          <Text style={styles.feature}>{t('splash.line2')}</Text>
          <Text style={styles.feature}>{t('splash.line3')}</Text>
        </View>
      </Animated.View>
    </Animated.View>
  );
}

/** null until the OS setting is known, so the first frame does not start an animation it must undo. */
function useReduceMotion(): boolean | null {
  const [reduce, setReduce] = useState<boolean | null>(null);
  useEffect(() => {
    let alive = true;
    AccessibilityInfo.isReduceMotionEnabled()
      .then((v) => alive && setReduce(v))
      .catch(() => alive && setReduce(false));
    const sub = AccessibilityInfo.addEventListener('reduceMotionChanged', setReduce);
    return () => {
      alive = false;
      sub.remove();
    };
  }, []);
  return reduce;
}

const styles = StyleSheet.create({
  root: { zIndex: 10, backgroundColor: SPLASH_BLUE },
  // Explicit size: on web an absolutely positioned image otherwise keeps its natural width.
  fillImage: { position: 'absolute', top: 0, left: 0, width: '100%', height: '100%' },
  center: { flex: 1, alignItems: 'center', paddingTop: '18%', gap: 14 },
  // The wordmark is dark blue, so it sits on a white card to stay readable on the splash blue.
  logoCard: { backgroundColor: '#fff', borderRadius: 30, paddingHorizontal: 18, paddingVertical: 14, boxShadow: '0 8px 24px rgba(11, 42, 111, 0.35)' },
  logo: { width: 150, height: 101 },
  titleRow: { flexDirection: 'row', alignItems: 'center', gap: 6, marginTop: 4 },
  // Sizes match scripts/assets/convert.mjs (46×82 dp and 42×82 dp at 3x); keep both in step.
  laurel: { width: 40, height: 76 },
  titleText: { alignItems: 'center', maxWidth: 220 },
  title: { color: '#fff', fontSize: 30, lineHeight: 36, fontWeight: '900', letterSpacing: 0.5, textAlign: 'center' },
  subtitle: { color: '#fff', fontSize: 13, lineHeight: 18, fontWeight: '600', opacity: 0.92, marginTop: 2 },
  bottom: { position: 'absolute', left: 0, right: 0, bottom: 0, alignItems: 'center' },
  building: { opacity: 0.95 },
  wave: { width: '100%' },
  features: { alignItems: 'center', gap: 6, marginTop: 28 },
  feature: { color: '#fff', fontSize: 15, fontWeight: '600', opacity: 0.88 },
});
