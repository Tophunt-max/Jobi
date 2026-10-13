package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "results")
data class ResultItem(
    @PrimaryKey val id: String,
    val examName: String,
    val recruitingOrg: String,
    val advtNumber: String,
    val publicationDate: String,
    val officialResultUrl: String,
    val cutOffSummary: String, // e.g. "UR: 142.5 | OBC: 138.2 | SC: 124.0 | ST: 115.5"
    val meritListAvailable: Boolean = true,
    val verificationDomain: String = "official.gov.in",
    val stage: String = "Final Selection List"
)

@Entity(tableName = "admit_cards")
data class AdmitCardItem(
    @PrimaryKey val id: String,
    val examName: String,
    val recruitingOrg: String,
    val releaseDate: String,
    val examDate: String,
    val officialDownloadUrl: String,
    val instructions: String,
    val citySlipReleased: Boolean = true,
    val verificationDomain: String = "official.gov.in"
)

@Entity(tableName = "syllabuses")
data class SyllabusItem(
    @PrimaryKey val id: String,
    val examName: String,
    val recruitingOrg: String,
    val examPattern: String, // e.g. "Computer Based Test (CBT) - 100 Questions, 200 Marks"
    val subjectsAndMarksJson: String, // e.g. "Reasoning (50), Quant (50), GA (50), English (50)"
    val durationMinutes: Int = 120,
    val negativeMarkingRule: String = "0.50 Marks for each wrong answer",
    val officialPdfUrl: String,
    val verificationDomain: String = "official.gov.in"
)

@Entity(tableName = "answer_keys")
data class AnswerKeyItem(
    @PrimaryKey val id: String,
    val examName: String,
    val recruitingOrg: String,
    val status: String, // "PROVISIONAL" or "FINAL"
    val releaseDate: String,
    val objectionDeadline: String,
    val objectionFee: String = "₹100 per question challenged",
    val officialLink: String,
    val verificationDomain: String = "official.gov.in"
)
