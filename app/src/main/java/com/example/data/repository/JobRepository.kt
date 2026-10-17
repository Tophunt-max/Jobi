package com.example.data.repository

import com.example.data.local.JobDao
import com.example.data.local.NotificationDao
import com.example.data.local.ResultDao
import com.example.data.sample.InitialDataSeed
import com.example.model.AdmissionItem
import com.example.model.AdmitCardItem
import com.example.model.AnswerKeyItem
import com.example.model.JobEntity
import com.example.model.NotificationItem
import com.example.model.ResultItem
import com.example.model.SyllabusItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class JobRepository(
    private val jobDao: JobDao,
    private val resultDao: ResultDao,
    private val notificationDao: NotificationDao
) {
    val allJobs: Flow<List<JobEntity>> = jobDao.getAllJobs()
    val bookmarkedJobs: Flow<List<JobEntity>> = jobDao.getBookmarkedJobs()
    val closingSoonJobs: Flow<List<JobEntity>> = jobDao.getClosingSoonJobs()

    val allResults: Flow<List<ResultItem>> = resultDao.getAllResults()
    val allAdmitCards: Flow<List<AdmitCardItem>> = resultDao.getAllAdmitCards()
    val allSyllabi: Flow<List<SyllabusItem>> = resultDao.getAllSyllabi()
    val allAnswerKeys: Flow<List<AnswerKeyItem>> = resultDao.getAllAnswerKeys()
    val allAdmissions: Flow<List<AdmissionItem>> = resultDao.getAllAdmissions()

    val allNotifications: Flow<List<NotificationItem>> = notificationDao.getAllNotifications()
    val unreadNotifCount: Flow<Int> = notificationDao.getUnreadCount()

    suspend fun initializeDatabaseIfEmpty() {
        val existingCount = jobDao.getActiveJobsCount()
        if (existingCount < InitialDataSeed.initialJobs.size) {
            jobDao.insertJobs(InitialDataSeed.initialJobs)
        }
        resultDao.insertResults(InitialDataSeed.initialResults)
        resultDao.insertAdmitCards(InitialDataSeed.initialAdmitCards)
        resultDao.insertSyllabi(InitialDataSeed.initialSyllabi)
        resultDao.insertAnswerKeys(InitialDataSeed.initialAnswerKeys)
        resultDao.insertAdmissions(InitialDataSeed.initialAdmissions)
        notificationDao.insertNotifications(InitialDataSeed.initialNotifications)
    }

    fun getJobById(id: String): Flow<JobEntity?> = jobDao.getJobById(id)

    fun searchJobs(query: String): Flow<List<JobEntity>> = jobDao.searchJobs(query)

    fun getJobsByState(state: String): Flow<List<JobEntity>> =
        if (state == "All India" || state == "ALL") jobDao.getAllJobs()
        else jobDao.getJobsByState(state)

    fun getJobsByCategory(category: String): Flow<List<JobEntity>> =
        if (category == "All Categories" || category == "ALL") jobDao.getAllJobs()
        else jobDao.getJobsByCategory(category)

    suspend fun toggleBookmark(id: String, currentStatus: Boolean) {
        jobDao.updateBookmark(id, !currentStatus)
    }

    suspend fun insertJob(job: JobEntity) {
        jobDao.insertJob(job)
    }

    suspend fun markNotificationAsRead(id: String) {
        notificationDao.markAsRead(id)
    }

    suspend fun markAllNotificationsAsRead() {
        notificationDao.markAllAsRead()
    }
}
