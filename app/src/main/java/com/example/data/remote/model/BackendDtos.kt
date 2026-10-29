package com.example.data.remote.model

import com.example.data.remote.cloudflare.CloudflareJobDto
import com.example.model.AdmissionItem
import com.example.model.AdmitCardItem
import com.example.model.AnswerKeyItem
import com.example.model.JobEntity
import com.example.model.NotificationItem
import com.example.model.ResultItem
import com.example.model.SyllabusItem
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ====================================================================
// Results DTO
// ====================================================================

@JsonClass(generateAdapter = true)
data class BackendResultsResponse(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "count") val count: Int = 0,
    @Json(name = "data") val data: List<ResultDto> = emptyList(),
    @Json(name = "message") val message: String? = null
)

@JsonClass(generateAdapter = true)
data class ResultDto(
    @Json(name = "id") val id: String,
    @Json(name = "exam_name") val examName: String,
    @Json(name = "recruiting_org") val recruitingOrg: String,
    @Json(name = "advt_number") val advtNumber: String = "",
    @Json(name = "publication_date") val publicationDate: String,
    @Json(name = "official_result_url") val officialResultUrl: String,
    @Json(name = "cut_off_summary") val cutOffSummary: String = "",
    @Json(name = "merit_list_available") val meritListAvailable: Boolean = true,
    @Json(name = "verification_domain") val verificationDomain: String = "official.gov.in",
    @Json(name = "stage") val stage: String = "Final Selection List"
) {
    fun toResultItem(): ResultItem {
        return ResultItem(
            id = id,
            examName = examName,
            recruitingOrg = recruitingOrg,
            advtNumber = advtNumber,
            publicationDate = publicationDate,
            officialResultUrl = officialResultUrl,
            cutOffSummary = cutOffSummary,
            meritListAvailable = meritListAvailable,
            verificationDomain = verificationDomain,
            stage = stage
        )
    }
}

fun ResultItem.toResultDto(): ResultDto {
    return ResultDto(
        id = id,
        examName = examName,
        recruitingOrg = recruitingOrg,
        advtNumber = advtNumber,
        publicationDate = publicationDate,
        officialResultUrl = officialResultUrl,
        cutOffSummary = cutOffSummary,
        meritListAvailable = meritListAvailable,
        verificationDomain = verificationDomain,
        stage = stage
    )
}

// ====================================================================
// Admit Cards DTO
// ====================================================================

@JsonClass(generateAdapter = true)
data class BackendAdmitCardsResponse(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "count") val count: Int = 0,
    @Json(name = "data") val data: List<AdmitCardDto> = emptyList(),
    @Json(name = "message") val message: String? = null
)

@JsonClass(generateAdapter = true)
data class AdmitCardDto(
    @Json(name = "id") val id: String,
    @Json(name = "exam_name") val examName: String,
    @Json(name = "recruiting_org") val recruitingOrg: String,
    @Json(name = "release_date") val releaseDate: String,
    @Json(name = "exam_date") val examDate: String,
    @Json(name = "official_download_url") val officialDownloadUrl: String,
    @Json(name = "instructions") val instructions: String = "",
    @Json(name = "city_slip_released") val citySlipReleased: Boolean = true,
    @Json(name = "verification_domain") val verificationDomain: String = "official.gov.in"
) {
    fun toAdmitCardItem(): AdmitCardItem {
        return AdmitCardItem(
            id = id,
            examName = examName,
            recruitingOrg = recruitingOrg,
            releaseDate = releaseDate,
            examDate = examDate,
            officialDownloadUrl = officialDownloadUrl,
            instructions = instructions,
            citySlipReleased = citySlipReleased,
            verificationDomain = verificationDomain
        )
    }
}

fun AdmitCardItem.toAdmitCardDto(): AdmitCardDto {
    return AdmitCardDto(
        id = id,
        examName = examName,
        recruitingOrg = recruitingOrg,
        releaseDate = releaseDate,
        examDate = examDate,
        officialDownloadUrl = officialDownloadUrl,
        instructions = instructions,
        citySlipReleased = citySlipReleased,
        verificationDomain = verificationDomain
    )
}

// ====================================================================
// Syllabi DTO
// ====================================================================

@JsonClass(generateAdapter = true)
data class BackendSyllabiResponse(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "count") val count: Int = 0,
    @Json(name = "data") val data: List<SyllabusDto> = emptyList(),
    @Json(name = "message") val message: String? = null
)

