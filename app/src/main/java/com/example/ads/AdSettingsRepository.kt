package com.example.ads

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.remote.cloudflare.CloudflareConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Data model representing the comprehensive monetization and ad configurations
 * controlled via the Admin Ads Panel.
 */
data class AdSettings(
    // Master & format-level switches
    val masterAdsEnabled: Boolean = true,
    val bannerAdsEnabled: Boolean = true,
    val interstitialAdsEnabled: Boolean = true,
    val nativeAdsEnabled: Boolean = true,
    val rewardedAdsEnabled: Boolean = true,

    // Production AdMob Unit IDs (empty means use default official test IDs)
    val customBannerUnitId: String = "",
    val customInterstitialUnitId: String = "",
    val customNativeUnitId: String = "",
    val customRewardedUnitId: String = "",

    // Frequency capping & UX guardrails
    val clicksBetweenInterstitials: Int = 3,
    val cooldownSeconds: Int = 60,

    // Custom direct sponsor / affiliate campaign
    val customSponsorActive: Boolean = false,
    val sponsorName: String = "",
    val sponsorHeadline: String = "",
    val sponsorBody: String = "",
    val sponsorRating: String = "",
    val sponsorCtaText: String = "",
    val sponsorIconEmoji: String = "",
    val sponsorTargetUrl: String = ""
) {
    /** A direct sponsor is shown only when fully configured with an https link. */
    fun hasValidSponsor(): Boolean =
        customSponsorActive && sponsorHeadline.isNotBlank() && sponsorTargetUrl.startsWith("https://")

    private fun pick(remote: String, local: String): String =
        if (AdMobConfig.ALLOW_REMOTE_AD_UNITS && AD_UNIT_REGEX.matches(remote.trim())) remote.trim() else local

    fun getEffectiveBannerId(): String = pick(customBannerUnitId, AdMobConfig.BANNER_AD_UNIT_ID)

    fun getEffectiveInterstitialId(): String = pick(customInterstitialUnitId, AdMobConfig.INTERSTITIAL_AD_UNIT_ID)

    fun getEffectiveNativeId(): String = pick(customNativeUnitId, AdMobConfig.NATIVE_AD_UNIT_ID)

    fun getEffectiveRewardedId(): String = pick(customRewardedUnitId, AdMobConfig.REWARDED_AD_UNIT_ID)

    private companion object {
        val AD_UNIT_REGEX = Regex("^ca-app-pub-\\d{16}/\\d{10}$")
    }
}

/**
 * Singleton repository managing ad preferences backed by SharedPreferences,
 * exposing reactive StateFlow for instant, zero-restart UI and SDK updates.
 */
object AdSettingsRepository {
    private const val PREFS_NAME = "sarkari_admin_ad_settings"

    private const val KEY_MASTER_ENABLED = "key_master_enabled"
    private const val KEY_BANNER_ENABLED = "key_banner_enabled"
    private const val KEY_INTERSTITIAL_ENABLED = "key_interstitial_enabled"
    private const val KEY_NATIVE_ENABLED = "key_native_enabled"
    private const val KEY_REWARDED_ENABLED = "key_rewarded_enabled"

    private const val KEY_BANNER_ID = "key_banner_unit_id"
    private const val KEY_INTERSTITIAL_ID = "key_interstitial_unit_id"
    private const val KEY_NATIVE_ID = "key_native_unit_id"
    private const val KEY_REWARDED_ID = "key_rewarded_unit_id"

    private const val KEY_CLICKS_INTERVAL = "key_clicks_interval"
    private const val KEY_COOLDOWN_SECONDS = "key_cooldown_seconds"

    private const val KEY_SPONSOR_ACTIVE = "key_sponsor_active"
    private const val KEY_SPONSOR_NAME = "key_sponsor_name"
    private const val KEY_SPONSOR_HEADLINE = "key_sponsor_headline"
    private const val KEY_SPONSOR_BODY = "key_sponsor_body"
    private const val KEY_SPONSOR_RATING = "key_sponsor_rating"
    private const val KEY_SPONSOR_CTA = "key_sponsor_cta"
    private const val KEY_SPONSOR_EMOJI = "key_sponsor_emoji"
    private const val KEY_SPONSOR_URL = "key_sponsor_url"

