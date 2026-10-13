package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.IndianStatesRegistry
import com.example.model.JobEntity
import com.example.model.LanguageRegistry
import com.example.ui.components.FilterChipsRow
import com.example.ui.components.JobCard
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.NavyContainer
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SaffronOrange
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900

@Composable
fun JobsScreen(
    language: AppLanguage,
    jobs: List<JobEntity>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedState: String,
    onStateSelect: (String) -> Unit,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit,
    selectedQualification: String,
    onQualificationSelect: (String) -> Unit,
    selectedStatus: String,
    onStatusSelect: (String) -> Unit,
    onJobClick: (JobEntity) -> Unit,
    onBookmarkToggle: (JobEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val statusTabs = listOf("ALL", "ACTIVE", "CLOSING_SOON", "UPCOMING")
    val statusLabels = listOf("All Jobs", "Active", "Closing Soon", "Upcoming")

    val categories = listOf(
        "All Categories",
        "UPSC",
        "SSC",
        "Railways",
        "Banking",
        "Police",
        "Defence",
        "Teaching",
        "State PSC",
        "Engineering",
        "10th / 12th Pass"
    )

    val qualifications = listOf(
        "All",
        "10th Pass",
        "12th Pass",
        "Graduate",
        "B.Tech/BE",
        "Post Graduate"
    )

    val states = listOf("All India") + IndianStatesRegistry.states.filter { it.code != "ALL" }.map { it.name }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Top Search Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PureWhite)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = {
                    Text(
                        text = "Filter by department, post, advt...",
                        fontSize = 13.sp,
                        color = Slate400
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Slate500
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = Slate500
                            )
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Slate100,
                    unfocusedContainerColor = Slate100,
                    focusedBorderColor = DeepNavy,
                    unfocusedBorderColor = Slate200,
                    focusedTextColor = Slate900,
                    unfocusedTextColor = Slate900
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("jobs_search_input")
            )
        }

        // Status Sub-Tabs (All, Active, Closing Soon, Upcoming)
        val selectedStatusIndex = statusTabs.indexOf(selectedStatus).coerceAtLeast(0)
        TabRow(
            selectedTabIndex = selectedStatusIndex,
            containerColor = PureWhite,
            contentColor = DeepNavy,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedStatusIndex]),
                    color = DeepNavy,
                    height = 3.dp
                )
            }
        ) {
            statusTabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedStatus == tab,
                    onClick = { onStatusSelect(tab) },
                    text = {
                        Text(
                            text = statusLabels[index],
                            fontSize = 12.sp,
                            fontWeight = if (selectedStatus == tab) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    modifier = Modifier.testTag("status_tab_$tab")
                )
            }
        }

        // Filter Rows (State, Category, Qualification)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(PureWhite)
                .padding(vertical = 8.dp)
        ) {
            // Category Filter Row
            FilterChipsRow(
                items = categories,
                selectedItem = selectedCategory,
                onItemSelected = onCategorySelect
            )

            Spacer(modifier = Modifier.height(6.dp))

            // State Selector Row
            FilterChipsRow(
                items = states,
                selectedItem = selectedState,
                onItemSelected = onStateSelect
            )
        }

        // Total Jobs Count Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${jobs.size} Verified Vacancies Found",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Slate700
            )

            if (selectedCategory != "All Categories" || selectedState != "All India" || searchQuery.isNotEmpty()) {
                Text(
                    text = "Clear Filters",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SaffronOrange,
                    modifier = Modifier.clickable {
                        onCategorySelect("All Categories")
                        onStateSelect("All India")
                        onSearchChange("")
                    }
                )
            }
        }

        // Job List or Empty State
        if (jobs.isEmpty()) {
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
                            imageVector = Icons.Default.WorkOutline,
                            contentDescription = null,
                            tint = DeepNavy,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No matching jobs found",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Try broadening your category or state filter to view other active government vacancies.",
                        fontSize = 13.sp,
                        color = Slate500,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            onCategorySelect("All Categories")
                            onStateSelect("All India")
                            onSearchChange("")
                            onStatusSelect("ALL")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                    ) {
                        Text("Reset All Filters")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(jobs, key = { it.id }) { job ->
                    JobCard(
                        job = job,
                        onClick = { onJobClick(job) },
                        onBookmarkToggle = { onBookmarkToggle(job) }
                    )
                }
            }
        }
    }
}
