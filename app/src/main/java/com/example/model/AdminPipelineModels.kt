package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "content_sources")
data class ContentSource(
    @PrimaryKey val id: String,
    val name: String,
    val officialDomain: String,
    val sourceType: String, // "OFFICIAL_RSS", "OFFICIAL_PORTAL", "GAZETTE_FEED"
    val sourceUrl: String,
    val pollingIntervalHours: Int = 6,
    val lastSyncTime: String,
    val isActive: Boolean = true,
    val totalDiscovered: Int = 0
)

@Entity(tableName = "import_candidates")
data class ImportCandidate(
    @PrimaryKey val id: String,
    val externalId: String,
    val title: String,
    val organization: String,
    val canonicalUrl: String,
    val sourceDomain: String,
    val retrievedTimestamp: Long,
    val contentHash: String,
    val parsedVacancies: Int,
    val parsedClosingDate: String,
    val parsedQualification: String,
    val validationStatus: String = "VALID", // VALID, WARNING, INVALID
    val reviewStatus: String = "PENDING_REVIEW", // PENDING_REVIEW, APPROVED, REJECTED, PUBLISHED
    val reviewerNotes: String = ""
)

data class AdminDashboardStats(
    val totalPublishedJobs: Int = 0,
    val activeApplications: Int = 0,
    val upcomingDeadlines: Int = 0,
    val expiredJobs: Int = 0,
    val totalResults: Int = 0,
    val totalAdmitCards: Int = 0,
    val pendingImports: Int = 0,
    val activeSources: Int = 0
)

data class AuditLogItem(
    val id: String,
    val action: String, // "JOB_APPROVED", "JOB_PUBLISHED", "IMPORT_SYNC", "STATUS_CHANGE"
    val actor: String,
    val timestamp: String,
    val details: String
)

data class ContentReport(
    val id: String = java.util.UUID.randomUUID().toString(),
    val jobId: String,
    val jobTitle: String,
    val reportReason: String,
    val userEmail: String = "",
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
