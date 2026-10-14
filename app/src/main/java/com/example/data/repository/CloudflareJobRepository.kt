package com.example.data.repository

import android.util.Log
import com.example.data.local.JobDao
import com.example.data.remote.cloudflare.CloudflareClient
import com.example.data.remote.cloudflare.CloudflareConfig
import com.example.data.remote.cloudflare.CloudflareHealthResponse
import com.example.data.remote.cloudflare.CloudflareStatsDto
import com.example.data.remote.cloudflare.toCloudflareDto
import com.example.model.JobEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

sealed class CloudflareSyncState {
    object Idle : CloudflareSyncState()
    object Syncing : CloudflareSyncState()
    data class Synced(val jobCount: Int, val timestamp: Long = System.currentTimeMillis()) : CloudflareSyncState()
    data class Error(val message: String) : CloudflareSyncState()
}

class CloudflareJobRepository(
    private val jobDao: JobDao
) {
    private val tag = "CloudflareJobRepo"

    private val _syncState = MutableStateFlow<CloudflareSyncState>(CloudflareSyncState.Idle)
    val syncState: StateFlow<CloudflareSyncState> = _syncState.asStateFlow()

    private val _edgeStats = MutableStateFlow<CloudflareStatsDto?>(null)
    val edgeStats: StateFlow<CloudflareStatsDto?> = _edgeStats.asStateFlow()

    /**
     * Synchronize job listings from Cloudflare D1 into local Room database
     */
    suspend fun syncJobsFromCloudflare(): Result<Int> = withContext(Dispatchers.IO) {
        _syncState.value = CloudflareSyncState.Syncing
        try {
            val api = CloudflareClient.getService()
            val response = api.getJobs(category = null, search = null, status = "ACTIVE", limit = 100)

            if (response.success && response.data.isNotEmpty()) {
                val entities = response.data.map { it.toJobEntity() }
                jobDao.insertJobs(entities)
                Log.d(tag, "Successfully synced ${entities.size} jobs from Cloudflare D1 to Room")
                _syncState.value = CloudflareSyncState.Synced(entities.size)
                Result.success(entities.size)
            } else {
                Log.d(tag, "Cloudflare returned empty or uninitialized dataset: ${response.message}")
                _syncState.value = CloudflareSyncState.Synced(0)
                Result.success(0)
            }
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Failed to connect to Cloudflare edge"
            Log.w(tag, "Cloudflare sync failed: $errorMsg")
            _syncState.value = CloudflareSyncState.Error(errorMsg)
            Result.failure(e)
        }
    }

    /**
     * Publish a new job to Cloudflare D1 SQLite database and cache locally in Room
     */
    suspend fun publishJobToCloudflare(job: JobEntity, adminKey: String? = null): Result<String> = withContext(Dispatchers.IO) {
        try {
            // First save locally in Room cache
            jobDao.insertJob(job)

            // Then push to Cloudflare D1 Worker
            val api = CloudflareClient.getService()
            val dto = job.toCloudflareDto()
            val response = api.createOrUpdateJob(dto, adminKey)

            if (response.success) {
                Log.d(tag, "Job published to Cloudflare D1: ${job.id}")
                Result.success(response.id ?: job.id)
            } else {
                Log.w(tag, "Cloudflare API rejected job: ${response.message}")
                Result.success(job.id) // Local success
            }
        } catch (e: Exception) {
            Log.w(tag, "Network error pushing to Cloudflare, saved locally: ${e.message}")
            Result.success(job.id) // Still saved in Room
        }
    }

    /**
     * Health check to test Cloudflare Worker + D1 + R2 connection
     */
    suspend fun testCloudflareConnection(): Result<CloudflareHealthResponse> = withContext(Dispatchers.IO) {
        try {
            val api = CloudflareClient.getService()
            val health = api.checkHealth()
            Result.success(health)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
