package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.NativeSponsoredJobCard
import com.example.model.AlertFrequency
import com.example.model.JobAlertMatcher
import com.example.model.JobAlertPreferences
import com.example.model.JobEntity
import com.example.model.MatchedJob
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldMint
import com.example.ui.theme.NavyContainer
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.RoyalBlueContainer
import com.example.ui.theme.SaffronOrange
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun JobAlertPreferencesScreen(
    currentPreferences: JobAlertPreferences,
    matchedJobs: List<MatchedJob>,
    onSavePreferences: (JobAlertPreferences) -> Unit,
    onSendTestAlert: () -> Unit,
    onJobClick: (JobEntity) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var preferences by remember(currentPreferences) { mutableStateOf(currentPreferences) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Live calculation of matches for the modified preferences in UI
    val liveMatches = remember(preferences, matchedJobs) {
        JobAlertMatcher.matchJobs(matchedJobs.map { it.job }, preferences)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Targeted Job Alert Settings",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                        )
                        Text(
                            text = if (preferences.isEnabled) "${liveMatches.size} Active Openings Match Criteria" else "Alerts Paused",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (preferences.isEnabled) EmeraldMint else PureWhite.copy(alpha = 0.7f)
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("alert_prefs_back_button")
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
                        onClick = {
                            onSendTestAlert()
                            scope.launch {
                                snackbarHostState.showSnackbar("🔔 Test Notification sent to system tray!")
                            }
                        },
                        modifier = Modifier.testTag("btn_test_alert_top")
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Test Notification",
                            tint = PureWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepNavy)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(
                color = PureWhite,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, Slate200),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            onSendTestAlert()
                            scope.launch {
                                snackbarHostState.showSnackbar("🔔 Test Alert delivered to notification panel!")
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_send_test_alert")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test Alert", fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            onSavePreferences(preferences)
                            scope.launch {
                                snackbarHostState.showSnackbar("✓ Alert preferences saved & synchronized!")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("btn_save_alert_prefs")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = null,
                            tint = PureWhite,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Save Preferences",
                            fontWeight = FontWeight.Bold,
                            color = PureWhite,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // 1. Master Toggle & Matching Metric Card
            item(key = "master_switch_card") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, Slate200),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(if (preferences.isEnabled) RoyalBlueContainer else Slate100),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = null,
                                        tint = if (preferences.isEnabled) RoyalBlue else Slate500,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Targeted Job Alerts",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Slate900
                                    )
                                    Text(
                                        text = if (preferences.isEnabled) "Actively filtering for you" else "Turned off",
                                        fontSize = 12.sp,
                                        color = if (preferences.isEnabled) EmeraldGreen else Slate500
                                    )
                                }
                            }

                            Switch(
                                checked = preferences.isEnabled,
                                onCheckedChange = { isChecked ->
                                    preferences = preferences.copy(isEnabled = isChecked)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = PureWhite,
                                    checkedTrackColor = DeepNavy
                                ),
                                modifier = Modifier.testTag("switch_enable_job_alerts")
                            )
                        }

                        AnimatedVisibility(
                            visible = preferences.isEnabled,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height(14.dp))
                                HorizontalDivider(color = Slate200)
                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${liveMatches.size} Matching Openings",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = DeepNavy
                                        )
                                        Text(
                                            text = "Based on your sectors, states & qualifications",
                                            fontSize = 11.sp,
                                            color = Slate600
                                        )
                                    }

                                    Surface(
                                        color = Color(0xFFEFF6FF),
                                        shape = RoundedCornerShape(20.dp),
                                        border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                                    ) {
                                        Text(
                                            text = "🎯 ${preferences.alertFrequency.displayName}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = RoyalBlue,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Exam Boards & Sectors Filter Chips
            item(key = "sectors_section") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, Slate200),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Target Exam Boards & Sectors",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                                Text(
                                    text = "Select agencies you want alerts for",
                                    fontSize = 11.sp,
                                    color = Slate500
                                )
                            }

                            Row {
                                Text(
                                    text = "Select All",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RoyalBlue,
                                    modifier = Modifier
                                        .clickable {
                                            preferences = preferences.copy(
                                                preferredCategories = JobAlertMatcher.AvailableCategories.map { it.first }.toSet()
                                            )
                                        }
                                        .padding(4.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Clear",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate500,
                                    modifier = Modifier
                                        .clickable {
                                            preferences = preferences.copy(preferredCategories = emptySet())
                                        }
                                        .padding(4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            JobAlertMatcher.AvailableCategories.forEach { (catName, icon) ->
                                val isSelected = preferences.preferredCategories.contains(catName)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        val updated = preferences.preferredCategories.toMutableSet()
                                        if (isSelected) updated.remove(catName) else updated.add(catName)
                                        preferences = preferences.copy(preferredCategories = updated)
                                    },
                                    label = {
                                        Text(
                                            text = "$icon $catName",
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    leadingIcon = if (isSelected) {
                                        {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = DeepNavy,
                                        selectedLabelColor = PureWhite,
                                        selectedLeadingIconColor = PureWhite
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 3. Educational Qualifications
            item(key = "qualifications_section") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, Slate200),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Eligible Qualifications",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = "Alert me only for degrees & levels I hold",
                            fontSize = 11.sp,
                            color = Slate500
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            JobAlertMatcher.AvailableQualifications.forEach { qual ->
                                val isSelected = preferences.preferredQualifications.contains(qual)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        val updated = preferences.preferredQualifications.toMutableSet()
                                        if (isSelected) updated.remove(qual) else updated.add(qual)
                                        preferences = preferences.copy(preferredQualifications = updated)
                                    },
                                    label = {
                                        Text(
                                            text = qual,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = RoyalBlue,
                                        selectedLabelColor = PureWhite
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 4. Preferred States & Regions
            item(key = "states_section") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, Slate200),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Target States & Locations",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                                Text(
                                    text = "Includes All India Pan-India central posts",
                                    fontSize = 11.sp,
                                    color = Slate500
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            JobAlertMatcher.AvailableStates.forEach { stateName ->
                                val isSelected = preferences.preferredStates.contains(stateName)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        val updated = preferences.preferredStates.toMutableSet()
                                        if (isSelected) updated.remove(stateName) else updated.add(stateName)
                                        preferences = preferences.copy(preferredStates = updated)
                                    },
                                    label = {
                                        Text(
                                            text = stateName,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = DeepNavy,
                                        selectedLabelColor = PureWhite
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 5. Minimum Vacancies Threshold
            item(key = "vacancies_section") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, Slate200),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Minimum Vacancy Filter",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = "Ignore recruitments with very few seats",
                            fontSize = 11.sp,
                            color = Slate500
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            JobAlertMatcher.VacancyThresholds.forEach { (count, label) ->
                                val isSelected = preferences.minVacancies == count
                                Surface(
                                    onClick = { preferences = preferences.copy(minVacancies = count) },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) DeepNavy else Slate100,
                                    border = BorderStroke(1.dp, if (isSelected) DeepNavy else Slate200),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) PureWhite else Slate800,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 6. Notification Schedule & Timing Channels
            item(key = "delivery_channels_section") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, Slate200),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Delivery Channels & Timing",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Closing soon reminder
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "3-Day & 24h Deadline Alerts",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Slate900
                                )
                                Text(
                                    text = "Remind me before portal closes so I never miss applying",
                                    fontSize = 11.sp,
                                    color = Slate500
                                )
                            }
                            Switch(
                                checked = preferences.alertOnClosingSoon,
                                onCheckedChange = { preferences = preferences.copy(alertOnClosingSoon = it) }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Slate100)
                        Spacer(modifier = Modifier.height(10.dp))

                        // New opening alerts
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "New Matching Vacancies",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Slate900
                                )
                                Text(
                                    text = "Notify when a newly gazetted opening matches my filters",
                                    fontSize = 11.sp,
                                    color = Slate500
                                )
                            }
                            Switch(
                                checked = preferences.alertOnNewOpenings,
                                onCheckedChange = { preferences = preferences.copy(alertOnNewOpenings = it) }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Slate100)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Alert Frequency
                        Text(
                            text = "Notification Frequency",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate900
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        AlertFrequency.entries.forEach { freq ->
                            val isSelected = preferences.alertFrequency == freq
                            Surface(
                                onClick = { preferences = preferences.copy(alertFrequency = freq) },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Color(0xFFEFF6FF) else PureWhite,
                                border = BorderStroke(1.dp, if (isSelected) RoyalBlue else Slate200),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Alarm,
                                        contentDescription = null,
                                        tint = if (isSelected) RoyalBlue else Slate500,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = freq.displayName,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) DeepNavy else Slate800
                                        )
                                        Text(
                                            text = freq.subtitle,
                                            fontSize = 10.sp,
                                            color = Slate500
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Slate100)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Quiet Hours
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Quiet Hours (10 PM – 7 AM)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Slate900
                                )
                                Text(
                                    text = "Silent notifications during sleep hours",
                                    fontSize = 11.sp,
                                    color = Slate500
                                )
                            }
                            Switch(
                                checked = preferences.quietHoursEnabled,
                                onCheckedChange = { preferences = preferences.copy(quietHoursEnabled = it) }
                            )
                        }
                    }
                }
            }

            // 7. Live Matched Jobs Preview
            item(key = "matched_jobs_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Matched Openings (${liveMatches.size})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = "Real-time feed matching your customized alert settings",
                            fontSize = 11.sp,
                            color = Slate500
                        )
                    }
                }
            }

            if (liveMatches.isEmpty()) {
                item(key = "empty_matched_jobs") {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Slate200),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🔍 No jobs match your current strict filters",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate800
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Try selecting 'All India' or adding more sectors like Railways / SSC above.",
                                fontSize = 12.sp,
                                color = Slate500
                            )
                        }
                    }
                }
            } else {
                items(liveMatches, key = { it.job.id }) { matched ->
                    MatchedJobCard(
                        matchedJob = matched,
                        onClick = { onJobClick(matched.job) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                NativeSponsoredJobCard(creativeIndex = 0)
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun MatchedJobCard(
    matchedJob: MatchedJob,
    onClick: () -> Unit
) {
    val job = matchedJob.job

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (matchedJob.isPerfectMatch) Color(0xFF93C5FD) else Slate200),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("matched_job_card_${job.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (matchedJob.isPerfectMatch) Color(0xFFDCFCE7) else Color(0xFFEFF6FF),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, if (matchedJob.isPerfectMatch) Color(0xFF86EFAC) else Color(0xFFBFDBFE))
                ) {
                    Text(
                        text = if (matchedJob.isPerfectMatch) "🎯 ${matchedJob.matchScorePercent}% Top Match" else "⭐ ${matchedJob.matchScorePercent}% Match",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (matchedJob.isPerfectMatch) EmeraldGreen else RoyalBlue,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "${job.totalVacancies} Vacancies",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate700
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = job.organization,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Slate500
            )

            Text(
                text = job.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Match Reasons Pills
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                matchedJob.matchReasons.take(3).forEach { reason ->
                    Surface(
                        color = Slate100,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = reason,
                            fontSize = 10.sp,
                            color = Slate700,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Closes: ${job.applicationClosingDate}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = SaffronOrange
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "View Details",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepNavy
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = DeepNavy,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
