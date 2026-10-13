package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.CasteCategory
import com.example.model.EducationLevel
import com.example.model.IndianStatesRegistry
import com.example.model.JobEntity
import com.example.model.LanguageRegistry
import com.example.model.UserEligibilityProfile
import com.example.ui.components.JobCard
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyContainer
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SaffronOrange
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userProfile: UserEligibilityProfile,
    onUpdateProfile: (UserEligibilityProfile) -> Unit,
    bookmarkedJobs: List<JobEntity>,
    language: AppLanguage,
    onOpenLanguageSheet: () -> Unit,
    onJobClick: (JobEntity) -> Unit,
    onBookmarkToggle: (JobEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Eligibility Criteria", "Saved Jobs (${bookmarkedJobs.size})", "App Settings")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Top User Header
        Surface(
            color = PureWhite,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(NavyContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Candidate Profile",
                        tint = DeepNavy,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Candidate Profile & Preferences",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    )
                    Text(
                        text = "${userProfile.category.displayName} • ${userProfile.homeState}",
                        fontSize = 12.sp,
                        color = Slate600
                    )
                }
            }
        }

        // Sub Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = PureWhite,
            contentColor = DeepNavy,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = DeepNavy,
                    height = 3.dp
                )
            }
        ) {
            tabs.forEachIndexed { index, tabTitle ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = tabTitle,
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    modifier = Modifier.testTag("profile_tab_$index")
                )
            }
        }

        when (selectedTab) {
            // 0: Eligibility Criteria Settings
            0 -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                        .padding(bottom = 80.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Slate200),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Personal & Reservation Details",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            Text(
                                text = "Used by AI Mentor to compute exact reservation relaxations",
                                fontSize = 11.sp,
                                color = Slate500
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Age Selector
                            Text(text = "Candidate Age: ${userProfile.age} Years", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Slate800)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(18, 21, 23, 26, 28, 30, 32, 35).forEach { ageVal ->
                                    val isSelected = userProfile.age == ageVal
                                    Surface(
                                        onClick = { onUpdateProfile(userProfile.copy(age = ageVal)) },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) DeepNavy else Slate100,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "$ageVal",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) PureWhite else Slate800
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Category Selector
                            Text(text = "Caste & Reservation Category", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Slate800)
                            Spacer(modifier = Modifier.height(6.dp))
                            CasteCategory.entries.forEach { cat ->
                                val isSelected = userProfile.category == cat
                                Surface(
                                    onClick = { onUpdateProfile(userProfile.copy(category = cat)) },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) NavyContainer else Slate100,
                                    border = BorderStroke(1.dp, if (isSelected) DeepNavy else Color.Transparent),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = cat.displayName,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) DeepNavy else Slate800
                                        )
                                        if (cat.standardRelaxationYears > 0) {
                                            Text(
                                                text = "+${cat.standardRelaxationYears} Yrs Age",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SaffronOrange
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Educational Qualification
                            Text(text = "Highest Qualification", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Slate800)
                            Spacer(modifier = Modifier.height(6.dp))
                            EducationLevel.entries.forEach { edu ->
                                val isSelected = userProfile.qualification == edu
                                Surface(
                                    onClick = { onUpdateProfile(userProfile.copy(qualification = edu)) },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) NavyContainer else Slate100,
                                    border = BorderStroke(1.dp, if (isSelected) DeepNavy else Color.Transparent),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                ) {
                                    Text(
                                        text = edu.displayName,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) DeepNavy else Slate800,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Specific Degree Input
                            Text(text = "Specific Degree / Branch", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Slate800)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = userProfile.specificDegree,
                                onValueChange = { onUpdateProfile(userProfile.copy(specificDegree = it)) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Slate100,
                                    unfocusedContainerColor = Slate100,
                                    focusedBorderColor = DeepNavy,
                                    unfocusedBorderColor = Slate200
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Domicile State
                            Text(text = "Home State (Domicile)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Slate800)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("Uttar Pradesh", "Bihar", "Rajasthan", "Madhya Pradesh", "Maharashtra").forEach { st ->
                                    val isSelected = userProfile.homeState == st
                                    Surface(
                                        onClick = { onUpdateProfile(userProfile.copy(homeState = st)) },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) DeepNavy else Slate100,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = st.take(6),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) PureWhite else Slate800,
                                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 1: Saved Jobs
            1 -> {
                if (bookmarkedJobs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(NavyContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = null,
                                    tint = DeepNavy,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No Saved Jobs Yet",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Bookmark recruitments to track upcoming deadlines offline.",
                                fontSize = 12.sp,
                                color = Slate500
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                            .padding(bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Deadline Alerts Active Banner
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = EmeraldGreen.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldGreen.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AlarmOn,
                                        contentDescription = null,
                                        tint = EmeraldGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Deadline Alerts Active (${bookmarkedJobs.size} Jobs)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldGreen
                                    )
                                    Text(
                                        text = "Automatic reminders will notify you 3 days and 24h prior to closing dates.",
                                        fontSize = 11.sp,
                                        color = Slate600
                                    )
                                }
                            }
                        }

                        bookmarkedJobs.forEach { job ->
                            JobCard(
                                job = job,
                                onClick = { onJobClick(job) },
                                onBookmarkToggle = { onBookmarkToggle(job) }
                            )
                        }
                    }
                }
            }

            // 2: App Settings & Language
            2 -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                        .padding(bottom = 80.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Slate200),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "App Preferences",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Language Setting
                            Surface(
                                onClick = onOpenLanguageSheet,
                                shape = RoundedCornerShape(12.dp),
                                color = Slate100,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Language,
                                            contentDescription = null,
                                            tint = DeepNavy,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Display Language",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Slate900
                                            )
                                            Text(
                                                text = "${language.nativeName} (${language.englishName})",
                                                fontSize = 11.sp,
                                                color = SaffronOrange
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "Open",
                                        tint = Slate500
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Notification Toggles
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Deadline Reminder Alerts",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Slate800
                                    )
                                    Text(
                                        text = "Receive notifications 5 days and 1 day prior to application closure",
                                        fontSize = 11.sp,
                                        color = Slate500
                                    )
                                }
                                Switch(
                                    checked = userProfile.enableDeadlineAlerts,
                                    onCheckedChange = { onUpdateProfile(userProfile.copy(enableDeadlineAlerts = it)) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = PureWhite, checkedTrackColor = SaffronOrange)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Slate100)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "New Vacancy Alerts",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Slate800
                                    )
                                    Text(
                                        text = "Receive notifications when new UPSC, SSC, RRB recruitments are verified",
                                        fontSize = 11.sp,
                                        color = Slate500
                                    )
                                }
                                Switch(
                                    checked = userProfile.enableNewJobAlerts,
                                    onCheckedChange = { onUpdateProfile(userProfile.copy(enableNewJobAlerts = it)) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = PureWhite, checkedTrackColor = SaffronOrange)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Statutory Disclaimer
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate100),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Slate200),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Slate700,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Statutory Public Service Notice",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate800
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "This application is an independent career information portal and is NOT affiliated with, authorized, or endorsed by any Government agency or authority. All recruitment data is retrieved from official government gazettes, UPSC, SSC, and state recruiting commissions.",
                                fontSize = 11.sp,
                                color = Slate600,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
