package com.example.ads

import com.example.BuildConfig

/**
 * AdMob ad unit IDs, injected per build type (see app/build.gradle.kts):
 * - debug: always Google's public test IDs;
 * - release: ADMOB_*_ID from .env / CI env (falling back to test IDs if not configured).
 * The admin panel can override unit IDs remotely for release builds only.
 */
object AdMobConfig {
    val ADAPTIVE_BANNER_AD_UNIT_ID: String = BuildConfig.ADMOB_BANNER_ID
    val BANNER_AD_UNIT_ID: String = BuildConfig.ADMOB_BANNER_ID
    val INTERSTITIAL_AD_UNIT_ID: String = BuildConfig.ADMOB_INTERSTITIAL_ID
    val REWARDED_AD_UNIT_ID: String = BuildConfig.ADMOB_REWARDED_ID
    val NATIVE_AD_UNIT_ID: String = BuildConfig.ADMOB_NATIVE_ID

    /** Remote overrides are ignored in debug builds to avoid invalid traffic on real units. */
    val ALLOW_REMOTE_AD_UNITS: Boolean = BuildConfig.ALLOW_REMOTE_AD_UNITS

    // Frequency capping: show interstitial at most once every 3 job views
    const val INTERSTITIAL_CLICK_FREQUENCY = 3
    // Frequency capping: minimum milliseconds between interstitials (60 seconds)
    const val INTERSTITIAL_MIN_INTERVAL_MS = 60_000L
}

data class SponsorCreative(
    val headline: String,
    val body: String,
    val advertiser: String,
    val rating: String,
    val ctaText: String,
    val iconEmoji: String,
    val targetUrl: String
)
