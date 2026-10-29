package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.model.NotificationItem
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationItem>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationItem>)

    @Query("SELECT id FROM notifications WHERE isRead = 1")
    suspend fun getReadIds(): List<String>

    /** Merges server announcements without losing the user's read state. */
    @Transaction
    suspend fun upsertFromServer(serverItems: List<NotificationItem>) {
        val read = getReadIds().toHashSet()
        insertNotifications(serverItems.map { if (it.id in read) it.copy(isRead = true) else it })
        pruneOlderThan(System.currentTimeMillis() - 60L * 24 * 3600 * 1000)
    }

    @Query("DELETE FROM notifications WHERE timestamp < :cutoff")
    suspend fun pruneOlderThan(cutoff: Long)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()
}
