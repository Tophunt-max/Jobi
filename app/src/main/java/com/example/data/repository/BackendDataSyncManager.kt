package com.example.data.repository

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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

sealed class BackendSyncState {
    object Idle : BackendSyncState()
    data class Syncing(val currentEntity: String) : BackendSyncState()
    data class Success(
        val message: String,
        val totalSynced: Int,
        val timestamp: Long = System.currentTimeMillis()
    ) : BackendSyncState()

    /** Sync failed; the UI keeps showing whatever is cached locally. */
    data class OfflineCached(
        val message: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : BackendSyncState()
}

/**
 * Single source of truth for pulling server content into Room.
 * One request (`/api/sync/all`) fetches every category; writes are transactional and
 * preserve local user state (bookmarks, read flags). Concurrent calls are serialised.
 */
class BackendDataSyncManager(
    private val jobDao: JobDao,
    private val resultDao: ResultDao,
    private val notificationDao: NotificationDao
) {
    companion object {
        private const val TAG = "BackendDataSync"
    }

    private val mutex = Mutex()
    private val _syncState = MutableStateFlow<BackendSyncState>(BackendSyncState.Idle)
    val syncState: StateFlow<BackendSyncState> = _syncState.asStateFlow()

    suspend fun syncAllData(): Result<Int> = mutex.withLock {
        withContext(Dispatchers.IO) {
            if (!CloudflareConfig.isConfigured()) {
                val msg = "Backend URL is not configured in this build"
                _syncState.value = BackendSyncState.OfflineCached(msg)
                return@withContext Result.failure(IllegalStateException(msg))
            }

            _syncState.value = BackendSyncState.Syncing("Fetching latest notices…")
            try {
                val response = CloudflareClient.getService().getSyncAllData()
                val data = response.data
                if (!response.success || data == null) {
                    throw IOException(response.message ?: "Backend returned no data")
                }

                val jobs = data.jobs.map { it.toJobEntity() }
                jobDao.replaceFromServer(jobs)
                resultDao.replaceAll(
                    results = data.results.map { it.toResultItem() },
                    admitCards = data.admitCards.map { it.toAdmitCardItem() },
                    syllabi = data.syllabi.map { it.toSyllabusItem() },
                    answerKeys = data.answerKeys.map { it.toAnswerKeyItem() },
                    admissions = data.admissions.map { it.toAdmissionItem() }
                )
                notificationDao.upsertFromServer(data.notifications.map { it.toNotificationItem() })

                val total = jobs.size + data.results.size + data.admitCards.size + data.syllabi.size +
                    data.admissions.size + data.answerKeys.size
                _syncState.value = BackendSyncState.Success("Up to date", total)
                Result.success(total)
            } catch (e: Exception) {
                val msg = when (e) {
                    is HttpException -> "Server error (HTTP ${e.code()}). Showing saved data."
                    is IOException -> "No connection. Showing saved data."
                    else -> "Sync failed. Showing saved data."
                }
                Log.w(TAG, "Sync failed: ${e.javaClass.simpleName}: ${e.message}")
                _syncState.value = BackendSyncState.OfflineCached(msg)
                Result.failure(e)
            }
        }
    }
}
