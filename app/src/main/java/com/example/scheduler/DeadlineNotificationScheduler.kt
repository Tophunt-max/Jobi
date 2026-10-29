package com.example.scheduler

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.util.Log
import com.example.model.JobEntity
import com.example.receiver.DeadlineAlertReceiver
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

object DeadlineNotificationScheduler {

    const val CHANNEL_ID = "job_deadline_alerts"
    private const val CHANNEL_NAME = "Application Deadline Alerts"
    private const val CHANNEL_DESCRIPTION = "Reminders before application deadlines of your bookmarked notices"
    private const val PREFS_NAME = "deadline_scheduler_prefs"
    private const val KEY_ALERT_PREFIX = "alert_scheduled_"
    private const val KEY_FIRED_PREFIX = "alert_fired_"
    private const val TAG = "DeadlineScheduler"

    /** Reminder offsets (days before the deadline) used for bookmarked jobs. */
    val REMINDER_OFFSETS = listOf(3, 1)
    private val ALL_OFFSETS = listOf(1, 3, 7)
    private const val REMINDER_HOUR = 9

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
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
     * Parses the application closing date into the end of that day (23:59:59, local time).
     */
    fun parseClosingDate(dateStr: String): Long? {
        val clean = dateStr.trim()
        if (clean.isEmpty()) return null
        val formats = listOf("yyyy-MM-dd", "dd-MM-yyyy", "dd/MM/yyyy", "d MMM yyyy", "dd MMM yyyy", "MMMM dd, yyyy", "dd-MMM-yyyy")
        for (pattern in formats) {
            try {
                val parsed = SimpleDateFormat(pattern, Locale.ENGLISH).apply { isLenient = false }.parse(clean) ?: continue
                return Calendar.getInstance().apply {
                    time = parsed
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            } catch (_: Exception) {
                // Try next format
            }
        }
        return null
    }

    /** Whole days left until the closing date; negative if already expired or unparseable. */
    fun calculateDaysRemaining(closingDateStr: String): Long {
        val deadlineMillis = parseClosingDate(closingDateStr) ?: return -1L
        return TimeUnit.MILLISECONDS.toDays(deadlineMillis - System.currentTimeMillis())
    }

    fun formatDaysRemainingText(closingDateStr: String): String {
        if (parseClosingDate(closingDateStr) == null) return "See notice"
        val days = calculateDaysRemaining(closingDateStr)
        return when {
            days < 0 -> "Expired"
            days == 0L -> "Last Day Today!"
            days == 1L -> "1 Day Left (Tomorrow)"
            days in 2..5 -> "$days Days Left (Urgent)"
            days in 6..14 -> "$days Days Left"
            else -> "$days Days Remaining"
        }
    }

    /**
     * Unique identity per (job, offset). Android matches PendingIntents by action + data
     * (+ request code), so encoding both in the data URI guarantees no two reminders
     * overwrite or cancel each other, regardless of hashCode collisions.
     */
    private fun alarmIntent(context: Context, jobId: String, daysBefore: Int): Intent =
        Intent(context, DeadlineAlertReceiver::class.java).apply {
            action = DeadlineAlertReceiver.ACTION_DEADLINE_ALERT
            data = Uri.Builder().scheme("sarkari").authority("deadline")
                .appendPath(jobId).appendPath(daysBefore.toString()).build()
        }

    /** Reminder time: [REMINDER_HOUR]:00 local time, [daysBefore] days before the deadline day. */
    internal fun reminderTime(deadlineEndOfDay: Long, daysBefore: Int): Long =
        Calendar.getInstance().apply {
            timeInMillis = deadlineEndOfDay
            add(Calendar.DAY_OF_YEAR, -daysBefore)
            set(Calendar.HOUR_OF_DAY, REMINDER_HOUR)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    /**
     * Schedules a reminder [daysBefore] days before the job's deadline.
     * If that moment has passed but the deadline hasn't, and this reminder has never fired,
     * a single catch-up reminder is scheduled one minute from now.
     */
    fun scheduleDeadlineReminder(context: Context, job: JobEntity, daysBefore: Int = 3) {
        val deadlineMillis = parseClosingDate(job.applicationClosingDate) ?: return
        val now = System.currentTimeMillis()
        if (deadlineMillis <= now) return

        val target = reminderTime(deadlineMillis, daysBefore)
        val triggerTime = when {
            target > now -> target
            hasFired(context, job.id, daysBefore) -> return
            else -> now + TimeUnit.MINUTES.toMillis(1)
        }

        createNotificationChannel(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = alarmIntent(context, job.id, daysBefore).apply {
            putExtra(DeadlineAlertReceiver.EXTRA_JOB_ID, job.id)
            putExtra(DeadlineAlertReceiver.EXTRA_JOB_TITLE, job.title)
            putExtra(DeadlineAlertReceiver.EXTRA_ORGANIZATION, job.organization)
            putExtra(DeadlineAlertReceiver.EXTRA_CLOSING_DATE, job.applicationClosingDate)
            putExtra(DeadlineAlertReceiver.EXTRA_APPLY_URL, job.applyOnlineUrl)
            putExtra(DeadlineAlertReceiver.EXTRA_DAYS_BEFORE, daysBefore)
            putExtra(DeadlineAlertReceiver.EXTRA_IS_TEST, false)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
            if (canExact) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                // Without the exact-alarm permission, Android may defer this by a few minutes.
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
            markAlertScheduled(context, job.id, true)
        } catch (e: SecurityException) {
            Log.w(TAG, "Exact alarm denied, falling back to inexact: ${e.message}")
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            markAlertScheduled(context, job.id, true)
        }
    }

    /** Sends a test notification immediately (user-triggered from the UI). */
    fun triggerInstantTestAlert(context: Context, job: JobEntity) {
        createNotificationChannel(context)
        val intent = Intent(context, DeadlineAlertReceiver::class.java).apply {
            action = DeadlineAlertReceiver.ACTION_DEADLINE_ALERT
            putExtra(DeadlineAlertReceiver.EXTRA_JOB_ID, job.id)
            putExtra(DeadlineAlertReceiver.EXTRA_JOB_TITLE, job.title)
            putExtra(DeadlineAlertReceiver.EXTRA_ORGANIZATION, job.organization)
            putExtra(DeadlineAlertReceiver.EXTRA_CLOSING_DATE, job.applicationClosingDate)
            putExtra(DeadlineAlertReceiver.EXTRA_APPLY_URL, job.applyOnlineUrl)
            putExtra(DeadlineAlertReceiver.EXTRA_IS_TEST, true)
        }
        context.sendBroadcast(intent)
    }

    fun cancelDeadlineReminder(context: Context, jobId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        for (days in ALL_OFFSETS) {
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, alarmIntent(context, jobId, days),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
        val editor = getPrefs(context).edit().remove(KEY_ALERT_PREFIX + jobId)
        ALL_OFFSETS.forEach { editor.remove(firedKey(jobId, it)) }
        editor.apply()
    }

    /** (Re)schedules reminders for all bookmarked jobs. Idempotent: safe to call repeatedly. */
    fun syncDeadlinesForBookmarkedJobs(context: Context, bookmarkedJobs: List<JobEntity>) {
        createNotificationChannel(context)
        for (job in bookmarkedJobs) {
            for (offset in REMINDER_OFFSETS) {
                scheduleDeadlineReminder(context, job, daysBefore = offset)
            }
        }
    }

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isAlertScheduled(context: Context, jobId: String): Boolean =
        getPrefs(context).getBoolean(KEY_ALERT_PREFIX + jobId, false)

    private fun markAlertScheduled(context: Context, jobId: String, isScheduled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_ALERT_PREFIX + jobId, isScheduled).apply()
    }

    private fun firedKey(jobId: String, daysBefore: Int) = "$KEY_FIRED_PREFIX${jobId}_$daysBefore"

    private fun hasFired(context: Context, jobId: String, daysBefore: Int): Boolean =
        getPrefs(context).getBoolean(firedKey(jobId, daysBefore), false)

    /** Called by the receiver so catch-up reminders are not repeated on every app start. */
    fun markFired(context: Context, jobId: String, daysBefore: Int) {
        getPrefs(context).edit().putBoolean(firedKey(jobId, daysBefore), true).apply()
    }
}
