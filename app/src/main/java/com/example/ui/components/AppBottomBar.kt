package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.LanguageRegistry
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.RoyalBlueContainer
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.viewmodel.MainTab

@Composable
fun AppBottomBar(
    currentTab: MainTab,
    language: AppLanguage,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = PureWhite,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        shadowElevation = 12.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200.copy(alpha = 0.6f)),
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        NavigationBar(
            containerColor = PureWhite,
            tonalElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
        ) {
            // 1. Home
            val isHome = currentTab == MainTab.HOME
            NavigationBarItem(
                selected = isHome,
                onClick = { onTabSelected(MainTab.HOME) },
                icon = {
                    Icon(
                        imageVector = if (isHome) Icons.Filled.Home else Icons.Outlined.Home,
                        contentDescription = "Home"
                    )
                },
                label = {
                    Text(
                        text = LanguageRegistry.getString("tab_home", language),
                        fontSize = 11.sp,
                        fontWeight = if (isHome) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = RoyalBlue,
                    selectedTextColor = RoyalBlue,
                    indicatorColor = RoyalBlueContainer,
                    unselectedIconColor = Slate400,
                    unselectedTextColor = Slate400
                ),
                modifier = Modifier.testTag("nav_home")
            )

            // 2. Latest Jobs
            val isJobs = currentTab == MainTab.JOBS
            NavigationBarItem(
                selected = isJobs,
                onClick = { onTabSelected(MainTab.JOBS) },
                icon = {
                    Icon(
                        imageVector = if (isJobs) Icons.Filled.Work else Icons.Outlined.WorkOutline,
                        contentDescription = "Jobs"
                    )
                },
                label = {
                    Text(
                        text = LanguageRegistry.getString("tab_jobs", language),
                        fontSize = 11.sp,
                        fontWeight = if (isJobs) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = RoyalBlue,
                    selectedTextColor = RoyalBlue,
                    indicatorColor = RoyalBlueContainer,
                    unselectedIconColor = Slate400,
                    unselectedTextColor = Slate400
                ),
                modifier = Modifier.testTag("nav_jobs")
            )

            // 3. Results & Cards
            val isResults = currentTab == MainTab.RESULTS
            NavigationBarItem(
                selected = isResults,
                onClick = { onTabSelected(MainTab.RESULTS) },
                icon = {
                    Icon(
                        imageVector = if (isResults) Icons.AutoMirrored.Filled.Assignment else Icons.AutoMirrored.Outlined.Assignment,
                        contentDescription = "Results"
                    )
                },
                label = {
                    Text(
                        text = LanguageRegistry.getString("tab_results", language),
                        fontSize = 11.sp,
                        fontWeight = if (isResults) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = RoyalBlue,
                    selectedTextColor = RoyalBlue,
                    indicatorColor = RoyalBlueContainer,
                    unselectedIconColor = Slate400,
                    unselectedTextColor = Slate400
                ),
                modifier = Modifier.testTag("nav_results")
            )

            // 4. AI Mentor (Gemini 3.1 Pro High Thinking)
            val isAiMentor = currentTab == MainTab.AI_MENTOR
            NavigationBarItem(
                selected = isAiMentor,
                onClick = { onTabSelected(MainTab.AI_MENTOR) },
                icon = {
                    Icon(
                        imageVector = if (isAiMentor) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome,
                        contentDescription = "AI Mentor"
                    )
                },
                label = {
                    Text(
                        text = LanguageRegistry.getString("tab_ai_mentor", language),
                        fontSize = 11.sp,
                        fontWeight = if (isAiMentor) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = RoyalBlue,
                    selectedTextColor = RoyalBlue,
                    indicatorColor = RoyalBlueContainer,
                    unselectedIconColor = Slate400,
                    unselectedTextColor = Slate400
                ),
                modifier = Modifier.testTag("nav_ai_mentor")
            )

            // 5. Profile & Saved
            val isProfile = currentTab == MainTab.PROFILE
            NavigationBarItem(
                selected = isProfile,
                onClick = { onTabSelected(MainTab.PROFILE) },
                icon = {
                    Icon(
                        imageVector = if (isProfile) Icons.Filled.Person else Icons.Outlined.Person,
                        contentDescription = "Profile"
                    )
                },
                label = {
                    Text(
                        text = LanguageRegistry.getString("tab_profile", language),
                        fontSize = 11.sp,
                        fontWeight = if (isProfile) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = RoyalBlue,
                    selectedTextColor = RoyalBlue,
                    indicatorColor = RoyalBlueContainer,
                    unselectedIconColor = Slate400,
                    unselectedTextColor = Slate400
                ),
                modifier = Modifier.testTag("nav_profile")
            )
        }
    }
}
