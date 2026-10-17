package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.model.AppLanguage
import com.example.model.IndianStatesRegistry
import com.example.model.JobCategory
import com.example.model.JobEntity
import com.example.model.LanguageRegistry
import com.example.ui.components.JobCard
import com.example.util.AppUtils
import com.example.ui.theme.BlueGradientBrush
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldMint
import com.example.ui.theme.GoldStar
import com.example.ui.theme.GreenContainer
import com.example.ui.theme.HeroGradientBrush
import com.example.ui.theme.NavyContainer
import com.example.ui.theme.OnGreenContainer
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
import com.example.ui.theme.StatusUrgent
import com.example.ui.theme.StatusUrgentBg
import com.example.ui.theme.VerifiedBlue
import com.example.ui.viewmodel.MainTab

data class SarkariPortalPillar(
    val title: String,
    val subtitle: String,
    val iconEmoji: String,
    val containerBg: Color,
    val accentColor: Color,
    val testTag: String,
    val actionType: Int // 0: Latest Jobs, 1: Results, 2: Admit Card, 3: Syllabus, 4: Admission, 5: Answer Key
)

data class JobSectorDef(
    val name: String,
    val iconEmoji: String,
    val subtitle: String,
    val filterKey: String
)

private val SarkariPillarsList = listOf(
    SarkariPortalPillar("Latest Job", "Active Recruitments", "💼", Color(0xFFEFF6FF), Color(0xFF1D4ED8), "pillar_latest_job", 0),
    SarkariPortalPillar("Admit Card", "Exam Hall Tickets", "🎫", Color(0xFFFFF7ED), Color(0xFFC2410C), "pillar_admit_card", 2),
    SarkariPortalPillar("Results", "Cut-off & Merit", "🏆", Color(0xFFF0FDF4), Color(0xFF15803D), "pillar_results", 1),
    SarkariPortalPillar("Syllabus", "Exam Pattern & Marks", "📚", Color(0xFFFAF5FF), Color(0xFF7E22CE), "pillar_syllabus", 3),
    SarkariPortalPillar("Admission", "Colleges & NTA", "🎓", Color(0xFFFDF2F8), Color(0xFFBE185D), "pillar_admission", 4),
    SarkariPortalPillar("Answer Key", "Official Objections", "🔑", Color(0xFFFFFBEB), Color(0xFFB45309), "pillar_answer_key", 5)
)

private val TopJobSectorsList = listOf(
    JobSectorDef("All India Govt Jobs", "🇮🇳", "UPSC, SSC & Central Ministries", "All India Govt Jobs"),
    JobSectorDef("State Govt Jobs", "🏛️", "State PSC, UP, Bihar, MP, Rajasthan", "State Govt Jobs"),
    JobSectorDef("Bank Jobs", "🏦", "IBPS, SBI, RBI & PSBs", "Bank Jobs"),
    JobSectorDef("Teaching Jobs", "🧑‍🏫", "KVS, NVS, CTET, DSSSB & TET", "Teaching Jobs"),
    JobSectorDef("Engineering Jobs", "⚙️", "ISRO, SSC JE, RRB JE & PSUs", "Engineering Jobs"),
    JobSectorDef("Railway Jobs", "🚆", "RRB NTPC, ALP, Group D & RPF", "Railway Jobs"),
    JobSectorDef("Police/Defence Jobs", "🛡️", "Army, Navy, NDA, CDS & Police", "Police/Defence Jobs")
)

private val PopularStates = listOf(
    "All India",
    "Uttar Pradesh",
    "Bihar",
    "Rajasthan",
    "Madhya Pradesh",
    "Maharashtra",
    "Delhi (NCT)",
    "West Bengal",
    "Odisha",
    "Tamil Nadu"
)

