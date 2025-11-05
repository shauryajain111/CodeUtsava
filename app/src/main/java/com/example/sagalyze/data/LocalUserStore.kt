package com.sagalyze.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * LocalUserStore — secure session manager using EncryptedSharedPreferences.
 */
object LocalUserStore {

    // Toggle this while developing (true => skip login checks)
    var devAlwaysLoggedIn: Boolean = true

    // ---- Keys & file ----
    private const val PREF_FILE_NAME = "sagalyze_secure_session"
    private const val KEY_LOGGED_IN_EMAIL = "logged_in_email"
    private const val KEY_CLINICIAN_NAME = "clinician_name"
    private const val KEY_LOGIN_TIMESTAMP = "login_timestamp"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"

    // ---- Secure prefs ----
    private fun securePrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREF_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // ---- Session writes ----
    suspend fun saveSession(context: Context, email: String, name: String?) {
        withContext(Dispatchers.IO) {
            securePrefs(context).edit().apply {
                putString(KEY_LOGGED_IN_EMAIL, email)
                putString(KEY_CLINICIAN_NAME, name)
                putLong(KEY_LOGIN_TIMESTAMP, System.currentTimeMillis())
                putBoolean(KEY_IS_LOGGED_IN, true)
                apply()
            }
        }
    }

    // ---- Session checks ----
    suspend fun isLoggedIn(context: Context): Boolean = withContext(Dispatchers.IO) {
        if (devAlwaysLoggedIn) return@withContext true
        securePrefs(context).getBoolean(KEY_IS_LOGGED_IN, false)
    }

    // ---- Getters ----
    suspend fun getLoggedInEmail(context: Context): String? = withContext(Dispatchers.IO) {
        securePrefs(context).getString(KEY_LOGGED_IN_EMAIL, null)
    }

    suspend fun getClinicianName(context: Context): String? = withContext(Dispatchers.IO) {
        securePrefs(context).getString(KEY_CLINICIAN_NAME, null)
    }

    suspend fun getLoginTimestamp(context: Context): Long = withContext(Dispatchers.IO) {
        securePrefs(context).getLong(KEY_LOGIN_TIMESTAMP, 0L)
    }

    // ---- Logout ----
    suspend fun clearSession(context: Context) {
        withContext(Dispatchers.IO) {
            securePrefs(context).edit().clear().apply()
        }
    }

    // ---- Debug helper ----
    suspend fun debugSessionSummary(context: Context): String = withContext(Dispatchers.IO) {
        val email = getLoggedInEmail(context) ?: "—"
        val name = getClinicianName(context) ?: "—"
        val ts = getLoginTimestamp(context)
        val status = if (isLoggedIn(context)) "Active" else "Inactive"
        val whenTxt = if (ts != 0L)
            java.text.SimpleDateFormat("dd MMM yyyy, HH:mm").format(java.util.Date(ts))
        else "—"

        """
            Clinician Session Summary
            -------------------------
            Status: $status
            Name: $name
            Email: $email
            Logged in at: $whenTxt
        """.trimIndent()
    }
}