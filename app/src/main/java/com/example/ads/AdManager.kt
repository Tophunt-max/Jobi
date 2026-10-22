package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AdManager {
    private const val TAG = "AdManager"

    private var isInitialized = false
    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false

    private var rewardedAd: RewardedAd? = null
    private var isRewardedLoading = false

    // Frequency capping state
    private var jobClicksCounter = 0
    private var lastInterstitialShownTime = 0L

    /**
     * Initializes Google Mobile Ads SDK and AdSettingsRepository in background.
     */
    fun initialize(context: Context) {
        val appContext = context.applicationContext
        AdSettingsRepository.initialize(appContext)

        if (isInitialized) return
        try {
            MobileAds.initialize(appContext) { initializationStatus ->
                Log.d(TAG, "AdMob initialized: ${initializationStatus.adapterStatusMap.keys}")
                isInitialized = true
                // Preload first interstitial and rewarded ads if enabled
                val settings = AdSettingsRepository.settings.value
                if (settings.masterAdsEnabled && settings.interstitialAdsEnabled) {
                    preloadInterstitial(appContext)
                }
                if (settings.masterAdsEnabled && settings.rewardedAdsEnabled) {
                    preloadRewardedAd(appContext)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "AdMob initialization notice: ${e.message}")
        }
    }

    /**
     * Preloads an Interstitial Ad using current admin-configured unit ID.
     */
    fun preloadInterstitial(context: Context) {
        val settings = AdSettingsRepository.settings.value
        if (!settings.masterAdsEnabled || !settings.interstitialAdsEnabled) {
            interstitialAd = null
            return
        }

        if (interstitialAd != null || isInterstitialLoading) return
        isInterstitialLoading = true

        val unitId = settings.getEffectiveInterstitialId()
        val adRequest = AdRequest.Builder().build()
        val appContext = context.applicationContext

        try {
            InterstitialAd.load(
                appContext,
                unitId,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                        isInterstitialLoading = false
                        Log.d(TAG, "Interstitial Ad loaded successfully with unit: $unitId")
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        interstitialAd = null
                        isInterstitialLoading = false
                        Log.d(TAG, "Interstitial Ad failed to load: ${loadAdError.message}")
                    }
                }
            )
        } catch (e: Exception) {
            isInterstitialLoading = false
            interstitialAd = null
            Log.w(TAG, "Error loading interstitial ad: ${e.message}")
        }
    }

    /**
     * Shows Interstitial Ad if enabled and frequency cap conditions are met.
     * If disabled or not loaded, calls [onAdDismissed] immediately without disrupting flow.
     */
    fun showInterstitialIfAllowed(activity: Activity, onAdDismissed: () -> Unit) {
        val settings = AdSettingsRepository.settings.value
        if (!settings.masterAdsEnabled || !settings.interstitialAdsEnabled) {
            onAdDismissed()
            return
        }

        jobClicksCounter++
        val currentTime = System.currentTimeMillis()
        val timeSinceLastAd = currentTime - lastInterstitialShownTime
        val minIntervalMs = settings.cooldownSeconds * 1000L

        val shouldShow = (jobClicksCounter % settings.clicksBetweenInterstitials == 0) &&
                (timeSinceLastAd >= minIntervalMs)

        val currentAd = interstitialAd
        if (shouldShow && currentAd != null) {
            currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial Ad dismissed")
                    interstitialAd = null
                    lastInterstitialShownTime = System.currentTimeMillis()
                    preloadInterstitial(activity.applicationContext)
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.w(TAG, "Interstitial Ad failed to show: ${adError.message}")
                    interstitialAd = null
                    preloadInterstitial(activity.applicationContext)
                    onAdDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Interstitial Ad showed full screen")
                }
            }
            currentAd.show(activity)
        } else {
            // Not eligible or ad not ready yet, continue seamlessly
            if (interstitialAd == null && !isInterstitialLoading) {
                preloadInterstitial(activity.applicationContext)
            }
            onAdDismissed()
        }
    }

    /**
     * Forces immediate display of Interstitial Ad for Admin Diagnostics & Verification.
     */
    fun forceShowTestInterstitial(activity: Activity, onAdDismissed: () -> Unit) {
        val currentAd = interstitialAd
        if (currentAd != null) {
            currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    preloadInterstitial(activity.applicationContext)
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                    preloadInterstitial(activity.applicationContext)
                    onAdDismissed()
                }

                override fun onAdShowedFullScreenContent() {}
            }
            currentAd.show(activity)
        } else {
            // Reload and inform
            preloadInterstitial(activity.applicationContext)
            onAdDismissed()
        }
    }

    /**
     * Preloads a Rewarded Video Ad using current admin-configured unit ID.
     */
    fun preloadRewardedAd(context: Context) {
        val settings = AdSettingsRepository.settings.value
        if (!settings.masterAdsEnabled || !settings.rewardedAdsEnabled) {
            rewardedAd = null
            return
        }

        if (rewardedAd != null || isRewardedLoading) return
        isRewardedLoading = true

        val unitId = settings.getEffectiveRewardedId()
        val adRequest = AdRequest.Builder().build()
        val appContext = context.applicationContext

        try {
            RewardedAd.load(
                appContext,
                unitId,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                        isRewardedLoading = false
                        Log.d(TAG, "Rewarded Ad loaded successfully with unit: $unitId")
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        rewardedAd = null
                        isRewardedLoading = false
                        Log.d(TAG, "Rewarded Ad failed to load: ${loadAdError.message}")
                    }
                }
            )
        } catch (e: Exception) {
            isRewardedLoading = false
            rewardedAd = null
            Log.w(TAG, "Error loading rewarded ad: ${e.message}")
        }
    }

    /**
     * Shows Rewarded Video Ad.
     * When user watches the ad, [onUserEarnedReward] is triggered.
     * If ads are disabled in admin panel, immediately grants reward so candidates are never blocked.
     */
    fun showRewardedAd(
        activity: Activity,
        onUserEarnedReward: () -> Unit,
        onAdClosed: () -> Unit
    ) {
        val settings = AdSettingsRepository.settings.value
        if (!settings.masterAdsEnabled || !settings.rewardedAdsEnabled) {
            // Rewarded ads disabled: grant instant access
            onUserEarnedReward()
            onAdClosed()
            return
        }

        val currentAd = rewardedAd
        if (currentAd != null) {
            var rewardEarned = false

            currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    preloadRewardedAd(activity.applicationContext)
                    if (rewardEarned) {
                        onUserEarnedReward()
                    }
                    onAdClosed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.w(TAG, "Rewarded Ad failed to show: ${adError.message}")
                    rewardedAd = null
                    preloadRewardedAd(activity.applicationContext)
                    // Graceful fallback: grant reward anyway
                    onUserEarnedReward()
                    onAdClosed()
                }
            }

            currentAd.show(activity) { rewardItem ->
                Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                rewardEarned = true
            }
        } else {
            // Graceful fallback when ad is loading or offline
            Log.d(TAG, "Rewarded Ad not ready; granting direct reward")
            preloadRewardedAd(activity.applicationContext)
            onUserEarnedReward()
            onAdClosed()
        }
    }

    fun isInterstitialReady(): Boolean = interstitialAd != null
    fun isRewardedReady(): Boolean = rewardedAd != null
}
