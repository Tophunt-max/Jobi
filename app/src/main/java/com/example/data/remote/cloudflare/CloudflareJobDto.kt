package com.example.data.remote.cloudflare

import com.example.model.JobEntity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

/** Shown when the backend did not provide a value. Never invent official details. */
const val NOT_SPECIFIED = "Refer official notification"

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
    @Json(name = "organization") val organization: String? = null,
    @Json(name = "department") val department: String? = null,
    @Json(name = "post_name") val postName: String? = null,
    @Json(name = "category") val category: String? = null,
    @Json(name = "state") val state: String? = null,
    @Json(name = "total_posts") val totalPosts: Int? = null,
    @Json(name = "qualification") val qualification: String? = null,
    @Json(name = "min_age") val minAge: Int? = null,
    @Json(name = "max_age") val maxAge: Int? = null,
    @Json(name = "salary_scale") val salaryScale: String? = null,
    @Json(name = "fee_general") val feeGeneral: String? = null,
    @Json(name = "fee_sc_st") val feeScSt: String? = null,
    @Json(name = "selection_process") val selectionProcess: String? = null,
    @Json(name = "application_start_date") val applicationStartDate: String? = null,
    @Json(name = "last_date") val lastDate: String? = null,
    @Json(name = "exam_date") val examDate: String? = null,
    @Json(name = "admit_card_date") val admitCardDate: String? = null,
    @Json(name = "official_url") val officialUrl: String? = null,
    @Json(name = "official_notification_url") val officialNotificationUrl: String? = null,
    @Json(name = "official_apply_url") val applyUrl: String? = null,
    @Json(name = "notification_pdf_r2_key") val notificationPdfR2Key: String? = null,
    @Json(name = "source_id") val sourceId: String? = null,
    @Json(name = "source_domain") val sourceDomain: String? = null,
    @Json(name = "advertisement_number") val advertisementNumber: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "verified") val verified: Int? = null,
    @Json(name = "is_featured") val isFeatured: Int? = null,
    @Json(name = "published_at") val publishedAt: String? = null,
    @Json(name = "created_at") val createdAt: String? = null
) {
    /**
     * Maps the server row to the local entity. [isBookmarked] is local user state and is
     * merged in by the DAO, never taken from the server.
     */
    fun toJobEntity(today: LocalDate = LocalDate.now()): JobEntity {
        val org = organization?.takeIf { it.isNotBlank() } ?: department?.takeIf { it.isNotBlank() } ?: NOT_SPECIFIED
        val notificationUrl = when {
            !notificationPdfR2Key.isNullOrBlank() -> "${CloudflareConfig.getActiveWorkerUrl()}/api/files/$notificationPdfR2Key"
            !officialNotificationUrl.isNullOrBlank() -> officialNotificationUrl
            else -> officialUrl.orEmpty()
        }
        val website = officialUrl?.takeIf { it.isNotBlank() } ?: notificationUrl
        val domain = sourceDomain?.takeIf { it.isNotBlank() }
            ?: runCatching { java.net.URI(website).host }.getOrNull().orEmpty()

        return JobEntity(
            id = id,
            slug = slug?.takeIf { it.isNotBlank() } ?: id,
            title = title,
            organization = org,
            department = department.orEmpty(),
            advtNumber = advertisementNumber.orEmpty(),
            stateOrAllIndia = state?.takeIf { it.isNotBlank() } ?: "All India",
            category = category?.takeIf { it.isNotBlank() } ?: "Latest Job",
            totalVacancies = totalPosts ?: 0,
            qualification = qualification?.takeIf { it.isNotBlank() } ?: NOT_SPECIFIED,
            minAge = minAge ?: 0,
            maxAge = maxAge ?: 0,
            ageCutoffDate = applicationStartDate?.takeIf { it.isNotBlank() } ?: NOT_SPECIFIED,
            salaryScale = salaryScale?.takeIf { it.isNotBlank() } ?: NOT_SPECIFIED,
            feeGeneral = feeGeneral?.takeIf { it.isNotBlank() } ?: NOT_SPECIFIED,
            feeReserved = feeScSt?.takeIf { it.isNotBlank() } ?: NOT_SPECIFIED,
            feeMode = NOT_SPECIFIED,
            selectionProcess = selectionProcess?.takeIf { it.isNotBlank() } ?: NOT_SPECIFIED,
            detailedEligibility = qualification?.takeIf { it.isNotBlank() } ?: NOT_SPECIFIED,
            applicationStartDate = applicationStartDate.orEmpty(),
            applicationClosingDate = lastDate.orEmpty(),
            examDate = examDate?.takeIf { it.isNotBlank() } ?: NOT_SPECIFIED,
            admitCardDate = admitCardDate?.takeIf { it.isNotBlank() } ?: NOT_SPECIFIED,
            officialNotificationUrl = notificationUrl,
            applyOnlineUrl = applyUrl?.takeIf { it.isNotBlank() } ?: website,
            officialWebsiteUrl = website,
            verifiedSourceDomain = domain,
            isVerified = verified == 1,
            status = deriveStatus(status, lastDate, today),
            publishedAt = parseServerTimestamp(publishedAt ?: createdAt)
        )
    }

    companion object {
        /** ACTIVE notices closing within 7 days are surfaced as CLOSING_SOON. */
        fun deriveStatus(serverStatus: String?, lastDate: String?, today: LocalDate): String {
            val base = serverStatus?.takeIf { it.isNotBlank() } ?: "ACTIVE"
            if (base != "ACTIVE") return base
            val closing = try {
                lastDate?.takeIf { it.length == 10 }?.let { LocalDate.parse(it) }
            } catch (_: DateTimeParseException) {
                null
            } ?: return base
            val days = ChronoUnit.DAYS.between(today, closing)
            return when {
                days < 0 -> "EXPIRED"
                days <= 7 -> "CLOSING_SOON"
                else -> base
            }
        }

        fun parseServerTimestamp(value: String?): Long {
            if (value.isNullOrBlank()) return 0L
            val iso = if (value.contains('T')) value else value.replace(' ', 'T')
            return runCatching { java.time.Instant.parse(if (iso.endsWith("Z")) iso else "${iso}Z").toEpochMilli() }
                .getOrDefault(0L)
        }
    }
}

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
    @Json(name = "state") val state: String = "All India",
    @Json(name = "last_successful_fetch_at") val lastSuccessfulFetchAt: String? = null
)
