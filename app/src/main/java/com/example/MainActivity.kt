package com.example

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.JobEntity
import com.example.ui.components.AgeCalculatorSheet
import com.example.ui.components.AppBottomBar
import com.example.ui.components.AppTopBar
import com.example.ui.components.LanguageSelectorSheet
import com.example.ui.components.NotificationsSheet
import com.example.ui.components.ReportDialog
import com.example.ui.components.SalaryCalculatorSheet
import com.example.ui.screens.AiMentorScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.JobAlertPreferencesScreen
import com.example.ui.screens.JobDetailScreen
import com.example.ui.screens.JobListingScreen
import com.example.ui.screens.JobsScreen
import android.app.Activity
import androidx.compose.foundation.layout.Column
import com.example.ads.AdManager
import com.example.ads.AdaptiveBannerAdView
import com.example.ui.screens.InAppBrowserScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ResultsAndCardsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AiMentorViewModel
import com.example.ui.viewmodel.JobViewModel
import com.example.ui.viewmodel.MainTab
import com.example.util.AppUtils
import com.example.util.NotificationPermission

class MainActivity : ComponentActivity() {
    /** Job requested by a notification tap; consumed once by the UI. */
    private val pendingJobId: MutableState<String?> = mutableStateOf(null)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val id = intent?.getStringExtra(EXTRA_OPEN_JOB_ID)
        if (!id.isNullOrBlank() && id.length <= 200) pendingJobId.value = id
    }

    companion object {
        const val EXTRA_OPEN_JOB_ID = "com.example.extra.OPEN_JOB_ID"

        /** Intent that opens the app on a specific job. Data URI keeps PendingIntents distinct per job. */
        fun openJobIntent(context: Context, jobId: String): Intent =
            Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                data = Uri.Builder().scheme("sarkari").authority("job").appendPath(jobId).build()
                putExtra(EXTRA_OPEN_JOB_ID, jobId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) handleIntent(intent)
        // Initialize Google Mobile Ads SDK with background preloads
        AdManager.initialize(this)

        enableEdgeToEdge(
            statusBarStyle = androidx.activity.SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = androidx.activity.SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        setContent {
            MyApplicationTheme {
                MainAppContent(
                    pendingJobId = pendingJobId.value,
                    onPendingJobConsumed = { pendingJobId.value = null }
                )
            }
        }
    }
}

