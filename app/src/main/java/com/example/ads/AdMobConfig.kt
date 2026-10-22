package com.example.ads

/**
 * Google AdMob Configuration & Official Sample Ad Unit IDs.
 * These demo unit IDs are guaranteed to work in development/testing without policy violations.
 * Replace with production ad unit IDs from your Google AdMob console when publishing.
 */
object AdMobConfig {
    // Official Google AdMob Sample App ID
    const val APP_ID = "ca-app-pub-3940256099942544~3347511713"

    // Standard Adaptive & Fixed Banner Sample ID
    const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    const val ADAPTIVE_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741"

    // Full-screen Interstitial Sample ID
    const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    // Rewarded Video Sample ID
    const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    // Native Advanced Sample ID
    const val NATIVE_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"

    // Frequency capping: show interstitial at most once every 3 job views
    const val INTERSTITIAL_CLICK_FREQUENCY = 3
    // Frequency capping: minimum milliseconds between interstitials (60 seconds)
    const val INTERSTITIAL_MIN_INTERVAL_MS = 60_000L

    // Educational & Coaching Sponsors for Native Cards
    val SPONSOR_PRESETS = listOf(
        SponsorCreative(
            headline = "Sarkari Pariksha 2026 Live Test Series",
            body = "500+ All India Mock Tests with Rank Predictor & Detailed Video Solutions for SSC, UPSC & RRB.",
            advertiser = "Exampur & Testbook Partner",
            rating = "4.8 ★",
            ctaText = "Start Free Test",
            iconEmoji = "🎯",
            targetUrl = "https://play.google.com/store/search?q=sarkari+test+series&c=apps"
        ),
        SponsorCreative(
            headline = "Daily Current Affairs & GK Capsule 2026",
            body = "100% Exam-targeted monthly e-book PDF in Hindi & English. Downloaded by 2 Lakh+ aspirants.",
            advertiser = "Drishti IAS & Chronicle Digest",
            rating = "4.9 ★",
            ctaText = "Download Free PDF",
            iconEmoji = "📚",
            targetUrl = "https://play.google.com/store/search?q=current+affairs+pdf&c=apps"
        ),
        SponsorCreative(
            headline = "Speed Typing Master for SSC & High Court",
            body = "Improve English & Hindi typing speed up to 45 WPM before typing skill tests with accuracy tracker.",
            advertiser = "TypingHero Pro",
            rating = "4.7 ★",
            ctaText = "Practice Typing",
            iconEmoji = "⌨️",
            targetUrl = "https://play.google.com/store/search?q=typing+practice&c=apps"
        )
    )
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
