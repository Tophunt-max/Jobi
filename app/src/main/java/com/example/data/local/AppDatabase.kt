package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.model.AdmitCardItem
import com.example.model.AnswerKeyItem
import com.example.model.ContentSource
import com.example.model.ImportCandidate
import com.example.model.JobEntity
import com.example.model.NotificationItem
import com.example.model.ResultItem
import com.example.model.SyllabusItem

@Database(
    entities = [
        JobEntity::class,
        ResultItem::class,
        AdmitCardItem::class,
        SyllabusItem::class,
        AnswerKeyItem::class,
        NotificationItem::class,
        ContentSource::class,
        ImportCandidate::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun jobDao(): JobDao
    abstract fun resultDao(): ResultDao
    abstract fun notificationDao(): NotificationDao
    abstract fun adminDao(): AdminDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sarkari_job_portal.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
