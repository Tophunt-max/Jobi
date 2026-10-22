package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
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
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ResultsAndCardsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AiMentorViewModel
import com.example.ui.viewmodel.JobViewModel
import com.example.ui.viewmodel.MainTab

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                MainAppContent()
            }
        }
    }
}

@Composable
fun MainAppContent() {
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

                MainTab.ADMIN -> {
                    LaunchedEffect(Unit) {
                        jobViewModel.setTab(MainTab.PROFILE)
                    }
                }
            }
        }
    }
}
