package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationItem(
    @PrimaryKey val id: String,
    val title: String,
    val body: String,
    val type: String, // "NEW_JOB", "DEADLINE_ALERT", "RESULT", "ADMIT_CARD", "SYLLABUS", "ANSWER_KEY"
    val targetSlug: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val priority: String = "HIGH"
)

enum class NotificationCategory {
    ALL,
    NEW_JOBS,
    DEADLINE_REMINDERS,
    RESULTS,
    ADMIT_CARDS
}
