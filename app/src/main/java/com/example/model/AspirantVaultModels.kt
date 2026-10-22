package com.example.model

data class AspirantDocumentLocker(
    val candidateName: String = "Sarkari Aspirant",
    val fatherName: String = "Govt Employee",
    val dob: String = "14/08/2003",
    
    // 10th Matriculation
    val tenthBoard: String = "CBSE / State Board",
    val tenthRollNo: String = "24681012",
    val tenthYear: String = "2019",
    val tenthPercentage: String = "86.4%",
    val tenthMarksheetNo: String = "CBSE-10-882910",
    
    // 12th Intermediate
    val twelfthBoard: String = "CBSE / State Board",
    val twelfthStream: String = "PCM (Physics, Chemistry, Maths)",
    val twelfthRollNo: String = "19283746",
    val twelfthYear: String = "2021",
    val twelfthPercentage: String = "84.2%",
    val twelfthCertificateNo: String = "CBSE-12-993821",
    
    // Graduation / Degree
    val gradDegree: String = "B.Sc / B.Tech Computer Science",
    val gradUniversity: String = "State Technical University",
    val gradRollNo: String = "210492810",
    val gradYear: String = "2024",
    val gradCgpaOrPercentage: String = "7.8 CGPA (78%)",
    val gradDegreeSerialNo: String = "DEG-2024-449102",
    
    // Reservation & Domicile
    val categoryCertNo: String = "OBC-UP-2024-883921",
    val categoryIssueAuthority: String = "Tehsildar / Sub-Divisional Magistrate",
    val categoryIssueDate: String = "12/04/2024",
    
    val domicileCertNo: String = "DOM-UP-2023-749201",
    val domicileState: String = "Uttar Pradesh",
    val domicileIssueDate: String = "05/09/2023",
    
    // Identity Verification
    val idType: String = "Aadhaar Card",
    val idNumberMasked: String = "XXXX-XXXX-8921"
)

enum class ExamApplicationStage(val displayName: String, val badgeColorHex: Long) {
    SUBMITTED("Application Submitted", 0xFF2563EB),
    ADMIT_CARD_OUT("Admit Card Out", 0xFFEA580C),
    EXAM_SCHEDULED("Exam Scheduled", 0xFF7C3AED),
    RESULT_AWAITED("Result Awaited", 0xFF0284C7),
    SELECTED("Selected / Merit Qualified", 0xFF059669),
    REJECTED("Not Qualified", 0xFFE11D48)
}

data class AppliedExamRecord(
    val id: String,
    val jobTitle: String,
    val organization: String,
    val advtNumber: String,
    val applicationNo: String,
    val rollNo: String = "",
    val examDate: String = "15 Dec 2026",
    val examCenter: String = "Lucknow / Delhi NCR",
    val stage: ExamApplicationStage = ExamApplicationStage.SUBMITTED,
    val feeReceiptNo: String = "SBI-REC-994821",
    val appliedDate: String = "18 Oct 2026",
    val notes: String = "Admit card download expected 10 days before exam."
)
