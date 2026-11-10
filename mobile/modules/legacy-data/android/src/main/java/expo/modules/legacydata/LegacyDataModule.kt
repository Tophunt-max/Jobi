package expo.modules.legacydata

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import expo.modules.kotlin.exception.Exceptions
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import org.json.JSONArray
import org.json.JSONObject

/**
 * Reads the data the old Kotlin app (same applicationId) left on the device, so a
 * Play Store update to the Expo app keeps the user's profile, locker, applications,
 * bookmarks and alert preferences.
 *
 * Sources (see the Kotlin app):
 *  - SharedPreferences "user_private_data": eligibility_profile_v1, document_locker_v1, applied_exams_v1 (Moshi JSON)
 *  - SharedPreferences "targeted_job_alert_prefs": pref_* keys
 *  - Room database "rozgarone.db": jobs.isBookmarked = 1, notifications.isRead = 1
 */
class LegacyDataModule : Module() {
  private val context: Context
    get() = appContext.reactContext ?: throw Exceptions.ReactContextLost()

  override fun definition() = ModuleDefinition {
    Name("LegacyData")

    /** Returns a JSON string, or null when there is nothing to migrate. */
    AsyncFunction("readAsync") {
      read(context)
    }

    /** Removes the old copies once the JS side has saved them. */
    AsyncFunction("clearAsync") {
      clear(context)
    }
  }

  private fun read(ctx: Context): String? {
    val out = JSONObject()

    try {
      val prefs = ctx.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE)
      for (key in USER_KEYS) {
        prefs.getString(key, null)?.let { out.put(key, it) }
      }
    } catch (e: Exception) {
      Log.w(TAG, "user prefs: ${e.javaClass.simpleName}")
    }

    try {
      val alerts = ctx.getSharedPreferences(ALERT_PREFS, Context.MODE_PRIVATE).all
      if (alerts.isNotEmpty()) {
        val obj = JSONObject()
        for ((k, v) in alerts) {
          when (v) {
            is Set<*> -> obj.put(k, JSONArray(v.filterIsInstance<String>()))
            null -> Unit
            else -> obj.put(k, v)
          }
        }
        out.put("alert_prefs", obj)
      }
    } catch (e: Exception) {
      Log.w(TAG, "alert prefs: ${e.javaClass.simpleName}")
    }

    val dbFile = ctx.getDatabasePath(DB_NAME)
    if (dbFile.exists()) {
      try {
        // Read-write open so SQLite can use the WAL/SHM files Room created; nothing is written.
        SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READWRITE).use { db ->
          db.rawQuery("SELECT * FROM jobs WHERE isBookmarked = 1", null).use { c ->
            val rows = JSONArray()
            while (c.moveToNext()) rows.put(rowToJson(c))
            if (rows.length() > 0) out.put("bookmarked_jobs", rows)
          }
          db.rawQuery("SELECT id FROM notifications WHERE isRead = 1", null).use { c ->
            val ids = JSONArray()
            while (c.moveToNext()) ids.put(c.getString(0))
            if (ids.length() > 0) out.put("read_notification_ids", ids)
          }
        }
      } catch (e: Exception) {
        Log.w(TAG, "room db: ${e.javaClass.simpleName}")
      }
    }

    return if (out.length() == 0) null else out.toString()
  }

  private fun rowToJson(c: Cursor): JSONObject {
    val obj = JSONObject()
    for (i in 0 until c.columnCount) {
      val name = c.getColumnName(i)
      when (c.getType(i)) {
        Cursor.FIELD_TYPE_INTEGER -> obj.put(name, c.getLong(i))
        Cursor.FIELD_TYPE_FLOAT -> obj.put(name, c.getDouble(i))
        Cursor.FIELD_TYPE_STRING -> obj.put(name, c.getString(i))
        else -> Unit
      }
    }
    return obj
  }

  private fun clear(ctx: Context) {
    try {
      ctx.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE).edit().clear().commit()
      ctx.getSharedPreferences(ALERT_PREFS, Context.MODE_PRIVATE).edit().clear().commit()
      ctx.getSharedPreferences(LEGACY_SCHEDULER_PREFS, Context.MODE_PRIVATE).edit().clear().commit()
      ctx.deleteDatabase(DB_NAME)
    } catch (e: Exception) {
      Log.w(TAG, "clear: ${e.javaClass.simpleName}")
    }
  }

  private companion object {
    const val TAG = "LegacyData"
    const val USER_PREFS = "user_private_data"
    const val ALERT_PREFS = "targeted_job_alert_prefs"
    const val LEGACY_SCHEDULER_PREFS = "deadline_scheduler_prefs"
    const val DB_NAME = "rozgarone.db"
    val USER_KEYS = listOf("eligibility_profile_v1", "document_locker_v1", "applied_exams_v1")
  }
}
