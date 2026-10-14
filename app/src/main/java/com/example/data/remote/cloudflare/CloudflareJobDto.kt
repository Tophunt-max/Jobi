package com.example.data.remote.cloudflare

import com.example.model.JobEntity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CloudflareListResponse(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "count") val count: Int = 0,
    @Json(name = "data") val data: List<CloudflareJobDto> = emptyList(),
    @Json(name = "message") val message: String? = null
)

@JsonClass(generateAdapter = true)
data class CloudflareSingleResponse(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "data") val data: CloudflareJobDto? = null,
    @Json(name = "message") val message: String? = null
)

@JsonClass(generateAdapter = true)
data class CloudflareActionResponse(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "message") val message: String? = null,
    @Json(name = "id") val id: String? = null,
    @Json(name = "summary") val summary: CloudflareImportSummaryDto? = null
)

@JsonClass(generateAdapter = true)
data class CloudflareStatsResponse(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "data") val data: CloudflareStatsDto? = null
)

@JsonClass(generateAdapter = true)
data class CloudflareStatsDto(
    @Json(name = "totalJobs") val totalJobs: Int = 0,
    @Json(name = "latestJobs") val latestJobs: Int = 0,
    @Json(name = "admitCards") val admitCards: Int = 0,
    @Json(name = "results") val results: Int = 0,
    @Json(name = "pendingReview") val pendingReview: Int = 0,
    @Json(name = "activeSources") val activeSources: Int = 0
)

@JsonClass(generateAdapter = true)
data class CloudflareHealthResponse(
    @Json(name = "status") val status: String = "",
    @Json(name = "backend") val backend: String = "",
    @Json(name = "database") val database: String = "",
    @Json(name = "storage") val storage: String = "",
    @Json(name = "importer") val importer: String = "",
    @Json(name = "timestamp") val timestamp: String = ""
)

@JsonClass(generateAdapter = true)
data class CloudflareJobDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "slug") val slug: String? = null,
    @Json(name = "organization") val organization: String? = "",
    @Json(name = "department") val department: String? = "",
    @Json(name = "post_name") val postName: String? = "",
    @Json(name = "category") val category: String? = "Latest Job",
    @Json(name = "state") val state: String? = "All India",
    @Json(name = "total_posts") val totalPosts: Int? = 0,
    @Json(name = "qualification") val qualification: String? = "",
    @Json(name = "age_limit") val ageLimit: String? = "",
    @Json(name = "min_age") val minAge: Int? = 18,
    @Json(name = "max_age") val maxAge: Int? = 40,
    @Json(name = "salary_scale") val salaryScale: String? = "Standard Pay Matrix",
    @Json(name = "fee_general") val feeGeneral: String? = "₹100",
    @Json(name = "fee_sc_st") val feeScSt: String? = "Nil",
    @Json(name = "selection_process") val selectionProcess: String? = "Written Exam, Interview",
    @Json(name = "application_start_date") val applicationStartDate: String? = "",
    @Json(name = "last_date") val lastDate: String? = "",
    @Json(name = "exam_date") val examDate: String? = "",
    @Json(name = "official_url") val officialUrl: String? = "",
    @Json(name = "official_notification_url") val officialNotificationUrl: String? = "",
    @Json(name = "official_apply_url") val applyUrl: String? = "",
    @Json(name = "notification_pdf_r2_key") val notificationPdfR2Key: String? = "",
    @Json(name = "source_id") val sourceId: String? = "",
    @Json(name = "source_domain") val sourceDomain: String? = "",
    @Json(name = "advertisement_number") val advertisementNumber: String? = "",
    @Json(name = "status") val status: String? = "ACTIVE",
    @Json(name = "verified") val verified: Int? = 1,
    @Json(name = "is_featured") val isFeatured: Int? = 0
) {
    fun toJobEntity(): JobEntity {
        val org = organization?.takeIf { it.isNotBlank() } ?: department?.takeIf { it.isNotBlank() } ?: "Government Department"
        val stateVal = state?.takeIf { it.isNotBlank() } ?: "All India"
        val pdfUrl = if (!notificationPdfR2Key.isNullOrBlank()) {
            "${CloudflareConfig.getActiveWorkerUrl()}/api/files/${notificationPdfR2Key}"
        } else if (!officialNotificationUrl.isNullOrBlank()) {
            officialNotificationUrl
        } else {
            officialUrl ?: "https://india.gov.in"
        }
        val advt = advertisementNumber?.takeIf { it.isNotBlank() } ?: "ADVT-${id.takeLast(6).uppercase()}"

        return JobEntity(
            id = id,
            slug = slug ?: id.lowercase().replace(" ", "-"),
            title = title,
            organization = org,
            department = department ?: org,
            advtNumber = advt,
            stateOrAllIndia = stateVal,
            category = category ?: "Latest Job",
            totalVacancies = totalPosts ?: 0,
            qualification = qualification ?: "Check Official Notification",
            minAge = minAge ?: 18,
            maxAge = maxAge ?: 40,
            ageCutoffDate = applicationStartDate?.takeIf { it.isNotBlank() } ?: "01-08-2026",
            salaryScale = salaryScale ?: "Pay Scale as per rules",
            feeGeneral = feeGeneral ?: "₹100",
            feeReserved = feeScSt ?: "Nil",
            feeMode = "Online via Net Banking / UPI / Debit Card",
            selectionProcess = selectionProcess ?: "Written Exam, Skill Test",
            detailedEligibility = qualification ?: "Bachelor Degree / Diploma / 10+2 from recognized institute.",
            applicationStartDate = applicationStartDate ?: "Active",
            applicationClosingDate = lastDate ?: "Closing Soon",
            examDate = examDate?.takeIf { it.isNotBlank() } ?: "As per schedule",
            admitCardDate = "Available before exam",
            officialNotificationUrl = pdfUrl,
            applyOnlineUrl = applyUrl?.takeIf { it.isNotBlank() } ?: officialUrl ?: "https://india.gov.in",
            officialWebsiteUrl = officialUrl ?: "https://india.gov.in",
            verifiedSourceDomain = sourceDomain ?: (officialUrl ?: "official.gov.in").removePrefix("https://").removePrefix("http://").split("/").firstOrNull() ?: "gov.in",
            isVerified = (verified ?: 0) == 1,
            status = status ?: "ACTIVE",
            publishedAt = System.currentTimeMillis()
        )
    }
}

