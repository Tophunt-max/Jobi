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
    @Json(name = "id") val id: String? = null
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
    @Json(name = "results") val results: Int = 0
)

@JsonClass(generateAdapter = true)
data class CloudflareHealthResponse(
    @Json(name = "status") val status: String = "",
    @Json(name = "backend") val backend: String = "",
    @Json(name = "database") val database: String = "",
    @Json(name = "storage") val storage: String = "",
    @Json(name = "timestamp") val timestamp: String = ""
)

@JsonClass(generateAdapter = true)
data class CloudflareJobDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "department") val department: String? = "",
    @Json(name = "post_name") val postName: String? = "",
    @Json(name = "category") val category: String? = "Latest Job",
    @Json(name = "total_posts") val totalPosts: Int? = 0,
    @Json(name = "qualification") val qualification: String? = "",
    @Json(name = "min_age") val minAge: Int? = 18,
    @Json(name = "max_age") val maxAge: Int? = 40,
    @Json(name = "application_start_date") val applicationStartDate: String? = "",
    @Json(name = "last_date") val lastDate: String? = "",
    @Json(name = "exam_date") val examDate: String? = "",
    @Json(name = "fee_general") val feeGeneral: String? = "₹100",
    @Json(name = "fee_sc_st") val feeScSt: String? = "Nil",
    @Json(name = "salary_scale") val salaryScale: String? = "Standard Pay Matrix",
    @Json(name = "selection_process") val selectionProcess: String? = "Written Exam, Interview",
    @Json(name = "official_url") val officialUrl: String? = "",
    @Json(name = "apply_url") val applyUrl: String? = "",
    @Json(name = "notification_pdf_r2_key") val notificationPdfR2Key: String? = "",
    @Json(name = "status") val status: String? = "ACTIVE",
    @Json(name = "is_featured") val isFeatured: Int? = 0
) {
    fun toJobEntity(): JobEntity {
        return JobEntity(
            id = id,
            slug = id.lowercase().replace(" ", "-"),
            title = title,
            organization = department ?: "Central / State Govt",
            department = department ?: "",
            advtNumber = "CF-${id.takeLast(6).uppercase()}",
            stateOrAllIndia = "All India",
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
            officialNotificationUrl = if (!notificationPdfR2Key.isNullOrBlank()) {
                "${CloudflareConfig.getActiveWorkerUrl()}/api/files/${notificationPdfR2Key}"
            } else {
                officialUrl ?: "https://sarkariresult.com"
            },
            applyOnlineUrl = applyUrl ?: officialUrl ?: "https://sarkariresult.com",
            officialWebsiteUrl = officialUrl ?: "https://sarkariresult.com",
            verifiedSourceDomain = (officialUrl ?: "cloudflare.edge").removePrefix("https://").removePrefix("http://").split("/").firstOrNull() ?: "cloudflare.edge",
            isVerified = true,
            status = status ?: "ACTIVE",
            publishedAt = System.currentTimeMillis()
        )
    }
}

fun JobEntity.toCloudflareDto(): CloudflareJobDto {
    return CloudflareJobDto(
        id = id,
        title = title,
        department = organization.ifBlank { department },
        postName = title,
        category = category,
        totalPosts = totalVacancies,
        qualification = qualification,
        minAge = minAge,
        maxAge = maxAge,
        applicationStartDate = applicationStartDate,
        lastDate = applicationClosingDate,
        examDate = examDate,
        feeGeneral = feeGeneral,
        feeScSt = feeReserved,
        salaryScale = salaryScale,
        selectionProcess = selectionProcess,
        officialUrl = officialWebsiteUrl,
        applyUrl = applyOnlineUrl,
        notificationPdfR2Key = "",
        status = status,
        isFeatured = 1
    )
}