@JsonClass(generateAdapter = true)
data class SyllabusDto(
    @Json(name = "id") val id: String,
    @Json(name = "exam_name") val examName: String,
    @Json(name = "recruiting_org") val recruitingOrg: String,
    @Json(name = "exam_pattern") val examPattern: String,
    @Json(name = "subjects_and_marks_json") val subjectsAndMarksJson: String = "[]",
    @Json(name = "duration_minutes") val durationMinutes: Int = 120,
    @Json(name = "negative_marking_rule") val negativeMarkingRule: String = "0.50 Marks for each wrong answer",
    @Json(name = "official_pdf_url") val officialPdfUrl: String,
    @Json(name = "verification_domain") val verificationDomain: String = "official.gov.in"
) {
    fun toSyllabusItem(): SyllabusItem {
        return SyllabusItem(
            id = id,
            examName = examName,
            recruitingOrg = recruitingOrg,
            examPattern = examPattern,
            subjectsAndMarksJson = subjectsAndMarksJson,
            durationMinutes = durationMinutes,
            negativeMarkingRule = negativeMarkingRule,
            officialPdfUrl = officialPdfUrl,
            verificationDomain = verificationDomain
        )
    }
}

fun SyllabusItem.toSyllabusDto(): SyllabusDto {
    return SyllabusDto(
        id = id,
        examName = examName,
        recruitingOrg = recruitingOrg,
        examPattern = examPattern,
        subjectsAndMarksJson = subjectsAndMarksJson,
        durationMinutes = durationMinutes,
        negativeMarkingRule = negativeMarkingRule,
        officialPdfUrl = officialPdfUrl,
        verificationDomain = verificationDomain
    )
}

// ====================================================================
// Admissions DTO
// ====================================================================

@JsonClass(generateAdapter = true)
data class BackendAdmissionsResponse(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "count") val count: Int = 0,
    @Json(name = "data") val data: List<AdmissionDto> = emptyList(),
    @Json(name = "message") val message: String? = null
)

@JsonClass(generateAdapter = true)
data class AdmissionDto(
    @Json(name = "id") val id: String,
    @Json(name = "exam_or_course_name") val examOrCourseName: String,
    @Json(name = "conducting_authority") val conductingAuthority: String,
    @Json(name = "eligibility_course") val eligibilityCourse: String,
    @Json(name = "application_start_date") val applicationStartDate: String,
    @Json(name = "application_closing_date") val applicationClosingDate: String,
    @Json(name = "entrance_exam_date") val entranceExamDate: String,
    @Json(name = "official_apply_url") val officialApplyUrl: String,
    @Json(name = "official_information_bulletin_url") val officialInformationBulletinUrl: String,
    @Json(name = "verification_domain") val verificationDomain: String = "nta.ac.in",
    @Json(name = "status") val status: String = "OPEN"
) {
    fun toAdmissionItem(): AdmissionItem {
        return AdmissionItem(
            id = id,
            examOrCourseName = examOrCourseName,
            conductingAuthority = conductingAuthority,
            eligibilityCourse = eligibilityCourse,
            applicationStartDate = applicationStartDate,
            applicationClosingDate = applicationClosingDate,
            entranceExamDate = entranceExamDate,
            officialApplyUrl = officialApplyUrl,
            officialInformationBulletinUrl = officialInformationBulletinUrl,
            verificationDomain = verificationDomain,
            status = status
        )
    }
}

fun AdmissionItem.toAdmissionDto(): AdmissionDto {
    return AdmissionDto(
        id = id,
        examOrCourseName = examOrCourseName,
        conductingAuthority = conductingAuthority,
        eligibilityCourse = eligibilityCourse,
        applicationStartDate = applicationStartDate,
        applicationClosingDate = applicationClosingDate,
        entranceExamDate = entranceExamDate,
        officialApplyUrl = officialApplyUrl,
        officialInformationBulletinUrl = officialInformationBulletinUrl,
        verificationDomain = verificationDomain,
        status = status
    )
}

// ====================================================================
// Answer Keys DTO
// ====================================================================

@JsonClass(generateAdapter = true)
data class BackendAnswerKeysResponse(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "count") val count: Int = 0,
    @Json(name = "data") val data: List<AnswerKeyDto> = emptyList(),
    @Json(name = "message") val message: String? = null
)