fun JobEntity.toCloudflareDto(): CloudflareJobDto {
    return CloudflareJobDto(
        id = id,
        title = title,
        slug = slug,
        organization = organization,
        department = department.ifBlank { organization },
        postName = title,
        category = category,
        state = stateOrAllIndia,
        totalPosts = totalVacancies,
        qualification = qualification,
        ageLimit = "${minAge}-${maxAge} Years",
        minAge = minAge,
        maxAge = maxAge,
        salaryScale = salaryScale,
        feeGeneral = feeGeneral,
        feeScSt = feeReserved,
        selectionProcess = selectionProcess,
        applicationStartDate = applicationStartDate,
        lastDate = applicationClosingDate,
        examDate = examDate,
        officialUrl = officialWebsiteUrl,
        officialNotificationUrl = officialNotificationUrl,
        applyUrl = applyOnlineUrl,
        notificationPdfR2Key = "",
        sourceId = "manual-admin",
        sourceDomain = verifiedSourceDomain,
        advertisementNumber = advtNumber,
        status = status,
        verified = if (isVerified) 1 else 0,
        isFeatured = 1
    )
}

// ====================================================================
// Sources & Importer Runs DTOs
// ====================================================================

@JsonClass(generateAdapter = true)
data class CloudflareSourcesResponse(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "sources") val sources: List<CloudflareSourceDto> = emptyList(),
    @Json(name = "message") val message: String? = null
)

@JsonClass(generateAdapter = true)
data class CloudflareSourceDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "official_domain") val officialDomain: String,
    @Json(name = "listing_url") val listingUrl: String,
    @Json(name = "parser_type") val parserType: String = "HTML_TABLE",
    @Json(name = "state") val state: String = "All India",
    @Json(name = "enabled") val enabled: Int = 1,
    @Json(name = "polling_interval_hours") val pollingIntervalHours: Int = 6,
    @Json(name = "last_fetched_at") val lastFetchedAt: String? = null,
    @Json(name = "last_successful_fetch_at") val lastSuccessfulFetchAt: String? = null,
    @Json(name = "last_error") val lastError: String? = null
)

@JsonClass(generateAdapter = true)
data class CloudflareImportSummaryDto(
    @Json(name = "runId") val runId: String = "",
    @Json(name = "status") val status: String = "",
    @Json(name = "startedAt") val startedAt: String = "",
    @Json(name = "finishedAt") val finishedAt: String? = null,
    @Json(name = "totalFetched") val totalFetched: Int = 0,
    @Json(name = "totalInserted") val totalInserted: Int = 0,
    @Json(name = "totalDuplicates") val totalDuplicates: Int = 0,
    @Json(name = "totalErrors") val totalErrors: Int = 0,
    @Json(name = "errors") val errors: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class CloudflareImportRunsResponse(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "runs") val runs: List<CloudflareImportRunItemDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class CloudflareImportRunItemDto(
    @Json(name = "id") val id: String,
    @Json(name = "source_id") val sourceId: String? = "ALL",
    @Json(name = "started_at") val startedAt: String,
    @Json(name = "finished_at") val finishedAt: String? = null,
    @Json(name = "fetched_count") val fetchedCount: Int = 0,
    @Json(name = "inserted_count") val insertedCount: Int = 0,
    @Json(name = "duplicate_count") val duplicateCount: Int = 0,
    @Json(name = "error_count") val errorCount: Int = 0,
    @Json(name = "status") val status: String = "SUCCESS",
    @Json(name = "error_summary") val errorSummary: String? = null,
    @Json(name = "triggered_by") val triggeredBy: String? = "ADMIN_MANUAL"
)

@JsonClass(generateAdapter = true)
data class CloudflareApproveRequest(
    @Json(name = "approved_by") val approvedBy: String = "Verified Admin"
)

@JsonClass(generateAdapter = true)
data class CloudflareRejectRequest(
    @Json(name = "reason") val reason: String = "Rejected by administrator"
)

@JsonClass(generateAdapter = true)
data class CloudflareRunImportRequest(
    @Json(name = "source_id") val sourceId: String? = null
)
