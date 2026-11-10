import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Animated, Image, StyleSheet, View } from 'react-native';

import { images } from '@/assets/images';
import { Text } from '@/components/ui';

const MIN_VISIBLE_MS = 1300;
// The splash only shows on cold start, so the module load time is when it appeared.
const SHOWN_AT = Date.now();
const FADE_MS = 350;

/**
 * Branded splash shown over the app on cold start. The native splash (same blue and logo)
 * stays up until the stores hydrate, then this view fades out.
 */
export function BrandSplash({ ready, onDone }: { ready: boolean; onDone: () => void }) {
  const { t } = useTranslation();
  const [opacity] = useState(() => new Animated.Value(1));
  const [lift] = useState(() => new Animated.Value(12));

  useEffect(() => {
    Animated.timing(lift, { toValue: 0, duration: 500, useNativeDriver: true }).start();
  }, [lift]);

  useEffect(() => {
    if (!ready) return;
    const wait = Math.max(0, MIN_VISIBLE_MS - (Date.now() - SHOWN_AT));
    const timer = setTimeout(() => {
      Animated.timing(opacity, { toValue: 0, duration: FADE_MS, useNativeDriver: true }).start(() => onDone());
    }, wait);
    return () => clearTimeout(timer);
  }, [ready, onDone, opacity]);

  return (
    <Animated.View style={[StyleSheet.absoluteFill, { opacity, zIndex: 10 }]} accessibilityLabel={t('app.title')}>
      <View style={[styles.fill, { backgroundColor: '#1D4ED8' }]}>
        <Image source={images.splash.background} style={styles.fillImage} resizeMode="cover" accessibilityIgnoresInvertColors />
        <Animated.View style={[styles.center, { transform: [{ translateY: lift }] }]}>
          <Image source={images.brand.logoTile} style={styles.logo} accessibilityIgnoresInvertColors />
          <View style={styles.titleWrap}>
            <Image source={images.brand.laurel} style={styles.laurel} resizeMode="contain" accessibilityIgnoresInvertColors />
            <Text style={styles.title}>{t('app.title')}</Text>
            <Text style={styles.subtitle}>{t('splash.tagline')}</Text>
          </View>
          <View style={styles.features}>
            <Text style={styles.feature}>{t('splash.line1')}</Text>
            <Text style={styles.feature}>{t('splash.line2')}</Text>
            <Text style={styles.feature}>{t('splash.line3')}</Text>
          </View>
        </Animated.View>
      </View>
    </Animated.View>
  );
}

const styles = StyleSheet.create({
  // Explicit size: on web an absolutely positioned image otherwise keeps its natural width.
  fillImage: { position: 'absolute', top: 0, left: 0, width: '100%', height: '100%' },
  fill: { flex: 1 },
  center: { flex: 1, alignItems: 'center', paddingTop: '30%', gap: 8 },
  logo: { width: 108, height: 108 },
  titleWrap: { alignItems: 'center', justifyContent: 'center', width: 300, height: 140, marginTop: -6 },
  laurel: { position: 'absolute', width: 300, height: 140, top: 0 },
  title: { color: '#fff', fontSize: 38, fontWeight: '900', letterSpacing: 1 },
  subtitle: { color: '#fff', fontSize: 15, fontWeight: '600', opacity: 0.92 },
  features: { alignItems: 'center', gap: 6, marginTop: 28 },
  feature: { color: '#fff', fontSize: 15, fontWeight: '600', opacity: 0.88 },
});
