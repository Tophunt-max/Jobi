package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ads.NativeSponsoredJobCard
import com.example.model.IndianStatesRegistry
import com.example.model.JobEntity
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.EmeraldMint
import com.example.ui.theme.GoldStar
import com.example.ui.theme.GreenContainer
import com.example.ui.theme.NavyContainer
import com.example.ui.theme.OnGreenContainer
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.RoyalBlueContainer
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.StatusUpcoming
import com.example.ui.theme.StatusUpcomingBg
import com.example.ui.theme.StatusUrgent
import com.example.ui.theme.StatusUrgentBg
import com.example.ui.theme.VerifiedBlue
import com.example.ui.viewmodel.JobViewModel
import com.example.ui.viewmodel.MainTab
import com.example.util.JobBrandHelper
import kotlinx.coroutines.launch

/**
 * Modern, responsive Latest Jobs screen for Sarkari Job Alert candidates.
 * Features:
 * - Compact, slim search capsule that doesn't consume screen space
 * - Natural scroll-away behavior: When scrolling through jobs, the header and search box
 *   scroll off smoothly, dedicating 100% of the screen to job vacancy listings
 * - Auto-dismiss keyboard on scroll
 * - Quick "Scroll to Top" floating action button
 * - Live category and state count badges
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun JobListingScreen(
    viewModel: JobViewModel = viewModel(),
    onJobClick: (JobEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val allJobs by viewModel.allJobs.collectAsState()
    val filteredJobs by viewModel.filteredJobs.collectAsState()

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Auto-dismiss keyboard when user begins scrolling through vacancies
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            keyboardController?.hide()
        }
    }

    // Show floating "Back to Top" button once candidate has scrolled past the header
    val showScrollToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 1 }
    }

    // Categories with Emojis
    val categories = remember {
        listOf(
            "All Categories" to "🌐",
            "UPSC" to "🏛️",
            "SSC" to "📑",
            "Railways" to "🚆",
            "Banking" to "🏦",
            "Police & Defence" to "🛡️",
            "Teaching" to "🧑‍🏫",
            "State PSC" to "🏛️",
            "Engineering" to "⚙️",
            "Medical & Health" to "🩺",
            "10th / 12th Pass" to "🎒",
            "Favorites" to "⭐"
        )
    }

    // Indian States & Union Territories
    val states = remember {
        listOf("All India") + IndianStatesRegistry.states.filter { it.code != "ALL" }.map { it.name }
    }

    // Status Filters
    val statusTabs = listOf("ALL", "ACTIVE", "CLOSING_SOON", "UPCOMING")
    val statusLabels = listOf("All Jobs", "Active", "🚨 Closing Soon", "Upcoming")

    // Dynamic Live Vacancy Count Lookups
    val categoryCounts = remember(allJobs) {
        categories.associate { (cat, _) ->
            cat to when (cat) {
                "All Categories" -> allJobs.size
                "Favorites" -> allJobs.count { it.isBookmarked }
                "Police & Defence" -> allJobs.count { it.category.contains("Police", ignoreCase = true) || it.category.contains("Defence", ignoreCase = true) || it.organization.contains("Police", ignoreCase = true) }
                "10th / 12th Pass" -> allJobs.count { it.qualification.contains("10th", ignoreCase = true) || it.qualification.contains("12th", ignoreCase = true) }
                else -> allJobs.count { job ->
                    job.category.contains(cat, ignoreCase = true) || job.organization.contains(cat, ignoreCase = true)
                }
            }
        }
    }

    val stateCounts = remember(allJobs) {
        states.associateWith { state ->
            if (state == "All India") allJobs.size
            else allJobs.count { it.stateOrAllIndia.equals(state, ignoreCase = true) || it.stateOrAllIndia.contains(state, ignoreCase = true) }
        }
    }

    val isAnyFilterActive = uiState.searchQuery.isNotBlank() ||
        (uiState.selectedCategory != "All Categories" && uiState.selectedCategory != "All") ||
        (uiState.selectedState != "All India" && uiState.selectedState != "ALL") ||
        uiState.selectedStatusFilter != "ALL"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("job_listing_screen")
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 110.dp)
        ) {
            // Header & Compact Search Capsule (Scrolls with feed)
            item(key = "header_and_search_bar") {
                Surface(
                    color = PureWhite,
                    shadowElevation = 1.5.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        // Title Row + Live Indicator + Refresh Action
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Latest Govt Openings",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = Slate900
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = GreenContainer,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.5.dp)
                                                .clip(CircleShape)
                                                .background(EmeraldMint)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "LIVE",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = OnGreenContainer
                                        )
                                    }
                                }
                            }

                            // Refresh Action Button
                            IconButton(
                                onClick = { viewModel.refreshData() },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("refresh_jobs_btn")
                            ) {
                                if (uiState.isRefreshing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(15.dp),
                                        strokeWidth = 2.dp,
                                        color = RoyalBlue
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh Vacancies",
                                        tint = Slate600,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Pixel-perfect Single-Line Search Capsule (Height 42dp, zero text/placeholder clipping)
                        BasicTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            singleLine = true,
                            textStyle = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                color = Slate900
                            ),
                            cursorBrush = SolidColor(RoyalBlue),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                            decorationBox = { innerTextField ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(42.dp)
                                        .background(Slate50, RoundedCornerShape(12.dp))
                                        .border(
                                            width = 1.dp,
                                            color = if (uiState.searchQuery.isNotEmpty()) RoyalBlue else Slate200,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = if (uiState.searchQuery.isNotEmpty()) RoyalBlue else Slate500,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier.weight(1f),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (uiState.searchQuery.isEmpty()) {
                                            Text(
                                                text = "Search jobs, department, advt no...",
                                                fontSize = 13.sp,
                                                color = Slate400,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        innerTextField()
                                    }
                                    if (uiState.searchQuery.isNotEmpty()) {
                                        IconButton(
                                            onClick = { viewModel.setSearchQuery("") },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear search",
                                                tint = Slate500,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("job_listing_search_input")
                        )
                    }
                }
            }

            // Sticky Filter Section: Status Tabs + Categories + Indian States
            // Never hides when candidate scrolls through vacancies!
            stickyHeader(key = "sticky_filter_section") {
                Surface(
                    color = PureWhite,
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Status Tabs Bar (All Jobs, Active, Closing Soon, Upcoming)
                        val selectedStatusIndex = statusTabs.indexOf(uiState.selectedStatusFilter).coerceAtLeast(0)
                        TabRow(
                            selectedTabIndex = selectedStatusIndex,
                            containerColor = PureWhite,
                            contentColor = DeepNavy,
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedStatusIndex]),
                                    color = DeepNavy,
                                    height = 2.5.dp
                                )
                            }
                        ) {
                            statusTabs.forEachIndexed { index, tabKey ->
                                val isSelected = uiState.selectedStatusFilter == tabKey
                                Tab(
                                    selected = isSelected,
                                    onClick = { viewModel.setStatusFilter(tabKey) },
                                    text = {
                                        Text(
                                            text = statusLabels[index],
                                            fontSize = 11.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) DeepNavy else Slate600
                                        )
                                    },
                                    modifier = Modifier
                                        .height(36.dp)
                                        .testTag("status_tab_$tabKey")
                                )
                            }
                        }

                        // Dual Filter Rows: Categories and States
                        Column(modifier = Modifier.padding(top = 5.dp, bottom = 6.dp)) {
                            // Row 1: Categories
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 14.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                categories.forEach { (catName, emoji) ->
                                    val isSelected = if (catName == "All Categories") {
                                        uiState.selectedCategory == "All Categories" || uiState.selectedCategory == "All" || uiState.selectedCategory.isBlank()
                                    } else if (catName == "Favorites") {
                                        uiState.selectedCategory == "Favorites"
                                    } else {
                                        uiState.selectedCategory.equals(catName, ignoreCase = true)
                                    }

                                    val count = categoryCounts[catName] ?: 0

                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            viewModel.setCategoryFilter(if (catName == "All Categories") "All Categories" else catName)
                                        },
                                        label = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "$emoji $catName",
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Surface(
                                                    color = if (isSelected) PureWhite.copy(alpha = 0.25f) else Slate200,
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text(
                                                        text = "$count",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isSelected) PureWhite else Slate600,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
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
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .height(30.dp)
                                            .testTag("category_chip_${catName.replace(" ", "_")}")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(5.dp))

                            // Row 2: States
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 14.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                states.forEach { stateName ->
                                    val isSelected = if (stateName == "All India") {
                                        uiState.selectedState == "All India" || uiState.selectedState == "ALL" || uiState.selectedState.isBlank()
                                    } else {
                                        uiState.selectedState.equals(stateName, ignoreCase = true)
                                    }

                                    val count = stateCounts[stateName] ?: 0

                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            viewModel.setStateFilter(stateName)
                                        },
                                        label = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = stateName,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                )
                                                if (count > 0 && stateName != "All India") {
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Surface(
                                                        color = if (isSelected) PureWhite.copy(alpha = 0.25f) else Slate200,
                                                        shape = RoundedCornerShape(8.dp)
                                                    ) {
                                                        Text(
                                                            text = "$count",
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isSelected) PureWhite else Slate600,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                            }
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
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .height(29.dp)
                                            .testTag("state_chip_${stateName.replace(" ", "_")}")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Summary Bar & Reset Action
            item(key = "filter_summary_bar") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(EmeraldMint)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "${filteredJobs.size} Verified Vacancies",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate700
                        )
                    }

                    if (isAnyFilterActive) {
                        Surface(
                            onClick = {
                                viewModel.setSearchQuery("")
                                viewModel.setCategoryFilter("All Categories")
                                viewModel.setStateFilter("All India")
                                viewModel.setStatusFilter("ALL")
                            },
                            color = Color(0xFFFEF2F2),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFFECACA)),
                            modifier = Modifier.testTag("clear_all_filters_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Reset Filters",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDC2626)
                                )
                            }
                        }
                    }
                }
            }

            // Smart Results/Admit Card Redirection Banner when search touches exam cards
            val query = uiState.searchQuery.trim().lowercase()
            if (query.contains("admit") || query.contains("card") || query.contains("result") || query.contains("score") || query.contains("key")) {
                item(key = "redirect_to_results_banner") {
                    Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFEFF6FF),
                            border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                            onClick = { viewModel.setTab(MainTab.RESULTS) },
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
                                        .background(Color(0xFFDBEAFE)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Assignment,
                                        contentDescription = null,
                                        tint = RoyalBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Looking for Admit Cards or Exam Results?",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepNavy
                                    )
                                    Text(
                                        text = "Tap to switch to the Results & Cards tab →",
                                        fontSize = 10.5.sp,
                                        color = Slate600
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Empty State
            if (filteredJobs.isEmpty()) {
                item(key = "empty_state") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp, vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(NavyContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (uiState.selectedCategory == "Favorites") Icons.Default.Bookmark else Icons.Default.WorkOutline,
                                    contentDescription = null,
                                    tint = if (uiState.selectedCategory == "Favorites") GoldStar else DeepNavy,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = if (uiState.selectedCategory == "Favorites") "No Bookmarked Vacancies" else "No matching jobs found",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900,
                                    fontSize = 15.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (uiState.selectedCategory == "Favorites")
                                    "Tap the bookmark icon ⭐ on any job card to save it for offline tracking and instant alerts."
                                else
                                    "Try clearing state or category filters to view other live recruitment notices.",
                                fontSize = 11.5.sp,
                                color = Slate500,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    viewModel.setSearchQuery("")
                                    viewModel.setCategoryFilter("All Categories")
                                    viewModel.setStateFilter("All India")
                                    viewModel.setStatusFilter("ALL")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(if (uiState.selectedCategory == "Favorites") "Browse All Openings" else "Reset All Filters", fontSize = 12.sp)
                            }
                        }
                    }
                }
            } else {
                // Verified Job Vacancy Cards
                itemsIndexed(
                    items = filteredJobs,
                    key = { _, job -> "job_${job.id}" },
                    contentType = { _, _ -> "job_opening_card" }
                ) { index, job ->
                    Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)) {
                        GovernmentJobOpeningCard(
                            job = job,
                            onClick = { onJobClick(job) },
                            onBookmarkToggle = { viewModel.toggleBookmark(job) }
                        )
                    }

                    // In-feed Native Sponsored Cards every 4 items
                    if (index > 0 && (index + 1) % 4 == 0) {
                        Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)) {
                            NativeSponsoredJobCard(creativeIndex = index / 4)
                        }
                    }
                }
            }
        }

        // Quick "Back to Top" Floating Action Button
        AnimatedVisibility(
            visible = showScrollToTop,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 116.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
                    }
                },
                containerColor = DeepNavy,
                contentColor = PureWhite,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .size(44.dp)
                    .testTag("scroll_to_top_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Scroll to top",
                    tint = PureWhite,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Modern, responsive GovernmentJobOpeningCard.
 * Cleaned up with strictly Bookmark toggle button and View Details button (as confirmed by user),
 * completely fixing text overflow and badge collisions.
 */
