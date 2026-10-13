package com.example.scheduler

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import com.example.model.JobEntity
import com.example.receiver.DeadlineAlertReceiver
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DeadlineNotificationScheduler {

    const val CHANNEL_ID = "job_deadline_alerts"
    private const val CHANNEL_NAME = "Application Deadline Alerts"
    private const val CHANNEL_DESCRIPTION = "Timely alerts and reminders for approaching job application deadlines"
    private const val PREFS_NAME = "deadline_scheduler_prefs"
    private const val KEY_ALERT_PREFIX = "alert_scheduled_"
    private const val KEY_REMINDER_DAYS_PREF = "reminder_days_window"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID)
            if (existingChannel == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = CHANNEL_DESCRIPTION
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 250, 150, 250)
                    setShowBadge(true)
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    /**
     * Parses the application closing date into milliseconds.
     * Supports formats: "dd-MM-yyyy", "yyyy-MM-dd", "dd/MM/yyyy", "d MMM yyyy", "dd MMMM yyyy".
     */
    fun parseClosingDate(dateStr: String): Long? {
        val clean = dateStr.trim()
        val formats = listOf(
            "dd-MM-yyyy",
            "yyyy-MM-dd",
            "dd/MM/yyyy",
            "d MMM yyyy",
            "dd MMM yyyy",
            "MMMM dd, yyyy",
            "dd-MMM-yyyy"
        )
        for (pattern in formats) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.ENGLISH).apply { isLenient = false }
                val parsed = sdf.parse(clean)
                if (parsed != null) {
                    // Set to end of the deadline day (23:59:59)
                    val calendar = Calendar.getInstance().apply {
                        time = parsed
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                    }
                    return calendar.timeInMillis
                }
            } catch (_: Exception) {
                // Try next format
            }
        }
        return null
    }

    /**
     * Calculates days left until application closing date.
     * Returns negative value if already expired.
     */
    fun calculateDaysRemaining(closingDateStr: String): Long {
        val deadlineMillis = parseClosingDate(closingDateStr) ?: return -1L
        val now = System.currentTimeMillis()
        val diffMillis = deadlineMillis - now
        return TimeUnit.MILLISECONDS.toDays(diffMillis)
    }

    /**
     * Formats days left into human-readable urgency status.
     */
    fun formatDaysRemainingText(closingDateStr: String): String {
        val days = calculateDaysRemaining(closingDateStr)
        return when {
            days < 0 -> "Expired"
            days == 0L -> "Last Day Today!"
            days == 1L -> "1 Day Left (Tomorrow)"
            days in 2..5 -> "$days Days Left (Urgent)"
            days in 6..14 -> "$days Days Left"
            days > 14 -> "$days Days Remaining"
            else -> "Closing Soon"
        }
    }

    /**
     * Schedules a notification reminder for a bookmarked job.
     * [daysBefore]: How many days before the deadline the alert should fire (default 3 days or 1 day).
     */
    fun scheduleDeadlineReminder(
        context: Context,
        job: JobEntity,
        daysBefore: Int = 3
    ) {
        createNotificationChannel(context)
        val deadlineMillis = parseClosingDate(job.applicationClosingDate) ?: return
        val daysLeft = calculateDaysRemaining(job.applicationClosingDate)

        // Target trigger time: daysBefore prior at 9:00 AM
        val targetMillis = deadlineMillis - TimeUnit.DAYS.toMillis(daysBefore.toLong())
        val now = System.currentTimeMillis()

        // If target alert time is in the past, but the deadline is still upcoming:
        val triggerTime = if (targetMillis <= now) {
            if (daysLeft >= 0) {
                // Fire in 3 seconds as a catch-up alert
                now + 3000L
            } else {
                // Already expired, no need to alert
                return
            }
        } else {
            targetMillis
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, DeadlineAlertReceiver::class.java).apply {
            action = DeadlineAlertReceiver.ACTION_DEADLINE_ALERT
            putExtra(DeadlineAlertReceiver.EXTRA_JOB_ID, job.id)
            putExtra(DeadlineAlertReceiver.EXTRA_JOB_TITLE, job.title)
            putExtra(DeadlineAlertReceiver.EXTRA_ORGANIZATION, job.organization)
            putExtra(DeadlineAlertReceiver.EXTRA_CLOSING_DATE, job.applicationClosingDate)
            putExtra(DeadlineAlertReceiver.EXTRA_APPLY_URL, job.applyOnlineUrl)
            putExtra(DeadlineAlertReceiver.EXTRA_DAYS_LEFT, daysLeft)
            putExtra(DeadlineAlertReceiver.EXTRA_IS_TEST, false)
        }

        val requestCode = (job.id.hashCode() + daysBefore).coerceAtLeast(1)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }

            markAlertScheduled(context, job.id, true)
        } catch (e: Exception) {
            Log.e("DeadlineScheduler", "Failed to schedule alarm: ${e.message}")
        }
    }

    /**
     * Instantly triggers a test deadline notification for the specified job.
     * Useful for emulator testing and immediate user feedback.
     */
    fun triggerInstantTestAlert(context: Context, job: JobEntity) {
        createNotificationChannel(context)
        val daysLeft = calculateDaysRemaining(job.applicationClosingDate).coerceAtLeast(0)

        val intent = Intent(context, DeadlineAlertReceiver::class.java).apply {
            action = DeadlineAlertReceiver.ACTION_DEADLINE_ALERT
            putExtra(DeadlineAlertReceiver.EXTRA_JOB_ID, job.id)
            putExtra(DeadlineAlertReceiver.EXTRA_JOB_TITLE, job.title)
            putExtra(DeadlineAlertReceiver.EXTRA_ORGANIZATION, job.organization)
            putExtra(DeadlineAlertReceiver.EXTRA_CLOSING_DATE, job.applicationClosingDate)
            putExtra(DeadlineAlertReceiver.EXTRA_APPLY_URL, job.applyOnlineUrl)
            putExtra(DeadlineAlertReceiver.EXTRA_DAYS_LEFT, daysLeft)
            putExtra(DeadlineAlertReceiver.EXTRA_IS_TEST, true)
        }
        context.sendBroadcast(intent)
    }

    /**
     * Cancels any scheduled reminders for a specific job.
     */
    fun cancelDeadlineReminder(context: Context, jobId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val daysList = listOf(1, 3, 7)
        for (days in daysList) {
            val requestCode = (jobId.hashCode() + days).coerceAtLeast(1)
            val intent = Intent(context, DeadlineAlertReceiver::class.java).apply {
                action = DeadlineAlertReceiver.ACTION_DEADLINE_ALERT
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
        markAlertScheduled(context, jobId, false)
    }

    /**
     * Automatically syncs reminders for all bookmarked jobs.
     */
    fun syncDeadlinesForBookmarkedJobs(
        context: Context,
        bookmarkedJobs: List<JobEntity>
    ) {
        createNotificationChannel(context)
        for (job in bookmarkedJobs) {
            val daysLeft = calculateDaysRemaining(job.applicationClosingDate)
            if (daysLeft >= 0) {
                // Schedule 3 days and 1 day reminders
                if (daysLeft >= 3) {
                    scheduleDeadlineReminder(context, job, daysBefore = 3)
                }
                scheduleDeadlineReminder(context, job, daysBefore = 1)
            }
        }
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isAlertScheduled(context: Context, jobId: String): Boolean {
        return getPrefs(context).getBoolean(KEY_ALERT_PREFIX + jobId, false)
    }

    private fun markAlertScheduled(context: Context, jobId: String, isScheduled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_ALERT_PREFIX + jobId, isScheduled).apply()
    }
}
