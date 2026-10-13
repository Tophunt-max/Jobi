package com.example.util

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import com.example.model.JobEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object AppUtils {

    /**
     * Parses standard Indian date formats (dd-MM-yyyy, dd/MM/yyyy, yyyy-MM-dd)
     */
    fun parseClosingDate(dateStr: String): Date? {
        val formats = listOf(
            SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH),
            SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH),
            SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH),
            SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH),
            SimpleDateFormat("d MMM yyyy", Locale.ENGLISH)
        )
        for (format in formats) {
            try {
                format.isLenient = false
                val date = format.parse(dateStr.trim())
                if (date != null) return date
            } catch (_: Exception) {
            }
        }
        return null
    }

    /**
     * Calculates days remaining until application closing date
     */
    fun getRemainingDays(dateStr: String): Int {
        val targetDate = parseClosingDate(dateStr) ?: return 14
        val now = Calendar.getInstance().timeInMillis
        val diff = targetDate.time - now
        return (diff / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
    }

    /**
     * Returns a modern badge countdown label
     */
    fun getCountdownBadgeText(dateStr: String): String {
        val days = getRemainingDays(dateStr)
        return when {
            days <= 0 -> "🚨 Closes Today!"
            days == 1 -> "⚡ 1 Day Left"
            days <= 3 -> "⏳ $days Days Left"
            days <= 7 -> "📅 $days Days Left"
            else -> "$days Days Left"
        }
    }

    /**
     * Launches Android System Calendar to add the application deadline as an event
     */
    fun addJobDeadlineToCalendar(context: Context, job: JobEntity) {
        try {
            val closingDate = parseClosingDate(job.applicationClosingDate)
            val startTime = if (closingDate != null) {
                val cal = Calendar.getInstance()
                cal.time = closingDate
                cal.set(Calendar.HOUR_OF_DAY, 9)
                cal.set(Calendar.MINUTE, 0)
                cal.timeInMillis
            } else {
                System.currentTimeMillis() + (3L * 24 * 60 * 60 * 1000)
            }

            val endTime = startTime + (2 * 60 * 60 * 1000) // 2 hours event

            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime)
                putExtra(CalendarContract.Events.TITLE, "LAST DATE: ${job.organization} - ${job.title}")
                putExtra(
                    CalendarContract.Events.DESCRIPTION,
                    "Application Closing Date for ${job.organization} (${job.advtNumber}).\n" +
                            "Total Vacancies: ${job.totalVacancies}\n" +
                            "Qualification: ${job.qualification}\n" +
                            "Official Portal: ${job.applyOnlineUrl}"
                )
                putExtra(CalendarContract.Events.EVENT_LOCATION, job.officialWebsiteUrl)
                putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    /**
     * Generates a rich, formatted text message for WhatsApp, Telegram, and SMS study groups
     */
    fun shareJobAlert(context: Context, job: JobEntity) {
        val shareMessage = buildString {
            append("📢 *GOVERNMENT RECRUITMENT ALERT* 🇮🇳\n\n")
            append("🏢 *Organization:* ${job.organization}\n")
            append("💼 *Post:* ${job.title}\n")
            append("👥 *Total Vacancies:* ${job.totalVacancies}\n")
            append("🎓 *Qualification:* ${job.qualification}\n")
            append("💰 *Salary:* ${job.salaryScale}\n")
            append("📍 *Location:* ${job.stateOrAllIndia}\n")
            append("🗓️ *Closing Date:* ${job.applicationClosingDate}\n\n")
            append("🔗 *Official Notification & Apply Link:*\n${job.applyOnlineUrl}\n\n")
            append("📲 Shared via Sarkari Job Alert App")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareMessage)
            type = "text/plain"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Job Alert via")
        shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(shareIntent)
    }
}
