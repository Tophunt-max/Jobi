import { useState } from 'react';
import { StyleSheet, View } from 'react-native';
import { BannerAd, BannerAdSize } from 'react-native-google-mobile-ads';

import { useTheme } from '@/theme';

import { useAdConfigStore } from './config';
import { useAdRuntime } from './runtime';
import { adUnitId } from './service';

/** Anchored adaptive banner. Renders nothing until consent and SDK init are done. */
export function BannerSlot() {
  const ready = useAdRuntime((s) => s.ready);
  const enabled = useAdConfigStore((s) => s.config.masterEnabled && s.config.bannerEnabled);
  const [failed, setFailed] = useState(false);
  const { colors } = useTheme();

  if (!ready || !enabled || failed) return null;
  return (
    <View style={[styles.wrap, { borderTopColor: colors.border, backgroundColor: colors.surface }]}>
      <BannerAd
        unitId={adUnitId('banner')}
        size={BannerAdSize.ANCHORED_ADAPTIVE_BANNER}
        onAdFailedToLoad={() => setFailed(true)}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: { alignItems: 'center', borderTopWidth: StyleSheet.hairlineWidth },
});
