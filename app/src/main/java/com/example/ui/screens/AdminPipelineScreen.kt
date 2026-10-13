package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.model.AuditLogItem
import com.example.model.ContentSource
import com.example.model.ImportCandidate
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GreenContainer
import com.example.ui.theme.NavyContainer
import com.example.ui.theme.OnGreenContainer
import com.example.ui.theme.PureWhite
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
import com.example.ui.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPipelineScreen(
    viewModel: AdminViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.dashboardStats.collectAsState()
    val sources by viewModel.sources.collectAsState()
    val candidates by viewModel.candidates.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()
    val statusMsg by viewModel.actionStatusMessage.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Dashboard", "Review Queue (${candidates.count { it.reviewStatus == "PENDING_REVIEW" }})", "Sources", "Add Job", "Audit Logs")

    // Manual Job Form State
    var newTitle by remember { mutableStateOf("") }
    var newOrg by remember { mutableStateOf("") }
    var newAdvtNo by remember { mutableStateOf("") }
    var newState by remember { mutableStateOf("All India") }
    var newCategory by remember { mutableStateOf("Central Govt") }
    var newVacancies by remember { mutableStateOf("500") }
    var newQual by remember { mutableStateOf("Graduate Degree") }
    var newSalary by remember { mutableStateOf("Level 7 (₹44,900 - ₹1,42,400)") }
    var newClosingDate by remember { mutableStateOf("30-11-2026") }
    var newPdfUrl by remember { mutableStateOf("https://official.gov.in/notice.pdf") }
    var newApplyUrl by remember { mutableStateOf("https://official.gov.in/apply") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = PureWhite,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Admin & Verified Pipeline",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PureWhite
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshStats() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = PureWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepNavy,
                    titleContentColor = PureWhite
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(innerPadding)
        ) {
            // Action Notification Banner
            if (statusMsg != null) {
                Surface(
                    color = GreenContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = statusMsg ?: "",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnGreenContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearActionStatus() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = OnGreenContainer)
                        }
                    }
                }
            }

            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = PureWhite,
                contentColor = DeepNavy,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = DeepNavy,
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        modifier = Modifier.testTag("admin_subtab_$index")
                    )
                }
            }

            when (selectedTab) {
                // 0: Dashboard Stats
                0 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Portal Metric & Ingestion Overview",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatCard(title = "Published Jobs", value = "${stats.totalPublishedJobs}", modifier = Modifier.weight(1f))
                            StatCard(title = "Active Applications", value = "${stats.activeApplications}", modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatCard(title = "Upcoming Deadlines", value = "${stats.upcomingDeadlines}", isAlert = true, modifier = Modifier.weight(1f))
                            StatCard(title = "Pending Imports", value = "${stats.pendingImports}", modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatCard(title = "Active Sources", value = "${stats.activeSources}", modifier = Modifier.weight(1f))
                            StatCard(title = "Results Published", value = "${stats.totalResults}", modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Slate200),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Automated Crawler Health",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "All 5 official source adapters (UPSC, SSC, RRB, IBPS, UPPRPB) are actively monitoring official gazette domains with SHA-256 duplicate checksum verification.",
                                    fontSize = 12.sp,
                                    color = Slate600,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                }

                // 1: Review Queue
                1 -> {
                    val pending = candidates.filter { it.reviewStatus == "PENDING_REVIEW" }
                    if (pending.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Review Queue Clear",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                                Text(
                                    text = "All discovered candidates have been reviewed and published.",
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
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            pending.forEach { cand ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, Slate200),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                color = NavyContainer,
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = cand.sourceDomain,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = DeepNavy,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Text(
                                                text = "Advt: ${cand.externalId}",
                                                fontSize = 11.sp,
                                                color = Slate500
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = cand.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Slate900
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = "Extracted: ${cand.parsedVacancies} Vacancies • Closes: ${cand.parsedClosingDate}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = DeepNavy
                                        )

                                        Text(
                                            text = "Checksum: ${cand.contentHash.take(24)}...",
                                            fontSize = 10.sp,
                                            color = Slate400
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = { viewModel.rejectCandidate(cand) },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusUrgent),
                                                border = BorderStroke(1.dp, StatusUrgent),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Reject", fontSize = 11.sp)
                                            }

                                            Button(
                                                onClick = { viewModel.approveCandidate(cand) },
                                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1.5f)
                                            ) {
                                                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Approve & Publish", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 2: Sources Registry
                2 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Official Source Adapters & Whitelist",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        sources.forEach { src ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = PureWhite),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, Slate200),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = src.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Slate900
                                        )
                                        Text(
                                            text = "${src.officialDomain} • ${src.sourceType}",
                                            fontSize = 11.sp,
                                            color = Slate500
                                        )
                                        Text(
                                            text = "Last Synced: ${src.lastSyncTime}",
                                            fontSize = 10.sp,
                                            color = Slate600
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = { viewModel.triggerPollSource(src) },
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, DeepNavy)
                                    ) {
                                        Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp), tint = DeepNavy)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Poll Now", fontSize = 11.sp, color = DeepNavy)
                                    }
                                }
                            }
                        }
                    }
                }

                // 3: Add Job
                3 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                            .padding(bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Publish Verified Recruitment",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )

                        OutlinedTextField(
                            value = newTitle,
                            onValueChange = { newTitle = it },
                            label = { Text("Recruitment Title") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newOrg,
                            onValueChange = { newOrg = it },
                            label = { Text("Recruiting Organization") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newAdvtNo,
                            onValueChange = { newAdvtNo = it },
                            label = { Text("Advertisement Number") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = newState,
                                onValueChange = { newState = it },
                                label = { Text("State / Scope") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = newVacancies,
                                onValueChange = { newVacancies = it },
                                label = { Text("Total Vacancies") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        OutlinedTextField(
                            value = newQual,
                            onValueChange = { newQual = it },
                            label = { Text("Required Qualification") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newClosingDate,
                            onValueChange = { newClosingDate = it },
                            label = { Text("Application Closing Date") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newPdfUrl,
                            onValueChange = { newPdfUrl = it },
                            label = { Text("Official Gazette PDF URL") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newApplyUrl,
                            onValueChange = { newApplyUrl = it },
                            label = { Text("Apply Online Portal URL") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                if (newTitle.isNotBlank() && newOrg.isNotBlank()) {
                                    viewModel.publishManualJob(
                                        title = newTitle,
                                        org = newOrg,
                                        advtNo = newAdvtNo.ifBlank { "ADV-2026/01" },
                                        state = newState,
                                        category = newCategory,
                                        vacancies = newVacancies.toIntOrNull() ?: 100,
                                        qual = newQual,
                                        minAge = 18,
                                        maxAge = 35,
                                        salary = newSalary,
                                        closeDate = newClosingDate,
                                        officialPdf = newPdfUrl,
                                        applyUrl = newApplyUrl
                                    )
                                    newTitle = ""
                                    newOrg = ""
                                    newAdvtNo = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Validate & Publish Recruitment")
                        }
                    }
                }

                // 4: Audit Logs
                4 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Immutable Verification Audit Logs",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )

                        auditLogs.forEach { log ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = PureWhite,
                                border = BorderStroke(1.dp, Slate200),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = log.action,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DeepNavy
                                        )
                                        Text(
                                            text = log.timestamp,
                                            fontSize = 10.sp,
                                            color = Slate500
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = log.details,
                                        fontSize = 12.sp,
                                        color = Slate700
                                    )
                                    Text(
                                        text = "Actor: ${log.actor}",
                                        fontSize = 10.sp,
                                        color = Slate500
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

@Composable
private fun StatCard(
    title: String,
    value: String,
    isAlert: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Slate200),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, fontSize = 11.sp, color = Slate500)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isAlert) StatusUrgent else DeepNavy
            )
        }
    }
}
