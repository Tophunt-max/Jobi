package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.model.JobEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JobDao {
    @Query("SELECT * FROM jobs ORDER BY publishedAt DESC")
    fun getAllJobs(): Flow<List<JobEntity>>

    @Query("SELECT * FROM jobs WHERE isBookmarked = 1 ORDER BY publishedAt DESC")
    fun getBookmarkedJobs(): Flow<List<JobEntity>>

    @Query("SELECT * FROM jobs WHERE isBookmarked = 1")
    suspend fun getBookmarkedJobsOnce(): List<JobEntity>

    @Query("SELECT * FROM jobs WHERE id = :id LIMIT 1")
    fun getJobById(id: String): Flow<JobEntity?>

    @Query("SELECT * FROM jobs WHERE id = :id OR slug = :id LIMIT 1")
    suspend fun findByIdOrSlug(id: String): JobEntity?

    @Query(
        "SELECT * FROM jobs WHERE status IN ('CLOSING_SOON', 'ACTIVE') AND applicationClosingDate != '' " +
            "ORDER BY applicationClosingDate ASC LIMIT 6"
    )
    fun getClosingSoonJobs(): Flow<List<JobEntity>>

    @Query("SELECT id FROM jobs WHERE isBookmarked = 1")
    suspend fun getBookmarkedIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobs(jobs: List<JobEntity>)

    @Query("DELETE FROM jobs WHERE isBookmarked = 0 AND id NOT IN (:keepIds)")
    suspend fun deleteUnbookmarkedExcept(keepIds: List<String>)

    @Query("DELETE FROM jobs WHERE isBookmarked = 0")
    suspend fun deleteAllUnbookmarked()

    /**
     * Replaces the server-owned job list while preserving local user state:
     * - bookmark flags are carried over for jobs that still exist;
     * - bookmarked jobs that disappeared from the server are kept so the user doesn't lose them;
     * - other jobs no longer returned by the server (expired, rejected) are removed.
     */
    @Transaction
    suspend fun replaceFromServer(serverJobs: List<JobEntity>) {
        val bookmarked = getBookmarkedIds().toHashSet()
        val merged = serverJobs.map { if (it.id in bookmarked) it.copy(isBookmarked = true) else it }
        if (merged.isEmpty()) {
            deleteAllUnbookmarked()
        } else {
            // SQLite limits bound variables; chunk the keep-list defensively.
            if (merged.size <= 900) {
                deleteUnbookmarkedExcept(merged.map { it.id })
            } else {
                val keep = merged.map { it.id }.toHashSet()
                getAllIds().filter { it !in keep && it !in bookmarked }.chunked(900).forEach { deleteByIds(it) }
            }
            insertJobs(merged)
        }
    }

    @Query("SELECT id FROM jobs")
    suspend fun getAllIds(): List<String>

    @Query("DELETE FROM jobs WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("UPDATE jobs SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun updateBookmark(id: String, isBookmarked: Boolean)
}
