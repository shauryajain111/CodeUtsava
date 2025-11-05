package com.sagalyze.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete

/**
 * ClinicianDao — Data Access Object for the Clinician entity.
 *
 * This interface defines all queries and database operations related to clinicians.
 * Each function is a suspend function (for use with coroutines) to ensure database I/O
 * runs off the main thread.
 *
 * Includes:
 * - CRUD operations (Create, Read, Update, Delete)
 * - Authentication query using stored password hash
 * - Utility queries for app logic (e.g., getClinicianByEmail)
 */
@Dao
interface ClinicianDao {

    /**
     * Inserts a new clinician record into the database.
     * If the email already exists, it replaces the existing record.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClinician(clinician: Clinician)

    /**
     * Updates clinician details (non-password fields typically).
     * Use to modify name, license, or Fitzpatrick type.
     */
    @Update
    suspend fun updateClinician(clinician: Clinician)

    /**
     * Deletes a clinician record.
     */
    @Delete
    suspend fun deleteClinician(clinician: Clinician)

    /**
     * Retrieves a clinician by email.
     * Returns null if no clinician is found.
     */
    @Query("SELECT * FROM clinician WHERE email = :email LIMIT 1")
    suspend fun getClinicianByEmail(email: String): Clinician?

    /**
     * Fetches all clinicians in the system.
     * Useful for debugging or analytics.
     */
    @Query("SELECT * FROM clinician ORDER BY created_at DESC")
    suspend fun getAllClinicians(): List<Clinician>

    /**
     * Authenticates a clinician by verifying stored hashed password.
     * Note: This does not compare plaintext passwords directly; verification
     * must be performed at the ViewModel level using Clinician.verifyPassword().
     *
     * This query fetches the clinician record (with password hash) for a given email.
     * The caller (ViewModel) should then verify the password securely.
     */
    @Query("SELECT * FROM clinician WHERE email = :email LIMIT 1")
    suspend fun fetchForAuth(email: String): Clinician?

    /**
     * Deletes all clinicians from the table (use only for testing or resets).
     */
    @Query("DELETE FROM clinician")
    suspend fun clearAll()
}