package com.example.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserEligibilityProfile(
    val candidateName: String = "Sarkari Aspirant",
    val age: Int = 23,
    val gender: String = "MALE", // "MALE", "FEMALE"
    val category: CasteCategory = CasteCategory.OBC_NCL,
    val qualification: EducationLevel = EducationLevel.GRADUATE,
    val specificDegree: String = "Bachelor of Science / Technology",
    val homeState: String = "Uttar Pradesh",
    val isPwD: Boolean = false,
    val isExServicemen: Boolean = false,
    val isWomenCandidate: Boolean = false,
    val preferredCategories: List<String> = listOf("SSC", "Banking", "Railways", "UPSC"),
    val enableDeadlineAlerts: Boolean = true,
    val enableNewJobAlerts: Boolean = true,
    // Physical measurement metrics for Police & Defence
    val heightCm: Float = 172f,
    val chestUnexpandedCm: Float = 82f,
    val chestExpandedCm: Float = 87f,
    val runningBenchmarkKm: Float = 5.0f,
    val runningTimeMinutes: Float = 24.0f
)

enum class CasteCategory(val displayName: String, val standardRelaxationYears: Int) {
    GENERAL("General (UR)", 0),
    EWS("Economically Weaker Section (EWS)", 0),
    OBC_NCL("Other Backward Classes (OBC-NCL)", 3),
    SC("Scheduled Caste (SC)", 5),
    ST("Scheduled Tribe (ST)", 5),
    PWD_GENERAL("PwD (General/EWS)", 10),
    PWD_OBC("PwD (OBC-NCL)", 13),
    PWD_SC_ST("PwD (SC/ST)", 15),
    EX_SERVICEMEN("Ex-Servicemen (ESM)", 3)
}

enum class EducationLevel(val displayName: String) {
    TENTH_PASS("10th Pass (Matriculation)"),
    TWELFTH_PASS("12th Pass (Intermediate / 10+2)"),
    ITI("ITI (Industrial Training Institute)"),
    DIPLOMA("Polytechnic Diploma"),
    GRADUATE("Graduate (Any Bachelor's Degree)"),
    BTECH("B.Tech / B.E. (Engineering)"),
    POST_GRADUATE("Post Graduate (Master's Degree)"),
    LLB("Law Graduate (LL.B.)"),
    MBBS_MEDICAL("Medical / Nursing (MBBS/B.Sc)")
}
