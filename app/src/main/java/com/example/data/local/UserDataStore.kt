package com.example.data.local

import android.content.Context
import android.util.Log
import com.example.data.remote.cloudflare.CloudflareClient
import com.example.model.AppliedExamRecord
import com.example.model.AspirantDocumentLocker
import com.example.model.UserEligibilityProfile
import com.squareup.moshi.Types

/**
 * Persists the user's own data (eligibility profile, document locker, applied exams)
 * in app-private storage. This data never leaves the device unless the user shares it,
 * and it is excluded from cloud backup (see res/xml/backup_rules.xml).
 */
class UserDataStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val moshi = CloudflareClient.moshi

    // Generated (@JsonClass) adapters; created lazily so construction is cheap on any thread.
    private val profileAdapter by lazy { moshi.adapter(UserEligibilityProfile::class.java) }
    private val lockerAdapter by lazy { moshi.adapter(AspirantDocumentLocker::class.java) }
    private val examsAdapter by lazy {
        moshi.adapter<List<AppliedExamRecord>>(
            Types.newParameterizedType(List::class.java, AppliedExamRecord::class.java)
        )
    }

    fun loadProfile(): UserEligibilityProfile = read(KEY_PROFILE) { profileAdapter.fromJson(it) } ?: UserEligibilityProfile()
    fun saveProfile(value: UserEligibilityProfile) = write(KEY_PROFILE, profileAdapter.toJson(value))

    fun loadLocker(): AspirantDocumentLocker = read(KEY_LOCKER) { lockerAdapter.fromJson(it) } ?: AspirantDocumentLocker()
    fun saveLocker(value: AspirantDocumentLocker) = write(KEY_LOCKER, lockerAdapter.toJson(value))

    fun loadAppliedExams(): List<AppliedExamRecord> = read(KEY_EXAMS) { examsAdapter.fromJson(it) } ?: emptyList()
    fun saveAppliedExams(value: List<AppliedExamRecord>) = write(KEY_EXAMS, examsAdapter.toJson(value))

    private fun <T> read(key: String, parse: (String) -> T?): T? {
        val raw = prefs.getString(key, null) ?: return null
        return try {
            parse(raw)
        } catch (e: Exception) {
            // Corrupt or schema-incompatible data: fall back to defaults rather than crash.
            Log.w(TAG, "Discarding unreadable $key: ${e.javaClass.simpleName}")
            null
        }
    }

    private fun write(key: String, json: String) {
        prefs.edit().putString(key, json).apply()
    }

    private companion object {
        const val TAG = "UserDataStore"
        const val PREFS_NAME = "user_private_data"
        const val KEY_PROFILE = "eligibility_profile_v1"
        const val KEY_LOCKER = "document_locker_v1"
        const val KEY_EXAMS = "applied_exams_v1"
    }
}
