package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.model.JobEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JobDao {
    @Query("SELECT * FROM jobs ORDER BY publishedAt DESC")
    fun getAllJobs(): Flow<List<JobEntity>>

    @Query("SELECT * FROM jobs WHERE isBookmarked = 1 ORDER BY publishedAt DESC")
    fun getBookmarkedJobs(): Flow<List<JobEntity>>

    @Query("SELECT * FROM jobs WHERE id = :id LIMIT 1")
    fun getJobById(id: String): Flow<JobEntity?>

    @Query("SELECT * FROM jobs WHERE slug = :slug LIMIT 1")
    fun getJobBySlug(slug: String): Flow<JobEntity?>

    @Query("SELECT * FROM jobs WHERE status = 'CLOSING_SOON' OR status = 'ACTIVE' ORDER BY applicationClosingDate ASC LIMIT 6")
    fun getClosingSoonJobs(): Flow<List<JobEntity>>

    @Query("SELECT * FROM jobs WHERE title LIKE '%' || :query || '%' OR organization LIKE '%' || :query || '%' OR advtNumber LIKE '%' || :query || '%' OR department LIKE '%' || :query || '%'")
    fun searchJobs(query: String): Flow<List<JobEntity>>

    @Query("SELECT * FROM jobs WHERE stateOrAllIndia = :state OR stateOrAllIndia = 'All India' ORDER BY publishedAt DESC")
    fun getJobsByState(state: String): Flow<List<JobEntity>>

    @Query("SELECT * FROM jobs WHERE category = :category ORDER BY publishedAt DESC")
    fun getJobsByCategory(category: String): Flow<List<JobEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobs(jobs: List<JobEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: JobEntity)

    @Query("UPDATE jobs SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun updateBookmark(id: String, isBookmarked: Boolean)

    @Query("UPDATE jobs SET status = :status WHERE id = :id")
    suspend fun updateJobStatus(id: String, status: String)

    @Query("SELECT COUNT(*) FROM jobs WHERE status != 'EXPIRED'")
    suspend fun getActiveJobsCount(): Int

    @Query("DELETE FROM jobs WHERE id = :id")
    suspend fun deleteJob(id: String)
}
