import { z } from 'zod';
import { create } from 'zustand';
import { persist } from 'zustand/middleware';

import { isHttpsUrl } from '@/lib/format';
import { createLogger } from '@/lib/logger';
import { persistStorage, StorageKeys } from '@/lib/storage';
import { fetchAdConfigRaw } from '@/services/api/endpoints';

const log = createLogger('ads');

/**
 * Remote ad settings edited in the admin panel (GET /api/ads/config).
 * Ad unit IDs are deliberately NOT taken from the server: they are fixed at build
 * time, so a compromised admin account cannot redirect ad revenue.
 */
const schema = z.object({
  masterEnabled: z.boolean().catch(true),
  bannerEnabled: z.boolean().catch(true),
  interstitialEnabled: z.boolean().catch(true),
  nativeEnabled: z.boolean().catch(true),
  rewardedEnabled: z.boolean().catch(true),
  interstitialClickInterval: z.number().int().min(1).max(50).catch(3),
  interstitialCooldownSeconds: z.number().int().min(15).max(3600).catch(60),
  customSponsorActive: z.boolean().catch(false),
  sponsorName: z.string().max(80).catch(''),
  sponsorHeadline: z.string().max(120).catch(''),
  sponsorBody: z.string().max(300).catch(''),
  sponsorRating: z.string().max(20).catch(''),
  sponsorCtaText: z.string().max(40).catch(''),
  sponsorEmoji: z.string().max(8).catch(''),
  sponsorTargetUrl: z.string().max(500).catch(''),
});

export type AdConfig = z.infer<typeof schema>;

export const DEFAULT_AD_CONFIG: AdConfig = schema.parse({});

export function parseAdConfig(raw: unknown): AdConfig {
  return schema.parse(typeof raw === 'object' && raw !== null ? raw : {});
}

export function hasSponsor(c: AdConfig): boolean {
  return c.masterEnabled && c.nativeEnabled && c.customSponsorActive && !!c.sponsorHeadline && isHttpsUrl(c.sponsorTargetUrl);
}

type AdConfigState = {
  config: AdConfig;
  refresh: () => Promise<void>;
};

export const useAdConfigStore = create<AdConfigState>()(
  persist(
    (set) => ({
      config: DEFAULT_AD_CONFIG,
      refresh: async () => {
        try {
          set({ config: parseAdConfig(await fetchAdConfigRaw()) });
        } catch (e) {
          log.warn('ad config fetch failed; keeping cached settings', e);
        }
      },
    }),
    {
      name: StorageKeys.ads,
      storage: persistStorage,
      version: 1,
      partialize: (s) => ({ config: s.config }),
      merge: (persisted, current) => ({
        ...current,
        config: parseAdConfig((persisted as { config?: unknown } | undefined)?.config),
      }),
    },
  ),
);
