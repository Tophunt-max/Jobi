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
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.JobEntity
import com.example.model.LanguageRegistry
import com.example.ui.components.JobCard
import com.example.util.AppUtils
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldMint
import com.example.ui.theme.GreenContainer
import com.example.ui.theme.HeroGradientBrush
import com.example.ui.theme.NavyContainer
import com.example.ui.theme.OnGreenContainer
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.RoyalBlueContainer
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
import com.example.ui.viewmodel.MainTab

data class CategoryTabItem(
    val id: String,
    val title: String,
    val emoji: String
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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Home Active Category Switcher State
    var selectedCategoryTab by remember { mutableStateOf("ALL") }
    // Quick Filter Pill State
    var selectedQuickPill by remember { mutableStateOf("ALL") }

    val categoryTabs = listOf(
        CategoryTabItem("ALL", "All Vacancies", "📋"),
        CategoryTabItem("JOBS", "Latest Jobs", "💼"),
        CategoryTabItem("CLOSING_SOON", "Closing Soon", "⏳"),
        CategoryTabItem("ADMIT_CARD", "Admit Cards", "🎫"),
        CategoryTabItem("RESULTS", "Results", "🏆"),
        CategoryTabItem("SSC_UPSC", "SSC & UPSC", "🏛️"),
        CategoryTabItem("RAILWAYS", "Railways", "🚆"),
        CategoryTabItem("BANKING", "Banking", "🏦"),
        CategoryTabItem("DEFENCE", "Defence", "🛡️"),
        CategoryTabItem("TEACHING", "Teaching", "📚")
    )

    // Compute live vacancy count for category switcher tabs
    val categoryCounts = remember(allJobs, closingSoonJobs) {
        mapOf(
            "ALL" to allJobs.size,
            "JOBS" to allJobs.count { !it.category.contains("Admit Card", true) && !it.category.contains("Result", true) },
            "CLOSING_SOON" to closingSoonJobs.size,
            "ADMIT_CARD" to allJobs.count { it.category.contains("Admit Card", true) || it.title.contains("Admit Card", true) },
            "RESULTS" to allJobs.count { it.category.contains("Result", true) || it.title.contains("Result", true) },
            "SSC_UPSC" to allJobs.count { it.category.contains("SSC", true) || it.organization.contains("SSC", true) || it.category.contains("UPSC", true) || it.organization.contains("UPSC", true) },
            "RAILWAYS" to allJobs.count { it.category.contains("Railway", true) || it.organization.contains("Railway", true) || it.organization.contains("RRB", true) },
            "BANKING" to allJobs.count { it.category.contains("Bank", true) || it.organization.contains("Bank", true) || it.organization.contains("SBI", true) || it.organization.contains("IBPS", true) },
            "DEFENCE" to allJobs.count { it.category.contains("Defence", true) || it.category.contains("Police", true) || it.organization.contains("Police", true) || it.organization.contains("Army", true) },
            "TEACHING" to allJobs.count { it.category.contains("Teaching", true) || it.category.contains("TET", true) || it.organization.contains("School", true) }
        )
    }

    // Filter displayed jobs on home screen according to category switcher & quick pill
    val filteredJobs = remember(allJobs, selectedCategoryTab, selectedQuickPill, searchQuery) {
        allJobs.filter { job ->
            val matchesCategory = when (selectedCategoryTab) {
                "ALL" -> true
                "JOBS" -> !job.category.contains("Admit Card", true) && !job.category.contains("Result", true)
                "CLOSING_SOON" -> AppUtils.getRemainingDays(job.applicationClosingDate) <= 7
                "ADMIT_CARD" -> job.category.contains("Admit Card", true) || job.title.contains("Admit Card", true)
                "RESULTS" -> job.category.contains("Result", true) || job.title.contains("Result", true)
                "SSC_UPSC" -> job.category.contains("SSC", true) || job.organization.contains("SSC", true) || job.category.contains("UPSC", true) || job.organization.contains("UPSC", true)
                "RAILWAYS" -> job.category.contains("Railway", true) || job.organization.contains("Railway", true) || job.organization.contains("RRB", true)
                "BANKING" -> job.category.contains("Bank", true) || job.organization.contains("Bank", true) || job.organization.contains("SBI", true) || job.organization.contains("IBPS", true)
                "DEFENCE" -> job.category.contains("Defence", true) || job.category.contains("Police", true) || job.organization.contains("Police", true)
                "TEACHING" -> job.category.contains("Teaching", true) || job.category.contains("TET", true)
                else -> true
            }

            val matchesPill = when (selectedQuickPill) {
                "ALL" -> true
                "ALL_INDIA" -> job.stateOrAllIndia.equals("All India", ignoreCase = true)
                "ODISHA" -> job.stateOrAllIndia.contains("Odisha", ignoreCase = true)
                "CENTRAL" -> job.category.contains("UPSC", true) || job.category.contains("SSC", true) || job.category.contains("Railway", true)
                "10TH_12TH" -> job.qualification.contains("10th", true) || job.qualification.contains("12th", true)
                "GRADUATE" -> job.qualification.contains("Graduat", true) || job.qualification.contains("Degree", true)
                "URGENT" -> AppUtils.getRemainingDays(job.applicationClosingDate) <= 3
                else -> true
            }

            val matchesSearch = if (searchQuery.isBlank()) true else {
                job.title.contains(searchQuery, ignoreCase = true) ||
                        job.organization.contains(searchQuery, ignoreCase = true) ||
                        job.category.contains(searchQuery, ignoreCase = true) ||
                        job.qualification.contains(searchQuery, ignoreCase = true) ||
                        job.stateOrAllIndia.contains(searchQuery, ignoreCase = true)
            }

            matchesCategory && matchesPill && matchesSearch
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // 1. Hero Section with Search Field & Live Metrics
        item {
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

                    // Search Bar inside Hero
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
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear search",
                                        tint = Slate500
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
                                    text = "${allJobs.sumOf { it.totalVacancies }}+",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PureWhite
                                )
                                Text(
                                    text = "Total Vacancies",
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
        item {
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

        // 3. Category Switcher with Live Vacancy Count Badges (FEATURE HIGHLIGHT)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PureWhite)
                    .padding(vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recruitment Categories",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = Slate900
                        )
                    )
                    Text(
                        text = "${filteredJobs.size} Active Notices",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = RoyalBlue
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Category Tabs with live count badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categoryTabs.forEach { tab ->
                        val isSelected = selectedCategoryTab == tab.id
                        val count = categoryCounts[tab.id] ?: 0

                        Surface(
                            onClick = {
                                selectedCategoryTab = tab.id
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) DeepNavy else Slate100,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) DeepNavy else Slate200
                            ),
                            shadowElevation = if (isSelected) 2.dp else 0.dp,
                            modifier = Modifier.testTag("category_tab_${tab.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = tab.emoji, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = tab.title,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) PureWhite else Slate800
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = if (isSelected) PureWhite.copy(alpha = 0.25f) else PureWhite,
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = if (count > 99) "99+" else count.toString(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isSelected) PureWhite else RoyalBlue,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Filter Pills Row (All India, Odisha, Central, 10th/12th, Graduate, Urgent)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val pills = listOf(
                        "ALL" to "All Streams",
                        "ALL_INDIA" to "🇮🇳 All India",
                        "ODISHA" to "📍 Odisha",
                        "CENTRAL" to "🏛️ Central Govt",
                        "10TH_12TH" to "📖 10th/12th Pass",
                        "GRADUATE" to "🎓 Graduate",
                        "URGENT" to "🚨 Urgent (≤3 Days)"
                    )

                    pills.forEach { (pillId, pillLabel) ->
                        val isPillActive = selectedQuickPill == pillId
                        FilterChip(
                            selected = isPillActive,
                            onClick = {
                                selectedQuickPill = if (isPillActive && pillId != "ALL") "ALL" else pillId
                            },
                            label = {
                                Text(
                                    text = pillLabel,
                                    fontSize = 11.sp,
                                    fontWeight = if (isPillActive) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RoyalBlue,
                                selectedLabelColor = PureWhite,
                                containerColor = PureWhite,
                                labelColor = Slate700
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isPillActive,
                                borderColor = if (isPillActive) RoyalBlue else Slate200
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("quick_pill_$pillId")
                        )
                    }
                }
            }
        }

        // 4. Candidate Utility Tools Hub (Age & Eligibility, 7th CPC Salary, Deadlines, Syllabus)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
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

        // 5. AI Eligibility Mentor Modern Hero Card
        item {
            Card(
                onClick = { onNavigateTab(MainTab.AI_MENTOR) },
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
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
                            text = "Instant AI Eligibility & Syllabus Mentor",
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

        // 6. Closing Soon (Urgent Deadlines) Horizontal Section
        if (closingSoonJobs.isNotEmpty()) {
            item {
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
                            text = "View All (${closingSoonJobs.size})",
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
                        items(closingSoonJobs) { job ->
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

        item { Spacer(modifier = Modifier.height(20.dp)) }

        // 7. State-wise Directory Shortcuts
        item {
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

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val popularStates = listOf(
                        "All India",
                        "Odisha",
                        "Uttar Pradesh",
                        "Bihar",
                        "Rajasthan",
                        "Madhya Pradesh",
                        "Maharashtra",
                        "Delhi (NCT)",
                        "West Bengal",
                        "Tamil Nadu"
                    )

                    popularStates.forEach { stateName ->
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

        item { Spacer(modifier = Modifier.height(20.dp)) }

        // 8. Filtered Jobs Feed Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = when (selectedCategoryTab) {
                            "ALL" -> "Recently Released Vacancies"
                            "JOBS" -> "Latest Active Jobs"
                            "CLOSING_SOON" -> "Deadlines Approaching"
                            "ADMIT_CARD" -> "Admit Cards & Hall Tickets"
                            "RESULTS" -> "Declared Results & Merit Lists"
                            else -> "Verified Job Notices"
                        },
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = Slate900
                        )
                    )
                    Text(
                        text = "Showing ${filteredJobs.size} results",
                        fontSize = 11.sp,
                        color = Slate500
                    )
                }

                Text(
                    text = "See All In Jobs ➔",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = RoyalBlue,
                    modifier = Modifier.clickable { onNavigateTab(MainTab.JOBS) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(10.dp)) }

        // 9. Filtered Jobs List
        if (filteredJobs.isEmpty()) {
            item {
                Surface(
                    color = PureWhite,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Slate200),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No matching vacancies in this filter",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Slate800
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try switching back to 'All Vacancies' or clear search filters",
                            fontSize = 12.sp,
                            color = Slate500
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                selectedCategoryTab = "ALL"
                                selectedQuickPill = "ALL"
                                onSearchChange("")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Reset Category Filters", fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            items(filteredJobs.take(10), key = { it.id }) { job ->
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    JobCard(
                        job = job,
                        onClick = { onJobClick(job) },
                        onBookmarkToggle = { onBookmarkToggle(job) },
                        onShare = { AppUtils.shareJobAlert(context, job) }
                    )
                }
            }
        }

        // 10. Statutory Independent Service Disclaimer Banner
        item {
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
