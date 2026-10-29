package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.repository.JobRepository
import com.example.data.local.UserDataStore
import com.example.data.remote.cloudflare.CloudflareClient
import com.example.data.remote.model.ReportRequest
import com.example.model.AdmissionItem
import com.example.model.AdmitCardItem
import com.example.model.AnswerKeyItem
import com.example.model.AppLanguage
import com.example.model.JobEntity
import android.content.Context
import android.content.Intent
import com.example.model.AppliedExamRecord
import com.example.model.AspirantDocumentLocker
import com.example.model.ExamApplicationStage
import com.example.data.local.JobAlertPreferencesManager
import com.example.data.repository.BackendDataSyncManager
import com.example.data.repository.BackendSyncState
import com.example.model.InAppBrowserState
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class MainTab {
    HOME,
    JOBS,
    RESULTS,
    AI_MENTOR,
    PROFILE
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
    val reportSubmittedMessage: String? = null,
    val inAppBrowserState: InAppBrowserState? = null
)

class JobViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = JobRepository(db.jobDao(), db.resultDao(), db.notificationDao())
    private val backendSyncManager = BackendDataSyncManager(db.jobDao(), db.resultDao(), db.notificationDao())
    val backendSyncState: StateFlow<BackendSyncState> = backendSyncManager.syncState
    private val userDataStore = UserDataStore(application)

    private val _uiState = MutableStateFlow(JobUiState())
    val uiState: StateFlow<JobUiState> = _uiState.asStateFlow()

    // Loaded from disk off the main thread in init; defaults until then.
    private val _userProfile = MutableStateFlow(UserEligibilityProfile())
    val userProfile: StateFlow<UserEligibilityProfile> = _userProfile.asStateFlow()

    private val _documentLocker = MutableStateFlow(AspirantDocumentLocker())
    val documentLocker: StateFlow<AspirantDocumentLocker> = _documentLocker.asStateFlow()

    private val _appliedExams = MutableStateFlow<List<AppliedExamRecord>>(emptyList())
    @Volatile private var userEditedBeforeLoad = false
    val appliedExams: StateFlow<List<AppliedExamRecord>> = _appliedExams.asStateFlow()

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
        viewModelScope.launch {
            val (profile, locker, exams) = withContext(Dispatchers.IO) {
                Triple(userDataStore.loadProfile(), userDataStore.loadLocker(), userDataStore.loadAppliedExams())
            }
            if (!userEditedBeforeLoad) {
                _userProfile.value = profile
                _documentLocker.value = locker
                _appliedExams.value = exams
            }
        }
        viewModelScope.launch {
            backendSyncManager.syncAllData()
        }
        // Keep deadline reminders in step with bookmarks (only when the set of bookmarked
        // jobs or their deadlines actually change, not on every list emission).
        viewModelScope.launch {
            repository.bookmarkedJobs
                .map { list -> list.map { it.id to it.applicationClosingDate } to list }
                .distinctUntilChanged { old, new -> old.first == new.first }
                .collect { (_, bookmarks) ->
                    DeadlineNotificationScheduler.syncDeadlinesForBookmarkedJobs(application, bookmarks)
                }
        }
    }

    /** Opens a job from a notification tap or deep link. Falls back to fetching it from the server. */
    fun openJobById(idOrSlug: String) {
        viewModelScope.launch {
            val local = repository.findJob(idOrSlug)
            val job = local ?: runCatching {
                CloudflareClient.getService().getJobById(idOrSlug).data?.toJobEntity()
            }.getOrNull()
            if (job != null) {
                _uiState.value = _uiState.value.copy(selectedJob = job, showNotificationsSheet = false)
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

    fun openInAppBrowser(url: String, title: String? = null) {
        if (url.isBlank()) return
        _uiState.value = _uiState.value.copy(
            inAppBrowserState = InAppBrowserState(url = url.trim(), title = title)
        )
    }

    fun closeInAppBrowser() {
        _uiState.value = _uiState.value.copy(inAppBrowserState = null)
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
            repository.setBookmark(job.id, willBeBookmarked)
            
            // Automatic deadline notification scheduling on bookmark toggle
            val app = getApplication<Application>()
            if (willBeBookmarked) {
                DeadlineNotificationScheduler.syncDeadlinesForBookmarkedJobs(app, listOf(job))
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
        userEditedBeforeLoad = true
        _userProfile.value = profile
        userDataStore.saveProfile(profile)
    }

    fun updateDocumentLocker(locker: AspirantDocumentLocker) {
        userEditedBeforeLoad = true
        _documentLocker.value = locker
        userDataStore.saveLocker(locker)
    }

    private fun setAppliedExams(list: List<AppliedExamRecord>) {
        userEditedBeforeLoad = true
        _appliedExams.value = list
        userDataStore.saveAppliedExams(list)
    }

    fun addAppliedExam(record: AppliedExamRecord) {
        setAppliedExams(listOf(record) + _appliedExams.value.filter { it.id != record.id })
    }

    fun updateExamStage(id: String, stage: ExamApplicationStage, notes: String = "") {
        setAppliedExams(_appliedExams.value.map {
            if (it.id == id) it.copy(stage = stage, notes = if (notes.isNotBlank()) notes else it.notes)
            else it
        })
    }

    fun deleteAppliedExam(id: String) {
        setAppliedExams(_appliedExams.value.filter { it.id != id })
    }

    fun generateAspirantDossierText(): String {
        val p = _userProfile.value
        val d = _documentLocker.value
        val exams = _appliedExams.value
        val sb = StringBuilder()
        sb.appendLine("==================================================")
        sb.appendLine("       SARKARI ASPIRANT CAREER DOSSIER           ")
        sb.appendLine("==================================================")
        sb.appendLine("Candidate Name: ${p.candidateName}")
        sb.appendLine("Father's Name : ${d.fatherName}")
        sb.appendLine("Date of Birth : ${d.dob} (Age: ${p.age} Years)")
        sb.appendLine("Gender        : ${p.gender}")
        sb.appendLine("Category      : ${p.category.displayName}")
        sb.appendLine("Home State    : ${p.homeState} (State Domicile Eligible)")
        sb.appendLine("Education     : ${p.qualification.displayName}")
        sb.appendLine("Specific Stream: ${p.specificDegree}")
        sb.appendLine("Physical Stats: Height ${p.heightCm} cm | Chest ${p.chestUnexpandedCm}-${p.chestExpandedCm} cm")
        sb.appendLine("--------------------------------------------------")
        sb.appendLine("DOCUMENT & CERTIFICATE LOCKER:")
        sb.appendLine("• 10th (Matric) : ${d.tenthBoard} (${d.tenthYear}) - Roll: ${d.tenthRollNo} | ${d.tenthPercentage}")
        sb.appendLine("• 12th (Inter)  : ${d.twelfthBoard} (${d.twelfthYear}) - Roll: ${d.twelfthRollNo} | ${d.twelfthPercentage}")
        sb.appendLine("• Graduation    : ${d.gradDegree} (${d.gradYear}) - ${d.gradCgpaOrPercentage}")
        sb.appendLine("• Category Cert : ${d.categoryCertNo} (Issued by ${d.categoryIssueAuthority})")
        sb.appendLine("• Domicile Cert : ${d.domicileCertNo} (${d.domicileState})")
        sb.appendLine("• Photo ID Card : ${d.idType} (${d.idNumberMasked})")
        sb.appendLine("--------------------------------------------------")
        sb.appendLine("ACTIVE EXAM APPLICATIONS (${exams.size}):")
        exams.forEachIndexed { i, ex ->
            sb.appendLine("${i + 1}. ${ex.jobTitle} - ${ex.organization}")
            sb.appendLine("   Advt: ${ex.advtNumber} | App No: ${ex.applicationNo} | Roll: ${ex.rollNo.ifBlank { "Pending" }}")
            sb.appendLine("   Status: ${ex.stage.displayName} | Exam Date: ${ex.examDate}")
            if (ex.notes.isNotBlank()) sb.appendLine("   Note: ${ex.notes}")
        }
        sb.appendLine("==================================================")
        sb.appendLine("Generated via Sarkari Portal • Verified Aspirant Profile")
        return sb.toString()
    }

    fun shareAspirantDossier(context: Context) {
        val text = generateAspirantDossierText()
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Sarkari Aspirant Profile Dossier - ${_userProfile.value.candidateName}")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(intent, "Share / Print Aspirant Dossier")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun openReportDialog(job: JobEntity) {
        _uiState.value = _uiState.value.copy(reportingJob = job, reportSubmittedMessage = null)
    }

    fun dismissReportDialog() {
        _uiState.value = _uiState.value.copy(reportingJob = null)
    }

    fun submitReport(reason: String, details: String) {
        val job = _uiState.value.reportingJob ?: return
        _uiState.value = _uiState.value.copy(reportingJob = null)
        viewModelScope.launch {
            val ok = runCatching {
                CloudflareClient.getService().submitReport(
                    ReportRequest(jobId = job.id, reason = reason.take(80), details = details.take(1000))
                ).success
            }.getOrDefault(false)
            _uiState.value = _uiState.value.copy(
                reportSubmittedMessage = if (ok) {
                    "Thank you. Your report has been sent to our editors."
                } else {
                    "Could not send the report. Please check your connection and try again."
                }
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
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }
}
