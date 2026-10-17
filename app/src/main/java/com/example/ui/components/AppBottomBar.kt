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
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.RoyalBlueDark
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
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
        Color(0xFFE0E7FF), // Soft Indigo
        Color(0xFFEDE9FE), // Soft Purple
        Color(0xFFFCE7F3)  // Soft Pink
    )
)

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

    Surface(
        color = PureWhite,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        shadowElevation = 16.dp,
        border = BorderStroke(1.dp, Slate200.copy(alpha = 0.85f)),
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentTab == item.tab

                // Smooth spring animation for icon scale
                val iconScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.15f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "iconScale_${item.tab}"
                )

                // Prominent animated active pill width for the bottom dot/bar
                val activeIndicatorWidth by animateDpAsState(
                    targetValue = if (isSelected) 22.dp else 0.dp,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "indicatorWidth_${item.tab}"
                )

                // Animated label color with crisp high-contrast active state
                val textColor by animateColorAsState(
                    targetValue = when {
                        isSelected && item.isSpecialAi -> Color(0xFF6D28D9)
                        isSelected -> RoyalBlueDark
                        else -> Slate500
                    },
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label = "textColor_${item.tab}"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = false, radius = 28.dp),
                            onClick = { onTabSelected(item.tab) }
                        )
                        .testTag(item.testTag),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    ) {
                        // Prominent active pill container around the icon
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .height(34.dp)
                                .width(if (item.isSpecialAi && isSelected) 58.dp else 52.dp)
                                .clip(RoundedCornerShape(17.dp))
                                .then(
                                    if (isSelected) {
                                        if (item.isSpecialAi) {
                                            Modifier
                                                .background(AiContainerGradient)
                                                .border(
                                                    width = 1.5.dp,
                                                    color = Color(0xFF7C3AED).copy(alpha = 0.35f),
                                                    shape = RoundedCornerShape(17.dp)
                                                )
                                        } else {
                                            Modifier
                                                .background(Color(0xFFDBEAFE)) // Rich Royal Blue Container
                                                .border(
                                                    width = 1.5.dp,
                                                    color = RoyalBlue.copy(alpha = 0.35f),
                                                    shape = RoundedCornerShape(17.dp)
                                                )
                                        }
                                    } else {
                                        Modifier
                                    }
                                )
                        ) {
                            // Icon with spring bounce animation
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

                            // Distinctive "AI" badge on AI Mentor
                            if (item.isSpecialAi) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 4.dp, y = (-2).dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(AiGradient)
                                        .padding(horizontal = 3.dp, vertical = 0.5.dp)
                                ) {
                                    Text(
                                        text = "AI",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = PureWhite,
                                        lineHeight = 9.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        // Tab Label with bold active font weight
                        Text(
                            text = LanguageRegistry.getString(item.labelKey, language),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            color = textColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Prominent active colored indicator pill / dot
                        Box(
                            modifier = Modifier
                                .height(4.dp)
                                .width(activeIndicatorWidth)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    if (item.isSpecialAi) AiGradient
                                    else Brush.horizontalGradient(
                                        listOf(
                                            RoyalBlueDark,
                                            RoyalBlue
                                        )
                                    )
                                )
                        )
                    }
                }
            }
        }
    }
}
