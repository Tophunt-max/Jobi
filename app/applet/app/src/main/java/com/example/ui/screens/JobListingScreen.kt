package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AdmitCardItem
import com.example.model.AppLanguage
import com.example.model.JobEntity
import com.example.model.ResultItem
import com.example.ui.components.JobCard
import com.example.util.AppUtils
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldMint
import com.example.ui.theme.GoldStar
import com.example.ui.theme.GreenContainer
import com.example.ui.theme.OnGreenContainer
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.RoyalBlueContainer
import com.example.ui.theme.SaffronOrange
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.StatusUrgent
import com.example.ui.theme.StatusUrgentBg
import com.example.ui.viewmodel.JobViewModel

/**
 * Modern JobListingScreen with clean user interface without internal backend clutter.
 * All technical pipeline/Cloudflare/Room inspection is housed in the Admin Panel.
 */
@Composable
fun JobListingScreen(
    viewModel: JobViewModel = viewModel(),
    onJobClick: (JobEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val allJobs by viewModel.allJobs.collectAsState()
    val admitCards by viewModel.admitCards.collectAsState()
    val results by viewModel.results.collectAsState()

    var searchQuery by remember { mutableStateOf(uiState.searchQuery) }
    var selectedCategoryFilter by remember {
        mutableStateOf(if (uiState.selectedCategory.isNotBlank() && uiState.selectedCategory != "All Categories") uiState.selectedCategory else "All")
    }
    var selectedOrgFilter by remember { mutableStateOf("All Organizations") }

    JobListingScreenContent(
        allJobs = allJobs,
        admitCards = admitCards,
        results = results,
        searchQuery = searchQuery,
        onSearchChange = {
            searchQuery = it
            viewModel.setSearchQuery(it)
        },
        selectedCategoryFilter = selectedCategoryFilter,
        onCategoryFilterSelect = {
            selectedCategoryFilter = it
            viewModel.setCategoryFilter(if (it == "All") "All Categories" else it)
        },
        selectedOrgFilter = selectedOrgFilter,
        onOrgFilterSelect = { selectedOrgFilter = it },
        isRefreshing = uiState.isRefreshing,
        onRefresh = { viewModel.refreshData() },
        onJobClick = onJobClick,
        onBookmarkToggle = { viewModel.toggleBookmark(it) },
        language = uiState.language,
        modifier = modifier
    )
}

@Composable
fun JobListingScreenContent(
    allJobs: List<JobEntity>,
    admitCards: List<AdmitCardItem>,
    results: List<ResultItem>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategoryFilter: String,
    onCategoryFilterSelect: (String) -> Unit,
    selectedOrgFilter: String,
    onOrgFilterSelect: (String) -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onJobClick: (JobEntity) -> Unit,
    onBookmarkToggle: (JobEntity) -> Unit,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Category Filter Chips with Admit Card and Result
    val categoryChips = listOf(
        "All" to "🌐",
        "Admit Card" to "🎫",
        "Result" to "🏆",
        "New Job" to "💼",
        "Favorites" to "⭐",
        "SSC" to "📑",
        "UPSC" to "🏛️",
        "Railways" to "🚆",
        "Banking" to "🏦"
    )

    // Organization Filter Chips
    val organizationChips = listOf(
        "All Organizations",
        "UPSC",
        "SSC",
        "Railways",
        "Banking",
        "Police",
        "State PSC",
        "Engineering / ISRO"
    )

    fun matchesOrg(text: String): Boolean {
        if (selectedOrgFilter == "All Organizations") return true
        val target = when (selectedOrgFilter) {
            "Railways" -> "Railway"
            "Banking" -> "Bank"
            "Engineering / ISRO" -> "ISRO"
            else -> selectedOrgFilter
        }
        return text.contains(target, ignoreCase = true)
    }

    // Filtered Jobs
    val filteredJobs = remember(allJobs, searchQuery, selectedCategoryFilter, selectedOrgFilter) {
        allJobs.filter { job ->
            val matchesCategory = when (selectedCategoryFilter) {
                "All" -> true
                "Favorites" -> job.isBookmarked
                "New Job" -> !job.category.equals("Admit Card", ignoreCase = true) && !job.category.equals("Result", ignoreCase = true)
                "Admit Card" -> job.category.equals("Admit Card", ignoreCase = true) ||
                        job.title.contains("Admit Card", ignoreCase = true) ||
                        job.title.contains("Hall Ticket", ignoreCase = true) ||
                        job.title.contains("City Intimation", ignoreCase = true)
                "Result", "Results" -> job.category.equals("Result", ignoreCase = true) ||
                        job.title.contains("Result", ignoreCase = true) ||
                        job.title.contains("Cutoff", ignoreCase = true) ||
                        job.title.contains("Scorecard", ignoreCase = true) ||
                        job.title.contains("Merit", ignoreCase = true)
                else -> job.category.contains(selectedCategoryFilter, ignoreCase = true) ||
                        job.organization.contains(selectedCategoryFilter, ignoreCase = true)
            }

            val matchesSearch = searchQuery.isBlank() ||
                    job.title.contains(searchQuery, ignoreCase = true) ||
                    job.organization.contains(searchQuery, ignoreCase = true) ||
                    job.advtNumber.contains(searchQuery, ignoreCase = true) ||
                    job.category.contains(searchQuery, ignoreCase = true) ||
                    job.qualification.contains(searchQuery, ignoreCase = true) ||
                    job.stateOrAllIndia.contains(searchQuery, ignoreCase = true)

            val matchesOrgFilter = matchesOrg("${job.organization} ${job.title} ${job.category}")

            matchesCategory && matchesSearch && matchesOrgFilter
        }
    }

    // Filtered Admit Cards
    val filteredAdmitCards = remember(admitCards, searchQuery, selectedCategoryFilter, selectedOrgFilter) {
        if (selectedCategoryFilter != "All" && selectedCategoryFilter != "Admit Card") {
            emptyList()
        } else {
            admitCards.filter { card ->
                val matchesSearch = searchQuery.isBlank() ||
                        card.examName.contains(searchQuery, ignoreCase = true) ||
                        card.recruitingOrg.contains(searchQuery, ignoreCase = true)
                val matchesOrgFilter = matchesOrg("${card.recruitingOrg} ${card.examName}")
                matchesSearch && matchesOrgFilter
            }
        }
    }

    // Filtered Results
    val filteredResults = remember(results, searchQuery, selectedCategoryFilter, selectedOrgFilter) {
        if (selectedCategoryFilter != "All" && selectedCategoryFilter != "Results" && selectedCategoryFilter != "Result") {
            emptyList()
        } else {
            results.filter { res ->
                val matchesSearch = searchQuery.isBlank() ||
                        res.examName.contains(searchQuery, ignoreCase = true) ||
                        res.recruitingOrg.contains(searchQuery, ignoreCase = true) ||
                        res.advtNumber.contains(searchQuery, ignoreCase = true)
                val matchesOrgFilter = matchesOrg("${res.recruitingOrg} ${res.examName}")
                matchesSearch && matchesOrgFilter
            }
        }
    }

    val totalItemCount = filteredJobs.size + filteredAdmitCards.size + filteredResults.size

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("job_listing_screen")
    ) {
        // Modern Floating Header with Search
        Surface(
            color = PureWhite,
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
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
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Government Job Openings",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = Slate900
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = GreenContainer,
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(EmeraldMint)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "LIVE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OnGreenContainer
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Central & State Vacancies, Admit Cards & Results",
                            fontSize = 12.sp,
                            color = Slate500,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Surface(
                        onClick = onRefresh,
                        shape = CircleShape,
                        color = Slate100,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("refresh_jobs_btn")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isRefreshing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = RoyalBlue
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh",
                                    tint = Slate700,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = {
                        Text(
                            text = "Search by job, department, advt no...",
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
                        focusedContainerColor = Slate50,
                        unfocusedContainerColor = Slate50,
                        focusedBorderColor = RoyalBlue,
                        unfocusedBorderColor = Slate200,
                        focusedTextColor = Slate900,
                        unfocusedTextColor = Slate900
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("job_listing_search_input")
                )
            }
        }

        // Category & Organization Pills Container
        Surface(
            color = PureWhite,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = 10.dp)) {
                // Category Pills Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categoryChips.forEach { (catName, iconEmoji) ->
                        val isSelected = selectedCategoryFilter == catName
                        FilterChip(
                            selected = isSelected,
                            onClick = { onCategoryFilterSelect(catName) },
                            label = {
                                Text(
                                    text = "$iconEmoji $catName",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DeepNavy,
                                selectedLabelColor = PureWhite,
                                containerColor = Slate100,
                                labelColor = Slate700
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) DeepNavy else Color.Transparent
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("category_chip_${catName.replace(" ", "_")}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Organization Pills Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    organizationChips.forEach { orgName ->
                        val isSelected = selectedOrgFilter == orgName
                        FilterChip(
                            selected = isSelected,
                            onClick = { onOrgFilterSelect(orgName) },
                            label = {
                                Text(
                                    text = orgName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RoyalBlue,
                                selectedLabelColor = PureWhite,
                                containerColor = Slate50,
                                labelColor = Slate600
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) RoyalBlue else Slate200
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("org_chip_${orgName.replace(" ", "_")}")
                        )
                    }
                }
            }
        }

        // Active Filter Summary & Reset Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(EmeraldMint)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (selectedCategoryFilter != "All") {
                        "$totalItemCount in '$selectedCategoryFilter'"
                    } else {
                        "$totalItemCount Openings & Notices"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate700
                )

                if (selectedCategoryFilter != "All") {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = RoyalBlue.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = selectedCategoryFilter,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoyalBlue,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (searchQuery.isNotEmpty() || selectedCategoryFilter != "All" || selectedOrgFilter != "All Organizations") {
                Surface(
                    onClick = {
                        onSearchChange("")
                        onCategoryFilterSelect("All")
                        onOrgFilterSelect("All Organizations")
                    },
                    color = Color(0xFFFEF2F2),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFFECACA)),
                    modifier = Modifier.testTag("clear_all_filters_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Reset Filters",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626)
                        )
                    }
                }
            }
        }

        // Listings View
        if (totalItemCount == 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(if (selectedCategoryFilter == "Favorites") Color(0xFFFEF3C7) else RoyalBlueContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (selectedCategoryFilter == "Favorites") Icons.Default.Bookmark else Icons.Default.Work,
                            contentDescription = null,
                            tint = if (selectedCategoryFilter == "Favorites") GoldStar else RoyalBlue,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (selectedCategoryFilter == "Favorites") "No Bookmarked Jobs Yet" else "No matching jobs found",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900,
                            fontSize = 17.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (selectedCategoryFilter == "Favorites")
                            "Tap the bookmark icon ⭐ on any job card to save it locally for offline reference and instant tracking."
                        else
                            "Try searching with fewer keywords or clearing active category filters.",
                        fontSize = 12.sp,
                        color = Slate500,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            onSearchChange("")
                            onCategoryFilterSelect("All")
                            onOrgFilterSelect("All Organizations")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (selectedCategoryFilter == "Favorites") "Browse All Openings" else "Reset All Filters")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("job_openings_list"),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Deadline Alert Scheduler Banner for Bookmarked Jobs
                if (selectedCategoryFilter == "Favorites") {
                    item(key = "favorites_deadline_scheduler_banner") {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFEFF6FF),
                            border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
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
                                        .background(Color(0xFFDBEAFE)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = RoyalBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Deadline Notification Scheduler Active",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepNavy
                                    )
                                    Text(
                                        text = "You will automatically receive alerts 3 days and 24 hours prior to closing dates for all bookmarked vacancies.",
                                        fontSize = 11.sp,
                                        color = Slate600
                                    )
                                }
                            }
                        }
                    }
                }

                // New Job Openings
                items(filteredJobs, key = { "job_${it.id}" }) { job ->
                    JobCard(
                        job = job,
                        onClick = { onJobClick(job) },
                        onBookmarkToggle = { onBookmarkToggle(job) },
                        onShare = { AppUtils.shareJobAlert(context, job) }
                    )
                }

                // Admit Cards
                items(filteredAdmitCards, key = { "admit_${it.id}" }) { card ->
                    AdmitCardListingCard(
                        card = card,
                        onDownloadClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(card.officialDownloadUrl))
                            context.startActivity(intent)
                        }
                    )
                }

                // Results
                items(filteredResults, key = { "result_${it.id}" }) { res ->
                    ResultListingCard(
                        result = res,
                        onViewResultClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(res.officialResultUrl))
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Modern AdmitCardListingCard
 */
@Composable
fun AdmitCardListingCard(
    card: AdmitCardItem,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Slate200.copy(alpha = 0.8f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("admit_card_${card.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "🎫 ADMIT CARD",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB45309),
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = "Released: ${card.releaseDate}",
                    fontSize = 11.sp,
                    color = Slate500
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = card.examName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900,
                lineHeight = 22.sp
            )
            Text(
                text = card.recruitingOrg,
                fontSize = 12.sp,
                color = Slate600,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = StatusUrgentBg,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "📅 Exam Date: ${card.examDate}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = StatusUrgent,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onDownloadClick,
                colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Download e-Admit Card",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

/**
 * Modern ResultListingCard
 */
@Composable
fun ResultListingCard(
    result: ResultItem,
    onViewResultClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Slate200.copy(alpha = 0.8f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("result_card_${result.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = GreenContainer,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "🏆 RESULT DECLARED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnGreenContainer,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = "Declared: ${result.publicationDate}",
                    fontSize = 11.sp,
                    color = Slate500
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = result.examName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900,
                lineHeight = 22.sp
            )
            Text(
                text = "${result.recruitingOrg} • Advt: ${result.advtNumber}",
                fontSize = 12.sp,
                color = Slate600,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = Slate100,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "📊 ${result.cutOffSummary}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate800,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onViewResultClick,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldMint),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "View Scorecard & Merit List",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = PureWhite
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    tint = PureWhite,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}