    @Volatile
    private var sharedPrefs: SharedPreferences? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _settings = MutableStateFlow(AdSettings())
    val settings: StateFlow<AdSettings> = _settings.asStateFlow()

    /**
     * Initializes repository with Context and loads persisted ad settings.
     */
    fun initialize(context: Context) {
        if (sharedPrefs == null) {
            val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            sharedPrefs = prefs
            loadFromPrefs(prefs)
        }
        // Asynchronously sync latest ad settings from Cloudflare Worker configured in Next.js web panel
        scope.launch {
            fetchRemoteConfig()
        }
    }

    private fun loadFromPrefs(prefs: SharedPreferences) {
        val loaded = AdSettings(
            masterAdsEnabled = prefs.getBoolean(KEY_MASTER_ENABLED, true),
            bannerAdsEnabled = prefs.getBoolean(KEY_BANNER_ENABLED, true),
            interstitialAdsEnabled = prefs.getBoolean(KEY_INTERSTITIAL_ENABLED, true),
            nativeAdsEnabled = prefs.getBoolean(KEY_NATIVE_ENABLED, true),
            rewardedAdsEnabled = prefs.getBoolean(KEY_REWARDED_ENABLED, true),

            customBannerUnitId = prefs.getString(KEY_BANNER_ID, "") ?: "",
            customInterstitialUnitId = prefs.getString(KEY_INTERSTITIAL_ID, "") ?: "",
            customNativeUnitId = prefs.getString(KEY_NATIVE_ID, "") ?: "",
            customRewardedUnitId = prefs.getString(KEY_REWARDED_ID, "") ?: "",

            clicksBetweenInterstitials = prefs.getInt(KEY_CLICKS_INTERVAL, 3),
            cooldownSeconds = prefs.getInt(KEY_COOLDOWN_SECONDS, 60),

            customSponsorActive = prefs.getBoolean(KEY_SPONSOR_ACTIVE, false),
            sponsorName = prefs.getString(KEY_SPONSOR_NAME, "") ?: "",
            sponsorHeadline = prefs.getString(KEY_SPONSOR_HEADLINE, "") ?: "",
            sponsorBody = prefs.getString(KEY_SPONSOR_BODY, "") ?: "",
            sponsorRating = prefs.getString(KEY_SPONSOR_RATING, "") ?: "",
            sponsorCtaText = prefs.getString(KEY_SPONSOR_CTA, "") ?: "",
            sponsorIconEmoji = prefs.getString(KEY_SPONSOR_EMOJI, "") ?: "",
            sponsorTargetUrl = prefs.getString(KEY_SPONSOR_URL, "") ?: ""
        )
        _settings.value = loaded
    }

    /**
     * Saves updated settings to SharedPreferences and emits new state to observers.
     */
    fun updateSettings(newSettings: AdSettings) {
        _settings.value = newSettings
        sharedPrefs?.edit()?.apply {
            putBoolean(KEY_MASTER_ENABLED, newSettings.masterAdsEnabled)
            putBoolean(KEY_BANNER_ENABLED, newSettings.bannerAdsEnabled)
            putBoolean(KEY_INTERSTITIAL_ENABLED, newSettings.interstitialAdsEnabled)
            putBoolean(KEY_NATIVE_ENABLED, newSettings.nativeAdsEnabled)
            putBoolean(KEY_REWARDED_ENABLED, newSettings.rewardedAdsEnabled)

            putString(KEY_BANNER_ID, newSettings.customBannerUnitId)
            putString(KEY_INTERSTITIAL_ID, newSettings.customInterstitialUnitId)
            putString(KEY_NATIVE_ID, newSettings.customNativeUnitId)
            putString(KEY_REWARDED_ID, newSettings.customRewardedUnitId)

            putInt(KEY_CLICKS_INTERVAL, newSettings.clicksBetweenInterstitials)
            putInt(KEY_COOLDOWN_SECONDS, newSettings.cooldownSeconds)

            putBoolean(KEY_SPONSOR_ACTIVE, newSettings.customSponsorActive)
            putString(KEY_SPONSOR_NAME, newSettings.sponsorName)
            putString(KEY_SPONSOR_HEADLINE, newSettings.sponsorHeadline)
            putString(KEY_SPONSOR_BODY, newSettings.sponsorBody)
            putString(KEY_SPONSOR_RATING, newSettings.sponsorRating)
            putString(KEY_SPONSOR_CTA, newSettings.sponsorCtaText)
            putString(KEY_SPONSOR_EMOJI, newSettings.sponsorIconEmoji)
            putString(KEY_SPONSOR_URL, newSettings.sponsorTargetUrl)
            remove("key_admin_pin")
            apply()
        }
    }

