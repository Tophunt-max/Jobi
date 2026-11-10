import { Platform } from 'react-native';
import mobileAds, {
  AdEventType,
  AdsConsent,
  AdsConsentPrivacyOptionsRequirementStatus,
  InterstitialAd,
  RewardedAd,
  RewardedAdEventType,
  TestIds,
} from 'react-native-google-mobile-ads';

import { env } from '@/config/env';
import { createLogger } from '@/lib/logger';

import { useAdConfigStore } from './config';
import { useAdRuntime, type RewardOutcome } from './runtime';

const log = createLogger('ads');

export const adsSupported = true;

type UnitKind = 'banner' | 'interstitial' | 'rewarded';

const TEST_UNITS: Record<UnitKind, string> = {
  banner: TestIds.ADAPTIVE_BANNER,
  interstitial: TestIds.INTERSTITIAL,
  rewarded: TestIds.REWARDED,
};

/** Production builds use the IDs baked in at build time; everything else uses Google test units. */
export function adUnitId(kind: UnitKind): string {
  if (!env.isProduction) return TEST_UNITS[kind];
  const key = `${kind}${Platform.OS === 'ios' ? 'Ios' : 'Android'}` as keyof typeof env.admob;
  return env.admob[key] || TEST_UNITS[kind];
}

function isNoFill(error: unknown): boolean {
  const text = `${(error as { code?: string })?.code ?? ''} ${(error as Error)?.message ?? ''}`.toLowerCase();
  return text.includes('no-fill') || text.includes('no fill');
}

let initStarted = false;
let interstitial: InterstitialAd | null = null;
let rewarded: RewardedAd | null = null;
let rewardedNoFill = false;
let clicks = 0;
let lastInterstitialAt = 0;
let retryTimer: ReturnType<typeof setTimeout> | null = null;

function loadInterstitial() {
  interstitial?.removeAllListeners();
  const ad = InterstitialAd.createForAdRequest(adUnitId('interstitial'));
  ad.addAdEventListener(AdEventType.ERROR, (e) => {
    log.debug(`interstitial load failed: ${(e as Error)?.message}`);
    if (retryTimer) clearTimeout(retryTimer);
    retryTimer = setTimeout(loadInterstitial, 60_000);
  });
  // Deferred so listeners added by runWithInterstitial still run for this close event.
  ad.addAdEventListener(AdEventType.CLOSED, () => setTimeout(loadInterstitial, 0));
  ad.load();
  interstitial = ad;
}

function loadRewarded() {
  rewarded?.removeAllListeners();
  const ad = RewardedAd.createForAdRequest(adUnitId('rewarded'));
  rewardedNoFill = false;
  ad.addAdEventListener(AdEventType.ERROR, (e) => {
    rewardedNoFill = isNoFill(e);
  });
  ad.load();
  rewarded = ad;
}

/**
 * Collects UMP consent (GDPR / US state privacy) and then starts the SDK.
 * Ads are only requested when consent allows it.
 */
export async function initAds(): Promise<void> {
  if (initStarted) return;
  initStarted = true;
  try {
    let info;
    try {
      info = await AdsConsent.gatherConsent();
    } catch (e) {
      log.warn('consent update failed; using the last known state', e);
      info = await AdsConsent.getConsentInfo();
    }
    if (!info.canRequestAds) {
      useAdRuntime.getState().setReady(false, true);
      return;
    }
    await mobileAds().initialize();
    const privacyRequired =
      info.privacyOptionsRequirementStatus === AdsConsentPrivacyOptionsRequirementStatus.REQUIRED;
    useAdRuntime.getState().setReady(true, privacyRequired);
    loadInterstitial();
    loadRewarded();
  } catch (e) {
    log.warn('ad SDK init failed', e);
    initStarted = false;
  }
}

/** Opens Google's privacy options form (required in the EEA/UK when consent was collected). */
export async function showPrivacyOptions(): Promise<void> {
  try {
    await AdsConsent.showPrivacyOptionsForm();
  } catch (e) {
    log.warn('privacy options form failed', e);
  }
}

/**
 * Runs `action` (usually opening a job), showing an interstitial first when the
 * admin-configured frequency and cooldown allow it. Never blocks the action.
 */
export function runWithInterstitial(action: () => void): void {
  const cfg = useAdConfigStore.getState().config;
  const ad = interstitial;
  if (!useAdRuntime.getState().ready || !cfg.masterEnabled || !cfg.interstitialEnabled || !ad) {
    action();
    return;
  }
  clicks += 1;
  const due =
    clicks % cfg.interstitialClickInterval === 0 &&
    Date.now() - lastInterstitialAt >= cfg.interstitialCooldownSeconds * 1000;
  if (!due || !ad.loaded) {
    action();
    return;
  }

  let finished = false;
  const finish = () => {
    if (finished) return;
    finished = true;
    offClosed();
    offError();
    action();
  };
  const offClosed = ad.addAdEventListener(AdEventType.CLOSED, finish);
  const offError = ad.addAdEventListener(AdEventType.ERROR, finish);
  lastInterstitialAt = Date.now();
  ad.show().catch(finish);
}

/** Shows a rewarded ad. The caller decides what to unlock based on the outcome. */
export function showRewarded(): Promise<RewardOutcome> {
  const cfg = useAdConfigStore.getState().config;
  if (!useAdRuntime.getState().ready || !cfg.masterEnabled || !cfg.rewardedEnabled) {
    return Promise.resolve('not_required');
  }
  const ad = rewarded;
  if (!ad || !ad.loaded) {
    if (rewardedNoFill) {
      loadRewarded();
      return Promise.resolve('not_required');
    }
    if (!ad) loadRewarded();
    return Promise.resolve('unavailable');
  }

  return new Promise<RewardOutcome>((resolve) => {
    let earned = false;
    let settled = false;
    const settle = (outcome: RewardOutcome) => {
      if (settled) return;
      settled = true;
      offEarned();
      offClosed();
      offError();
      setTimeout(loadRewarded, 0);
      resolve(outcome);
    };
    const offEarned = ad.addAdEventListener(RewardedAdEventType.EARNED_REWARD, () => {
      earned = true;
    });
    const offClosed = ad.addAdEventListener(AdEventType.CLOSED, () => settle(earned ? 'rewarded' : 'dismissed'));
    const offError = ad.addAdEventListener(AdEventType.ERROR, () => settle('unavailable'));
    ad.show().catch(() => settle('unavailable'));
  });
}
