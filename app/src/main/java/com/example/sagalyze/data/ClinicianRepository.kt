package com.sagalyze.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import timber.log.Timber

/**
 * ------------------------------------------------------------------------------
 *  ClinicianRepository.kt
 * ------------------------------------------------------------------------------
 * Responsible for all Firestore reads/writes related to clinicians.
 *
 * ✅ Uses Firestore "clinicians" collection, document ID = email
 * ✅ Supports offline access (Firestore persistence enabled in App.kt)
 * ✅ Auto-creates a clinician document on first login
 * ✅ Exposes suspend functions for cleaner ViewModel usage
 * ✅ Returns standardized RepositoryResult<T> (Success / Error)
 * ------------------------------------------------------------------------------
 */
object ClinicianRepository {

    private const val COLLECTION_NAME = "clinicians"
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    /**
     * Fetch clinician profile using email as document ID.
     * If document does NOT exist, returns null (first time login).
     */
    suspend fun getClinician(email: String): RepositoryResult<Clinician?> {
        return try {
            val snapshot = db.collection(COLLECTION_NAME)
                .document(email)
                .get()
                .await()

            if (snapshot.exists()) {
                RepositoryResult.Success(snapshot.toObject(Clinician::class.java))
            } else {
                RepositoryResult.Success(null) // no profile yet
            }
        } catch (e: Exception) {
            Timber.e(e, "Firestore getClinician() failed")
            RepositoryResult.Error(e)
        }
    }

    /**
     * Creates or updates an existing clinician record.
     * Uses merge() so future schema changes do not overwrite fields.
     */
    suspend fun saveClinician(clinician: Clinician): RepositoryResult<Boolean> {
        return try {
            db.collection(COLLECTION_NAME)
                .document(clinician.email)
                .set(clinician, SetOptions.merge())
                .await()

            RepositoryResult.Success(true)
        } catch (e: Exception) {
            Timber.e(e, "Firestore saveClinician() failed")
            RepositoryResult.Error(e)
        }
    }
}

/**
 * Helper wrapper class so ViewModel can cleanly handle success / fail
 */
sealed class RepositoryResult<out T> {
    data class Success<T>(val data: T) : RepositoryResult<T>()
    data class Error(val exception: Throwable) : RepositoryResult<Nothing>()
}
