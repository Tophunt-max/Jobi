package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.LanguageRegistry
import com.example.ui.theme.EmeraldMint
import com.example.ui.theme.GoldStar
import com.example.ui.theme.HeroGradientBrush
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SaffronOrange

@Composable
fun AppTopBar(
    language: AppLanguage,
    unreadNotifCount: Int,
    isAdminActive: Boolean,
    onLanguageClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onAdminToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.Transparent,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(HeroGradientBrush)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Modern Emblem Logo Crest
                Surface(
                    color = PureWhite.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .size(42.dp)
                        .border(1.dp, PureWhite.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "National Crest",
                            tint = SaffronOrange,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // App Title and Live Badge
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = LanguageRegistry.getString("app_title", language),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 19.sp,
                                color = PureWhite,
                                letterSpacing = 0.3.sp
                            )
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(EmeraldMint)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "VERIFIED PORTAL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldMint,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Actions: Language Selector Pill
                Surface(
                    onClick = onLanguageClick,
                    shape = RoundedCornerShape(20.dp),
                    color = PureWhite.copy(alpha = 0.14f),
                    modifier = Modifier
                        .border(1.dp, PureWhite.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                        .testTag("language_selector_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Language",
                            tint = PureWhite,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = language.nativeName,
                            color = PureWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Notifications Icon Button
                IconButton(
                    onClick = onNotificationsClick,
                    modifier = Modifier.testTag("notifications_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (unreadNotifCount > 0) {
                                Badge(
                                    containerColor = SaffronOrange,
                                    contentColor = PureWhite,
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Text(
                                        text = "$unreadNotifCount",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = PureWhite.copy(alpha = 0.95f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Admin Console Quick Link
                IconButton(
                    onClick = onAdminToggle,
                    modifier = Modifier.testTag("admin_toggle_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "Admin Verification Panel",
                        tint = if (isAdminActive) GoldStar else PureWhite.copy(alpha = 0.75f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
