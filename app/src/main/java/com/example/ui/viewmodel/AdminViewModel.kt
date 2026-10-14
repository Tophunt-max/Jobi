package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.remote.firestore.JobFirestoreService
import com.example.data.remote.firestore.JobSyncCoordinator
import com.example.data.repository.CloudflareJobRepository
import com.example.data.repository.PipelineRepository
import com.example.model.AdminDashboardStats
import com.example.model.AuditLogItem
import com.example.model.ContentSource
import com.example.model.ImportCandidate
import com.example.model.JobEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class AdminViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val pipelineRepository = PipelineRepository(db.adminDao(), db.jobDao())
    val firestoreService = JobFirestoreService()
    val syncCoordinator = JobSyncCoordinator(firestoreService, db.jobDao())
    val cloudflareRepo = CloudflareJobRepository(db.jobDao())

    val sources: StateFlow<List<ContentSource>> = pipelineRepository.contentSources
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val candidates: StateFlow<List<ImportCandidate>> = pipelineRepository.importCandidates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _dashboardStats = MutableStateFlow(AdminDashboardStats())
    val dashboardStats: StateFlow<AdminDashboardStats> = _dashboardStats.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<AuditLogItem>>(emptyList())
    val auditLogs: StateFlow<List<AuditLogItem>> = _auditLogs.asStateFlow()

    private val _actionStatusMessage = MutableStateFlow<String?>(null)
    val actionStatusMessage: StateFlow<String?> = _actionStatusMessage.asStateFlow()

    init {
        viewModelScope.launch {
            pipelineRepository.initializeSourcesIfEmpty()
            refreshStats()
        }
    }

    fun refreshStats() {
        viewModelScope.launch {
            _dashboardStats.value = pipelineRepository.getDashboardStats()
            _auditLogs.value = pipelineRepository.getAuditLogs()
        }
    }

    fun triggerPollSource(source: ContentSource) {
        viewModelScope.launch {
            val msg = pipelineRepository.triggerSourceSync(source.id)
            _actionStatusMessage.value = msg
            refreshStats()
        }
    }

    fun approveCandidate(candidate: ImportCandidate) {
        viewModelScope.launch {
            pipelineRepository.approveAndPublishCandidate(candidate)
            _actionStatusMessage.value = "Candidate ${candidate.externalId} approved and published into live vacancies."
            refreshStats()
        }
    }

    fun rejectCandidate(candidate: ImportCandidate, reason: String = "Missing official gazette signature") {
        viewModelScope.launch {
            pipelineRepository.rejectCandidate(candidate.id, reason)
            _actionStatusMessage.value = "Candidate ${candidate.externalId} rejected."
            refreshStats()
        }
    }

    fun clearActionStatus() {
        _actionStatusMessage.value = null
    }

    fun publishManualJob(
        title: String,
        org: String,
        advtNo: String,
        state: String,
        category: String,
        vacancies: Int,
        qual: String,
        minAge: Int,
        maxAge: Int,
        salary: String,
        closeDate: String,
        officialPdf: String,
        applyUrl: String
    ) {
        viewModelScope.launch {
            val newJob = JobEntity(
                id = "manual-" + UUID.randomUUID().toString().take(8),
                slug = title.lowercase().replace(" ", "-").replace(Regex("[^a-z0-9-]"), ""),
                title = title,
                organization = org,
                department = "Government Department",
                advtNumber = advtNo,
                stateOrAllIndia = state,
                category = category,
                totalVacancies = vacancies,
                qualification = qual,
                minAge = minAge,
                maxAge = maxAge,
                salaryScale = salary,
                feeGeneral = "₹100",
                feeReserved = "Nil",
                selectionProcess = "Written Exam + Interview",
                detailedEligibility = "Eligibility criteria as specified in official advertisement.",
                applicationStartDate = "Today",
                applicationClosingDate = closeDate,
                officialNotificationUrl = officialPdf,
                applyOnlineUrl = applyUrl,
                officialWebsiteUrl = "https://india.gov.in",
                verifiedSourceDomain = "official.gov.in",
                isVerified = true,
                status = "ACTIVE"
            )
            val pubResult = syncCoordinator.publishJob(newJob)
            cloudflareRepo.publishJobToCloudflare(newJob)
            val isCloud = pubResult.isSuccess && firestoreService.isAvailable()
            _actionStatusMessage.value = if (isCloud) {
                "New job '$title' published to Cloudflare D1 & Cloud Firestore."
            } else {
                "New job '$title' saved to Cloudflare Edge & local database."
            }
            refreshStats()
        }
    }
}
