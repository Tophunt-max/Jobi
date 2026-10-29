package com.example.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.model.NotificationItem
import com.example.scheduler.DeadlineNotificationScheduler
import com.example.util.NotificationPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DeadlineAlertReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_DEADLINE_ALERT) return
        val jobId = intent.getStringExtra(EXTRA_JOB_ID) ?: return
        val jobTitle = intent.getStringExtra(EXTRA_JOB_TITLE) ?: "Government job notice"
        val organization = intent.getStringExtra(EXTRA_ORGANIZATION) ?: ""
        val closingDate = intent.getStringExtra(EXTRA_CLOSING_DATE) ?: ""
        val applyUrl = intent.getStringExtra(EXTRA_APPLY_URL) ?: ""
        val daysBefore = intent.getIntExtra(EXTRA_DAYS_BEFORE, -1)
        val isTest = intent.getBooleanExtra(EXTRA_IS_TEST, false)

        // Compute from the actual deadline at delivery time (alarms can be deferred).
        val daysLeft = DeadlineNotificationScheduler.calculateDaysRemaining(closingDate)
        if (!isTest && daysLeft < 0) return

        if (!isTest && daysBefore > 0) {
            DeadlineNotificationScheduler.markFired(context, jobId, daysBefore)
        }

        DeadlineNotificationScheduler.createNotificationChannel(context)

        val contentPendingIntent = PendingIntent.getActivity(
            context,
            0,
            MainActivity.openJobIntent(context, jobId),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val urgencyPrefix = when {
            isTest -> "🔔 [Test] "
            daysLeft == 0L -> "🚨 Last day: "
            daysLeft == 1L -> "⚠ Closes tomorrow: "
            daysLeft in 2..3 -> "⏰ Closes in $daysLeft days: "
            else -> "📅 Deadline reminder: "
        }
        val title = "$urgencyPrefix${organization.ifBlank { jobTitle }}"
        val body = if (closingDate.isNotBlank()) {
            "Last date to apply for $jobTitle is $closingDate."
        } else {
            "Check the last date to apply for $jobTitle."
        }

        val builder = NotificationCompat.Builder(context, DeadlineNotificationScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)

        if (applyUrl.startsWith("https://")) {
            val webPendingIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(Intent.ACTION_VIEW, Uri.parse(applyUrl)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(android.R.drawable.ic_menu_send, "Apply online", webPendingIntent)
        }

        if (NotificationPermission.isGranted(context)) {
            try {
                NotificationManagerCompat.from(context)
                    .notify("deadline:$jobId", if (isTest) 1 else 0, builder.build())
            } catch (e: SecurityException) {
                Log.w(TAG, "Notification permission revoked: ${e.message}")
            }
        }

        // Persist to the in-app inbox. goAsync() keeps the process alive until the write completes.
        val pending = goAsync()
        scope.launch {
            try {
                AppDatabase.getDatabase(context).notificationDao().insertNotification(
                    NotificationItem(
                        id = "deadline_${jobId}_${if (isTest) "test" else daysBefore}",
                        title = title,
                        body = body,
                        type = "DEADLINE_ALERT",
                        targetSlug = jobId,
                        timestamp = System.currentTimeMillis(),
                        isRead = false,
                        priority = if (daysLeft in 0..2) "URGENT" else "HIGH"
                    )
                )
            } catch (e: Exception) {
                Log.w(TAG, "Could not store in-app notification: ${e.message}")
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        private const val TAG = "DeadlineAlertReceiver"
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        const val ACTION_DEADLINE_ALERT = "com.example.action.DEADLINE_ALERT"
        const val EXTRA_JOB_ID = "extra_job_id"
        const val EXTRA_JOB_TITLE = "extra_job_title"
        const val EXTRA_ORGANIZATION = "extra_organization"
        const val EXTRA_CLOSING_DATE = "extra_closing_date"
        const val EXTRA_APPLY_URL = "extra_apply_url"
        const val EXTRA_DAYS_BEFORE = "extra_days_before"
        const val EXTRA_IS_TEST = "extra_is_test"
    }
}
