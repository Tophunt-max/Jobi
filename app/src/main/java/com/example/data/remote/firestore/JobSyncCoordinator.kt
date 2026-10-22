package com.example.data.remote.firestore

import android.util.Log
import com.example.data.local.JobDao
import com.example.model.JobEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

sealed class SyncStatus {
    object Idle : SyncStatus()
    object Syncing : SyncStatus()
    data class Synced(val count: Int, val timestamp: Long = System.currentTimeMillis()) : SyncStatus()
    data class Offline(val reason: String) : SyncStatus()
}

/**
 * Bridges cloud Firestore job updates into the local Room database,
 * delivering an offline-first architecture with instant cloud synchronizations.
 */
class JobSyncCoordinator(
    private val firestoreService: JobFirestoreService,
    private val jobDao: JobDao
) {
    companion object {
        private const val TAG = "JobSyncCoordinator"
    }

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    /**
     * Initiates real-time listener observing cloud Firestore emissions and updating Room cache.
     */
    fun startRealtimeSync(scope: CoroutineScope) {
        if (!firestoreService.isAvailable()) {
            _syncStatus.value = SyncStatus.Offline("Cloud sync offline; running on local Room cache")
            Log.d(TAG, "Firestore unavailable, keeping offline mode")
            return
        }

        scope.launch {
            _syncStatus.value = SyncStatus.Syncing
            firestoreService.observeJobs()
                .catch { exception ->
                    Log.w(TAG, "Error in real-time Firestore sync stream", exception)
                    _syncStatus.value = SyncStatus.Offline("Sync error: ${exception.localizedMessage}")
                }
                .collect { documents ->
                    try {
                        if (documents.isNotEmpty()) {
                            val entities = documents.map { it.toJobEntity() }
                            jobDao.insertJobs(entities)
                            _syncStatus.value = SyncStatus.Synced(entities.size)
                            Log.d(TAG, "Successfully synced ${entities.size} jobs from Firestore to Room")
                        } else {
                            _syncStatus.value = SyncStatus.Synced(0)
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Notice: could not persist Firestore updates into Room: ${e.message}")
                        _syncStatus.value = SyncStatus.Offline("Local storage notice: ${e.message}")
                    }
                }
        }
    }

    /**
     * Performs a one-time manual pull from Firestore to Room cache.
     */
    suspend fun syncNow(): Result<Int> {
        if (!firestoreService.isAvailable()) {
            _syncStatus.value = SyncStatus.Offline("Cloud sync offline")
            return Result.failure(IllegalStateException("Firestore is not available"))
        }

        _syncStatus.value = SyncStatus.Syncing
        val result = firestoreService.fetchJobsOnce()
        return result.mapCatching { docs ->
            if (docs.isNotEmpty()) {
                val entities = docs.map { it.toJobEntity() }
                jobDao.insertJobs(entities)
                _syncStatus.value = SyncStatus.Synced(entities.size)
                entities.size
            } else {
                _syncStatus.value = SyncStatus.Synced(0)
                0
            }
        }.onFailure { err ->
            _syncStatus.value = SyncStatus.Offline(err.localizedMessage ?: "Sync failed")
        }
    }

    /**
     * Publishes a job to Cloud Firestore and updates local cache.
     */
    suspend fun publishJob(job: JobEntity): Result<String> {
        val doc = JobFirestoreDocument.fromJobEntity(job)
        jobDao.insertJob(job) // Save locally first

        return if (firestoreService.isAvailable()) {
            val cloudResult = firestoreService.publishJob(doc)
            cloudResult.onSuccess { cloudId ->
                Log.i(TAG, "Job published both locally and to Cloud Firestore with ID $cloudId")
            }
            cloudResult
        } else {
            Log.i(TAG, "Firestore offline, job saved to local Room database: ${job.id}")
            Result.success(job.id)
        }
    }

    /**
     * Searches and filters jobs using Firestore data service with automatic Room fallback.
     */
    fun searchAndFilterJobs(
        category: String? = null,
        searchQuery: String? = null
    ): Flow<List<JobEntity>> {
        return if (firestoreService.isAvailable()) {
            firestoreService.searchAndFilterJobs(category, searchQuery)
                .map { docs -> docs.map { it.toJobEntity() } }
        } else {
            if (category.isNullOrBlank() || category.equals("ALL", ignoreCase = true) || category.equals("All Categories", ignoreCase = true)) {
                if (searchQuery.isNullOrBlank()) {
                    jobDao.getAllJobs()
                } else {
                    jobDao.searchJobs(searchQuery)
                }
            } else {
                jobDao.getJobsByCategory(category).map { list ->
                    if (searchQuery.isNullOrBlank()) {
                        list
                    } else {
                        val q = searchQuery.trim()
                        list.filter {
                            it.title.contains(q, ignoreCase = true) ||
                                it.organization.contains(q, ignoreCase = true) ||
                                it.advtNumber.contains(q, ignoreCase = true)
                        }
                    }
                }
            }
        }
    }
}
