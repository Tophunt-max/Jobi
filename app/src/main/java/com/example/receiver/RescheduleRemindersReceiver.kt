package com.example.receiver

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.scheduler.DeadlineNotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Alarms are cleared by Android on reboot, app update and when the exact-alarm
 * permission changes. This receiver restores reminders for bookmarked jobs.
 */
class RescheduleRemindersReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED -> Unit
            else -> return
        }

        val pending = goAsync()
        scope.launch {
            try {
                val bookmarks = AppDatabase.getDatabase(context).jobDao().getBookmarkedJobsOnce()
                DeadlineNotificationScheduler.syncDeadlinesForBookmarkedJobs(context, bookmarks)
            } catch (e: Exception) {
                Log.w("RescheduleReminders", "Failed to restore reminders: ${e.message}")
            } finally {
                pending.finish()
            }
        }
    }

    private companion object {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}
