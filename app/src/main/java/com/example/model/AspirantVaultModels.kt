package com.example.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AspirantDocumentLocker(
    val candidateName: String = "",
    val fatherName: String = "",
    val dob: String = "",
    
    // 10th Matriculation
    val tenthBoard: String = "",
    val tenthRollNo: String = "",
    val tenthYear: String = "",
    val tenthPercentage: String = "",
    val tenthMarksheetNo: String = "",
    
    // 12th Intermediate
    val twelfthBoard: String = "",
    val twelfthStream: String = "",
    val twelfthRollNo: String = "",
    val twelfthYear: String = "",
    val twelfthPercentage: String = "",
    val twelfthCertificateNo: String = "",
    
    // Graduation / Degree
    val gradDegree: String = "",
    val gradUniversity: String = "",
    val gradRollNo: String = "",
    val gradYear: String = "",
    val gradCgpaOrPercentage: String = "",
    val gradDegreeSerialNo: String = "",
    
    // Reservation & Domicile
    val categoryCertNo: String = "",
    val categoryIssueAuthority: String = "",
    val categoryIssueDate: String = "",
    
    val domicileCertNo: String = "",
    val domicileState: String = "",
    val domicileIssueDate: String = "",
    
    // Identity Verification
    val idType: String = "",
    val idNumberMasked: String = ""
)

enum class ExamApplicationStage(val displayName: String, val badgeColorHex: Long) {
    SUBMITTED("Application Submitted", 0xFF2563EB),
    ADMIT_CARD_OUT("Admit Card Out", 0xFFEA580C),
    EXAM_SCHEDULED("Exam Scheduled", 0xFF7C3AED),
    RESULT_AWAITED("Result Awaited", 0xFF0284C7),
    SELECTED("Selected / Merit Qualified", 0xFF059669),
    REJECTED("Not Qualified", 0xFFE11D48)
}

@JsonClass(generateAdapter = true)
data class AppliedExamRecord(
    val id: String,
    val jobTitle: String,
    val organization: String,
    val advtNumber: String,
    val applicationNo: String,
    val rollNo: String = "",
    val examDate: String = "",
    val examCenter: String = "",
    val stage: ExamApplicationStage = ExamApplicationStage.SUBMITTED,
    val feeReceiptNo: String = "",
    val appliedDate: String = "",
    val notes: String = ""
)
