package com.example.data.repository

import com.example.data.local.JobDao
import com.example.data.local.NotificationDao
import com.example.data.local.ResultDao
import com.example.model.AdmissionItem
import com.example.model.AdmitCardItem
import com.example.model.AnswerKeyItem
import com.example.model.JobEntity
import com.example.model.NotificationItem
import com.example.model.ResultItem
import com.example.model.SyllabusItem
import kotlinx.coroutines.flow.Flow

/** Read access to the local cache. All server data arrives via [BackendDataSyncManager]. */
class JobRepository(
    private val jobDao: JobDao,
    resultDao: ResultDao,
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

    suspend fun findJob(idOrSlug: String): JobEntity? = jobDao.findByIdOrSlug(idOrSlug)

    suspend fun setBookmark(id: String, bookmarked: Boolean) {
        jobDao.updateBookmark(id, bookmarked)
    }

    suspend fun markNotificationAsRead(id: String) {
        notificationDao.markAsRead(id)
    }

    suspend fun markAllNotificationsAsRead() {
        notificationDao.markAllAsRead()
    }
}
