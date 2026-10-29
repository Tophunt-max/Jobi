package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.model.AdmissionItem
import com.example.model.AdmitCardItem
import com.example.model.AnswerKeyItem
import com.example.model.ResultItem
import com.example.model.SyllabusItem
import kotlinx.coroutines.flow.Flow

/**
 * Server-owned reference content (no local user state), so each sync replaces it wholesale.
 */
@Dao
interface ResultDao {
    @Query("SELECT * FROM results ORDER BY publicationDate DESC")
    fun getAllResults(): Flow<List<ResultItem>>

    @Query("SELECT * FROM admit_cards ORDER BY examDate ASC")
    fun getAllAdmitCards(): Flow<List<AdmitCardItem>>

    @Query("SELECT * FROM syllabuses ORDER BY examName ASC")
    fun getAllSyllabi(): Flow<List<SyllabusItem>>

    @Query("SELECT * FROM answer_keys ORDER BY releaseDate DESC")
    fun getAllAnswerKeys(): Flow<List<AnswerKeyItem>>

    @Query("SELECT * FROM admissions ORDER BY applicationClosingDate ASC")
    fun getAllAdmissions(): Flow<List<AdmissionItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResults(results: List<ResultItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdmitCards(cards: List<AdmitCardItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyllabi(syllabi: List<SyllabusItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswerKeys(keys: List<AnswerKeyItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdmissions(admissions: List<AdmissionItem>)

    @Query("DELETE FROM results") suspend fun clearResults()
    @Query("DELETE FROM admit_cards") suspend fun clearAdmitCards()
    @Query("DELETE FROM syllabuses") suspend fun clearSyllabi()
    @Query("DELETE FROM answer_keys") suspend fun clearAnswerKeys()
    @Query("DELETE FROM admissions") suspend fun clearAdmissions()

    @Transaction
    suspend fun replaceAll(
        results: List<ResultItem>,
        admitCards: List<AdmitCardItem>,
        syllabi: List<SyllabusItem>,
        answerKeys: List<AnswerKeyItem>,
        admissions: List<AdmissionItem>
    ) {
        clearResults(); insertResults(results)
        clearAdmitCards(); insertAdmitCards(admitCards)
        clearSyllabi(); insertSyllabi(syllabi)
        clearAnswerKeys(); insertAnswerKeys(answerKeys)
        clearAdmissions(); insertAdmissions(admissions)
    }
}
