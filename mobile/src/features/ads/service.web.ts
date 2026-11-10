import type { RewardOutcome } from './runtime';

/** AdMob has no web SDK. The web build shows only the direct sponsor card. */
export const adsSupported = false;

export function adUnitId(_kind: 'banner' | 'interstitial' | 'rewarded'): string {
  return '';
}

export async function initAds(): Promise<void> {}

export async function showPrivacyOptions(): Promise<void> {}

export function runWithInterstitial(action: () => void): void {
  action();
}

export function showRewarded(): Promise<RewardOutcome> {
  return Promise.resolve('not_required');
}
