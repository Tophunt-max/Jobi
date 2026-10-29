package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.model.AdmissionItem
import com.example.model.AdmitCardItem
import com.example.model.AnswerKeyItem
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
        AdmissionItem::class,
        NotificationItem::class
    ],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun jobDao(): JobDao
    abstract fun resultDao(): ResultDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * v2 -> v3: admin-only tables were removed from the user app, and the bundled
         * sample (non-official) content is purged. Bookmarks and read state are kept.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS content_sources")
                db.execSQL("DROP TABLE IF EXISTS import_candidates")
                // Sample data shipped by v2 is not from an official source. Non-bookmarked rows
                // are removed; the next sync repopulates from the backend.
                db.execSQL("DELETE FROM jobs WHERE isBookmarked = 0")
                db.execSQL("DELETE FROM results")
                db.execSQL("DELETE FROM admit_cards")
                db.execSQL("DELETE FROM syllabuses")
                db.execSQL("DELETE FROM answer_keys")
                db.execSQL("DELETE FROM admissions")
                db.execSQL("DELETE FROM notifications WHERE type != 'DEADLINE_ALERT'")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sarkari_job_portal.db"
                )
                    .addMigrations(MIGRATION_2_3)
                    // v1 only existed in pre-release builds and has no migration path.
                    .fallbackToDestructiveMigrationFrom(dropAllTables = true, 1)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