@Composable
fun MainAppContent(
    pendingJobId: String? = null,
    onPendingJobConsumed: () -> Unit = {}
) {
    val jobViewModel: JobViewModel = viewModel()
    val aiMentorViewModel: AiMentorViewModel = viewModel()

    val uiState by jobViewModel.uiState.collectAsState()
    val userProfile by jobViewModel.userProfile.collectAsState()
    val allJobs by jobViewModel.allJobs.collectAsState()
    val filteredJobs by jobViewModel.filteredJobs.collectAsState()
    val closingSoonJobs by jobViewModel.closingSoonJobs.collectAsState()
    val bookmarkedJobs by jobViewModel.bookmarkedJobs.collectAsState()
    val results by jobViewModel.results.collectAsState()
    val admitCards by jobViewModel.admitCards.collectAsState()
    val syllabi by jobViewModel.syllabi.collectAsState()
    val answerKeys by jobViewModel.answerKeys.collectAsState()
    val admissions by jobViewModel.admissions.collectAsState()
    val notifications by jobViewModel.notifications.collectAsState()
    val unreadNotifCount by jobViewModel.unreadNotifCount.collectAsState()
    val jobAlertPreferences by jobViewModel.jobAlertPreferences.collectAsState()
    val matchedJobs by jobViewModel.matchedJobs.collectAsState()
    val backendSyncState by jobViewModel.backendSyncState.collectAsState()
    val documentLocker by jobViewModel.documentLocker.collectAsState()
    val appliedExams by jobViewModel.appliedExams.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showAgeCalculator by remember { mutableStateOf(false) }
    var showSalaryCalculator by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.reportSubmittedMessage) {
        val msg = uiState.reportSubmittedMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            jobViewModel.clearReportMessage()
        }
    }

    // Route links into the in-app browser while this screen is alive; clear on dispose
    // so the global hook never outlives (and leaks) the ViewModel.
    DisposableEffect(jobViewModel) {
        AppUtils.onOpenInAppBrowser = { url, title ->
            jobViewModel.openInAppBrowser(url, title)
        }
        onDispose { AppUtils.onOpenInAppBrowser = null }
    }

    // Notification tap -> open that job.
    LaunchedEffect(pendingJobId) {
        if (pendingJobId != null) {
            jobViewModel.openJobById(pendingJobId)
            onPendingJobConsumed()
        }
    }

    // Ask for notification permission once (Android 13+), so deadline reminders can be shown.
    val permissionContext = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Result is read on demand via NotificationPermission.isGranted */ }
    LaunchedEffect(Unit) {
        val prefs = permissionContext.getSharedPreferences("app_prompts", Context.MODE_PRIVATE)
        if (NotificationPermission.needsRuntimeRequest(permissionContext) &&
            !prefs.getBoolean("asked_post_notifications", false)
        ) {
            prefs.edit().putBoolean("asked_post_notifications", true).apply()
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // System Back on a secondary tab returns to Home instead of exiting the app.
    BackHandler(
        enabled = uiState.currentTab != MainTab.HOME &&
            uiState.selectedJob == null &&
            uiState.inAppBrowserState == null &&
            !uiState.showJobAlertsScreen
    ) {
        jobViewModel.setTab(MainTab.HOME)
    }
    BackHandler(enabled = uiState.showJobAlertsScreen && uiState.selectedJob == null && uiState.inAppBrowserState == null) {
        jobViewModel.toggleJobAlertsScreen(false)
    }

    // Modal Sheets
    if (uiState.showLanguageSheet) {
        LanguageSelectorSheet(
            currentLanguage = uiState.language,
            onSelectLanguage = { jobViewModel.setLanguage(it) },
            onDismiss = { jobViewModel.toggleLanguageSheet(false) }
        )
    }

    if (uiState.showNotificationsSheet) {
        NotificationsSheet(
            notifications = notifications,
            onMarkAllRead = { jobViewModel.markAllNotificationsAsRead() },
            onDismiss = { jobViewModel.toggleNotificationsSheet(false) },
            onSyncDeadlines = { jobViewModel.syncAllBookmarkedDeadlines() },
            onOpenAlertPreferences = { jobViewModel.toggleJobAlertsScreen(true) }
        )
    }

    val context = LocalContext.current
    val activity = context as? Activity
    val handleJobClick: (JobEntity) -> Unit = { job ->
        if (activity != null) {
            AdManager.showInterstitialIfAllowed(activity) {
                jobViewModel.selectJob(job)
            }
        } else {
            jobViewModel.selectJob(job)
        }
    }

    if (showAgeCalculator) {
        AgeCalculatorSheet(
            allJobs = allJobs,
            onSelectJob = { job ->
                showAgeCalculator = false
                handleJobClick(job)
            },
            onDismiss = { showAgeCalculator = false }
        )
    }

    if (showSalaryCalculator) {
        SalaryCalculatorSheet(
            initialLevel = 7,
            onDismiss = { showSalaryCalculator = false }
        )
    }

    // Report Discrepancy Dialog
    val reportingJob = uiState.reportingJob
    if (reportingJob != null) {
        ReportDialog(
            job = reportingJob,
            onDismiss = { jobViewModel.dismissReportDialog() },
            onSubmit = { reason, details -> jobViewModel.submitReport(reason, details) }
        )
    }

    // In-App WebView Browser Overlay (highest priority over main app and modals)
    val inAppBrowser = uiState.inAppBrowserState
    if (inAppBrowser != null) {
        InAppBrowserScreen(
            state = inAppBrowser,
            onClose = { jobViewModel.closeInAppBrowser() }
        )
        return
    }

    // Detail Screen Overlay
    val selectedJob = uiState.selectedJob
    if (selectedJob != null) {
        JobDetailScreen(
            job = selectedJob,
            language = uiState.language,
            onBack = { jobViewModel.selectJob(null) },
            onBookmarkToggle = { jobViewModel.toggleBookmark(selectedJob) },
            onCheckEligibilityWithAi = {
                aiMentorViewModel.evaluateJobEligibility(userProfile, selectedJob)
                jobViewModel.selectJob(null)
                jobViewModel.setTab(MainTab.AI_MENTOR)
            },
            onReportIssue = { jobViewModel.openReportDialog(selectedJob) }
        )
        return
    }

    // Targeted Job Alert Preferences Overlay
    if (uiState.showJobAlertsScreen) {
        JobAlertPreferencesScreen(
            currentPreferences = jobAlertPreferences,
            matchedJobs = matchedJobs,
            onSavePreferences = { jobViewModel.updateJobAlertPreferences(it) },
            onSendTestAlert = { jobViewModel.sendTestJobAlert() },
            onJobClick = { job ->
                jobViewModel.toggleJobAlertsScreen(false)
                handleJobClick(job)
            },
            onBack = { jobViewModel.toggleJobAlertsScreen(false) }
        )
        return
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            AppTopBar(
                language = uiState.language,
                unreadNotifCount = unreadNotifCount,
                onLanguageClick = { jobViewModel.toggleLanguageSheet(true) },
                onNotificationsClick = { jobViewModel.toggleNotificationsSheet(true) }
            )
        },
        bottomBar = {
            Column {
                AdaptiveBannerAdView()
                AppBottomBar(
                    currentTab = uiState.currentTab,
                    language = uiState.language,
                    onTabSelected = { jobViewModel.setTab(it) }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.currentTab) {
                MainTab.HOME -> {
                    HomeScreen(
                        language = uiState.language,
                        searchQuery = uiState.searchQuery,
                        onSearchChange = {
                            jobViewModel.setSearchQuery(it)
                            if (it.isNotBlank()) jobViewModel.setTab(MainTab.JOBS)
                        },
                        closingSoonJobs = closingSoonJobs,
                        allJobs = allJobs,
                        onJobClick = handleJobClick,
                        onBookmarkToggle = { jobViewModel.toggleBookmark(it) },
                        onNavigateTab = { jobViewModel.setTab(it) },
                        onFilterByState = {
                            jobViewModel.setStateFilter(it)
                            jobViewModel.setTab(MainTab.JOBS)
                        },
                        onFilterByCategory = {
                            jobViewModel.setCategoryFilter(it)
                            jobViewModel.setTab(MainTab.JOBS)
                        },
                        onOpenAgeCalculator = { showAgeCalculator = true },
                        onOpenSalaryCalculator = { showSalaryCalculator = true },
                        onOpenJobAlerts = { jobViewModel.toggleJobAlertsScreen(true) },
                        onOpenResultsSubTab = { jobViewModel.setResultsSubTab(it) },
                        matchedJobsCount = matchedJobs.size,
                        isJobAlertsEnabled = jobAlertPreferences.isEnabled
                    )
                }

                MainTab.JOBS -> {
                    JobListingScreen(
                        viewModel = jobViewModel,
                        onJobClick = handleJobClick
                    )
                }

                MainTab.RESULTS -> {
                    ResultsAndCardsScreen(
                        language = uiState.language,
                        results = results,
                        admitCards = admitCards,
                        syllabi = syllabi,
                        answerKeys = answerKeys,
                        admissions = admissions,
                        initialSubTab = uiState.selectedResultsSubTab,
                        onSubTabChange = { jobViewModel.setResultsSubTab(it) }
                    )
                }

                MainTab.AI_MENTOR -> {
                    AiMentorScreen(
                        userProfile = userProfile,
                        allJobs = allJobs,
                        viewModel = aiMentorViewModel,
                        onNavigateToProfile = { jobViewModel.setTab(MainTab.PROFILE) }
                    )
                }

                MainTab.PROFILE -> {
                    val context = LocalContext.current
                    ProfileScreen(
                        userProfile = userProfile,
                        onUpdateProfile = { jobViewModel.updateUserProfile(it) },
                        bookmarkedJobs = bookmarkedJobs,
                        language = uiState.language,
                        onOpenLanguageSheet = { jobViewModel.toggleLanguageSheet(true) },
                        onJobClick = { jobViewModel.selectJob(it) },
                        onBookmarkToggle = { jobViewModel.toggleBookmark(it) },
                        onOpenJobAlerts = { jobViewModel.toggleJobAlertsScreen(true) },
                        onNavigateToJobs = { jobViewModel.setTab(MainTab.JOBS) },
                        documentLocker = documentLocker,
                        onUpdateDocumentLocker = { jobViewModel.updateDocumentLocker(it) },
                        appliedExams = appliedExams,
                        onAddAppliedExam = { jobViewModel.addAppliedExam(it) },
                        onUpdateExamStage = { id, stage, notes -> jobViewModel.updateExamStage(id, stage, notes) },
                        onDeleteAppliedExam = { jobViewModel.deleteAppliedExam(it) },
                        onShareDossier = { jobViewModel.shareAspirantDossier(context) },
                        backendSyncState = backendSyncState,
                        onRefreshData = { jobViewModel.refreshData() },
                        allJobsCount = allJobs.size,
                        resultsCount = results.size,
                        admitCardsCount = admitCards.size,
                        syllabiCount = syllabi.size,
                        admissionsCount = admissions.size
                    )
                }
            }
        }
    }
}
