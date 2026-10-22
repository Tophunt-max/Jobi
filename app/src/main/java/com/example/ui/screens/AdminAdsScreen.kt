package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdManager
import com.example.ads.AdMobConfig
import com.example.ads.AdSettings
import com.example.ads.AdSettingsRepository
import com.example.ads.NativeSponsoredJobCard
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.SaffronOrange
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAdsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val activity = context as? Activity
    val currentSettings by AdSettingsRepository.settings.collectAsState()

    // Form local states for editing
    var masterEnabled by remember(currentSettings) { mutableStateOf(currentSettings.masterAdsEnabled) }
    var bannerEnabled by remember(currentSettings) { mutableStateOf(currentSettings.bannerAdsEnabled) }
    var interstitialEnabled by remember(currentSettings) { mutableStateOf(currentSettings.interstitialAdsEnabled) }
    var nativeEnabled by remember(currentSettings) { mutableStateOf(currentSettings.nativeAdsEnabled) }
    var rewardedEnabled by remember(currentSettings) { mutableStateOf(currentSettings.rewardedAdsEnabled) }

    var bannerUnitId by remember(currentSettings) { mutableStateOf(currentSettings.customBannerUnitId) }
    var interstitialUnitId by remember(currentSettings) { mutableStateOf(currentSettings.customInterstitialUnitId) }
    var nativeUnitId by remember(currentSettings) { mutableStateOf(currentSettings.customNativeUnitId) }
    var rewardedUnitId by remember(currentSettings) { mutableStateOf(currentSettings.customRewardedUnitId) }

    var clickInterval by remember(currentSettings) { mutableFloatStateOf(currentSettings.clicksBetweenInterstitials.toFloat()) }
    var cooldownSec by remember(currentSettings) { mutableFloatStateOf(currentSettings.cooldownSeconds.toFloat()) }

    var customSponsorActive by remember(currentSettings) { mutableStateOf(currentSettings.customSponsorActive) }
    var sponsorName by remember(currentSettings) { mutableStateOf(currentSettings.sponsorName) }
    var sponsorHeadline by remember(currentSettings) { mutableStateOf(currentSettings.sponsorHeadline) }
    var sponsorBody by remember(currentSettings) { mutableStateOf(currentSettings.sponsorBody) }
    var sponsorRating by remember(currentSettings) { mutableStateOf(currentSettings.sponsorRating) }
    var sponsorCta by remember(currentSettings) { mutableStateOf(currentSettings.sponsorCtaText) }
    var sponsorEmoji by remember(currentSettings) { mutableStateOf(currentSettings.sponsorIconEmoji) }
    var sponsorUrl by remember(currentSettings) { mutableStateOf(currentSettings.sponsorTargetUrl) }

    var showResetDefaultsDialog by remember { mutableStateOf(false) }
    var showChangePinDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = listOf("Toggles & UX", "AdMob Unit IDs", "Direct Sponsor", "Diagnostics")

    fun saveAllChanges() {
        val updated = currentSettings.copy(
            masterAdsEnabled = masterEnabled,
            bannerAdsEnabled = bannerEnabled,
            interstitialAdsEnabled = interstitialEnabled,
            nativeAdsEnabled = nativeEnabled,
            rewardedAdsEnabled = rewardedEnabled,
            customBannerUnitId = bannerUnitId.trim(),
            customInterstitialUnitId = interstitialUnitId.trim(),
            customNativeUnitId = nativeUnitId.trim(),
            customRewardedUnitId = rewardedUnitId.trim(),
            clicksBetweenInterstitials = clickInterval.toInt(),
            cooldownSeconds = cooldownSec.toInt(),
            customSponsorActive = customSponsorActive,
            sponsorName = sponsorName.trim(),
            sponsorHeadline = sponsorHeadline.trim(),
            sponsorBody = sponsorBody.trim(),
            sponsorRating = sponsorRating.trim(),
            sponsorCtaText = sponsorCta.trim(),
            sponsorIconEmoji = sponsorEmoji.trim().ifEmpty { "🎯" },
            sponsorTargetUrl = sponsorUrl.trim()
        )
        AdSettingsRepository.updateSettings(updated)
        Toast.makeText(context, "✅ Ad settings saved and live immediately!", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Ads & Monetization Control",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                        Text(
                            text = "Admin Panel • विज्ञापन प्रबंधन केंद्र",
                            fontSize = 11.sp,
                            color = Color(0xFF93C5FD)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("admin_ads_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PureWhite
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showResetDefaultsDialog = true },
                        modifier = Modifier.testTag("admin_ads_reset_defaults_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restore,
                            contentDescription = "Reset",
                            tint = PureWhite
                        )
                    }
                    IconButton(
                        onClick = { showChangePinDialog = true },
                        modifier = Modifier.testTag("admin_ads_change_pin_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Security PIN",
                            tint = PureWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepNavy
                )
            )
        },
        bottomBar = {
            Surface(
                color = PureWhite,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Exit", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = { saveAllChanges() },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = PureWhite),
                        modifier = Modifier
                            .weight(2f)
                            .height(48.dp)
                            .testTag("admin_save_all_ad_settings_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save & Apply Now", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        },
        containerColor = Color(0xFFF8FAFC),
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Live Status Banner
            Surface(
                color = if (masterEnabled) DeepNavy else Color(0xFF7F1D1D),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (masterEnabled) Color(0xFF22C55E) else Color(0xFFEF4444))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (masterEnabled) "Monetization: ACTIVE" else "Monetization: PAUSED",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PureWhite
                            )
                            Text(
                                text = if (currentSettings.isUsingProductionIds()) "Production AdMob IDs Active" else "Official Google Test IDs (Safe)",
                                fontSize = 11.sp,
                                color = if (currentSettings.isUsingProductionIds()) Color(0xFFFDE68A) else Color(0xFFBAE6FD)
                            )
                        }
                    }

                    Switch(
                        checked = masterEnabled,
                        onCheckedChange = {
                            masterEnabled = it
                            saveAllChanges()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PureWhite,
                            checkedTrackColor = Color(0xFF10B981),
                            uncheckedThumbColor = PureWhite,
                            uncheckedTrackColor = Color(0xFFB91C1C)
                        ),
                        modifier = Modifier.testTag("admin_master_ad_switch")
                    )
                }
            }

            // Quick Category Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = PureWhite,
                contentColor = RoyalBlue,
                edgePadding = 16.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            // Tab Body Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // TAB 0: Toggles & UX Guardrails
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            border = BorderStroke(1.dp, Slate200)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = RoyalBlue)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Per-Format Ad Switches", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Turn individual ad formats on or off instantly across the app without publishing updates.",
                                    fontSize = 12.sp,
                                    color = Slate500
                                )

                                Spacer(modifier = Modifier.height(14.dp))
                                HorizontalDivider(color = Slate100)
                                Spacer(modifier = Modifier.height(10.dp))

                                FormatToggleRow(
                                    title = "Adaptive Banner Ads",
                                    subtitle = "Docked above bottom bar, smooth loading & zero UI shift",
                                    badge = "Banner",
                                    badgeColor = Color(0xFFDBEAFE),
                                    badgeTextColor = Color(0xFF1D4ED8),
                                    checked = bannerEnabled,
                                    onCheckedChange = { bannerEnabled = it },
                                    testTag = "toggle_banner_ads"
                                )

                                HorizontalDivider(color = Slate100, modifier = Modifier.padding(vertical = 10.dp))

                                FormatToggleRow(
                                    title = "Full-Screen Interstitials",
                                    subtitle = "Shown on vacancy detail clicks, strictly frequency capped",
                                    badge = "Interstitial",
                                    badgeColor = Color(0xFFFEF3C7),
                                    badgeTextColor = Color(0xFFB45309),
                                    checked = interstitialEnabled,
                                    onCheckedChange = { interstitialEnabled = it },
                                    testTag = "toggle_interstitial_ads"
                                )

                                HorizontalDivider(color = Slate100, modifier = Modifier.padding(vertical = 10.dp))

                                FormatToggleRow(
                                    title = "Native In-Feed Sponsored Cards",
                                    subtitle = "Integrated harmoniously inside job listings & home feeds",
                                    badge = "Native Feed",
                                    badgeColor = Color(0xFFDCFCE7),
                                    badgeTextColor = Color(0xFF15803D),
                                    checked = nativeEnabled,
                                    onCheckedChange = { nativeEnabled = it },
                                    testTag = "toggle_native_ads"
                                )

                                HorizontalDivider(color = Slate100, modifier = Modifier.padding(vertical = 10.dp))

                                FormatToggleRow(
                                    title = "Rewarded Video Ads",
                                    subtitle = "Optional watch to unlock Syllabus & Mock Test PDFs",
                                    badge = "Rewarded",
                                    badgeColor = Color(0xFFF3E8FF),
                                    badgeTextColor = Color(0xFF7E22CE),
                                    checked = rewardedEnabled,
                                    onCheckedChange = { rewardedEnabled = it },
                                    testTag = "toggle_rewarded_ads"
                                )
                            }
                        }

                        // Frequency Capping & UX Guardrails
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            border = BorderStroke(1.dp, Slate200)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = SaffronOrange)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("UX & Frequency Capping", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Protects candidates from excessive ad fatigue and preserves high Play Store ratings.",
                                    fontSize = 12.sp,
                                    color = Slate500
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Clicks between Interstitials
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Clicks Between Interstitials", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Slate800)
                                        Text("Shows ad after every N vacancy clicks", fontSize = 11.sp, color = Slate500)
                                    }
                                    Surface(
                                        color = RoyalBlue,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "Every ${clickInterval.toInt()} clicks",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PureWhite,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Slider(
                                    value = clickInterval,
                                    onValueChange = { clickInterval = it },
                                    valueRange = 1f..10f,
                                    steps = 8,
                                    colors = SliderDefaults.colors(
                                        thumbColor = RoyalBlue,
                                        activeTrackColor = RoyalBlue
                                    ),
                                    modifier = Modifier.testTag("admin_slider_click_interval")
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Minimum Cooldown Seconds
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Minimum Cooldown Gap", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Slate800)
                                        Text("Prevents multiple interstitials within this time", fontSize = 11.sp, color = Slate500)
                                    }
                                    Surface(
                                        color = DeepNavy,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "${cooldownSec.toInt()} seconds",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PureWhite,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Slider(
                                    value = cooldownSec,
                                    onValueChange = { cooldownSec = it },
                                    valueRange = 15f..180f,
                                    steps = 10,
                                    colors = SliderDefaults.colors(
                                        thumbColor = DeepNavy,
                                        activeTrackColor = DeepNavy
                                    ),
                                    modifier = Modifier.testTag("admin_slider_cooldown_sec")
                                )
                            }
                        }
                    }

                    1 -> {
                        // TAB 1: AdMob Production Unit IDs Management
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            border = BorderStroke(1.dp, Slate200)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, tint = EmeraldGreen)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("AdMob Production IDs", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            bannerUnitId = ""
                                            interstitialUnitId = ""
                                            nativeUnitId = ""
                                            rewardedUnitId = ""
                                            AdSettingsRepository.resetUnitIdsToTestMode()
                                            Toast.makeText(context, "Reset to Official Google Test IDs", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Use Test IDs", fontSize = 11.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Enter your production Ad Unit IDs from Google AdMob Console. Leave empty to automatically use official Google Test Unit IDs.",
                                    fontSize = 12.sp,
                                    color = Slate500
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                AdUnitIdInputField(
                                    label = "Banner Ad Unit ID",
                                    value = bannerUnitId,
                                    onValueChange = { bannerUnitId = it },
                                    defaultTestId = AdMobConfig.BANNER_AD_UNIT_ID,
                                    testTag = "input_banner_unit_id"
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                AdUnitIdInputField(
                                    label = "Interstitial Ad Unit ID",
                                    value = interstitialUnitId,
                                    onValueChange = { interstitialUnitId = it },
                                    defaultTestId = AdMobConfig.INTERSTITIAL_AD_UNIT_ID,
                                    testTag = "input_interstitial_unit_id"
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                AdUnitIdInputField(
                                    label = "Native Advanced Ad Unit ID",
                                    value = nativeUnitId,
                                    onValueChange = { nativeUnitId = it },
                                    defaultTestId = AdMobConfig.NATIVE_AD_UNIT_ID,
                                    testTag = "input_native_unit_id"
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                AdUnitIdInputField(
                                    label = "Rewarded Video Ad Unit ID",
                                    value = rewardedUnitId,
                                    onValueChange = { rewardedUnitId = it },
                                    defaultTestId = AdMobConfig.REWARDED_AD_UNIT_ID,
                                    testTag = "input_rewarded_unit_id"
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Surface(
                                    color = Color(0xFFFEF3C7),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Color(0xFFFDE68A))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "AdMob Policy Rule: Never click on your own live production ads. In testing or emulation, test IDs are recommended.",
                                            fontSize = 11.5.sp,
                                            color = Color(0xFF92400E),
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // TAB 2: Custom Direct Sponsor / Affiliate Campaign
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            border = BorderStroke(1.dp, Slate200)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Campaign, contentDescription = null, tint = RoyalBlue)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Direct Sponsor Campaign", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900)
                                    }

                                    Switch(
                                        checked = customSponsorActive,
                                        onCheckedChange = { customSponsorActive = it },
                                        colors = SwitchDefaults.colors(checkedThumbColor = PureWhite, checkedTrackColor = RoyalBlue),
                                        modifier = Modifier.testTag("toggle_custom_sponsor_active")
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Promote direct coaching sponsors, test series apps, or course affiliate links across native job feed cards.",
                                    fontSize = 12.sp,
                                    color = Slate500
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                OutlinedTextField(
                                    value = sponsorName,
                                    onValueChange = { sponsorName = it },
                                    label = { Text("Advertiser / Coaching Name") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = sponsorHeadline,
                                    onValueChange = { sponsorHeadline = it },
                                    label = { Text("Headline (Punchy & Exam Focused)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = sponsorBody,
                                    onValueChange = { sponsorBody = it },
                                    label = { Text("Body / Offer Description") },
                                    maxLines = 3,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = sponsorCta,
                                        onValueChange = { sponsorCta = it },
                                        label = { Text("CTA Button") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = sponsorRating,
                                        onValueChange = { sponsorRating = it },
                                        label = { Text("Rating") },
                                        singleLine = true,
                                        modifier = Modifier.weight(0.7f)
                                    )
                                    OutlinedTextField(
                                        value = sponsorEmoji,
                                        onValueChange = { sponsorEmoji = it },
                                        label = { Text("Emoji") },
                                        singleLine = true,
                                        modifier = Modifier.weight(0.5f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = sponsorUrl,
                                    onValueChange = { sponsorUrl = it },
                                    label = { Text("Destination URL / Play Store Link") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Real-time Live Preview
                                Text(
                                    text = "👁️ Real-Time Live Feed Card Preview:",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate800
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                NativeSponsoredJobCard(
                                    creativeIndex = 0
                                )
                            }
                        }
                    }

                    3 -> {
                        // TAB 3: Diagnostics & Immediate Testing
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            border = BorderStroke(1.dp, Slate200)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = DeepNavy)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Diagnostics & Test Triggers", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Test ad rendering and callbacks immediately without navigating through job listings.",
                                    fontSize = 12.sp,
                                    color = Slate500
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Status Indicators
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        color = if (AdManager.isInterstitialReady()) Color(0xFFDCFCE7) else Slate100,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text("Interstitial Status", fontSize = 11.sp, color = Slate500)
                                            Text(
                                                text = if (AdManager.isInterstitialReady()) "Ready in Memory" else "Preloading...",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (AdManager.isInterstitialReady()) Color(0xFF15803D) else Slate700
                                            )
                                        }
                                    }

                                    Surface(
                                        color = if (AdManager.isRewardedReady()) Color(0xFFDCFCE7) else Slate100,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text("Rewarded Status", fontSize = 11.sp, color = Slate500)
                                            Text(
                                                text = if (AdManager.isRewardedReady()) "Ready in Memory" else "Preloading...",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (AdManager.isRewardedReady()) Color(0xFF15803D) else Slate700
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        if (activity != null) {
                                            AdManager.forceShowTestInterstitial(activity) {
                                                Toast.makeText(context, "Interstitial ad completed", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue, contentColor = PureWhite),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .testTag("admin_test_interstitial_btn"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Force Show Test Interstitial Ad", fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        if (activity != null) {
                                            AdManager.showRewardedAd(
                                                activity = activity,
                                                onUserEarnedReward = {
                                                    Toast.makeText(context, "🎉 Reward Granted via Admin Test!", Toast.LENGTH_SHORT).show()
                                                },
                                                onAdClosed = {
                                                    Toast.makeText(context, "Rewarded ad closed", Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E22CE), contentColor = PureWhite),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .testTag("admin_test_rewarded_btn"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.VideoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Force Show Test Rewarded Video", fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                OutlinedButton(
                                    onClick = {
                                        AdManager.preloadInterstitial(context)
                                        AdManager.preloadRewardedAd(context)
                                        Toast.makeText(context, "Triggered ad preloads in background", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Re-trigger Background Preload")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Reset Defaults Confirmation Dialog
    if (showResetDefaultsDialog) {
        AlertDialog(
            onDismissRequest = { showResetDefaultsDialog = false },
            icon = { Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = SaffronOrange) },
            title = { Text("Reset All Ad Settings?", fontWeight = FontWeight.Bold) },
            text = {
                Text("This will restore default official Google AdMob test IDs, enable all ad formats, and reset frequency limits to defaults.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        AdSettingsRepository.resetToDefaults()
                        showResetDefaultsDialog = false
                        Toast.makeText(context, "All settings restored to defaults", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange)
                ) {
                    Text("Confirm Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDefaultsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Change PIN Dialog
    if (showChangePinDialog) {
        var newPin by remember { mutableStateOf("") }
        var confirmPin by remember { mutableStateOf("") }
        var pinError by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showChangePinDialog = false },
            icon = { Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = RoyalBlue) },
            title = { Text("Change Admin Ads PIN", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Current PIN is required to open the Admin Ads Control screen.", fontSize = 12.sp, color = Slate600)
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { if (it.length <= 6) newPin = it },
                        label = { Text("New 4-Digit PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = { if (it.length <= 6) confirmPin = it },
                        label = { Text("Confirm PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (pinError.isNotBlank()) {
                        Text(pinError, fontSize = 11.sp, color = Color(0xFFDC2626))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPin.length < 4) {
                            pinError = "PIN must be at least 4 digits"
                        } else if (newPin != confirmPin) {
                            pinError = "PINs do not match"
                        } else {
                            AdSettingsRepository.updatePin(newPin)
                            showChangePinDialog = false
                            Toast.makeText(context, "Admin PIN updated successfully!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                ) {
                    Text("Update PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePinDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun FormatToggleRow(
    title: String,
    subtitle: String,
    badge: String,
    badgeColor: Color,
    badgeTextColor: Color,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    color = badgeColor,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeTextColor,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.5.sp,
                color = Slate500
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PureWhite,
                checkedTrackColor = RoyalBlue
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun AdUnitIdInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    defaultTestId: String,
    testTag: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = Slate800)
            if (value.isBlank()) {
                Surface(
                    color = Color(0xFFEFF6FF),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Test ID in Use",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalBlue,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            } else {
                Surface(
                    color = Color(0xFFDCFCE7),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Production Override",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF15803D),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(defaultTestId, fontSize = 11.5.sp, color = Slate400) },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag)
        )
    }
}
