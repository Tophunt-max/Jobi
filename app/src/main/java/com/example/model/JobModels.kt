package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "jobs")
data class JobEntity(
    @PrimaryKey val id: String,
    val slug: String,
    val title: String,
    val organization: String,
    val department: String = "",
    val advtNumber: String,
    val stateOrAllIndia: String, // "All India", "Uttar Pradesh", "Bihar", etc.
    val category: String, // "SSC", "UPSC", "Railways", "Banking", etc.
    val totalVacancies: Int,
    val qualification: String, // "10th Pass", "12th Pass", "Graduate", "B.Tech/BE", etc.
    val minAge: Int,
    val maxAge: Int,
    val ageCutoffDate: String = "01-08-2026",
    val salaryScale: String, // e.g. "Pay Level 7 (₹44,900 - ₹1,42,400)"
    val feeGeneral: String, // e.g. "₹100"
    val feeReserved: String, // e.g. "Nil (SC/ST/PwD/Female Exempted)"
    val feeMode: String = "Online via Net Banking / UPI / Debit Card",
    val selectionProcess: String, // "Prelims (Tier-I), Mains (Tier-II), Document Verification"
    val detailedEligibility: String,
    val applicationStartDate: String,
    val applicationClosingDate: String,
    val examDate: String = "Tentative: Nov 2026",
    val admitCardDate: String = "10 days prior to exam",
    val officialNotificationUrl: String,
    val applyOnlineUrl: String,
    val officialWebsiteUrl: String,
    val verifiedSourceDomain: String, // e.g. "upsc.gov.in"
    val isVerified: Boolean = true,
    val status: String = "ACTIVE", // ACTIVE, CLOSING_SOON, UPCOMING, EXPIRED
    val postWiseBreakdownJson: String = "[]",
    val categoryAgeRelaxationJson: String = "[]",
    val isBookmarked: Boolean = false,
    val publishedAt: Long = System.currentTimeMillis()
)

data class JobPost(
    val postTitle: String,
    val vacancyCount: Int,
    val qualification: String,
    val payLevel: String
)

data class CategoryAgeRelaxation(
    val categoryName: String,
    val relaxationYears: String // e.g. "+5 Years", "+3 Years", "+10 Years"
)

enum class JobStatus {
    ACTIVE,
    CLOSING_SOON,
    UPCOMING,
    EXPIRED
}

enum class JobCategory(val displayName: String, val iconCode: String) {
    ALL("All Categories", "all"),
    ALL_INDIA("All India", "india"),
    SSC("Staff Selection (SSC)", "ssc"),
    UPSC("Union Public Service (UPSC)", "upsc"),
    BANKING("Banking & Insurance", "bank"),
    RAILWAYS("Railways (RRB)", "train"),
    DEFENCE("Defence & Paramilitary", "shield"),
    POLICE("State Police", "police"),
    TEACHING("Teaching & TET", "school"),
    STATE_PSC("State PSC", "state"),
    ENGINEERING("Engineering & PSUs", "engineering"),
    TENTH_TWELFTH("10th / 12th Pass", "book"),
    ADMIT_CARD("Admit Card", "card"),
    RESULT("Result", "result")
}

data class IndianState(
    val code: String,
    val name: String,
    val jobCount: Int = 0
)

object IndianStatesRegistry {
    val states = listOf(
        IndianState("ALL", "All India"),
        IndianState("UP", "Uttar Pradesh"),
        IndianState("BR", "Bihar"),
        IndianState("RJ", "Rajasthan"),
        IndianState("MP", "Madhya Pradesh"),
        IndianState("MH", "Maharashtra"),
        IndianState("DL", "Delhi (NCT)"),
        IndianState("WB", "West Bengal"),
        IndianState("OD", "Odisha"),
        IndianState("GJ", "Gujarat"),
        IndianState("TN", "Tamil Nadu"),
        IndianState("AP", "Andhra Pradesh"),
        IndianState("TG", "Telangana"),
        IndianState("KA", "Karnataka"),
        IndianState("KL", "Kerala"),
        IndianState("PB", "Punjab"),
        IndianState("HR", "Haryana"),
        IndianState("JH", "Jharkhand"),
        IndianState("CT", "Chhattisgarh"),
        IndianState("UK", "Uttarakhand"),
        IndianState("HP", "Himachal Pradesh"),
        IndianState("AS", "Assam"),
        IndianState("JK", "Jammu & Kashmir")
    )
}
