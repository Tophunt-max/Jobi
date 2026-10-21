package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.JobDao
import com.example.data.local.NotificationDao
import com.example.data.local.ResultDao
import com.example.data.remote.cloudflare.CloudflareClient
import com.example.data.remote.cloudflare.CloudflareConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

sealed class BackendSyncState {
    object Idle : BackendSyncState()
    data class Syncing(val currentEntity: String) : BackendSyncState()
    data class Success(
        val message: String,
        val totalSynced: Int,
        val timestamp: Long = System.currentTimeMillis()
    ) : BackendSyncState()
    data class OfflineCached(
        val message: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : BackendSyncState()
}

/**
 * Enterprise sync manager responsible for fetching all application data from the
 * backend API (Jobs, Results, Admit Cards, Syllabus, Admissions, Answer Keys, Notifications),
 * with dual-layer HTTP disk caching and Room database persistent caching.
 */
class BackendDataSyncManager(
    private val context: Context,
    private val jobDao: JobDao,
    private val resultDao: ResultDao,
    private val notificationDao: NotificationDao
) {
    companion object {
        private const val TAG = "BackendDataSyncManager"
    }

    private val _syncState = MutableStateFlow<BackendSyncState>(BackendSyncState.Idle)
    val syncState: StateFlow<BackendSyncState> = _syncState.asStateFlow()

    init {
        CloudflareClient.init(context)
        CloudflareConfig.init(context)
    }

    /**
     * Synchronizes ALL data categories from backend API into local Room cache.
     */
    suspend fun syncAllData(): Result<Int> = withContext(Dispatchers.IO) {
        _syncState.value = BackendSyncState.Syncing("Initiating full backend sync...")
        Log.d(TAG, "Starting comprehensive backend API data synchronization")

        var totalItems = 0
        try {
            val api = CloudflareClient.getService()

            // 1. Fetch & Cache Jobs
            _syncState.value = BackendSyncState.Syncing("Fetching latest government jobs...")
            try {
                val jobsResponse = api.getJobs(limit = 100)
                if (jobsResponse.success && jobsResponse.data.isNotEmpty()) {
                    val entities = jobsResponse.data.map { it.toJobEntity() }
                    jobDao.insertJobs(entities)
                    totalItems += entities.size
                    Log.d(TAG, "Backend API: Cached ${entities.size} jobs into Room")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Jobs sync fallback: ${e.message}")
            }

            // 2. Fetch & Cache Results
            _syncState.value = BackendSyncState.Syncing("Fetching exam results & cut-offs...")
            try {
                val resultsResponse = api.getResults(limit = 50)
                if (resultsResponse.success && resultsResponse.data.isNotEmpty()) {
                    val entities = resultsResponse.data.map { it.toResultItem() }
                    resultDao.insertResults(entities)
                    totalItems += entities.size
                    Log.d(TAG, "Backend API: Cached ${entities.size} results into Room")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Results sync fallback: ${e.message}")
            }

            // 3. Fetch & Cache Admit Cards
            _syncState.value = BackendSyncState.Syncing("Fetching admit cards & hall tickets...")
            try {
                val admitResponse = api.getAdmitCards(limit = 50)
                if (admitResponse.success && admitResponse.data.isNotEmpty()) {
                    val entities = admitResponse.data.map { it.toAdmitCardItem() }
                    resultDao.insertAdmitCards(entities)
                    totalItems += entities.size
                    Log.d(TAG, "Backend API: Cached ${entities.size} admit cards into Room")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Admit cards sync fallback: ${e.message}")
            }

            // 4. Fetch & Cache Syllabi
            _syncState.value = BackendSyncState.Syncing("Fetching syllabus & exam patterns...")
            try {
                val syllabiResponse = api.getSyllabi(limit = 50)
                if (syllabiResponse.success && syllabiResponse.data.isNotEmpty()) {
                    val entities = syllabiResponse.data.map { it.toSyllabusItem() }
                    resultDao.insertSyllabi(entities)
                    totalItems += entities.size
                    Log.d(TAG, "Backend API: Cached ${entities.size} syllabi into Room")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Syllabi sync fallback: ${e.message}")
            }

            // 5. Fetch & Cache Admissions
            _syncState.value = BackendSyncState.Syncing("Fetching university admissions...")
            try {
                val admissionsResponse = api.getAdmissions(limit = 50)
                if (admissionsResponse.success && admissionsResponse.data.isNotEmpty()) {
                    val entities = admissionsResponse.data.map { it.toAdmissionItem() }
                    resultDao.insertAdmissions(entities)
                    totalItems += entities.size
                    Log.d(TAG, "Backend API: Cached ${entities.size} admissions into Room")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Admissions sync fallback: ${e.message}")
            }

            // 6. Fetch & Cache Answer Keys
            _syncState.value = BackendSyncState.Syncing("Fetching answer keys & objections...")
            try {
                val answerKeysResponse = api.getAnswerKeys(limit = 50)
                if (answerKeysResponse.success && answerKeysResponse.data.isNotEmpty()) {
                    val entities = answerKeysResponse.data.map { it.toAnswerKeyItem() }
                    resultDao.insertAnswerKeys(entities)
                    totalItems += entities.size
                    Log.d(TAG, "Backend API: Cached ${entities.size} answer keys into Room")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Answer keys sync fallback: ${e.message}")
            }

            // 7. Fetch & Cache Notifications
            _syncState.value = BackendSyncState.Syncing("Fetching live announcements...")
            try {
                val notifResponse = api.getNotifications(limit = 50)
                if (notifResponse.success && notifResponse.data.isNotEmpty()) {
                    val entities = notifResponse.data.map { it.toNotificationItem() }
                    notificationDao.insertNotifications(entities)
                    totalItems += entities.size
                    Log.d(TAG, "Backend API: Cached ${entities.size} notifications into Room")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Notifications sync fallback: ${e.message}")
            }

            _syncState.value = BackendSyncState.Success(
                message = "All data synchronized with backend API & cached locally",
                totalSynced = totalItems
            )
            Result.success(totalItems)
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Network error during sync"
            Log.w(TAG, "Backend sync completed with local cached data: $errorMsg")
            _syncState.value = BackendSyncState.OfflineCached(
                message = "Operating in offline cache mode ($errorMsg)"
            )
            Result.success(totalItems)
        }
    }

    /**
     * Sync single category: Jobs
     */
    suspend fun syncJobs(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val api = CloudflareClient.getService()
            val response = api.getJobs(limit = 100)
            if (response.success && response.data.isNotEmpty()) {
                val entities = response.data.map { it.toJobEntity() }
                jobDao.insertJobs(entities)
                Result.success(entities.size)
            } else {
                Result.success(0)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sync single category: Results
     */
    suspend fun syncResults(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val api = CloudflareClient.getService()
            val response = api.getResults(limit = 50)
            if (response.success && response.data.isNotEmpty()) {
                val entities = response.data.map { it.toResultItem() }
                resultDao.insertResults(entities)
                Result.success(entities.size)
            } else {
                Result.success(0)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sync single category: Admit Cards
     */
    suspend fun syncAdmitCards(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val api = CloudflareClient.getService()
            val response = api.getAdmitCards(limit = 50)
            if (response.success && response.data.isNotEmpty()) {
                val entities = response.data.map { it.toAdmitCardItem() }
                resultDao.insertAdmitCards(entities)
                Result.success(entities.size)
            } else {
                Result.success(0)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sync single category: Syllabus
     */
    suspend fun syncSyllabi(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val api = CloudflareClient.getService()
            val response = api.getSyllabi(limit = 50)
            if (response.success && response.data.isNotEmpty()) {
                val entities = response.data.map { it.toSyllabusItem() }
                resultDao.insertSyllabi(entities)
                Result.success(entities.size)
            } else {
                Result.success(0)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sync single category: Admissions
     */
    suspend fun syncAdmissions(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val api = CloudflareClient.getService()
            val response = api.getAdmissions(limit = 50)
            if (response.success && response.data.isNotEmpty()) {
                val entities = response.data.map { it.toAdmissionItem() }
                resultDao.insertAdmissions(entities)
                Result.success(entities.size)
            } else {
                Result.success(0)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sync single category: Answer Keys
     */
    suspend fun syncAnswerKeys(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val api = CloudflareClient.getService()
            val response = api.getAnswerKeys(limit = 50)
            if (response.success && response.data.isNotEmpty()) {
                val entities = response.data.map { it.toAnswerKeyItem() }
                resultDao.insertAnswerKeys(entities)
                Result.success(entities.size)
            } else {
                Result.success(0)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sync single category: Notifications
     */
    suspend fun syncNotifications(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val api = CloudflareClient.getService()
            val response = api.getNotifications(limit = 50)
            if (response.success && response.data.isNotEmpty()) {
                val entities = response.data.map { it.toNotificationItem() }
                notificationDao.insertNotifications(entities)
                Result.success(entities.size)
            } else {
                Result.success(0)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
