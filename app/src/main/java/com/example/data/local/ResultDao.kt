package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.model.AdmissionItem
import com.example.model.AdmitCardItem
import com.example.model.AnswerKeyItem
import com.example.model.ResultItem
import com.example.model.SyllabusItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ResultDao {
    @Query("SELECT * FROM results ORDER BY publicationDate DESC")
    fun getAllResults(): Flow<List<ResultItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResults(results: List<ResultItem>)

    @Query("SELECT * FROM admit_cards ORDER BY examDate ASC")
    fun getAllAdmitCards(): Flow<List<AdmitCardItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdmitCards(cards: List<AdmitCardItem>)

    @Query("SELECT * FROM syllabuses ORDER BY examName ASC")
    fun getAllSyllabi(): Flow<List<SyllabusItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyllabi(syllabi: List<SyllabusItem>)

    @Query("SELECT * FROM answer_keys ORDER BY releaseDate DESC")
    fun getAllAnswerKeys(): Flow<List<AnswerKeyItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswerKeys(keys: List<AnswerKeyItem>)

    @Query("SELECT * FROM admissions ORDER BY applicationClosingDate ASC")
    fun getAllAdmissions(): Flow<List<AdmissionItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdmissions(admissions: List<AdmissionItem>)
}
