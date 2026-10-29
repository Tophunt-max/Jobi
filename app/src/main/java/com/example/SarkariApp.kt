package com.example

import android.app.Application
import com.example.data.local.JobAlertPreferencesManager
import com.example.data.remote.cloudflare.CloudflareClient
import com.example.scheduler.DeadlineNotificationScheduler

class SarkariApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Must run before any network client is created so the HTTP cache is attached.
        CloudflareClient.init(this)
        DeadlineNotificationScheduler.createNotificationChannel(this)
        JobAlertPreferencesManager.createNotificationChannel(this)
    }
}
