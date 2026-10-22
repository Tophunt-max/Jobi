package com.example.data.remote.firestore

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Service class for managing Government Job Listing documents in Cloud Firestore.
 * Supports real-time snapshot streams, direct queries, and full CRUD operations.
 */
class JobFirestoreService(
    private val context: Context? = null,
    private val firestoreProvider: () -> FirebaseFirestore? = {
        try {
            val hasApps = context?.let {
                try {
                    FirebaseApp.getApps(it).isNotEmpty()
                } catch (_: Exception) {
                    false
                }
            } ?: false

            if (hasApps) {
                FirebaseFirestore.getInstance()
            } else {
                null
            }
        } catch (e: Throwable) {
            Log.w("JobFirestoreService", "Firestore not initialized or unavailable: ${e.message}")
            null
        }
    }
) {
    companion object {
        private const val TAG = "JobFirestoreService"
        const val JOBS_COLLECTION = "jobs"
    }

    private val firestore: FirebaseFirestore?
        get() = try {
            firestoreProvider()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to resolve Firestore: ${e.message}")
            null
        }

    /**
     * Checks if Firestore client is initialized and operational.
     */
    fun isAvailable(): Boolean = firestore != null

    /**
     * Real-time stream of all active government jobs ordered by publishedAt or creation date.
     */
    fun observeJobs(): Flow<List<JobFirestoreDocument>> {
        val db = firestore ?: return emptyFlow()

        return callbackFlow {
            val collectionRef = db.collection(JOBS_COLLECTION)
            val registration = collectionRef
                .orderBy("applicationClosingDate", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Error listening to jobs collection", error)
                        // Don't close flow, keep waiting for reconnection
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val docs = snapshot.documents.mapNotNull { doc ->
                            try {
                                doc.toObject(JobFirestoreDocument::class.java)?.copy(id = doc.id)
                            } catch (e: Exception) {
                                Log.w(TAG, "Error deserializing job doc ${doc.id}", e)
                                null
                            }
                        }
                        trySend(docs)
                    }
                }

            awaitClose {
                registration.remove()
            }
        }
    }

    /**
     * Real-time stream of jobs filtered by category (e.g. 'Admit Card', 'Result', 'SSC', 'UPSC').
     */
    fun observeJobsByCategory(category: String): Flow<List<JobFirestoreDocument>> {
        val db = firestore ?: return emptyFlow()
        if (category.isBlank() || category.equals("ALL", ignoreCase = true) || category.equals("All Categories", ignoreCase = true)) {
            return observeJobs()
        }

        return callbackFlow {
            val collectionRef = db.collection(JOBS_COLLECTION)
            val registration = collectionRef
                .whereEqualTo("category", category)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Error listening to category $category", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val docs = snapshot.documents.mapNotNull { doc ->
                            try {
                                doc.toObject(JobFirestoreDocument::class.java)?.copy(id = doc.id)
                            } catch (e: Exception) {
                                Log.w(TAG, "Error deserializing job doc ${doc.id}", e)
                                null
                            }
                        }
                        trySend(docs)
                    }
                }

            awaitClose {
                registration.remove()
            }
        }
    }

    /**
     * Searches and filters jobs in Cloud Firestore by category and text query.
     */
    fun searchAndFilterJobs(
        category: String? = null,
        searchQuery: String? = null
    ): Flow<List<JobFirestoreDocument>> {
        val db = firestore ?: return emptyFlow()

        return callbackFlow {
            val collectionRef = db.collection(JOBS_COLLECTION)
            val baseQuery = if (!category.isNullOrBlank() && 
                !category.equals("ALL", ignoreCase = true) && 
                !category.equals("All Categories", ignoreCase = true)) {
                collectionRef.whereEqualTo("category", category)
            } else {
                collectionRef
            }

            val registration = baseQuery.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Error in searchAndFilterJobs", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val filteredDocs = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(JobFirestoreDocument::class.java)?.copy(id = doc.id)
                        } catch (e: Exception) {
                            null
                        }
                    }.filter { doc ->
                        if (searchQuery.isNullOrBlank()) {
                            true
                        } else {
                            val q = searchQuery.trim()
                            doc.title.contains(q, ignoreCase = true) ||
                                doc.organization.contains(q, ignoreCase = true) ||
                                doc.advtNumber.contains(q, ignoreCase = true) ||
                                doc.qualification.contains(q, ignoreCase = true) ||
                                doc.stateOrAllIndia.contains(q, ignoreCase = true) ||
                                doc.category.contains(q, ignoreCase = true)
                        }
                    }
                    trySend(filteredDocs)
                }
            }

            awaitClose {
                registration.remove()
            }
        }
    }

    /**
     * Fetches all job documents once.
     */
    suspend fun fetchJobsOnce(): Result<List<JobFirestoreDocument>> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))

        return suspendCancellableCoroutine { continuation ->
            db.collection(JOBS_COLLECTION)
                .get()
                .addOnSuccessListener { snapshot ->
                    val docs = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(JobFirestoreDocument::class.java)?.copy(id = doc.id)
                        } catch (e: Exception) {
                            Log.w(TAG, "Error deserializing job doc ${doc.id}", e)
                            null
                        }
                    }
                    continuation.resume(Result.success(docs))
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error fetching jobs from Firestore", exception)
                    continuation.resume(Result.failure(exception))
                }
        }
    }

    /**
     * Retrieves a single job document by ID.
     */
    suspend fun getJobById(jobId: String): Result<JobFirestoreDocument?> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))

        return suspendCancellableCoroutine { continuation ->
            db.collection(JOBS_COLLECTION)
                .document(jobId)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val item = doc.toObject(JobFirestoreDocument::class.java)?.copy(id = doc.id)
                        continuation.resume(Result.success(item))
                    } else {
                        continuation.resume(Result.success(null))
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error fetching job $jobId", exception)
                    continuation.resume(Result.failure(exception))
                }
        }
    }

    /**
     * Publishes a new government job or creates a document with a designated ID.
     */
    suspend fun publishJob(document: JobFirestoreDocument): Result<String> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))

        return suspendCancellableCoroutine { continuation ->
            val docRef = if (document.id.isNotBlank()) {
                db.collection(JOBS_COLLECTION).document(document.id)
            } else {
                db.collection(JOBS_COLLECTION).document()
            }

            val docToSave = document.copy(id = docRef.id)

            docRef.set(docToSave)
                .addOnSuccessListener {
                    Log.i(TAG, "Successfully published job ${docRef.id}")
                    continuation.resume(Result.success(docRef.id))
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Failed to publish job doc", exception)
                    continuation.resume(Result.failure(exception))
                }
        }
    }

    /**
     * Updates an existing government job document.
     */
    suspend fun updateJob(document: JobFirestoreDocument): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        if (document.id.isBlank()) {
            return Result.failure(IllegalArgumentException("Document ID cannot be blank for update"))
        }

        return suspendCancellableCoroutine { continuation ->
            db.collection(JOBS_COLLECTION)
                .document(document.id)
                .set(document)
                .addOnSuccessListener {
                    Log.i(TAG, "Successfully updated job ${document.id}")
                    continuation.resume(Result.success(Unit))
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Failed to update job doc ${document.id}", exception)
                    continuation.resume(Result.failure(exception))
                }
        }
    }

    /**
     * Deletes a government job document by its ID.
     */
    suspend fun deleteJob(jobId: String): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        if (jobId.isBlank()) {
            return Result.failure(IllegalArgumentException("Job ID cannot be blank"))
        }

        return suspendCancellableCoroutine { continuation ->
            db.collection(JOBS_COLLECTION)
                .document(jobId)
                .delete()
                .addOnSuccessListener {
                    Log.i(TAG, "Successfully deleted job $jobId")
                    continuation.resume(Result.success(Unit))
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Failed to delete job $jobId", exception)
                    continuation.resume(Result.failure(exception))
                }
        }
    }
}
