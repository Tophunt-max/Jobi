package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.model.ContentSource
import com.example.model.ImportCandidate
import kotlinx.coroutines.flow.Flow

@Dao
interface AdminDao {
    @Query("SELECT * FROM content_sources ORDER BY name ASC")
    fun getContentSources(): Flow<List<ContentSource>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSources(sources: List<ContentSource>)

    @Query("SELECT * FROM import_candidates ORDER BY retrievedTimestamp DESC")
    fun getImportCandidates(): Flow<List<ImportCandidate>>

    @Query("SELECT * FROM import_candidates WHERE reviewStatus = 'PENDING_REVIEW' ORDER BY retrievedTimestamp DESC")
    fun getPendingCandidates(): Flow<List<ImportCandidate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCandidate(candidate: ImportCandidate)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCandidates(candidates: List<ImportCandidate>)

    @Query("UPDATE import_candidates SET reviewStatus = :status, reviewerNotes = :notes WHERE id = :id")
    suspend fun updateCandidateReview(id: String, status: String, notes: String)

    @Query("UPDATE content_sources SET lastSyncTime = :syncTime, totalDiscovered = totalDiscovered + :addedCount WHERE id = :sourceId")
    suspend fun updateSourceSync(sourceId: String, syncTime: String, addedCount: Int)
}
