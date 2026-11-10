import { create } from 'zustand';

/** Live ad SDK state, shared by banners and fullscreen ads. Not persisted. */
type AdRuntimeState = {
  ready: boolean;
  privacyOptionsRequired: boolean;
  setReady: (ready: boolean, privacyOptionsRequired?: boolean) => void;
};

export const useAdRuntime = create<AdRuntimeState>()((set) => ({
  ready: false,
  privacyOptionsRequired: false,
  setReady: (ready, privacyOptionsRequired = false) => set({ ready, privacyOptionsRequired }),
}));

export type RewardOutcome =
  /** The user watched the ad. */
  | 'rewarded'
  /** Ads are off, unsupported, or there is no inventory: continue without an ad. */
  | 'not_required'
  /** The user closed the ad early. */
  | 'dismissed'
  /** An ad is enabled but not ready yet; ask the user to retry. */
  | 'unavailable';
