package com.sagalyze.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * AppDatabase — Central Room database for the SAGAlyze application.
 *
 * Responsibilities:
 * 1. Provides DAOs to access persisted entities.
 * 2. Manages database creation, versioning, and migrations.
 * 3. Ensures a single, thread-safe instance (Singleton pattern).
 * 4. Optionally includes TypeConverters for complex types.
 *
 * Entities:
 *  - Clinician (stores secure clinician credentials and metadata)
 *
 * Version:
 *  - version = 1 → first stable schema
 *  - Always increment version when schema changes.
 *
 * ExportSchema:
 *  - Set to false to prevent Room from exporting DB schema to a folder.
 *    In production apps, you can export it for version control.
 */
@Database(
    entities = [Clinician::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /**
     * DAO Access Points — Add one function per DAO interface.
     */
    abstract fun clinicianDao(): ClinicianDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        const val DATABASE_NAME = "sagalyze_db"

        /**
         * Returns the singleton instance of AppDatabase.
         *
         * Uses double-checked locking to ensure thread-safety.
         */
        fun getInstance(context: Context): AppDatabase {
            // If instance already exists, return it
            return INSTANCE ?: synchronized(this) {
                // Otherwise, build new instance
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    // Optional: destructive migration for initial testing
                    .fallbackToDestructiveMigration()
                    // Uncomment below for DB callback logging (e.g., prepopulate data)
                    // .addCallback(object : RoomDatabase.Callback() {
                    //     override fun onCreate(db: SupportSQLiteDatabase) {
                    //         super.onCreate(db)
                    //         Log.d("AppDatabase", "Database created")
                    //     }
                    // })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Destroys the current instance — useful for tests or logout flows.
         */
        fun destroyInstance() {
            INSTANCE = null
        }
    }
}

/**
 * Example usage:
 *
 * val db = AppDatabase.getInstance(context)
 * val dao = db.clinicianDao()
 * val clinicians = dao.getAllClinicians()
 *
 * In coroutines:
 * viewModelScope.launch {
 *     val clinician = dao.getClinicianByEmail("doctor@sagalyze.ai")
 * }
 */