    /**
     * Fetches remote ad configuration from the Cloudflare Worker API (configured via the Next.js Admin Panel).
     * Automatically updates local state and SharedPreferences cache.
     */
    suspend fun fetchRemoteConfig(workerBaseUrl: String? = null): Boolean = withContext(Dispatchers.IO) {
        try {
            if (workerBaseUrl == null && !CloudflareConfig.isConfigured()) return@withContext false
            val baseUrl = (workerBaseUrl ?: CloudflareConfig.getActiveWorkerUrl()).trimEnd('/')
            val url = URL("$baseUrl/api/ads/config")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Accept", "application/json")
            }
            if (connection.responseCode in 200..299) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val rootJson = JSONObject(responseText)
                val dataObj = if (rootJson.has("data")) rootJson.getJSONObject("data") else rootJson

                val current = _settings.value
                val updated = current.copy(
                    masterAdsEnabled = dataObj.optBoolean("masterEnabled", current.masterAdsEnabled),
                    bannerAdsEnabled = dataObj.optBoolean("bannerEnabled", current.bannerAdsEnabled),
                    interstitialAdsEnabled = dataObj.optBoolean("interstitialEnabled", current.interstitialAdsEnabled),
                    nativeAdsEnabled = dataObj.optBoolean("nativeEnabled", current.nativeAdsEnabled),
                    rewardedAdsEnabled = dataObj.optBoolean("rewardedEnabled", current.rewardedAdsEnabled),
                    customBannerUnitId = dataObj.optString("bannerId", current.customBannerUnitId),
                    customInterstitialUnitId = dataObj.optString("interstitialId", current.customInterstitialUnitId),
                    customNativeUnitId = dataObj.optString("nativeId", current.customNativeUnitId),
                    customRewardedUnitId = dataObj.optString("rewardedId", current.customRewardedUnitId),
                    clicksBetweenInterstitials = dataObj.optInt("interstitialClickInterval", current.clicksBetweenInterstitials),
                    cooldownSeconds = dataObj.optInt("interstitialCooldownSeconds", current.cooldownSeconds),
                    customSponsorActive = dataObj.optBoolean("customSponsorActive", current.customSponsorActive),
                    sponsorName = dataObj.optString("sponsorName", current.sponsorName),
                    sponsorHeadline = dataObj.optString("sponsorHeadline", current.sponsorHeadline),
                    sponsorBody = dataObj.optString("sponsorBody", current.sponsorBody),
                    sponsorRating = dataObj.optString("sponsorRating", current.sponsorRating),
                    sponsorCtaText = dataObj.optString("sponsorCtaText", current.sponsorCtaText),
                    sponsorIconEmoji = dataObj.optString("sponsorEmoji", current.sponsorIconEmoji),
                    sponsorTargetUrl = dataObj.optString("sponsorTargetUrl", current.sponsorTargetUrl)
                )
                updateSettings(updated)
                Log.d("AdSettingsRepo", "Successfully synced remote ad config from $baseUrl")
                true
            } else {
                Log.w("AdSettingsRepo", "Remote ad config endpoint returned HTTP ${connection.responseCode}")
                false
            }
        } catch (e: Exception) {
            Log.w("AdSettingsRepo", "Could not fetch remote ad config: ${e.message}")
            false
        }
    }
}
