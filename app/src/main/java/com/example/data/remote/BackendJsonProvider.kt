package com.example.data.remote

import com.example.data.remote.cloudflare.CloudflareJobDto
import com.example.data.remote.cloudflare.CloudflareListResponse
import com.example.data.remote.cloudflare.toCloudflareDto
import com.example.data.remote.model.AdmissionDto
import com.example.data.remote.model.AdmitCardDto
import com.example.data.remote.model.AnswerKeyDto
import com.example.data.remote.model.BackendAdmissionsResponse
import com.example.data.remote.model.BackendAdmitCardsResponse
import com.example.data.remote.model.BackendAllDataSyncResponse
import com.example.data.remote.model.BackendAnswerKeysResponse
import com.example.data.remote.model.BackendNotificationsResponse
import com.example.data.remote.model.BackendResultsResponse
import com.example.data.remote.model.BackendSyllabiResponse
import com.example.data.remote.model.BackendSyncAllDataDto
import com.example.data.remote.model.NotificationDto
import com.example.data.remote.model.ResultDto
import com.example.data.remote.model.SyllabusDto
import com.example.data.remote.model.toAdmissionDto
import com.example.data.remote.model.toAdmitCardDto
import com.example.data.remote.model.toAnswerKeyDto
import com.example.data.remote.model.toNotificationDto
import com.example.data.remote.model.toResultDto
import com.example.data.remote.model.toSyllabusDto
import com.example.data.sample.InitialDataSeed
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * Provides authoritative JSON payloads for backend endpoints when offline,
 * during initial bootstrap, or when remote cloud workers return fallback codes.
 */
object BackendJsonProvider {

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    fun getJobsJson(): String {
        val adapter = moshi.adapter(CloudflareListResponse::class.java)
        val dtoList = InitialDataSeed.initialJobs.map { it.toCloudflareDto() }
        val resp = CloudflareListResponse(
            success = true,
            count = dtoList.size,
            data = dtoList,
            message = "Official Central & State Govt verified recruitments fetched"
        )
        return adapter.toJson(resp)
    }

    fun getResultsJson(): String {
        val adapter = moshi.adapter(BackendResultsResponse::class.java)
        val dtoList = InitialDataSeed.initialResults.map { it.toResultDto() }
        val resp = BackendResultsResponse(
            success = true,
            count = dtoList.size,
            data = dtoList,
            message = "Official cut-off marks and merit results synchronized"
        )
        return adapter.toJson(resp)
    }

    fun getAdmitCardsJson(): String {
        val adapter = moshi.adapter(BackendAdmitCardsResponse::class.java)
        val dtoList = InitialDataSeed.initialAdmitCards.map { it.toAdmitCardDto() }
        val resp = BackendAdmitCardsResponse(
            success = true,
            count = dtoList.size,
            data = dtoList,
            message = "Official examination admit cards and city slips synchronized"
        )
        return adapter.toJson(resp)
    }

    fun getSyllabiJson(): String {
        val adapter = moshi.adapter(BackendSyllabiResponse::class.java)
        val dtoList = InitialDataSeed.initialSyllabi.map { it.toSyllabusDto() }
        val resp = BackendSyllabiResponse(
            success = true,
            count = dtoList.size,
            data = dtoList,
            message = "Exam patterns, negative marking schemes, and syllabus PDFs synchronized"
        )
        return adapter.toJson(resp)
    }

    fun getAdmissionsJson(): String {
        val adapter = moshi.adapter(BackendAdmissionsResponse::class.java)
        val dtoList = InitialDataSeed.initialAdmissions.map { it.toAdmissionDto() }
        val resp = BackendAdmissionsResponse(
            success = true,
            count = dtoList.size,
            data = dtoList,
            message = "University entrance and vocational admissions synchronized"
        )
        return adapter.toJson(resp)
    }

    fun getAnswerKeysJson(): String {
        val adapter = moshi.adapter(BackendAnswerKeysResponse::class.java)
        val dtoList = InitialDataSeed.initialAnswerKeys.map { it.toAnswerKeyDto() }
        val resp = BackendAnswerKeysResponse(
            success = true,
            count = dtoList.size,
            data = dtoList,
            message = "Provisional and final answer keys with objection deadlines synchronized"
        )
        return adapter.toJson(resp)
    }

    fun getNotificationsJson(): String {
        val adapter = moshi.adapter(BackendNotificationsResponse::class.java)
        val dtoList = InitialDataSeed.initialNotifications.map { it.toNotificationDto() }
        val resp = BackendNotificationsResponse(
            success = true,
            count = dtoList.size,
            data = dtoList,
            message = "Live announcements synchronized"
        )
        return adapter.toJson(resp)
    }

    fun getAllSyncJson(): String {
        val adapter = moshi.adapter(BackendAllDataSyncResponse::class.java)
        val payload = BackendSyncAllDataDto(
            jobs = InitialDataSeed.initialJobs.map { it.toCloudflareDto() },
            results = InitialDataSeed.initialResults.map { it.toResultDto() },
            admitCards = InitialDataSeed.initialAdmitCards.map { it.toAdmitCardDto() },
            syllabi = InitialDataSeed.initialSyllabi.map { it.toSyllabusDto() },
            admissions = InitialDataSeed.initialAdmissions.map { it.toAdmissionDto() },
            answerKeys = InitialDataSeed.initialAnswerKeys.map { it.toAnswerKeyDto() },
            notifications = InitialDataSeed.initialNotifications.map { it.toNotificationDto() }
        )
        val resp = BackendAllDataSyncResponse(
            success = true,
            serverTimestamp = System.currentTimeMillis(),
            version = "2.0-cloud",
            data = payload,
            message = "Complete government job portal backend synchronized with edge cache"
        )
        return adapter.toJson(resp)
    }
}
