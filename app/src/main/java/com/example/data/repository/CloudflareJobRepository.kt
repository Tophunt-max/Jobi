package com.example.data.repository

import android.util.Log
import com.example.data.local.JobDao
import com.example.data.remote.cloudflare.CloudflareApproveRequest
import com.example.data.remote.cloudflare.CloudflareClient
import com.example.data.remote.cloudflare.CloudflareHealthResponse
import com.example.data.remote.cloudflare.CloudflareImportRunItemDto
import com.example.data.remote.cloudflare.CloudflareImportSummaryDto
import com.example.data.remote.cloudflare.CloudflareJobDto
import com.example.data.remote.cloudflare.CloudflareRejectRequest
import com.example.data.remote.cloudflare.CloudflareRunImportRequest
import com.example.data.remote.cloudflare.CloudflareSourceDto
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

    private val _sources = MutableStateFlow<List<CloudflareSourceDto>>(emptyList())
    val sources: StateFlow<List<CloudflareSourceDto>> = _sources.asStateFlow()

    private val _pendingJobs = MutableStateFlow<List<CloudflareJobDto>>(emptyList())
    val pendingJobs: StateFlow<List<CloudflareJobDto>> = _pendingJobs.asStateFlow()

    private val _importRuns = MutableStateFlow<List<CloudflareImportRunItemDto>>(emptyList())
    val importRuns: StateFlow<List<CloudflareImportRunItemDto>> = _importRuns.asStateFlow()

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
     * Fetch all configured recruitment sources (All India & Odisha) from Cloudflare D1
     */
    suspend fun fetchJobSources(adminKey: String? = null): Result<List<CloudflareSourceDto>> = withContext(Dispatchers.IO) {
        try {
            val api = CloudflareClient.getService()
            val response = api.getJobSources(adminKey)
            if (response.success) {
                _sources.value = response.sources
                Result.success(response.sources)
            } else {
                Result.failure(Exception(response.message ?: "Failed to fetch sources"))
            }
        } catch (e: Exception) {
            Log.w(tag, "Failed to fetch sources: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Trigger automated job import pipeline across enabled sources
     */
    suspend fun runAutomatedImport(sourceId: String? = null, adminKey: String? = null): Result<CloudflareImportSummaryDto> = withContext(Dispatchers.IO) {
        try {
            val api = CloudflareClient.getService()
            val response = api.runJobImport(CloudflareRunImportRequest(sourceId), adminKey)
            if (response.success && response.summary != null) {
                // Refresh pending list and sources
                fetchPendingJobs(adminKey)
                fetchJobSources(adminKey)
                fetchImportRuns(adminKey)
                Result.success(response.summary)
            } else {
                Result.failure(Exception(response.message ?: "Import run failed"))
            }
        } catch (e: Exception) {
            Log.w(tag, "Automated import error: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Fetch pending notices awaiting admin review
     */
    suspend fun fetchPendingJobs(adminKey: String? = null): Result<List<CloudflareJobDto>> = withContext(Dispatchers.IO) {
        try {
            val api = CloudflareClient.getService()
            val response = api.getPendingJobs(adminKey)
            if (response.success) {
                _pendingJobs.value = response.data
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Failed to fetch pending jobs"))
            }
        } catch (e: Exception) {
            Log.w(tag, "Failed to fetch pending jobs: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Approve and publish a pending job into active vacancies
     */
    suspend fun approvePendingJob(jobId: String, adminKey: String? = null, approvedBy: String = "Verified Admin"): Result<String> = withContext(Dispatchers.IO) {
        try {
            val api = CloudflareClient.getService()
            val response = api.approveJob(jobId, CloudflareApproveRequest(approvedBy), adminKey)
            if (response.success) {
                // Find candidate from pending list and insert into Room database
                val approvedJobDto = _pendingJobs.value.find { it.id == jobId }
                if (approvedJobDto != null) {
                    val entity = approvedJobDto.copy(status = "ACTIVE", verified = 1).toJobEntity()
                    jobDao.insertJob(entity)
                }
                // Refresh pending list
                _pendingJobs.value = _pendingJobs.value.filterNot { it.id == jobId }
                Result.success(response.message ?: "Job approved and published successfully")
            } else {
                Result.failure(Exception(response.message ?: "Failed to approve job"))
            }
        } catch (e: Exception) {
            Log.w(tag, "Approval error: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Reject a pending job with specific reason
     */
    suspend fun rejectPendingJob(jobId: String, reason: String, adminKey: String? = null): Result<String> = withContext(Dispatchers.IO) {
        try {
            val api = CloudflareClient.getService()
            val response = api.rejectJob(jobId, CloudflareRejectRequest(reason), adminKey)
            if (response.success) {
                _pendingJobs.value = _pendingJobs.value.filterNot { it.id == jobId }
                Result.success(response.message ?: "Job rejected successfully")
            } else {
                Result.failure(Exception(response.message ?: "Failed to reject job"))
            }
        } catch (e: Exception) {
            Log.w(tag, "Reject error: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Fetch import execution history
     */
    suspend fun fetchImportRuns(adminKey: String? = null): Result<List<CloudflareImportRunItemDto>> = withContext(Dispatchers.IO) {
        try {
            val api = CloudflareClient.getService()
            val response = api.getImportRuns(adminKey)
            if (response.success) {
                _importRuns.value = response.runs
                Result.success(response.runs)
            } else {
                Result.failure(Exception("Failed to fetch import runs"))
            }
        } catch (e: Exception) {
            Log.w(tag, "Failed to fetch import runs: ${e.message}")
            Result.failure(e)
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
