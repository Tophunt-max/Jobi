package com.example.data.remote.cloudflare

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object CloudflareConfig {
    private const val PREFS_NAME = "cloudflare_prefs"
    private const val KEY_WORKER_URL = "worker_url"
    
    // Default placeholder URL that points to Cloudflare Worker edge
    const val DEFAULT_WORKER_URL = "https://sarkari-jobs-worker.workers.dev"

    private val _workerUrlFlow = MutableStateFlow(DEFAULT_WORKER_URL)
    val workerUrlFlow: StateFlow<String> = _workerUrlFlow.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_WORKER_URL, DEFAULT_WORKER_URL) ?: DEFAULT_WORKER_URL
        _workerUrlFlow.value = saved
    }

    fun getActiveWorkerUrl(): String {
        return _workerUrlFlow.value.trimEnd('/')
    }

    fun setWorkerUrl(context: Context, newUrl: String) {
        val cleanUrl = newUrl.trim().trimEnd('/')
        if (cleanUrl.isNotBlank()) {
            _workerUrlFlow.value = cleanUrl
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_WORKER_URL, cleanUrl).apply()
        }
    }
}
