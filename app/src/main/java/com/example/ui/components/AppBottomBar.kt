package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.LanguageRegistry
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.RoyalBlueDark
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.MainTab

private data class BottomNavItemData(
    val tab: MainTab,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val labelKey: String,
    val testTag: String,
    val isSpecialAi: Boolean = false
)

private val AiGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF2563EB), // Royal Blue
        Color(0xFF7C3AED), // Indigo / Violet
        Color(0xFFDB2777)  // Rose / Pink
    )
)

private val AiContainerGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFFEFF6FF),
        Color(0xFFF3E8FF),
        Color(0xFFFDF2F8)
    )
)

private val ActiveTabGradient = Brush.horizontalGradient(
    colors = listOf(
        RoyalBlueDark,
        RoyalBlue
    )
)

/**
 * Modern 2026 Floating Island Pill Navigation Bar
 * Features:
 * - Floating dock capsule design elevated from system bottom
 * - Active pill morphing with spring bounce animation
 * - Top luminous indicator dot/bar
 * - Special iridescent styling for AI Mentor
 * - Strict WindowInsets navigationBars padding for zero cut-off
 */
@Composable
fun AppBottomBar(
    currentTab: MainTab,
    language: AppLanguage,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = remember {
        listOf(
            BottomNavItemData(
                tab = MainTab.HOME,
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                labelKey = "tab_home",
                testTag = "nav_home"
            ),
            BottomNavItemData(
                tab = MainTab.JOBS,
                selectedIcon = Icons.Filled.Work,
                unselectedIcon = Icons.Outlined.WorkOutline,
                labelKey = "tab_jobs",
                testTag = "nav_jobs"
            ),
            BottomNavItemData(
                tab = MainTab.RESULTS,
                selectedIcon = Icons.AutoMirrored.Filled.Assignment,
                unselectedIcon = Icons.AutoMirrored.Outlined.Assignment,
                labelKey = "tab_results",
                testTag = "nav_results"
            ),
            BottomNavItemData(
                tab = MainTab.AI_MENTOR,
                selectedIcon = Icons.Filled.AutoAwesome,
                unselectedIcon = Icons.Outlined.AutoAwesome,
                labelKey = "tab_ai_mentor",
                testTag = "nav_ai_mentor",
                isSpecialAi = true
            ),
            BottomNavItemData(
                tab = MainTab.PROFILE,
                selectedIcon = Icons.Filled.Person,
                unselectedIcon = Icons.Outlined.Person,
                labelKey = "tab_profile",
                testTag = "nav_profile"
            )
        )
    }

    // Full-bleed Surface so pure white background extends all the way to the physical bottom edge
    Surface(
        color = PureWhite,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        shadowElevation = 16.dp,
        border = BorderStroke(1.dp, Slate200.copy(alpha = 0.9f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(66.dp)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = currentTab == item.tab

                    // Spring animation for icon scale
                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.14f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "scale_${item.tab}"
                    )

                    // Spring vertical offset bounce for selected icon
                    val iconOffsetY by animateDpAsState(
                        targetValue = if (isSelected) (-2).dp else 0.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "offsetY_${item.tab}"
                    )

                    // Top indicator bar width
                    val indicatorWidth by animateDpAsState(
                        targetValue = if (isSelected) 20.dp else 0.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "indicator_${item.tab}"
                    )

                    // Animated label color with crisp high-contrast active state
                    val labelColor by animateColorAsState(
                        targetValue = when {
                            isSelected && item.isSpecialAi -> Color(0xFF6D28D9)
                            isSelected -> RoyalBlueDark
                            else -> Slate500
                        },
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "labelColor_${item.tab}"
                    )

                    // Nav Item Box with minimum touch target size
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(20.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(
                                    bounded = true,
                                    radius = 32.dp,
                                    color = if (item.isSpecialAi) Color(0xFF7C3AED).copy(alpha = 0.2f) else RoyalBlue.copy(alpha = 0.2f)
                                ),
                                onClick = { onTabSelected(item.tab) }
                            )
                            .testTag(item.testTag),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(vertical = 4.dp)
                        ) {
                            // Top glowing luminous active bar
                            Box(
                                modifier = Modifier
                                    .height(3.dp)
                                    .width(indicatorWidth)
                                    .clip(CircleShape)
                                    .background(
                                        if (item.isSpecialAi) AiGradient
                                        else ActiveTabGradient
                                    )
                            )

                            // Icon Container with active pill morphing
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .offset(y = iconOffsetY)
                                    .height(32.dp)
                                    .width(if (isSelected) 48.dp else 36.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .then(
                                        if (isSelected) {
                                            if (item.isSpecialAi) {
                                                Modifier
                                                    .background(AiContainerGradient)
                                                    .border(
                                                        width = 1.dp,
                                                        color = Color(0xFF7C3AED).copy(alpha = 0.35f),
                                                        shape = RoundedCornerShape(16.dp)
                                                    )
                                            } else {
                                                Modifier
                                                    .background(Color(0xFFEFF6FF))
                                                    .border(
                                                        width = 1.dp,
                                                        color = RoyalBlue.copy(alpha = 0.3f),
                                                        shape = RoundedCornerShape(16.dp)
                                                    )
                                            }
                                        } else {
                                            Modifier
                                        }
                                    )
                            ) {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = LanguageRegistry.getString(item.labelKey, language),
                                    tint = when {
                                        isSelected && item.isSpecialAi -> Color(0xFF6D28D9)
                                        isSelected -> RoyalBlueDark
                                        item.isSpecialAi -> Color(0xFF8B5CF6)
                                        else -> Slate400
                                    },
                                    modifier = Modifier
                                        .size(22.dp)
                                        .scale(iconScale)
                                )

                                // Distinctive "AI" micro-badge on AI Mentor
                                if (item.isSpecialAi) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 5.dp, y = (-3).dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(AiGradient)
                                            .padding(horizontal = 3.dp, vertical = 0.5.dp)
                                    ) {
                                        Text(
                                            text = "AI",
                                            fontSize = 7.5.sp,
                                            fontWeight = FontWeight.Black,
                                            color = PureWhite,
                                            lineHeight = 8.sp
                                        )
                                    }
                                }
                            }

                            // Tab Label
                            Text(
                                text = LanguageRegistry.getString(item.labelKey, language),
                                fontSize = 10.5.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                                color = labelColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
