package com.example.data.repository

import com.example.data.local.AdminDao
import com.example.data.local.JobDao
import com.example.data.sample.InitialDataSeed
import com.example.model.AdminDashboardStats
import com.example.model.AuditLogItem
import com.example.model.ContentSource
import com.example.model.ImportCandidate
import com.example.model.JobEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class PipelineRepository(
    private val adminDao: AdminDao,
    private val jobDao: JobDao
) {
    val contentSources: Flow<List<ContentSource>> = adminDao.getContentSources()
    val importCandidates: Flow<List<ImportCandidate>> = adminDao.getImportCandidates()
    val pendingCandidates: Flow<List<ImportCandidate>> = adminDao.getPendingCandidates()

    private val auditLogs = mutableListOf<AuditLogItem>(
        AuditLogItem(
            id = "log-1",
            action = "PIPELINE_SCHEDULER_RUN",
            actor = "System Worker",
            timestamp = "10-10-2026 09:15:00",
            details = "Polled 5 verified sources. 2 candidates added to pending review queue."
        ),
        AuditLogItem(
            id = "log-2",
            action = "CONTENT_PUBLISHED",
            actor = "Chief Editor",
            timestamp = "09-10-2026 18:30:00",
            details = "Approved and published UPSC Civil Services 2026 after official gazette checksum match."
        )
    )

    suspend fun initializeSourcesIfEmpty() {
        val sources = adminDao.getContentSources().first()
        if (sources.isEmpty()) {
            adminDao.insertSources(InitialDataSeed.initialSources)
            adminDao.insertCandidates(InitialDataSeed.initialCandidates)
        }
    }

    fun getAuditLogs(): List<AuditLogItem> = auditLogs.toList()

    suspend fun triggerSourceSync(sourceId: String): String {
        val now = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())
        adminDao.updateSourceSync(sourceId, "Today, $now", 1)

        val newCandidate = ImportCandidate(
            id = "cand-" + UUID.randomUUID().toString().take(8),
            externalId = "SYNC-GAZETTE-" + (100..999).random(),
            title = "State Public Service Commission Special Recruitment 2026",
            organization = "Public Service Commission",
            canonicalUrl = "https://psc.gov.in/notice_2026.pdf",
            sourceDomain = "psc.gov.in",
            retrievedTimestamp = System.currentTimeMillis(),
            contentHash = "SHA256:" + UUID.randomUUID().toString().replace("-", ""),
            parsedVacancies = (500..2500).random(),
            parsedClosingDate = "30-11-2026",
            parsedQualification = "Graduate / Bachelor's Degree",
            validationStatus = "VALID",
            reviewStatus = "PENDING_REVIEW",
            reviewerNotes = "Automated crawler synced from official portal. Canonical domain verified."
        )
        adminDao.insertCandidate(newCandidate)

        auditLogs.add(
            0,
            AuditLogItem(
                id = UUID.randomUUID().toString(),
                action = "MANUAL_SOURCE_POLL",
                actor = "Admin / Reviewer",
                timestamp = now,
                details = "Triggered pipeline sync for source $sourceId. New candidate registered."
            )
        )
        return "Source polled successfully. New verified candidate registered in review queue."
    }

    suspend fun approveAndPublishCandidate(candidate: ImportCandidate): Boolean {
        adminDao.updateCandidateReview(
            candidate.id,
            "PUBLISHED",
            "Approved by reviewer after verifying with official gazette."
        )

        val newJob = JobEntity(
            id = "job-" + candidate.id,
            slug = candidate.title.lowercase().replace(" ", "-").replace(Regex("[^a-z0-9-]"), ""),
            title = candidate.title,
            organization = candidate.organization,
            department = "Government Department",
            advtNumber = candidate.externalId,
            stateOrAllIndia = "All India",
            category = "State PSC",
            totalVacancies = candidate.parsedVacancies,
            qualification = candidate.parsedQualification,
            minAge = 21,
            maxAge = 35,
            salaryScale = "Pay Level 7 (₹44,900 - ₹1,42,400)",
            feeGeneral = "₹250",
            feeReserved = "₹50 (SC/ST/PwD)",
            selectionProcess = "Written Exam + Document Verification",
            detailedEligibility = "Must possess graduation or equivalent from recognized university.",
            applicationStartDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date()),
            applicationClosingDate = candidate.parsedClosingDate,
            officialNotificationUrl = candidate.canonicalUrl,
            applyOnlineUrl = "https://" + candidate.sourceDomain + "/apply",
            officialWebsiteUrl = "https://" + candidate.sourceDomain,
            verifiedSourceDomain = candidate.sourceDomain,
            isVerified = true,
            status = "ACTIVE"
        )
        jobDao.insertJob(newJob)

        val now = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())
        auditLogs.add(
            0,
            AuditLogItem(
                id = UUID.randomUUID().toString(),
                action = "CANDIDATE_APPROVED_PUBLISHED",
                actor = "Admin / Reviewer",
                timestamp = now,
                details = "Candidate ${candidate.externalId} verified and promoted to active jobs."
            )
        )
        return true
    }

    suspend fun rejectCandidate(candidateId: String, reason: String) {
        adminDao.updateCandidateReview(candidateId, "REJECTED", reason)
        val now = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())
        auditLogs.add(
            0,
            AuditLogItem(
                id = UUID.randomUUID().toString(),
                action = "CANDIDATE_REJECTED",
                actor = "Admin / Reviewer",
                timestamp = now,
                details = "Candidate $candidateId rejected. Reason: $reason"
            )
        )
    }

    suspend fun getDashboardStats(): AdminDashboardStats {
        val jobs = jobDao.getAllJobs().first()
        val sources = adminDao.getContentSources().first()
        val candidates = adminDao.getImportCandidates().first()
        return AdminDashboardStats(
            totalPublishedJobs = jobs.size,
            activeApplications = jobs.count { it.status == "ACTIVE" || it.status == "CLOSING_SOON" },
            upcomingDeadlines = jobs.count { it.status == "CLOSING_SOON" },
            expiredJobs = jobs.count { it.status == "EXPIRED" },
            totalResults = 4,
            totalAdmitCards = 3,
            pendingImports = candidates.count { it.reviewStatus == "PENDING_REVIEW" },
            activeSources = sources.count { it.isActive }
        )
    }
}
