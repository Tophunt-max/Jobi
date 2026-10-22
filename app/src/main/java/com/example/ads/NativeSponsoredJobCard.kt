package com.example.ads

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900

/**
 * Native-styled Sponsored Card for job listing feeds with Admin Panel control.
 * Features:
 * - Controlled via Admin Ads Panel: collapses if Master or Native Ads are disabled.
 * - Supports Custom Direct Sponsor override (instant affiliate campaign updates).
 * - Complies strictly with Google Play Advertising policies via prominent "Ad • प्रायोजित" badge.
 */
@Composable
fun NativeSponsoredJobCard(
    modifier: Modifier = Modifier,
    creativeIndex: Int = 0,
    onSponsorClicked: ((SponsorCreative) -> Unit)? = null
) {
    val context = LocalContext.current
    val adSettings by AdSettingsRepository.settings.collectAsState()

    // Respect master & native ads toggle from Admin Panel
    if (!adSettings.masterAdsEnabled || !adSettings.nativeAdsEnabled) {
        return
    }

    val creative = remember(creativeIndex, adSettings.customSponsorActive, adSettings.sponsorHeadline) {
        if (adSettings.customSponsorActive) {
            SponsorCreative(
                headline = adSettings.sponsorHeadline,
                body = adSettings.sponsorBody,
                advertiser = adSettings.sponsorName,
                rating = adSettings.sponsorRating,
                ctaText = adSettings.sponsorCtaText,
                iconEmoji = adSettings.sponsorIconEmoji,
                targetUrl = adSettings.sponsorTargetUrl
            )
        } else {
            val presets = AdMobConfig.SPONSOR_PRESETS
            presets[creativeIndex % presets.size]
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("native_sponsored_card_${creativeIndex}")
            .clickable {
                if (onSponsorClicked != null) {
                    onSponsorClicked(creative)
                } else {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(creative.targetUrl))
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                }
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(Color(0xFFE2E8F0), Color(0xFFF1F5F9))
            ),
            width = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Ad Tag + Advertiser Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
                    ) {
                        Text(
                            text = "Ad • प्रायोजित",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB45309),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = creative.advertiser,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    color = Color(0xFFFEF9C3),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = creative.rating,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Creative Content
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFEFF6FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = creative.iconEmoji,
                        fontSize = 22.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = creative.headline,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = creative.body,
                        fontSize = 11.5.sp,
                        color = Slate700,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action CTA Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (adSettings.customSponsorActive) "Direct Sponsor" else "Verified Career Partner",
                        fontSize = 11.sp,
                        color = Slate500,
                        fontWeight = FontWeight.Medium
                    )
                }

                Surface(
                    color = RoyalBlue,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = creative.ctaText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = PureWhite,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}