@Composable
fun HomeScreen(
    language: AppLanguage,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    closingSoonJobs: List<JobEntity>,
    allJobs: List<JobEntity>,
    onJobClick: (JobEntity) -> Unit,
    onBookmarkToggle: (JobEntity) -> Unit,
    onNavigateTab: (MainTab) -> Unit,
    onFilterByState: (String) -> Unit,
    onFilterByCategory: (String) -> Unit,
    onOpenAgeCalculator: () -> Unit = {},
    onOpenSalaryCalculator: () -> Unit = {},
    onOpenJobAlerts: () -> Unit = {},
    onOpenResultsSubTab: (Int) -> Unit = {},
    matchedJobsCount: Int = 0,
    isJobAlertsEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val topJobs = remember(allJobs) { allJobs.take(8) }
    val sectorCounts = remember(allJobs) {
        TopJobSectorsList.associate { sector ->
            val count = allJobs.count { job ->
                when (sector.name) {
                    "All India Govt Jobs" -> job.stateOrAllIndia.equals("All India", ignoreCase = true) || job.category.equals("UPSC", ignoreCase = true) || job.category.equals("SSC", ignoreCase = true)
                    "State Govt Jobs" -> !job.stateOrAllIndia.equals("All India", ignoreCase = true) || job.category.contains("State", ignoreCase = true)
                    "Bank Jobs" -> job.category.contains("Bank", ignoreCase = true) || job.organization.contains("Bank", ignoreCase = true) || job.organization.contains("IBPS", ignoreCase = true) || job.organization.contains("SBI", ignoreCase = true)
                    "Teaching Jobs" -> job.category.contains("Teach", ignoreCase = true) || job.title.contains("Teacher", ignoreCase = true) || job.title.contains("TET", ignoreCase = true) || job.organization.contains("KVS", ignoreCase = true) || job.organization.contains("DSSSB", ignoreCase = true)
                    "Engineering Jobs" -> job.category.contains("Engin", ignoreCase = true) || job.title.contains("Engineer", ignoreCase = true) || job.title.contains("Scientist", ignoreCase = true) || job.organization.contains("ISRO", ignoreCase = true) || job.title.contains("JE", ignoreCase = true)
                    "Railway Jobs" -> job.category.contains("Railway", ignoreCase = true) || job.organization.contains("Railway", ignoreCase = true) || job.organization.contains("RRB", ignoreCase = true)
                    "Police/Defence Jobs" -> job.category.contains("Police", ignoreCase = true) || job.category.contains("Defence", ignoreCase = true) || job.title.contains("Police", ignoreCase = true) || job.title.contains("Army", ignoreCase = true) || job.title.contains("NDA", ignoreCase = true) || job.title.contains("Constable", ignoreCase = true)
                    else -> job.category.contains(sector.name, ignoreCase = true)
                }
            }
            sector.name to count
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // 1. Modern Hero Section with Search Field
        item(key = "hero_section") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HeroGradientBrush)
                    .padding(horizontal = 16.dp, vertical = 18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Find Your Dream Government Career",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp,
                                    color = PureWhite
                                )
                            )
                            Text(
                                text = "Real-time verified recruitment notices across India",
                                fontSize = 12.sp,
                                color = PureWhite.copy(alpha = 0.8f),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Modern Pill Search Bar inside Hero
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = {
                            Text(
                                text = LanguageRegistry.getString("search_hint", language),
                                fontSize = 13.sp,
                                color = Slate400
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = RoyalBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear Search",
                                        tint = Slate500,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = PureWhite,
                            unfocusedContainerColor = PureWhite,
                            focusedBorderColor = RoyalBlue,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Slate900,
                            unfocusedTextColor = Slate900
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_search_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Hero Quick Metric Pills (Vacancies, Official, Updates)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            color = PureWhite.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "48,200+",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PureWhite
                                )
                                Text(
                                    text = "Active Posts",
                                    fontSize = 10.sp,
                                    color = PureWhite.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            color = PureWhite.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "100% Official",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EmeraldMint
                                )
                                Text(
                                    text = "Gazette Verified",
                                    fontSize = 10.sp,
                                    color = PureWhite.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            color = PureWhite.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "14 Languages",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SaffronOrange
                                )
                                Text(
                                    text = "Multilingual",
                                    fontSize = 10.sp,
                                    color = PureWhite.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Verified Live Announcements Ticker
        item(key = "live_alerts") {
            Surface(
                color = PureWhite,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = StatusUrgentBg,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(StatusUrgent)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "LIVE ALERTS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusUrgent
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "SSC CGL 2026 Tier-1 application open • UPSC CSE 2026 Prelims closing soon • RRB NTPC vacancies active",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate800,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 2b. Official Sarkari 6 Pillar Portals Hub (Latest Jobs, Admit Card, Results, Syllabus, Admission, Answer Key)
        item(key = "sarkari_pillars_hub") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Official Recruitment Portals",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Slate900
                    )
                    Text(
                        text = "All 6 Services",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalBlue
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2 Rows x 3 Columns grid of pillars
                val chunkedPillars = SarkariPillarsList.chunked(3)
                chunkedPillars.forEachIndexed { rowIndex, rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { pillar ->
                            Surface(
                                onClick = {
                                    when (pillar.actionType) {
                                        0 -> {
                                            onFilterByCategory("Latest Job")
                                            onNavigateTab(MainTab.JOBS)
                                        }
                                        1 -> onOpenResultsSubTab(0)
                                        2 -> onOpenResultsSubTab(1)
                                        3 -> onOpenResultsSubTab(2)
                                        4 -> onOpenResultsSubTab(3)
                                        5 -> onOpenResultsSubTab(4)
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                color = PureWhite,
                                border = BorderStroke(1.dp, Slate200),
                                shadowElevation = 1.dp,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag(pillar.testTag)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(pillar.containerBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = pillar.iconEmoji, fontSize = 18.sp)
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = pillar.title,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900,
                                        maxLines = 1
                                    )

                                    Text(
                                        text = pillar.subtitle,
                                        fontSize = 9.sp,
                                        color = Slate500,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                    if (rowIndex < chunkedPillars.size - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // 2c. Targeted Custom Job Alerts Banner Card
        item(key = "targeted_job_alerts_card") {
            Card(
                onClick = onOpenJobAlerts,
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("home_card_targeted_alerts")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEFF6FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = RoyalBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🎯 Targeted Job Alerts",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = DeepNavy
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = if (isJobAlertsEnabled) Color(0xFFDCFCE7) else Slate100,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = if (isJobAlertsEnabled) "● $matchedJobsCount Matches" else "Paused",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isJobAlertsEnabled) EmeraldGreen else Slate600,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = if (isJobAlertsEnabled) "Curated for your state, degree & target agencies" else "Tap to configure personalized exam notifications",
                            fontSize = 11.sp,
                            color = Slate600,
                            lineHeight = 14.sp
                        )
                    }

                    Surface(
                        color = NavyContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Tune",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepNavy,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Candidate Utility Tools Hub (Age & Eligibility, 7th CPC Salary, Deadlines, Syllabus)
        item(key = "utility_tools") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Candidate Utility Tools",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Slate900
                    )
                    Text(
                        text = "Instant Calculators",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalBlue
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. Age & Eligibility Calculator
                    Surface(
                        onClick = onOpenAgeCalculator,
                        shape = RoundedCornerShape(16.dp),
                        color = PureWhite,
                        border = BorderStroke(1.dp, Slate200),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_tool_age_calc")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Surface(
                                color = Color(0xFFEFF6FF),
                                shape = CircleShape,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Cake,
                                        contentDescription = null,
                                        tint = RoyalBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Age Calculator",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy
                            )
                            Text(
                                text = "Check cut-off & quota eligibility",
                                fontSize = 10.sp,
                                color = Slate500,
                                lineHeight = 13.sp
                            )
                        }
                    }

                    // 2. 7th CPC Salary Calculator
                    Surface(
                        onClick = onOpenSalaryCalculator,
                        shape = RoundedCornerShape(16.dp),
                        color = PureWhite,
                        border = BorderStroke(1.dp, Slate200),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_tool_salary_calc")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Surface(
                                color = Color(0xFFECFDF5),
                                shape = CircleShape,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Payments,
                                        contentDescription = null,
                                        tint = EmeraldGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Salary Calculator",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy
                            )
                            Text(
                                text = "50% DA, HRA & in-hand pay",
                                fontSize = 10.sp,
                                color = Slate500,
                                lineHeight = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 3. Deadline Scheduler & Alerts Hub
                    Surface(
                        onClick = {
                            onFilterByCategory("Favorites")
                            onNavigateTab(MainTab.JOBS)
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = PureWhite,
                        border = BorderStroke(1.dp, Slate200),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_tool_deadlines")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Surface(
                                color = Color(0xFFFFFBEB),
                                shape = CircleShape,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = SaffronOrange,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Deadline Alerts",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy
                            )
                            Text(
                                text = "3-day & 24h reminder scheduler",
                                fontSize = 10.sp,
                                color = Slate500,
                                lineHeight = 13.sp
                            )
                        }
                    }

                    // 4. Syllabus & Exam Pattern
                    Surface(
                        onClick = { onNavigateTab(MainTab.RESULTS) },
                        shape = RoundedCornerShape(16.dp),
                        color = PureWhite,
                        border = BorderStroke(1.dp, Slate200),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_tool_syllabus")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Surface(
                                color = Color(0xFFF3E8FF),
                                shape = CircleShape,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = null,
                                        tint = Color(0xFF7C3AED),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Syllabus Tracker",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy
                            )
                            Text(
                                text = "Exam pattern & prep checklist",
                                fontSize = 10.sp,
                                color = Slate500,
                                lineHeight = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // 3. AI Eligibility Mentor Modern Hero Card (Gemini 3.1 Pro with High Thinking)
        item(key = "ai_mentor_hero") {
            Card(
                onClick = { onNavigateTab(MainTab.AI_MENTOR) },
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .testTag("ai_mentor_hero_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF1E1B4B),
                                    Color(0xFF312E81),
                                    Color(0xFF1E3A8A)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = PureWhite.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color(0xFFFDE047),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "GEMINI 3.1 PRO • HIGH THINKING",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PureWhite
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(PureWhite.copy(alpha = 0.18f))
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Open",
                                    tint = PureWhite,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Instant Eligibility & Syllabus Mentor",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = PureWhite,
                                fontSize = 18.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Check your precise age relaxations (OBC / SC / ST / EWS), qualification match, and get an AI 90-day exam roadmap in seconds.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = PureWhite.copy(alpha = 0.85f),
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        )
                    }
                }
            }
        }

        // 4. Closing Soon (Urgent Deadlines) Horizontal Section
        if (closingSoonJobs.isNotEmpty()) {
            item(key = "closing_soon_section") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(StatusUrgent)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = LanguageRegistry.getString("urgent_deadlines", language),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = Slate900
                                )
                            )
                        }

                        Text(
                            text = "View All",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoyalBlue,
                            modifier = Modifier.clickable { onNavigateTab(MainTab.JOBS) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(closingSoonJobs, key = { "closing_${it.id}" }, contentType = { "closing_card" }) { job ->
                            val countdownBadge = AppUtils.getCountdownBadgeText(job.applicationClosingDate)
                            Card(
                                onClick = { onJobClick(job) },
                                colors = CardDefaults.cardColors(containerColor = PureWhite),
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                                shape = RoundedCornerShape(18.dp),
                                border = BorderStroke(1.dp, Slate200.copy(alpha = 0.8f)),
                                modifier = Modifier
                                    .width(290.dp)
                                    .testTag("urgent_card_${job.id}")
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = job.organization,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Slate700,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Surface(
                                            color = StatusUrgentBg,
                                            shape = RoundedCornerShape(20.dp)
                                        ) {
                                            Text(
                                                text = countdownBadge,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = StatusUrgent,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = job.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        lineHeight = 20.sp
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "👥 ${job.totalVacancies} Posts",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = RoyalBlue
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = { AppUtils.addJobDeadlineToCalendar(context, job) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CalendarMonth,
                                                    contentDescription = "Calendar",
                                                    tint = Slate600,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = { AppUtils.shareJobAlert(context, job) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Share,
                                                    contentDescription = "Share",
                                                    tint = Slate600,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // 5. Explore Government Job Sectors (State, Central, Bank, Teaching, Engineering, Railway, Police/Defence)
        item(key = "sectors_section") {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Explore Job Sectors",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = Slate900
                        )
                    )

                    Text(
                        text = "All Categories",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalBlue,
                        modifier = Modifier.clickable {
                            onFilterByCategory("All Categories")
                            onNavigateTab(MainTab.JOBS)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(TopJobSectorsList, key = { it.name }) { sector ->
                        val count = sectorCounts[sector.name] ?: 0
                        Surface(
                            onClick = {
                                onFilterByCategory(sector.filterKey)
                                onNavigateTab(MainTab.JOBS)
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = PureWhite,
                            shadowElevation = 2.dp,
                            border = BorderStroke(1.dp, Slate200.copy(alpha = 0.8f)),
                            modifier = Modifier.testTag("sector_shortcut_${sector.filterKey}")
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = sector.iconEmoji, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = sector.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                    if (count > 0) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = Color(0xFFEFF6FF),
                                            shape = CircleShape
                                        ) {
                                            Text(
                                                text = if (count > 99) "99+" else count.toString(),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = RoyalBlue,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = sector.subtitle,
                                    fontSize = 10.sp,
                                    color = Slate500
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // 6. State-wise Directory Shortcuts
        item(key = "states_section") {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "State & Central Vacancies",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = Slate900
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(PopularStates, key = { it }) { stateName ->
                        val isAllIndia = stateName == "All India"
                        Surface(
                            onClick = {
                                onFilterByState(stateName)
                                onNavigateTab(MainTab.JOBS)
                            },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isAllIndia) DeepNavy else PureWhite,
                            shadowElevation = if (isAllIndia) 3.dp else 1.dp,
                            border = BorderStroke(
                                1.dp,
                                if (isAllIndia) DeepNavy else Slate200
                            ),
                            modifier = Modifier.testTag("state_pill_${stateName.replace(" ", "_")}")
                        ) {
                            Text(
                                text = stateName,
                                fontSize = 12.sp,
                                fontWeight = if (isAllIndia) FontWeight.Bold else FontWeight.Medium,
                                color = if (isAllIndia) PureWhite else Slate800,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // 7. Latest Published Jobs Header
        item(key = "latest_jobs_header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recently Released Jobs",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Slate900
                    )
                )

                Text(
                    text = "See All (${allJobs.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = RoyalBlue,
                    modifier = Modifier.clickable { onNavigateTab(MainTab.JOBS) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }

        // 8. Latest Jobs List
        items(topJobs, key = { "home_job_${it.id}" }, contentType = { "job_card" }) { job ->
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                JobCard(
                    job = job,
                    onClick = { onJobClick(job) },
                    onBookmarkToggle = { onBookmarkToggle(job) },
                    onShare = { AppUtils.shareJobAlert(context, job) }
                )
            }
        }

        // 9. Statutory Independent Service Disclaimer Banner
        item(key = "disclaimer") {
            Surface(
                color = PureWhite,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Slate200.copy(alpha = 0.8f)),
                shadowElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Disclaimer",
                        tint = Slate600,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(top = 1.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = LanguageRegistry.getString("disclaimer_text", language),
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = Slate600
                    )
                }
            }
        }
    }
}
