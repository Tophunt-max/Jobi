package com.example.data.remote.cloudflare

import com.example.BuildConfig

/**
 * Backend location. Fixed at build time via API_BASE_URL (see app/build.gradle.kts);
 * it cannot be changed at runtime, so a user or another app cannot redirect traffic.
 */
object CloudflareConfig {
    fun getActiveWorkerUrl(): String = BuildConfig.API_BASE_URL.trimEnd('/')

    fun isConfigured(): Boolean = getActiveWorkerUrl().startsWith("https://")
}
