package com.example.ui.screens

import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import com.example.data.remote.firestore.SyncStatus
import com.example.data.repository.CloudflareSyncState
import com.example.ui.components.CloudflareStatusDialog

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
    val localSources by viewModel.sources.collectAsState()
    val localCandidates by viewModel.candidates.collectAsState()
    val cfSources by viewModel.cloudflareSources.collectAsState()
    val cfPendingJobs by viewModel.cloudflarePendingJobs.collectAsState()
    val cfImportRuns by viewModel.cloudflareImportRuns.collectAsState()
    val isImporting by viewModel.isCloudflareImporting.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()
    val statusMsg by viewModel.actionStatusMessage.collectAsState()
    val cfSyncState by viewModel.cloudflareSyncState.collectAsState()
    val cfWorkerUrl by viewModel.cloudflareWorkerUrl.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    var showCloudflareDialog by remember { mutableStateOf(false) }
    var editedWorkerUrl by remember(cfWorkerUrl) { mutableStateOf(cfWorkerUrl) }

    val pendingLocalCount = localCandidates.count { it.reviewStatus == "PENDING_REVIEW" }
    val totalPendingCount = cfPendingJobs.size + pendingLocalCount

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        "Dashboard",
        "Review Queue ($totalPendingCount)",
        "Sources (${if (cfSources.isNotEmpty()) cfSources.size else localSources.size})",
        "Backend & Edge",
        "Import History",
        "Add Job",
        "Audit Logs"
    )

    if (showCloudflareDialog) {
        CloudflareStatusDialog(
            currentWorkerUrl = cfWorkerUrl,
            syncState = cfSyncState,
            onDismiss = { showCloudflareDialog = false },
            onSaveUrl = { viewModel.setCloudflareWorkerUrl(it) },
            onSyncNow = { viewModel.syncCloudflareNow() }
        )
    }

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
                        Column {
                            Text(
                                text = "Admin Portal & Importer",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite
                                )
                            )
                            Text(
                                text = "Cloudflare Workers + D1 Backend",
                                fontSize = 10.sp,
                                color = SaffronOrange
                            )
                        }
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
                    IconButton(onClick = {
                        viewModel.refreshStats()
                        viewModel.refreshCloudflareData()
                    }) {
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
                // 0: Dashboard Stats & Automated Importer Control
                0 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        // Automated Importer Hero Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DeepNavy),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = SaffronOrange,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Cloudflare Automated Importer",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PureWhite
                                        )
                                    }
                                    Surface(
                                        color = EmeraldGreen.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "Cron: Every 6h",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldGreen,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Automated crawler for All India (SSC, UPSC, RRB, IBPS, SBI, KVS) & Odisha (OPSC, OSSC, OSSSC) with SHA-256 deduplication and approval queue.",
                                    fontSize = 12.sp,
                                    color = Slate200,
                                    lineHeight = 17.sp
                                )

                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = { viewModel.runCloudflareImport(null) },
                                    enabled = !isImporting,
                                    colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("run_importer_button")
                                ) {
                                    if (isImporting) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = DeepNavy,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Fetching Official Sources...",
                                            fontWeight = FontWeight.Bold,
                                            color = DeepNavy
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Sync,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = DeepNavy
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Run Importer Now",
                                            fontWeight = FontWeight.Bold,
                                            color = DeepNavy
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Cloudflare Edge & Database Sync Engine Card
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
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Storage,
                                            contentDescription = null,
                                            tint = DeepNavy,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Edge & Backend Infrastructure",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Slate900
                                        )
                                    }
                                    Surface(
                                        color = when (cfSyncState) {
                                            is CloudflareSyncState.Synced -> GreenContainer
                                            is CloudflareSyncState.Syncing -> Color(0xFFFEF3C7)
                                            else -> Slate100
                                        },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = when (cfSyncState) {
                                                is CloudflareSyncState.Synced -> "D1+R2 ACTIVE"
                                                is CloudflareSyncState.Syncing -> "SYNCING..."
                                                else -> "EDGE READY"
                                            },
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (cfSyncState) {
                                                is CloudflareSyncState.Synced -> OnGreenContainer
                                                is CloudflareSyncState.Syncing -> Color(0xFFB45309)
                                                else -> Slate700
                                            },
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        color = when (syncStatus) {
                                            is SyncStatus.Synced -> GreenContainer
                                            is SyncStatus.Syncing -> Color(0xFFFEF3C7)
                                            else -> Slate100
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = when (syncStatus) {
                                                    is SyncStatus.Synced -> Icons.Default.CloudDone
                                                    is SyncStatus.Syncing -> Icons.Default.CloudSync
                                                    else -> Icons.Default.CloudOff
                                                },
                                                contentDescription = null,
                                                tint = when (syncStatus) {
                                                    is SyncStatus.Synced -> OnGreenContainer
                                                    is SyncStatus.Syncing -> Color(0xFFB45309)
                                                    else -> Slate600
                                                },
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = when (syncStatus) {
                                                    is SyncStatus.Synced -> "Firestore Synced"
                                                    is SyncStatus.Syncing -> "Syncing..."
                                                    else -> "Room DB Cache"
                                                },
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Slate800
                                            )
                                        }
                                    }

                                    Surface(
                                        color = Color(0xFFFFF7ED),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Cloud,
                                                contentDescription = null,
                                                tint = Color(0xFFEA580C),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Cloudflare D1 SQL",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFFEA580C)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { showCloudflareDialog = true },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(imageVector = Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Configure URL", fontSize = 11.sp)
                                    }

                                    Button(
                                        onClick = { viewModel.syncCloudflareNow() },
                                        colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Sync Edge Now", fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "Portal Metric & Ingestion Overview",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))

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
                            StatCard(title = "Pending Review", value = "$totalPendingCount", modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatCard(title = "Verified Sources", value = "${if (cfSources.isNotEmpty()) cfSources.size else stats.activeSources}", modifier = Modifier.weight(1f))
                            StatCard(title = "Results Published", value = "${stats.totalResults}", modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Workflow Steps Summary Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Slate200),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Publishing Workflow Cycle",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    WorkflowChip(step = "1", title = "Auto Fetch")
                                    WorkflowChip(step = "2", title = "Deduplicate")
                                    WorkflowChip(step = "3", title = "Admin Review")
                                    WorkflowChip(step = "4", title = "Publish + Push")
                                }
                            }
                        }
                    }
                }

                // 1: Review Queue (Pending Approval)
                1 -> {
                    val pendingLocal = localCandidates.filter { it.reviewStatus == "PENDING_REVIEW" }
                    if (cfPendingJobs.isEmpty() && pendingLocal.isEmpty()) {
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
                                    text = "All fetched official notifications have been reviewed and published.",
                                    fontSize = 12.sp,
                                    color = Slate500
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedButton(
                                    onClick = { viewModel.runCloudflareImport(null) },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Fetch New Notices from Sources")
                                }
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
                            Text(
                                text = "Pending Official Notices Awaiting Approval ($totalPendingCount)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )

                            // Cloudflare D1 Pending Jobs
                            cfPendingJobs.forEach { job ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, Slate200),
                                    modifier = Modifier.fillMaxWidth().testTag("pending_job_${job.id}")
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Surface(
                                                    color = if (job.state == "Odisha") SaffronOrange.copy(alpha = 0.15f) else NavyContainer,
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = job.state ?: "All India",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (job.state == "Odisha") Color(0xFFC25500) else DeepNavy,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Surface(
                                                    color = Slate100,
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = job.sourceDomain?.ifBlank { "official.gov.in" } ?: "official.gov.in",
                                                        fontSize = 10.sp,
                                                        color = Slate600,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "Advt: ${job.advertisementNumber ?: "Pending"}",
                                                fontSize = 10.sp,
                                                color = Slate500
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = job.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Slate900
                                        )
                                        Text(
                                            text = job.organization?.ifBlank { job.department } ?: "Government Department",
                                            fontSize = 12.sp,
                                            color = Slate600
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            Text(
                                                text = "Vacancies: ${job.totalPosts ?: "Check Notice"}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = DeepNavy
                                            )
                                            Text(
                                                text = "Last Date: ${job.lastDate ?: "Check Notice"}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = StatusUrgent
                                            )
                                        }

                                        Text(
                                            text = "Qual: ${job.qualification ?: "As per gazette"}",
                                            fontSize = 11.sp,
                                            color = Slate600,
                                            maxLines = 2
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = { viewModel.rejectCloudflareJob(job.id, "Rejected by admin") },
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
                                                onClick = { viewModel.approveCloudflareJob(job.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1.5f).testTag("approve_btn_${job.id}")
                                            ) {
                                                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Approve & Publish", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }

                            // Local Candidates
                            pendingLocal.forEach { cand ->
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
                                        Spacer(modifier = Modifier.height(4.dp))
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

                // 2: Sources Registry (All India + Odisha)
                2 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Verified Recruitment Portals",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            Button(
                                onClick = { viewModel.runCloudflareImport(null) },
                                enabled = !isImporting,
                                colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Poll All Sources", fontSize = 11.sp)
                            }
                        }

                        // Display Cloudflare Sources or fallback to local
                        if (cfSources.isNotEmpty()) {
                            // Section: Odisha State Portals
                            val odishaSources = cfSources.filter { it.state == "Odisha" }
                            if (odishaSources.isNotEmpty()) {
                                Text(
                                    text = "Odisha State Government Sources",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SaffronOrange,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                                odishaSources.forEach { src ->
                                    SourceCard(
                                        name = src.name,
                                        domain = src.officialDomain,
                                        type = src.parserType,
                                        state = src.state,
                                        lastFetched = src.lastFetchedAt ?: "Ready",
                                        onPoll = { viewModel.runCloudflareImport(src.id) }
                                    )
                                }
                            }

                            // Section: All India Central Portals
                            val allIndiaSources = cfSources.filter { it.state != "Odisha" }
                            if (allIndiaSources.isNotEmpty()) {
                                Text(
                                    text = "All India Central Recruitment Sources",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepNavy,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                                allIndiaSources.forEach { src ->
                                    SourceCard(
                                        name = src.name,
                                        domain = src.officialDomain,
                                        type = src.parserType,
                                        state = src.state,
                                        lastFetched = src.lastFetchedAt ?: "Ready",
                                        onPoll = { viewModel.runCloudflareImport(src.id) }
                                    )
                                }
                            }
                        } else {
                            localSources.forEach { src ->
                                SourceCard(
                                    name = src.name,
                                    domain = src.officialDomain,
                                    type = src.sourceType,
                                    state = if (src.id.contains("opsc") || src.id.contains("ossc")) "Odisha" else "All India",
                                    lastFetched = src.lastSyncTime,
                                    onPoll = { viewModel.triggerPollSource(src) }
                                )
                            }
                        }
                    }
                }

                // 3: Import Runs History
                3 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Automated Crawler Runs History",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )

                        if (cfImportRuns.isEmpty()) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = PureWhite),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Slate200),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "No runs recorded on Cloudflare yet. Tap 'Run Importer Now' from the Dashboard tab to execute your first import cycle.",
                                        fontSize = 12.sp,
                                        color = Slate600
                                    )
                                }
                            }
                        } else {
                            cfImportRuns.forEach { run ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Slate200),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    color = if (run.status == "SUCCESS") GreenContainer else StatusUrgentBg,
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = run.status,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (run.status == "SUCCESS") OnGreenContainer else StatusUrgent,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = run.triggeredBy ?: "CRON",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Slate700
                                                )
                                            }
                                            Text(
                                                text = run.startedAt.take(19).replace("T", " "),
                                                fontSize = 10.sp,
                                                color = Slate500
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = "Fetched: ${run.fetchedCount}", fontSize = 11.sp, color = Slate700)
                                            Text(text = "New: ${run.insertedCount}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                                            Text(text = "Duplicates: ${run.duplicateCount}", fontSize = 11.sp, color = Slate500)
                                            Text(text = "Errors: ${run.errorCount}", fontSize = 11.sp, color = if (run.errorCount > 0) StatusUrgent else Slate400)
                                        }

                                        if (!run.errorSummary.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Details: ${run.errorSummary}",
                                                fontSize = 10.sp,
                                                color = StatusUrgent
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 4: Add Manual Job
                4 -> {
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

                // 6: Audit Logs
                6 -> {
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

@Composable
private fun WorkflowChip(step: String, title: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = NavyContainer,
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = step, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = title, fontSize = 10.sp, color = Slate600)
    }
}

@Composable
private fun SourceCard(
    name: String,
    domain: String,
    type: String,
    state: String,
    lastFetched: String,
    onPoll: () -> Unit
) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        color = if (state == "Odisha") SaffronOrange.copy(alpha = 0.15f) else NavyContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = state,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (state == "Odisha") Color(0xFFC25500) else DeepNavy,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                    Text(
                        text = "$domain • $type",
                        fontSize = 10.sp,
                        color = Slate500
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Last Synced: $lastFetched",
                    fontSize = 10.sp,
                    color = Slate600
                )
            }

            OutlinedButton(
                onClick = onPoll,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, DeepNavy)
            ) {
                Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(13.dp), tint = DeepNavy)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Poll", fontSize = 11.sp, color = DeepNavy)
            }
        }
    }
}
