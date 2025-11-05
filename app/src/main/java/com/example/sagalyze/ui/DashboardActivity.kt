package com.sagalyze.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.sagalyze.R
import com.sagalyze.data.LocalUserStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * -----------------------------------------------------------------------------
 * DashboardActivity.kt
 * -----------------------------------------------------------------------------
 * Clinician’s main dashboard after successful login.
 *
 * RESPONSIBILITIES:
 *  - Displays clinician session info (name, email, login time)
 *  - Verifies session validity at launch (redirects to login if expired)
 *  - Provides secure logout and profile access
 *  - Acts as a gateway to future features (patient management, lesion tracking)
 *
 * SECURITY DESIGN:
 *  - Loads clinician session from EncryptedSharedPreferences
 *  - Clears session data on logout (using LocalUserStore)
 *  - Prevents back navigation to LoginActivity after logout
 * -----------------------------------------------------------------------------
 */
class DashboardActivity : ComponentActivity() {

    // UI Components
    private lateinit var welcomeText: TextView
    private lateinit var sessionInfoText: TextView
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        initViews()
        setupToolbar()
        verifyActiveSession()
    }

    // -------------------------------------------------------------------------
    // INITIALIZATION
    // -------------------------------------------------------------------------
    private fun initViews() {
        welcomeText = findViewById(R.id.tvWelcome)
        sessionInfoText = findViewById(R.id.tvSessionInfo)
        toolbar = findViewById(R.id.toolbarDashboard)
    }

    private fun setupToolbar() {
        toolbar.title = "SAGAlyze Dashboard"
        toolbar.subtitle = "Clinician Console"
        toolbar.inflateMenu(R.menu.dashboard_menu)

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_profile -> {
                    Toast.makeText(this, "Profile coming soon", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.menu_logout -> {
                    confirmLogout()
                    true
                }
                else -> false
            }
        }
    }

    // -------------------------------------------------------------------------
    // SESSION VALIDATION
    // -------------------------------------------------------------------------
    private fun verifyActiveSession() {
        lifecycleScope.launch(Dispatchers.IO) {
            val isLoggedIn = LocalUserStore.isLoggedIn(this@DashboardActivity)
            if (!isLoggedIn) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DashboardActivity, "Session expired, please log in again", Toast.LENGTH_SHORT).show()
                    navigateToLogin()
                }
                return@launch
            }

            val email = LocalUserStore.getLoggedInEmail(this@DashboardActivity)
            val name = LocalUserStore.getClinicianName(this@DashboardActivity)
            val timestamp = LocalUserStore.getLoginTimestamp(this@DashboardActivity)

            val formattedTime = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
                .format(java.util.Date(timestamp))

            withContext(Dispatchers.Main) {
                welcomeText.text = "Welcome, ${name ?: "Clinician"}"
                sessionInfoText.text = """
                    Email: $email
                    Logged in: $formattedTime
                """.trimIndent()
            }
        }
    }

    // -------------------------------------------------------------------------
    // LOGOUT FLOW
    // -------------------------------------------------------------------------
    private fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle("Confirm Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                LocalUserStore.clearSession(this@DashboardActivity)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DashboardActivity, "You have been logged out.", Toast.LENGTH_SHORT).show()
                    navigateToLogin()
                }
            } catch (e: Exception) {
                Log.e("DashboardActivity", "Logout failed: ${e.message}")
            }
        }
    }

    // -------------------------------------------------------------------------
    // NAVIGATION
    // -------------------------------------------------------------------------
    private fun navigateToLogin() {
        val intent = Intent(this@DashboardActivity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // -------------------------------------------------------------------------
    // TOOLBAR MENU CREATION
    // -------------------------------------------------------------------------
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.dashboard_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_logout -> {
                confirmLogout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
