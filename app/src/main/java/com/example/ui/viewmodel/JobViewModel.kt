package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.repository.JobRepository
import com.example.data.remote.firestore.JobFirestoreService
import com.example.data.remote.firestore.JobSyncCoordinator
import com.example.data.remote.firestore.SyncStatus
import com.example.data.remote.cloudflare.CloudflareConfig
import com.example.data.repository.CloudflareJobRepository
import com.example.data.repository.CloudflareSyncState
import com.example.model.AdmissionItem
import com.example.model.AdmitCardItem
import com.example.model.AnswerKeyItem
import com.example.model.AppLanguage
import com.example.model.ContentReport
import com.example.model.JobEntity
import com.example.data.local.JobAlertPreferencesManager
import com.example.data.repository.BackendDataSyncManager
import com.example.data.repository.BackendSyncState
import com.example.model.JobAlertMatcher
import com.example.model.JobAlertPreferences
import com.example.model.MatchedJob
import com.example.model.NotificationItem
import com.example.model.ResultItem
import com.example.model.SyllabusItem
import com.example.model.UserEligibilityProfile
import com.example.scheduler.DeadlineNotificationScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainTab {
    HOME,
    JOBS,
    RESULTS,
    AI_MENTOR,
    PROFILE,
    ADMIN
}

data class JobUiState(
    val language: AppLanguage = AppLanguage.ENGLISH,
    val currentTab: MainTab = MainTab.HOME,
    val selectedJob: JobEntity? = null,
    val searchQuery: String = "",
    val selectedState: String = "All India",
    val selectedCategory: String = "All Categories",
    val selectedQualification: String = "All",
    val selectedStatusFilter: String = "ALL", // ALL, ACTIVE, CLOSING_SOON, UPCOMING
    val selectedResultsSubTab: Int = 0, // 0: Results, 1: Admit Cards, 2: Syllabus, 3: Admission, 4: Answer Keys
    val isRefreshing: Boolean = false,
    val showLanguageSheet: Boolean = false,
    val showNotificationsSheet: Boolean = false,
    val showJobAlertsScreen: Boolean = false,
    val reportingJob: JobEntity? = null,
    val reportSubmittedMessage: String? = null
)

class JobViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = JobRepository(db.jobDao(), db.resultDao(), db.notificationDao())
    val firestoreService = JobFirestoreService()
    val syncCoordinator = JobSyncCoordinator(firestoreService, db.jobDao())
    val syncStatus: StateFlow<SyncStatus> = syncCoordinator.syncStatus

    val cloudflareRepo = CloudflareJobRepository(db.jobDao())
    val cloudflareSyncState: StateFlow<CloudflareSyncState> = cloudflareRepo.syncState
    val cloudflareWorkerUrl: StateFlow<String> = CloudflareConfig.workerUrlFlow
    val officialSources = cloudflareRepo.sources

    val backendSyncManager = BackendDataSyncManager(application, db.jobDao(), db.resultDao(), db.notificationDao())
    val backendSyncState: StateFlow<BackendSyncState> = backendSyncManager.syncState

    private val _uiState = MutableStateFlow(JobUiState())
    val uiState: StateFlow<JobUiState> = _uiState.asStateFlow()

    private val _userProfile = MutableStateFlow(UserEligibilityProfile())
    val userProfile: StateFlow<UserEligibilityProfile> = _userProfile.asStateFlow()

    val allJobs: StateFlow<List<JobEntity>> = repository.allJobs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkedJobs: StateFlow<List<JobEntity>> = repository.bookmarkedJobs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val closingSoonJobs: StateFlow<List<JobEntity>> = repository.closingSoonJobs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val results: StateFlow<List<ResultItem>> = repository.allResults
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val admitCards: StateFlow<List<AdmitCardItem>> = repository.allAdmitCards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val syllabi: StateFlow<List<SyllabusItem>> = repository.allSyllabi
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val answerKeys: StateFlow<List<AnswerKeyItem>> = repository.allAnswerKeys
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val admissions: StateFlow<List<AdmissionItem>> = repository.allAdmissions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationItem>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotifCount: StateFlow<Int> = repository.unreadNotifCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val alertPreferencesManager = JobAlertPreferencesManager(application)
    val jobAlertPreferences: StateFlow<JobAlertPreferences> = alertPreferencesManager.preferencesFlow

    val matchedJobs: StateFlow<List<MatchedJob>> = combine(
        allJobs,
        jobAlertPreferences
    ) { jobs, prefs ->
        JobAlertMatcher.matchJobs(jobs, prefs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredJobs: StateFlow<List<JobEntity>> = combine(
        allJobs,
        _uiState
    ) { jobs, state ->
        jobs.filter { job ->
            val matchesQuery = state.searchQuery.isBlank() ||
                job.title.contains(state.searchQuery, ignoreCase = true) ||
                job.organization.contains(state.searchQuery, ignoreCase = true) ||
                job.advtNumber.contains(state.searchQuery, ignoreCase = true) ||
                job.category.contains(state.searchQuery, ignoreCase = true) ||
                job.stateOrAllIndia.contains(state.searchQuery, ignoreCase = true)

            val matchesState = state.selectedState == "All India" ||
                state.selectedState == "ALL" ||
                job.stateOrAllIndia == "All India" ||
                job.stateOrAllIndia.contains(state.selectedState, ignoreCase = true)

            val matchesCategory = when (state.selectedCategory) {
                "All Categories", "ALL", "All" -> true
                "Latest Job", "Latest Jobs", "New Job" -> job.status == "ACTIVE" || job.status == "CLOSING_SOON" || job.status == "UPCOMING"
                "All India Govt", "All India Govt Jobs" -> job.stateOrAllIndia.equals("All India", ignoreCase = true) || job.category.equals("UPSC", ignoreCase = true) || job.category.equals("SSC", ignoreCase = true)
                "State Govt", "State Govt Jobs" -> !job.stateOrAllIndia.equals("All India", ignoreCase = true) || job.category.contains("State", ignoreCase = true)
                "Bank Jobs", "Banking" -> job.category.contains("Bank", ignoreCase = true) || job.organization.contains("Bank", ignoreCase = true) || job.organization.contains("IBPS", ignoreCase = true) || job.organization.contains("SBI", ignoreCase = true)
                "Teaching Jobs", "Teaching" -> job.category.contains("Teach", ignoreCase = true) || job.title.contains("Teacher", ignoreCase = true) || job.title.contains("TET", ignoreCase = true) || job.organization.contains("KVS", ignoreCase = true) || job.organization.contains("DSSSB", ignoreCase = true)
                "Engineering Jobs", "Engineering" -> job.category.contains("Engin", ignoreCase = true) || job.title.contains("Engineer", ignoreCase = true) || job.title.contains("Scientist", ignoreCase = true) || job.organization.contains("ISRO", ignoreCase = true) || job.organization.contains("DRDO", ignoreCase = true) || job.title.contains("JE", ignoreCase = true)
                "Railway Jobs", "Railways" -> job.category.contains("Railway", ignoreCase = true) || job.organization.contains("Railway", ignoreCase = true) || job.organization.contains("RRB", ignoreCase = true)
                "Police/Defence Jobs", "Police / Defence", "Police", "Defence" -> job.category.contains("Police", ignoreCase = true) || job.category.contains("Defence", ignoreCase = true) || job.title.contains("Police", ignoreCase = true) || job.title.contains("Constable", ignoreCase = true) || job.title.contains("SI", ignoreCase = true) || job.title.contains("Army", ignoreCase = true) || job.title.contains("NDA", ignoreCase = true) || job.organization.contains("Police", ignoreCase = true) || job.organization.contains("Army", ignoreCase = true)
                "Admit Card" -> job.category.equals("Admit Card", ignoreCase = true) || job.title.contains("Admit Card", ignoreCase = true) || job.title.contains("Hall Ticket", ignoreCase = true)
                "Result", "Results" -> job.category.equals("Result", ignoreCase = true) || job.title.contains("Result", ignoreCase = true) || job.title.contains("Scorecard", ignoreCase = true)
                else -> job.category.contains(state.selectedCategory, ignoreCase = true) || job.organization.contains(state.selectedCategory, ignoreCase = true)
            }

            val matchesQualification = state.selectedQualification == "All" ||
                job.qualification.contains(state.selectedQualification, ignoreCase = true)

            val matchesStatus = when (state.selectedStatusFilter) {
                "ACTIVE" -> job.status == "ACTIVE" || job.status == "CLOSING_SOON"
                "CLOSING_SOON" -> job.status == "CLOSING_SOON"
                "UPCOMING" -> job.status == "UPCOMING"
                else -> true
            }

            matchesQuery && matchesState && matchesCategory && matchesQualification && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        CloudflareConfig.init(application)
        viewModelScope.launch {
            repository.initializeDatabaseIfEmpty()
            // Comprehensive sync from Backend API with HTTP Disk & Room persistent caching
            backendSyncManager.syncAllData()
            // Start real-time Firestore synchronization
            syncCoordinator.startRealtimeSync(viewModelScope)
            // Synchronize with Cloudflare D1 Edge database in background
            cloudflareRepo.syncJobsFromCloudflare()
            // Fetch verified official recruitment portals from Cloudflare
            cloudflareRepo.fetchPublicSources()
            // Sync deadline reminders for bookmarked jobs
            repository.bookmarkedJobs.collect { bookmarks ->
                if (bookmarks.isNotEmpty()) {
                    DeadlineNotificationScheduler.syncDeadlinesForBookmarkedJobs(
                        application,
                        bookmarks
                    )
                }
            }
        }
    }

    fun setLanguage(language: AppLanguage) {
        _uiState.value = _uiState.value.copy(language = language, showLanguageSheet = false)
    }

    fun toggleLanguageSheet(show: Boolean) {
        _uiState.value = _uiState.value.copy(showLanguageSheet = show)
    }

    fun toggleNotificationsSheet(show: Boolean) {
        _uiState.value = _uiState.value.copy(showNotificationsSheet = show)
    }

    fun toggleJobAlertsScreen(show: Boolean) {
        _uiState.value = _uiState.value.copy(showJobAlertsScreen = show)
    }

    fun updateJobAlertPreferences(prefs: JobAlertPreferences) {
        alertPreferencesManager.savePreferences(prefs)
    }

    fun sendTestJobAlert() {
        alertPreferencesManager.sendTestJobAlertNotification(matchedJobs.value.firstOrNull())
    }

    fun setTab(tab: MainTab) {
        _uiState.value = _uiState.value.copy(currentTab = tab, selectedJob = null)
    }

    fun setResultsSubTab(subTab: Int) {
        _uiState.value = _uiState.value.copy(
            currentTab = MainTab.RESULTS,
            selectedResultsSubTab = subTab,
            selectedJob = null
        )
    }

    fun selectJob(job: JobEntity?) {
        _uiState.value = _uiState.value.copy(selectedJob = job)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setStateFilter(state: String) {
        _uiState.value = _uiState.value.copy(selectedState = state)
    }

    fun setCategoryFilter(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun setQualificationFilter(qual: String) {
        _uiState.value = _uiState.value.copy(selectedQualification = qual)
    }

    fun setStatusFilter(status: String) {
        _uiState.value = _uiState.value.copy(selectedStatusFilter = status)
    }

    fun toggleBookmark(job: JobEntity) {
        viewModelScope.launch {
            val willBeBookmarked = !job.isBookmarked
            repository.toggleBookmark(job.id, job.isBookmarked)
            
            // Automatic deadline notification scheduling on bookmark toggle
            val app = getApplication<Application>()
            if (willBeBookmarked) {
                DeadlineNotificationScheduler.scheduleDeadlineReminder(app, job, daysBefore = 3)
                DeadlineNotificationScheduler.scheduleDeadlineReminder(app, job, daysBefore = 1)
            } else {
                DeadlineNotificationScheduler.cancelDeadlineReminder(app, job.id)
            }

            // also update selectedJob if it's currently open
            if (_uiState.value.selectedJob?.id == job.id) {
                _uiState.value = _uiState.value.copy(
                    selectedJob = _uiState.value.selectedJob?.copy(isBookmarked = willBeBookmarked)
                )
            }
        }
    }

    fun triggerTestDeadlineAlert(job: JobEntity) {
        DeadlineNotificationScheduler.triggerInstantTestAlert(getApplication(), job)
    }

    fun scheduleCustomDeadlineReminder(job: JobEntity, daysBefore: Int) {
        DeadlineNotificationScheduler.scheduleDeadlineReminder(getApplication(), job, daysBefore)
    }

    fun syncAllBookmarkedDeadlines() {
        val bookmarks = bookmarkedJobs.value
        if (bookmarks.isNotEmpty()) {
            DeadlineNotificationScheduler.syncDeadlinesForBookmarkedJobs(getApplication(), bookmarks)
        }
    }

    fun markNotificationAsRead(id: String) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }

    fun updateUserProfile(profile: UserEligibilityProfile) {
        _userProfile.value = profile
    }

    fun openReportDialog(job: JobEntity) {
        _uiState.value = _uiState.value.copy(reportingJob = job, reportSubmittedMessage = null)
    }

    fun dismissReportDialog() {
        _uiState.value = _uiState.value.copy(reportingJob = null)
    }

    fun submitReport(reason: String, details: String) {
        val job = _uiState.value.reportingJob ?: return
        viewModelScope.launch {
            // Register report
            _uiState.value = _uiState.value.copy(
                reportingJob = null,
                reportSubmittedMessage = "Thank you. Your report regarding '${job.advtNumber}' has been submitted for editorial audit."
            )
        }
    }

    fun clearReportMessage() {
        _uiState.value = _uiState.value.copy(reportSubmittedMessage = null)
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            backendSyncManager.syncAllData()
            syncCoordinator.syncNow()
            cloudflareRepo.syncJobsFromCloudflare()
            cloudflareRepo.fetchPublicSources()
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }

    fun setCloudflareWorkerUrl(newUrl: String) {
        val app = getApplication<Application>()
        CloudflareConfig.setWorkerUrl(app, newUrl)
        viewModelScope.launch {
            cloudflareRepo.syncJobsFromCloudflare()
        }
    }

    fun syncCloudflareNow() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            cloudflareRepo.syncJobsFromCloudflare()
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }
}
