package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import com.example.data.repository.BackendSyncState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.CasteCategory
import com.example.model.EducationLevel
import com.example.model.IndianStatesRegistry
import com.example.model.JobEntity
import com.example.model.UserEligibilityProfile
import com.example.ui.components.JobCard
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldMint
import com.example.ui.theme.NavyContainer
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.RoyalBlueContainer
import com.example.ui.theme.SaffronContainer
import com.example.ui.theme.SaffronOrange
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

private val PopularProfileStates = listOf(
    "All India",
    "Uttar Pradesh",
    "Bihar",
    "Rajasthan",
    "Madhya Pradesh",
    "Delhi (NCT)",
    "Maharashtra",
    "West Bengal",
    "Odisha",
    "Haryana",
    "Punjab",
    "Jharkhand"
)

private val QuickAgeOptions = listOf(18, 20, 21, 23, 25, 28, 30, 32, 35, 38)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    userProfile: UserEligibilityProfile,
    onUpdateProfile: (UserEligibilityProfile) -> Unit,
    bookmarkedJobs: List<JobEntity>,
    language: AppLanguage,
    onOpenLanguageSheet: () -> Unit,
    onJobClick: (JobEntity) -> Unit,
    onBookmarkToggle: (JobEntity) -> Unit,
    onOpenJobAlerts: () -> Unit = {},
    onNavigateToJobs: (() -> Unit)? = null,
    backendSyncState: BackendSyncState = BackendSyncState.Idle,
    onRefreshData: () -> Unit = {},
    allJobsCount: Int = 0,
    resultsCount: Int = 0,
    admitCardsCount: Int = 0,
    syllabiCount: Int = 0,
    admissionsCount: Int = 0,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val profileTabs = listOf(
        "Eligibility Criteria",
        "Saved Jobs (${bookmarkedJobs.size})",
        "Preferences & Alerts"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // 1. Executive Modern Digital Aspirant Passport Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 3.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF070D1E),
                                DeepNavy,
                                Color(0xFF162238)
                            )
                        )
                    )
                    .padding(horizontal = 18.dp, vertical = 20.dp)
            ) {
                Column {
                    // Top identity row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Avatar with gradient halo
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                RoyalBlue,
                                                Color(0xFF60A5FA),
                                                Color(0xFF818CF8)
                                            )
                                        )
                                    )
                                    .padding(2.5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(DeepNavy),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Candidate Avatar",
                                        tint = PureWhite,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Aspirant Profile",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = PureWhite
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified",
                                        tint = EmeraldMint,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = "Government Job Candidate ID",
                                    fontSize = 11.sp,
                                    color = PureWhite.copy(alpha = 0.7f)
                                )
                            }
                        }

                        // Status Pill
                        Surface(
                            color = EmeraldGreen.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, EmeraldMint.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldMint)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Active Profile",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldMint
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Candidate Attribute Pills (Category, State, Education)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = PureWhite.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "⚖️ ${userProfile.category.displayName}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PureWhite,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            color = PureWhite.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "📍 ${userProfile.homeState}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PureWhite,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            color = PureWhite.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "🎓 ${userProfile.qualification.displayName}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PureWhite,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4-Card Quick Metrics Glass Dashboard Strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. Saved Jobs
                        Surface(
                            onClick = { selectedTab = 1 },
                            shape = RoundedCornerShape(12.dp),
                            color = PureWhite.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.14f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${bookmarkedJobs.size}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PureWhite
                                )
                                Text(
                                    text = "Saved Jobs",
                                    fontSize = 10.sp,
                                    color = PureWhite.copy(alpha = 0.75f)
                                )
                            }
                        }

                        // 2. Targeted Alerts
                        Surface(
                            onClick = onOpenJobAlerts,
                            shape = RoundedCornerShape(12.dp),
                            color = PureWhite.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.14f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(EmeraldMint)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Alerts",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = EmeraldMint
                                    )
                                }
                                Text(
                                    text = "Targeted",
                                    fontSize = 10.sp,
                                    color = PureWhite.copy(alpha = 0.75f)
                                )
                            }
                        }

                        // 3. Candidate Age
                        Surface(
                            onClick = { selectedTab = 0 },
                            shape = RoundedCornerShape(12.dp),
                            color = PureWhite.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.14f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${userProfile.age} Yrs",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PureWhite
                                )
                                Text(
                                    text = "Age",
                                    fontSize = 10.sp,
                                    color = PureWhite.copy(alpha = 0.75f)
                                )
                            }
                        }

                        // 4. Age Relaxation Benefit
                        Surface(
                            onClick = { selectedTab = 0 },
                            shape = RoundedCornerShape(12.dp),
                            color = PureWhite.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.14f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (userProfile.category.standardRelaxationYears > 0) "+${userProfile.category.standardRelaxationYears}y" else "UR",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SaffronOrange
                                )
                                Text(
                                    text = "Quota",
                                    fontSize = 10.sp,
                                    color = PureWhite.copy(alpha = 0.75f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Modern Segmented Pill Selector (High-End Material 3 Navigation)
        Surface(
            color = PureWhite,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                profileTabs.forEachIndexed { index, tabTitle ->
                    val isSelected = selectedTab == index
                    Surface(
                        onClick = { selectedTab = index },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) DeepNavy else Color(0xFFF1F5F9),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) DeepNavy else Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("profile_tab_$index")
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (index) {
                                    0 -> "📋 Eligibility"
                                    1 -> "🔖 Saved (${bookmarkedJobs.size})"
                                    else -> "⚙️ Settings"
                                },
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) PureWhite else Slate700,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // 3. Tab Contents
        when (selectedTab) {
            // TAB 0: Interactive Eligibility & Quota Studio
            0 -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                        .padding(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Card A: Interactive Age & Cut-off Readiness
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, Slate200),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Candidate Age & Eligibility",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                    Text(
                                        text = "Cut-off calculated as of 01-08-2026",
                                        fontSize = 11.sp,
                                        color = Slate500
                                    )
                                }

                                Surface(
                                    color = Color(0xFFEFF6FF),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "Official Rules",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RoyalBlue,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Interactive Stepper Control
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        if (userProfile.age > 18) {
                                            onUpdateProfile(userProfile.copy(age = userProfile.age - 1))
                                        }
                                    },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Slate100)
                                        .testTag("btn_age_decrement")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Decrease Age",
                                        tint = Slate800
                                    )
                                }

                                Spacer(modifier = Modifier.width(24.dp))

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${userProfile.age}",
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Black,
                                        color = DeepNavy
                                    )
                                    Text(
                                        text = "Years Old",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate500
                                    )
                                }

                                Spacer(modifier = Modifier.width(24.dp))

                                IconButton(
                                    onClick = {
                                        if (userProfile.age < 45) {
                                            onUpdateProfile(userProfile.copy(age = userProfile.age + 1))
                                        }
                                    },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(DeepNavy)
                                        .testTag("btn_age_increment")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Increase Age",
                                        tint = PureWhite
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Quick Age Chips Selector
                            Text(
                                text = "Quick Select:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate500
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                QuickAgeOptions.forEach { ageVal ->
                                    val isSelected = userProfile.age == ageVal
                                    Surface(
                                        onClick = { onUpdateProfile(userProfile.copy(age = ageVal)) },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) RoyalBlue else Slate100,
                                        modifier = Modifier.testTag("chip_age_$ageVal")
                                    ) {
                                        Text(
                                            text = "$ageVal",
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) PureWhite else Slate800,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Live Eligibility Insight Banner
                            val maxLimit = 32 + userProfile.category.standardRelaxationYears + (if (userProfile.isPwD) 10 else 0)
                            Surface(
                                color = Color(0xFFF0FDF4),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = EmeraldGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Maximum Upper Age Limit: $maxLimit Years",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldGreen
                                        )
                                        Text(
                                            text = if (userProfile.age <= maxLimit) {
                                                "✓ Eligible for UPSC CSE, SSC CGL, RRB NTPC, State PSC & Central Defence Openings."
                                            } else {
                                                "⚠️ Candidate age exceeds general limits for several standard posts."
                                            },
                                            fontSize = 11.sp,
                                            color = Slate700
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Card B: Reservation & Caste Category (Visual Grid)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, Slate200),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Caste & Reservation Category",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                    Text(
                                        text = "Determines fee exemptions & age relaxations",
                                        fontSize = 11.sp,
                                        color = Slate500
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Badge,
                                    contentDescription = null,
                                    tint = DeepNavy,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            CasteCategory.entries.forEach { cat ->
                                val isSelected = userProfile.category == cat
                                Surface(
                                    onClick = { onUpdateProfile(userProfile.copy(category = cat)) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) NavyContainer else PureWhite,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) DeepNavy else Slate200
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .testTag("category_option_${cat.name}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) DeepNavy else Slate200),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = PureWhite,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = cat.displayName,
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) DeepNavy else Slate800
                                            )
                                        }

                                        if (cat.standardRelaxationYears > 0) {
                                            Surface(
                                                color = if (isSelected) SaffronContainer else Slate100,
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "+${cat.standardRelaxationYears} Years",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SaffronOrange,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Card C: Educational Qualification & Stream
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, Slate200),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Highest Qualification",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                    Text(
                                        text = "AI Mentor checks posts matching this tier",
                                        fontSize = 11.sp,
                                        color = Slate500
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = RoyalBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                EducationLevel.entries.forEach { edu ->
                                    val isSelected = userProfile.qualification == edu
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { onUpdateProfile(userProfile.copy(qualification = edu)) },
                                        label = {
                                            Text(
                                                text = edu.displayName,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = DeepNavy,
                                            selectedLabelColor = PureWhite
                                        ),
                                        modifier = Modifier.testTag("edu_chip_${edu.name}")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Specific Degree Input Field
                            Text(
                                text = "Specific Degree / Branch Stream",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate800
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = userProfile.specificDegree,
                                onValueChange = { onUpdateProfile(userProfile.copy(specificDegree = it)) },
                                singleLine = true,
                                placeholder = { Text("e.g. B.Tech Computer Science, B.Sc Physics, B.Com", fontSize = 12.sp) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Slate100,
                                    unfocusedContainerColor = Slate100,
                                    focusedBorderColor = DeepNavy,
                                    unfocusedBorderColor = Slate200
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_specific_degree")
                            )
                        }
                    }

                    // Card D: Home State & Domicile
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, Slate200),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Domicile / Home State",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                    Text(
                                        text = "Enables 100% reservation in state vacancies",
                                        fontSize = 11.sp,
                                        color = Slate500
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = SaffronOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                PopularProfileStates.forEach { st ->
                                    val isSelected = userProfile.homeState == st
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { onUpdateProfile(userProfile.copy(homeState = st)) },
                                        label = {
                                            Text(
                                                text = st,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = RoyalBlue,
                                            selectedLabelColor = PureWhite
                                        ),
                                        modifier = Modifier.testTag("state_chip_$st")
                                    )
                                }
                            }
                        }
                    }

                    // Card E: Special Quota & Concessions
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, Slate200),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "Special Quotas & Fee Concessions",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            Text(
                                text = "Checked by AI Mentor for fee exemption & extra age relaxation",
                                fontSize = 11.sp,
                                color = Slate500
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // 1. PwD
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Accessibility,
                                        contentDescription = null,
                                        tint = RoyalBlue,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "PwD (Benchmark Disabilities)",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Slate900
                                        )
                                        Text(
                                            text = "+10 to +15 Years Age Relaxation",
                                            fontSize = 11.sp,
                                            color = Slate500
                                        )
                                    }
                                }
                                Switch(
                                    checked = userProfile.isPwD,
                                    onCheckedChange = { onUpdateProfile(userProfile.copy(isPwD = it)) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = PureWhite,
                                        checkedTrackColor = DeepNavy
                                    ),
                                    modifier = Modifier.testTag("switch_pwd")
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Slate100)
                            Spacer(modifier = Modifier.height(10.dp))

                            // 2. Ex-Servicemen
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = EmeraldGreen,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Ex-Servicemen (ESM Quota)",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Slate900
                                        )
                                        Text(
                                            text = "Armed forces service deduction relaxation",
                                            fontSize = 11.sp,
                                            color = Slate500
                                        )
                                    }
                                }
                                Switch(
                                    checked = userProfile.isExServicemen,
                                    onCheckedChange = { onUpdateProfile(userProfile.copy(isExServicemen = it)) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = PureWhite,
                                        checkedTrackColor = DeepNavy
                                    ),
                                    modifier = Modifier.testTag("switch_esm")
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Slate100)
                            Spacer(modifier = Modifier.height(10.dp))

                            // 3. Women Candidate Quota
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Female,
                                        contentDescription = null,
                                        tint = SaffronOrange,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Female Candidate Quota",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Slate900
                                        )
                                        Text(
                                            text = "100% Fee Exemption in SSC & UPSC Exams",
                                            fontSize = 11.sp,
                                            color = Slate500
                                        )
                                    }
                                }
                                Switch(
                                    checked = userProfile.isWomenCandidate,
                                    onCheckedChange = { onUpdateProfile(userProfile.copy(isWomenCandidate = it)) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = PureWhite,
                                        checkedTrackColor = DeepNavy
                                    ),
                                    modifier = Modifier.testTag("switch_women")
                                )
                            }
                        }
                    }
                }
            }

            // TAB 1: Saved Jobs & Deadline Reminders
            1 -> {
                if (bookmarkedJobs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Slate200),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEFF6FF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bookmark,
                                        contentDescription = null,
                                        tint = RoyalBlue,
                                        modifier = Modifier.size(34.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "No Saved Openings Yet",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Slate900
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Bookmark government recruitments to automatically schedule 3-day and 24-hour application closing deadline alerts.",
                                    fontSize = 12.sp,
                                    color = Slate600,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 17.sp
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                if (onNavigateToJobs != null) {
                                    Button(
                                        onClick = onNavigateToJobs,
                                        colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.testTag("btn_explore_jobs_from_bookmarks")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null,
                                            tint = PureWhite,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Explore Active Openings",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PureWhite
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                            .padding(bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Deadline Alerts Active Banner
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFF0FDF4),
                            border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFDCFCE7)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AlarmOn,
                                        contentDescription = null,
                                        tint = EmeraldGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Deadline Alerts Active (${bookmarkedJobs.size} Saved)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldGreen
                                    )
                                    Text(
                                        text = "3-day & 24h reminders scheduled for each saved opening.",
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

            // TAB 2: Preferences, Custom Alerts & System Settings
            2 -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                        .padding(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Feature Highlight: Targeted Custom Job Alert Preferences Card
                    Card(
                        onClick = onOpenJobAlerts,
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_btn_targeted_alerts")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            Color(0xFFEFF6FF),
                                            Color(0xFFDBEAFE)
                                        )
                                    )
                                )
                                .padding(18.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(DeepNavy),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Tune,
                                                contentDescription = null,
                                                tint = PureWhite,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "Targeted Job Alerts",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = DeepNavy
                                            )
                                            Text(
                                                text = "Multi-Criteria Alert Engine",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = RoyalBlue
                                            )
                                        }
                                    }

                                    Surface(
                                        color = EmeraldGreen,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "Tune",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PureWhite,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "Filter alerts strictly by your target state, degree qualification, preferred departments (SSC, Railways, Banking, Police) & minimum vacancies.",
                                    fontSize = 12.sp,
                                    color = Slate700,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    // Display Language Settings
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, Slate200),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "App Preferences",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Surface(
                                onClick = onOpenLanguageSheet,
                                shape = RoundedCornerShape(12.dp),
                                color = Slate100,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("profile_btn_language")
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
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
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
                                                color = SaffronOrange,
                                                fontWeight = FontWeight.SemiBold
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

                            // Deadline notification toggle
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
                                        color = Slate900
                                    )
                                    Text(
                                        text = "Receive notifications 3 days and 24h prior to closing",
                                        fontSize = 11.sp,
                                        color = Slate500
                                    )
                                }
                                Switch(
                                    checked = userProfile.enableDeadlineAlerts,
                                    onCheckedChange = { onUpdateProfile(userProfile.copy(enableDeadlineAlerts = it)) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = PureWhite,
                                        checkedTrackColor = DeepNavy
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Slate100)
                            Spacer(modifier = Modifier.height(10.dp))

                            // New openings alert toggle
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
                                        color = Slate900
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
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = PureWhite,
                                        checkedTrackColor = DeepNavy
                                    )
                                )
                            }
                        }
                    }

                    // Modern Backend API & Offline Dual Caching Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_backend_api_card")
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(Color(0xFF0284C7), RoyalBlue)
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudSync,
                                            contentDescription = "Backend API Sync",
                                            tint = PureWhite,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Backend API & Dual Caching",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Slate900
                                        )
                                        Text(
                                            text = "Edge Worker & SQLite Room Layer",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = RoyalBlue
                                        )
                                    }
                                }

                                Surface(
                                    color = when (backendSyncState) {
                                        is BackendSyncState.Syncing -> SaffronContainer
                                        is BackendSyncState.Success -> Color(0xFFDCFCE7)
                                        else -> Slate100
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when (backendSyncState) {
                                                        is BackendSyncState.Syncing -> SaffronOrange
                                                        is BackendSyncState.Success -> EmeraldGreen
                                                        else -> Slate500
                                                    }
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = when (backendSyncState) {
                                                is BackendSyncState.Syncing -> "Syncing..."
                                                is BackendSyncState.Success -> "Connected"
                                                is BackendSyncState.OfflineCached -> "Cached Mode"
                                                else -> "Ready"
                                            },
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (backendSyncState) {
                                                is BackendSyncState.Syncing -> SaffronOrange
                                                is BackendSyncState.Success -> EmeraldGreen
                                                else -> Slate700
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Sync state descriptive message
                            val syncMessage = when (backendSyncState) {
                                is BackendSyncState.Syncing -> backendSyncState.currentEntity
                                is BackendSyncState.Success -> backendSyncState.message
                                is BackendSyncState.OfflineCached -> backendSyncState.message
                                else -> "Local SQLite cache synced with official Cloudflare backend API endpoints."
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Slate100,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Storage,
                                        contentDescription = null,
                                        tint = Slate600,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = syncMessage,
                                        fontSize = 11.sp,
                                        color = Slate700,
                                        lineHeight = 15.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Real-time Caching Metrics Grid
                            Text(
                                text = "Local Database Cache Breakdown",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate800
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Jobs metric
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFEFF6FF),
                                    border = BorderStroke(1.dp, Color(0xFFDBEAFE))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(text = "$allJobsCount", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = RoyalBlue)
                                        Text(text = "Jobs", fontSize = 10.sp, color = Slate600)
                                    }
                                }

                                // Results metric
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF0FDF4),
                                    border = BorderStroke(1.dp, Color(0xFFDCFCE7))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(text = "$resultsCount", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = EmeraldGreen)
                                        Text(text = "Results", fontSize = 10.sp, color = Slate600)
                                    }
                                }

                                // Admit Cards metric
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFFFF7ED),
                                    border = BorderStroke(1.dp, Color(0xFFFFEDD5))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(text = "$admitCardsCount", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = SaffronOrange)
                                        Text(text = "Cards", fontSize = 10.sp, color = Slate600)
                                    }
                                }

                                // Syllabus metric
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFFAF5FF),
                                    border = BorderStroke(1.dp, Color(0xFFF3E8FF))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(text = "$syllabiCount", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF7E22CE))
                                        Text(text = "Syllabus", fontSize = 10.sp, color = Slate600)
                                    }
                                }

                                // Admission metric
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFFDF2F8),
                                    border = BorderStroke(1.dp, Color(0xFFFCE7F3))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(text = "$admissionsCount", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFBE185D))
                                        Text(text = "Admission", fontSize = 10.sp, color = Slate600)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Force Refresh API Button
                            Button(
                                onClick = onRefreshData,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DeepNavy,
                                    contentColor = PureWhite
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_sync_all_backend_data")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = PureWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Force Refresh & Sync All Backend Data",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Statutory Disclaimer & Gazette Integrity
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate100),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, Slate200),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Slate600,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Editorial & Gazette Transparency",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate800
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "All recruitment circulars, examination schemes, and syllabus PDFs are parsed strictly from official Government Gazette releases (upsc.gov.in, ssc.gov.in, rrbcdg.gov.in). Always cross-verify final terms on official recruitment portals.",
                                fontSize = 11.sp,
                                color = Slate600,
                                lineHeight = 15.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Sarkari Jobs App v2.4.0 • Built for Indian Aspirants 🇮🇳",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate500
                            )
                        }
                    }
                }
            }
        }
    }
}
