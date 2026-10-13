package com.example.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.model.NotificationItem
import com.example.scheduler.DeadlineNotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DeadlineAlertReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val jobId = intent.getStringExtra(EXTRA_JOB_ID) ?: return
        val jobTitle = intent.getStringExtra(EXTRA_JOB_TITLE) ?: "Government Job Opening"
        val organization = intent.getStringExtra(EXTRA_ORGANIZATION) ?: "Official Recruitment"
        val closingDate = intent.getStringExtra(EXTRA_CLOSING_DATE) ?: "Approaching Soon"
        val applyUrl = intent.getStringExtra(EXTRA_APPLY_URL) ?: ""
        val daysLeft = intent.getLongExtra(EXTRA_DAYS_LEFT, -1L)
        val isTest = intent.getBooleanExtra(EXTRA_IS_TEST, false)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        DeadlineNotificationScheduler.createNotificationChannel(context)

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_JOB_ID, jobId)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            jobId.hashCode(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val urgencyPrefix = when {
            isTest -> "🔔 [Test Alert] "
            daysLeft == 0L -> "🚨 FINAL DAY: "
            daysLeft == 1L -> "⚠️ CLOSES TOMORROW: "
            daysLeft in 2..3 -> "⏰ CLOSING IN $daysLeft DAYS: "
            else -> "📅 DEADLINE REMINDER: "
        }

        val notificationTitle = "$urgencyPrefix$organization"
        val notificationBody = "Last date to submit application for $jobTitle is $closingDate. Apply before the portal closes!"

        val notificationBuilder = NotificationCompat.Builder(context, DeadlineNotificationScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(notificationTitle)
            .setContentText(notificationBody)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$notificationBody\n\nOrganization: $organization\nDeadline: $closingDate\nEnsure all documents & fee receipts are verified.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .setContentIntent(contentPendingIntent)

        // Action: Apply Online if URL available
        if (applyUrl.isNotBlank() && (applyUrl.startsWith("http://") || applyUrl.startsWith("https://"))) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(applyUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val webPendingIntent = PendingIntent.getActivity(
                context,
                (jobId + "_web").hashCode(),
                webIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            notificationBuilder.addAction(
                android.R.drawable.ic_menu_send,
                "Apply Online",
                webPendingIntent
            )
        }

        val notificationId = (jobId.hashCode() + (if (isTest) 9999 else 0)).coerceAtLeast(1)
        notificationManager.notify(notificationId, notificationBuilder.build())

        // Also persist alert in local Room database so it shows up in in-app notifications
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val inAppNotification = NotificationItem(
                    id = "deadline_${jobId}_${System.currentTimeMillis()}",
                    title = notificationTitle,
                    body = notificationBody,
                    type = "DEADLINE_ALERT",
                    targetSlug = jobId,
                    timestamp = System.currentTimeMillis(),
                    isRead = false,
                    priority = if (daysLeft in 0..2) "URGENT" else "HIGH"
                )
                db.notificationDao().insertNotification(inAppNotification)
            } catch (e: Exception) {
                // Ignore DB logging failure in receiver
            }
        }
    }

    companion object {
        const val ACTION_DEADLINE_ALERT = "com.example.action.DEADLINE_ALERT"
        const val EXTRA_JOB_ID = "extra_job_id"
        const val EXTRA_JOB_TITLE = "extra_job_title"
        const val EXTRA_ORGANIZATION = "extra_organization"
        const val EXTRA_CLOSING_DATE = "extra_closing_date"
        const val EXTRA_APPLY_URL = "extra_apply_url"
        const val EXTRA_DAYS_LEFT = "extra_days_left"
        const val EXTRA_IS_TEST = "extra_is_test"
    }
}