@JsonClass(generateAdapter = true)
data class AnswerKeyDto(
    @Json(name = "id") val id: String,
    @Json(name = "exam_name") val examName: String,
    @Json(name = "recruiting_org") val recruitingOrg: String,
    @Json(name = "status") val status: String = "PROVISIONAL",
    @Json(name = "release_date") val releaseDate: String,
    @Json(name = "objection_deadline") val objectionDeadline: String,
    @Json(name = "objection_fee") val objectionFee: String = "₹100 per question challenged",
    @Json(name = "official_link") val officialLink: String,
    @Json(name = "verification_domain") val verificationDomain: String = "official.gov.in"
) {
    fun toAnswerKeyItem(): AnswerKeyItem {
        return AnswerKeyItem(
            id = id,
            examName = examName,
            recruitingOrg = recruitingOrg,
            status = status,
            releaseDate = releaseDate,
            objectionDeadline = objectionDeadline,
            objectionFee = objectionFee,
            officialLink = officialLink,
            verificationDomain = verificationDomain
        )
    }
}

fun AnswerKeyItem.toAnswerKeyDto(): AnswerKeyDto {
    return AnswerKeyDto(
        id = id,
        examName = examName,
        recruitingOrg = recruitingOrg,
        status = status,
        releaseDate = releaseDate,
        objectionDeadline = objectionDeadline,
        objectionFee = objectionFee,
        officialLink = officialLink,
        verificationDomain = verificationDomain
    )
}

// ====================================================================
// Notifications DTO
// ====================================================================

@JsonClass(generateAdapter = true)
data class BackendNotificationsResponse(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "count") val count: Int = 0,
    @Json(name = "data") val data: List<NotificationDto> = emptyList(),
    @Json(name = "message") val message: String? = null
)

@JsonClass(generateAdapter = true)
data class NotificationDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "body") val body: String = "",
    @Json(name = "type") val type: String = "NEW_JOB",
    @Json(name = "target_slug") val targetSlug: String = "",
    @Json(name = "timestamp") val timestamp: Long = System.currentTimeMillis(),
    @Json(name = "is_read") val isRead: Boolean = false,
    @Json(name = "priority") val priority: String = "HIGH"
) {
    fun toNotificationItem(): NotificationItem {
        return NotificationItem(
            id = id,
            title = title,
            body = body,
            type = type,
            targetSlug = targetSlug,
            timestamp = timestamp,
            isRead = isRead,
            priority = priority
        )
    }
}

fun NotificationItem.toNotificationDto(): NotificationDto {
    return NotificationDto(
        id = id,
        title = title,
        body = body,
        type = type,
        targetSlug = targetSlug,
        timestamp = timestamp,
        isRead = isRead,
        priority = priority
    )
}

// ====================================================================
// Unified All-Data Sync DTO
// ====================================================================

@JsonClass(generateAdapter = true)
data class BackendAllDataSyncResponse(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "server_timestamp") val serverTimestamp: Long = System.currentTimeMillis(),
    @Json(name = "version") val version: String = "1.0",
    @Json(name = "data") val data: BackendSyncAllDataDto? = null,
    @Json(name = "message") val message: String? = null
)

@JsonClass(generateAdapter = true)
data class BackendSyncAllDataDto(
    @Json(name = "jobs") val jobs: List<CloudflareJobDto> = emptyList(),
    @Json(name = "results") val results: List<ResultDto> = emptyList(),
    @Json(name = "admit_cards") val admitCards: List<AdmitCardDto> = emptyList(),
    @Json(name = "syllabi") val syllabi: List<SyllabusDto> = emptyList(),
    @Json(name = "admissions") val admissions: List<AdmissionDto> = emptyList(),
    @Json(name = "answer_keys") val answerKeys: List<AnswerKeyDto> = emptyList(),
    @Json(name = "notifications") val notifications: List<NotificationDto> = emptyList()
)

// ====================================================================
// AI mentor proxy (POST /api/ai/generate)
// ====================================================================

@JsonClass(generateAdapter = true)
data class AiGenerateRequest(
    @Json(name = "task") val task: String,
    @Json(name = "prompt") val prompt: String
)

@JsonClass(generateAdapter = true)
data class AiGenerateResponse(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "text") val text: String? = null,
    @Json(name = "message") val message: String? = null
)

// ====================================================================
// Content reports (POST /api/reports)
// ====================================================================

@JsonClass(generateAdapter = true)
data class ReportRequest(
    @Json(name = "job_id") val jobId: String,
    @Json(name = "reason") val reason: String,
    @Json(name = "details") val details: String
)
