package com.example.data.local

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.model.AlertFrequency
import com.example.model.JobAlertPreferences
import com.example.model.JobEntity
import com.example.model.MatchedJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class JobAlertPreferencesManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _preferencesFlow = MutableStateFlow(loadPreferences())
    val preferencesFlow: StateFlow<JobAlertPreferences> = _preferencesFlow.asStateFlow()

    fun getPreferences(): JobAlertPreferences = _preferencesFlow.value

    fun savePreferences(newPrefs: JobAlertPreferences) {
        prefs.edit()
            .putBoolean(KEY_ENABLED, newPrefs.isEnabled)
            .putStringSet(KEY_STATES, newPrefs.preferredStates)
            .putStringSet(KEY_CATEGORIES, newPrefs.preferredCategories)
            .putStringSet(KEY_QUALIFICATIONS, newPrefs.preferredQualifications)
            .putInt(KEY_MIN_VACANCIES, newPrefs.minVacancies)
            .putBoolean(KEY_ALERT_CLOSING, newPrefs.alertOnClosingSoon)
            .putBoolean(KEY_ALERT_NEW, newPrefs.alertOnNewOpenings)
            .putString(KEY_FREQUENCY, newPrefs.alertFrequency.name)
            .putBoolean(KEY_QUIET_HOURS, newPrefs.quietHoursEnabled)
            .apply()

        _preferencesFlow.value = newPrefs
    }

    private fun loadPreferences(): JobAlertPreferences {
        val enabled = prefs.getBoolean(KEY_ENABLED, true)
        val states = prefs.getStringSet(
            KEY_STATES,
            setOf("All India", "Uttar Pradesh", "Bihar", "Rajasthan", "Delhi")
        ) ?: setOf("All India", "Uttar Pradesh", "Bihar", "Rajasthan", "Delhi")

        val categories = prefs.getStringSet(
            KEY_CATEGORIES,
            setOf("SSC", "Banking", "Railways", "UPSC", "Police", "Defence", "Teaching")
        ) ?: setOf("SSC", "Banking", "Railways", "UPSC", "Police", "Defence", "Teaching")

        val quals = prefs.getStringSet(
            KEY_QUALIFICATIONS,
            setOf("Graduate", "12th Pass", "10th Pass")
        ) ?: setOf("Graduate", "12th Pass", "10th Pass")

        val minVacancies = prefs.getInt(KEY_MIN_VACANCIES, 0)
        val alertClosing = prefs.getBoolean(KEY_ALERT_CLOSING, true)
        val alertNew = prefs.getBoolean(KEY_ALERT_NEW, true)
        val freqName = prefs.getString(KEY_FREQUENCY, AlertFrequency.DAILY_DIGEST.name)
        val frequency = try {
            AlertFrequency.valueOf(freqName ?: AlertFrequency.DAILY_DIGEST.name)
        } catch (_: Exception) {
            AlertFrequency.DAILY_DIGEST
        }
        val quietHours = prefs.getBoolean(KEY_QUIET_HOURS, true)

        return JobAlertPreferences(
            isEnabled = enabled,
            preferredStates = states,
            preferredCategories = categories,
            preferredQualifications = quals,
            minVacancies = minVacancies,
            alertOnClosingSoon = alertClosing,
            alertOnNewOpenings = alertNew,
            alertFrequency = frequency,
            quietHoursEnabled = quietHours
        )
    }

    fun sendTestJobAlertNotification(matchedJob: MatchedJob?) {
        createNotificationChannel(context)
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            matchedJob?.let { putExtra("EXTRA_JOB_ID", it.job.id) }
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            10099,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val job = matchedJob?.job
        val title = if (job != null) {
            "🎯 ${job.organization} [${matchedJob.matchScorePercent}% Match]"
        } else {
            "🎯 Targeted Job Alert Activated!"
        }

        val body = if (job != null) {
            "New opening: ${job.title} (${job.totalVacancies} Posts). Fits your preference: ${matchedJob.matchReasons.joinToString(" • ")}"
        } else {
            "Custom alerts configured. You will receive notifications when new government jobs match your criteria."
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(7771, notification)
    }

    companion object {
        const val CHANNEL_ID = "targeted_custom_job_alerts"
        private const val CHANNEL_NAME = "Targeted Job Alert Preferences"
        private const val CHANNEL_DESCRIPTION = "Alerts curated strictly for your state, qualification and preferred exam boards"

        private const val PREFS_NAME = "targeted_job_alert_prefs"
        private const val KEY_ENABLED = "pref_enabled"
        private const val KEY_STATES = "pref_states"
        private const val KEY_CATEGORIES = "pref_categories"
        private const val KEY_QUALIFICATIONS = "pref_quals"
        private const val KEY_MIN_VACANCIES = "pref_min_vacancies"
        private const val KEY_ALERT_CLOSING = "pref_alert_closing"
        private const val KEY_ALERT_NEW = "pref_alert_new"
        private const val KEY_FREQUENCY = "pref_frequency"
        private const val KEY_QUIET_HOURS = "pref_quiet_hours"

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                    val channel = NotificationChannel(
                        CHANNEL_ID,
                        CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = CHANNEL_DESCRIPTION
                        enableVibration(true)
                    }
                    notificationManager.createNotificationChannel(channel)
                }
            }
        }
    }
}
