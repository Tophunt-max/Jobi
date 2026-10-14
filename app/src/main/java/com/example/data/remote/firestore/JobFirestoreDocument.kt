package com.example.data.remote.firestore

import com.example.model.JobEntity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class JobFirestoreDocument(
    @DocumentId val id: String = "",
    val slug: String = "",
    val title: String = "",
    val organization: String = "",
    val department: String = "",
    val advtNumber: String = "",
    val stateOrAllIndia: String = "All India",
    val category: String = "ALL",
    val totalVacancies: Int = 0,
    val qualification: String = "",
    val minAge: Int = 18,
    val maxAge: Int = 30,
    val ageCutoffDate: String = "01-08-2026",
    val salaryScale: String = "",
    val feeGeneral: String = "",
    val feeReserved: String = "",
    val feeMode: String = "Online via Net Banking / UPI / Debit Card",
    val selectionProcess: String = "",
    val detailedEligibility: String = "",
    val applicationStartDate: String = "",
    val applicationClosingDate: String = "",
    val examDate: String = "Tentative: Nov 2026",
    val admitCardDate: String = "10 days prior to exam",
    val officialNotificationUrl: String = "",
    val applyOnlineUrl: String = "",
    val officialWebsiteUrl: String = "",
    val verifiedSourceDomain: String = "",
    val isVerified: Boolean = true,
    val status: String = "ACTIVE",
    val postWiseBreakdownJson: String = "[]",
    val categoryAgeRelaxationJson: String = "[]",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toJobEntity(): JobEntity {
        return JobEntity(
            id = id.ifBlank { "fb_${System.currentTimeMillis()}" },
            slug = slug.ifBlank { title.lowercase().replace(" ", "-").replace(Regex("[^a-z0-9-]"), "") },
            title = title,
            organization = organization,
            department = department,
            advtNumber = advtNumber,
            stateOrAllIndia = stateOrAllIndia,
            category = category,
            totalVacancies = totalVacancies,
            qualification = qualification,
            minAge = minAge,
            maxAge = maxAge,
            ageCutoffDate = ageCutoffDate,
            salaryScale = salaryScale,
            feeGeneral = feeGeneral,
            feeReserved = feeReserved,
            feeMode = feeMode,
            selectionProcess = selectionProcess,
            detailedEligibility = detailedEligibility,
            applicationStartDate = applicationStartDate,
            applicationClosingDate = applicationClosingDate,
            examDate = examDate,
            admitCardDate = admitCardDate,
            officialNotificationUrl = officialNotificationUrl,
            applyOnlineUrl = applyOnlineUrl,
            officialWebsiteUrl = officialWebsiteUrl,
            verifiedSourceDomain = verifiedSourceDomain,
            isVerified = isVerified,
            status = status,
            postWiseBreakdownJson = postWiseBreakdownJson,
            categoryAgeRelaxationJson = categoryAgeRelaxationJson,
            isBookmarked = false,
            publishedAt = createdAt?.toDate()?.time ?: System.currentTimeMillis()
        )
    }

    companion object {
        fun fromJobEntity(entity: JobEntity): JobFirestoreDocument {
            return JobFirestoreDocument(
                id = entity.id,
                slug = entity.slug,
                title = entity.title,
                organization = entity.organization,
                department = entity.department,
                advtNumber = entity.advtNumber,
                stateOrAllIndia = entity.stateOrAllIndia,
                category = entity.category,
                totalVacancies = entity.totalVacancies,
                qualification = entity.qualification,
                minAge = entity.minAge,
                maxAge = entity.maxAge,
                ageCutoffDate = entity.ageCutoffDate,
                salaryScale = entity.salaryScale,
                feeGeneral = entity.feeGeneral,
                feeReserved = entity.feeReserved,
                feeMode = entity.feeMode,
                selectionProcess = entity.selectionProcess,
                detailedEligibility = entity.detailedEligibility,
                applicationStartDate = entity.applicationStartDate,
                applicationClosingDate = entity.applicationClosingDate,
                examDate = entity.examDate,
                admitCardDate = entity.admitCardDate,
                officialNotificationUrl = entity.officialNotificationUrl,
                applyOnlineUrl = entity.applyOnlineUrl,
                officialWebsiteUrl = entity.officialWebsiteUrl,
                verifiedSourceDomain = entity.verifiedSourceDomain,
                isVerified = entity.isVerified,
                status = entity.status,
                postWiseBreakdownJson = entity.postWiseBreakdownJson,
                categoryAgeRelaxationJson = entity.categoryAgeRelaxationJson
            )
        }
    }
}
