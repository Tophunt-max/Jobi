/**
 * App settings stored in D1 (`app_settings` table, created by migration 0001).
 */

/**
 * Default ad configuration. Ad unit IDs are empty on purpose: the Android app
 * falls back to its own build-time IDs (Google test IDs in debug builds).
 */
export const DEFAULT_AD_CONFIG = Object.freeze({
    masterEnabled: true,
    bannerEnabled: true,
    interstitialEnabled: true,
    nativeEnabled: true,
    rewardedEnabled: true,
    bannerId: "",
    interstitialId: "",
    nativeId: "",
    rewardedId: "",
    interstitialClickInterval: 3,
    interstitialCooldownSeconds: 60,
    customSponsorActive: false,
    sponsorName: "",
    sponsorHeadline: "",
    sponsorBody: "",
    sponsorRating: "",
    sponsorCtaText: "",
    sponsorEmoji: "",
    sponsorTargetUrl: ""
});

export async function loadAdConfig(env) {
    const row = await env.DB.prepare("SELECT value, updated_at FROM app_settings WHERE key = 'ad_config'").first();
    if (!row || !row.value) return { ...DEFAULT_AD_CONFIG };
    let parsed = {};
    try {
        parsed = JSON.parse(row.value);
    } catch {
        parsed = {};
    }
    return { ...DEFAULT_AD_CONFIG, ...parsed, updatedAt: row.updated_at };
}

export async function saveAdConfig(env, config) {
    const { updatedAt, ...value } = config;
    await env.DB.prepare(`
        INSERT INTO app_settings (key, value, updated_at)
        VALUES ('ad_config', ?, CURRENT_TIMESTAMP)
        ON CONFLICT(key) DO UPDATE SET value = excluded.value, updated_at = CURRENT_TIMESTAMP
    `).bind(JSON.stringify(value)).run();
}
