package com.sagalyze.ui

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.sagalyze.data.AppDatabase
import com.sagalyze.data.Clinician
import com.sagalyze.data.ClinicianDao
import com.sagalyze.data.LocalUserStore
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.firebase.auth.FirebaseAuth
import com.sagalyze.data.ClinicianRepository
import com.sagalyze.data.RepositoryResult


/**
 * -----------------------------------------------------------------------------
 * LoginViewModel.kt
 * -----------------------------------------------------------------------------
 * Handles clinician authentication logic for SAGAlyze.
 *
 * ROLE:
 *  - Acts as the middle layer between the UI (LoginActivity/Compose screen)
 *    and the data layer (Room Database + LocalUserStore).
 *  - Responsible for validation, authentication, session persistence, and errors.
 *
 * DESIGN:
 *  - Offline-first (no internet dependency).
 *  - Secure password hashing and verification using Clinician’s companion utilities.
 *  - Uses coroutines for background I/O (non-blocking DB operations).
 *  - Logs safely (no sensitive data in logs).
 * -----------------------------------------------------------------------------
 */
class LoginViewModel : ViewModel() {

    // -------------------------------------------------------------------------
    // Coroutine Error Handling
    // -------------------------------------------------------------------------
    private val errorHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("LoginViewModel", "Unhandled error in coroutine: ${throwable.message}")
    }

    // -------------------------------------------------------------------------
    // DATABASE INITIALIZATION
    // -------------------------------------------------------------------------
    private fun getDao(context: Context): ClinicianDao {
        val db = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
        return db.clinicianDao()
    }

    // -------------------------------------------------------------------------
    // AUTHENTICATION LOGIC
    // -------------------------------------------------------------------------

    /**
     * Authenticates clinician using email & password.
     * If valid → saves session securely using LocalUserStore.
     *
     * @param context Application context (required for DB and preferences).
     * @param email Clinician’s email input.
     * @param password Clinician’s password input (plaintext from EditText).
     * @param onResult Callback returning success/failure and optional message.
     */
    fun authenticateClinician(
        context: Context,
        email: String,
        password: String,
        onResult: (success: Boolean, message: String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO + errorHandler) {
            try {
                val auth = FirebaseAuth.getInstance()

                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {

                            // ✅ Firestore sync in coroutine
                            viewModelScope.launch(Dispatchers.IO) {
                                val user = auth.currentUser
                                val result = ClinicianRepository.getClinician(email)

                                if (result is RepositoryResult.Success && result.data == null) {
                                    // First-time login → create Firestore doc
                                    val newClinician = Clinician(
                                        email = email,
                                        name = user?.displayName ?: "Clinician",
                                        passwordHash = null,   // Firebase handles auth now
                                        licenseNumber = null,
                                        fitzpatrickType = null
                                    )
                                    ClinicianRepository.saveClinician(newClinician)
                                }

                                withContext(Dispatchers.Main) {
                                    onResult(true, "Login Successful ✅")
                                }
                            }
                        } else {
                            // ❌ Auth failed
                            viewModelScope.launch(Dispatchers.Main) {
                                onResult(false, task.exception?.message ?: "Login failed ❌")
                            }
                        }
                    }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(false, "Unexpected error: ${e.message}")
                }
            }
        }
    }


    // -------------------------------------------------------------------------
    // ONBOARDING / SEEDING DATA
    // -------------------------------------------------------------------------

    /**
     * Inserts a demo clinician record (for first app launch or testing).
     * Use this for local validation without needing an external API.
     */
    fun insertDemoClinician(context: Context) {
        viewModelScope.launch(Dispatchers.IO + errorHandler) {
            try {
                val dao = getDao(context)

                // Only insert if no clinician exists
                val existing = dao.getClinicianByEmail("doctor@sagalyze.ai")
                if (existing == null) {
                    val demoClinician = Clinician.create(
                        email = "doctor@sagalyze.ai",
                        plainPassword = "Secure@123".toCharArray(),
                        name = "Dr. Meera",
                        licenseNumber = "NITR-DR001",
                        fitzpatrickType = 3
                    )
                    dao.insertClinician(demoClinician)
                    Log.i("LoginViewModel", "Demo clinician inserted for testing")
                } else {
                    Log.i("LoginViewModel", "Demo clinician already exists")
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Failed to insert demo clinician", e)
            }
        }
    }

    // -------------------------------------------------------------------------
    // LOGOUT / SESSION CLEARING
    // -------------------------------------------------------------------------

    /**
     * Logs the clinician out by clearing session data from EncryptedSharedPreferences.
     */
    fun logout(context: Context, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO + errorHandler) {
            try {
                LocalUserStore.clearSession(context)
                withContext(Dispatchers.Main) {
                    onComplete?.invoke()
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Logout failed: ${e.message}")
            }
        }
    }

    // -------------------------------------------------------------------------
    // SESSION STATUS
    // -------------------------------------------------------------------------

    /**
     * Checks if a clinician is already logged in (for auto-navigation at startup).
     */
    fun checkActiveSession(
        context: Context,
        onChecked: (isActive: Boolean, email: String?) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO + errorHandler) {
            val isActive = LocalUserStore.isLoggedIn(context)
            val email = if (isActive) LocalUserStore.getLoggedInEmail(context) else null
            withContext(Dispatchers.Main) {
                onChecked(isActive, email)
            }
        }
    }
}