@Composable
fun GovernmentJobOpeningCard(
    job: JobEntity,
    onClick: () -> Unit,
    onBookmarkToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (orgMonogram, gradientColors) = JobBrandHelper.getBrand(job.organization, job.category, job.title)

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 1.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Slate200.copy(alpha = 0.8f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("job_opening_card_${job.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Organization Monogram + Org details + Status pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Brush.linearGradient(gradientColors)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = orgMonogram,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PureWhite
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = job.organization,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate900,
                                fontSize = 13.5.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 1.dp)
                        ) {
                            Text(
                                text = "Advt: ${job.advtNumber}",
                                fontSize = 10.5.sp,
                                color = Slate500,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (job.isVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified Official Source",
                                    tint = VerifiedBlue,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "Verified",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = VerifiedBlue,
                                    modifier = Modifier.padding(start = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Dynamic Status Pill
                when (job.status) {
                    "CLOSING_SOON" -> {
                        Surface(
                            color = StatusUrgentBg,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(StatusUrgent)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Closing Soon",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusUrgent
                                )
                            }
                        }
                    }
                    "UPCOMING" -> {
                        Surface(
                            color = StatusUpcomingBg,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(StatusUpcoming)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Upcoming",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusUpcoming
                                )
                            }
                        }
                    }
                    else -> {
                        Surface(
                            color = GreenContainer,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldMint)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Active",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnGreenContainer
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Job Title
            Text(
                text = job.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.5.sp,
                    color = Slate900,
                    lineHeight = 19.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Responsive Badges Row: Vacancies, Qualification, Location
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Vacancy Badge
                Surface(
                    color = Slate100,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = null,
                            tint = RoyalBlue,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${job.totalVacancies} Posts",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate800
                        )
                    }
                }

                // Qualification Badge (weight ensures clean ellipsis, preventing wrap clipping)
                Surface(
                    color = Slate100,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = Slate600,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = job.qualification,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Slate700,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // State / All India Badge
                Surface(
                    color = Slate100,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "📍 ${job.stateOrAllIndia}",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate700,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Application Deadline & Clean Actions Footer
            Surface(
                color = if (job.status == "CLOSING_SOON") StatusUrgentBg.copy(alpha = 0.5f) else Color(0xFFF8FAFC),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Deadline indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = "Deadline",
                            tint = if (job.status == "CLOSING_SOON") StatusUrgent else RoyalBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = "Last Date to Apply",
                                fontSize = 9.sp,
                                color = Slate500
                            )
                            Text(
                                text = job.applicationClosingDate,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (job.status == "CLOSING_SOON") StatusUrgent else DeepNavy,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // User Confirmed Actions: Bookmark Icon Button + Primary Details Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        IconButton(
                            onClick = onBookmarkToggle,
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("bookmark_btn_${job.id}")
                        ) {
                            Icon(
                                imageVector = if (job.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = if (job.isBookmarked) "Remove bookmark" else "Bookmark job",
                                tint = if (job.isBookmarked) GoldStar else Slate400,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Button(
                            onClick = onClick,
                            colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 3.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("job_opening_card_btn_${job.id}")
                        ) {
                            Text(
                                text = "Details",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
