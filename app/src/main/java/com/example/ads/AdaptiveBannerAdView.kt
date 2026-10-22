package com.example.ads

import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate800
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

/**
 * High-performance AdMob Adaptive Banner View with Admin Panel control.
 * Observes AdSettingsRepository:
 * - If master or banner ads are disabled by Admin, collapses seamlessly.
 * - Dynamically uses the Admin-configured AdMob Banner Unit ID.
 * - Renders custom sponsor or educational fallback if AdMob is loading/offline.
 */
@Composable
fun AdaptiveBannerAdView(
    modifier: Modifier = Modifier,
    onAdClicked: () -> Unit = {}
) {
    val context = LocalContext.current
    val adSettings by AdSettingsRepository.settings.collectAsState()

    // Respect master and banner ad switches from Admin Panel
    if (!adSettings.masterAdsEnabled || !adSettings.bannerAdsEnabled) {
        return
    }

    var isAdLoaded by remember { mutableStateOf(false) }
    var hasFailed by remember { mutableStateOf(false) }

    val effectiveBannerId = remember(adSettings.customBannerUnitId) {
        adSettings.getEffectiveBannerId()
    }

    Surface(
        color = PureWhite,
        shadowElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("admob_adaptive_banner")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            // Live AdMob Banner View (re-creates if unit ID changes in admin)
            key(effectiveBannerId) {
                AndroidView(
                    factory = { ctx ->
                        try {
                            AdView(ctx).apply {
                                setAdSize(AdSize.BANNER)
                                adUnitId = effectiveBannerId
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                )
                                adListener = object : AdListener() {
                                    override fun onAdLoaded() {
                                        isAdLoaded = true
                                        hasFailed = false
                                    }

                                    override fun onAdFailedToLoad(error: LoadAdError) {
                                        isAdLoaded = false
                                        hasFailed = true
                                    }

                                    override fun onAdClicked() {
                                        onAdClicked()
                                    }
                                }
                                try {
                                    loadAd(AdRequest.Builder().build())
                                } catch (_: Throwable) {
                                    hasFailed = true
                                }
                            }
                        } catch (_: Throwable) {
                            hasFailed = true
                            android.view.View(ctx)
                        }
                    },
                    onRelease = { adView ->
                        try {
                            adView.destroy()
                        } catch (_: Exception) {}
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                )
            }

            // Sleek Fallback when AdMob is offline, loading, or has no fill
            if (!isAdLoaded) {
                val bannerTitle = if (adSettings.customSponsorActive) {
                    adSettings.sponsorHeadline
                } else {
                    "🎯 Free Sarkari 2026 Test Series & PDF"
                }

                val bannerSubtitle = if (adSettings.customSponsorActive) {
                    adSettings.sponsorName
                } else {
                    "Daily Current Affairs • All India Mock Tests"
                }

                val bannerCta = if (adSettings.customSponsorActive) {
                    adSettings.sponsorCtaText
                } else {
                    "Explore"
                }

                val targetUrl = if (adSettings.customSponsorActive) {
                    adSettings.sponsorTargetUrl
                } else {
                    "https://play.google.com/store/search?q=sarkari+test+series&c=apps"
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF1F5F9))
                        .border(1.dp, Slate200, RoundedCornerShape(8.dp))
                        .clickable {
                            onAdClicked()
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "AD",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF92400E),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = bannerTitle,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate800,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = bannerSubtitle,
                                fontSize = 10.sp,
                                color = Slate500,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Surface(
                        color = RoyalBlue,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = bannerCta,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "Open",
                                tint = PureWhite,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
