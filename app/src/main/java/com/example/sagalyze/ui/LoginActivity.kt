package com.sagalyze.ui

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.sagalyze.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import com.sagalyze.app.MainActivity
import com.sagalyze.data.Clinician
import com.sagalyze.data.ClinicianRepository
import com.sagalyze.data.RepositoryResult

class LoginActivity : ComponentActivity() {

    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var loginButton: Button
    private lateinit var forgotText: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initViews()
        auth = FirebaseAuth.getInstance()
        checkActiveSession()
        setListeners()
    }

    private fun initViews() {
        emailField = findViewById(R.id.etEmail)
        passwordField = findViewById(R.id.etPassword)
        loginButton = findViewById(R.id.btnLogin)
        forgotText = findViewById(R.id.tvForgot)

        progressBar = ProgressBar(this, null, android.R.attr.progressBarStyle)
        progressBar.visibility = View.GONE

        passwordField.transformationMethod = PasswordTransformationMethod.getInstance()
    }

    private fun checkActiveSession() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Toast.makeText(this, "Welcome back, ${currentUser.email}", Toast.LENGTH_SHORT).show()
            navigateToHome()
        }
    }

    private fun setListeners() {
        loginButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            performLogin(email, password)
        }

        forgotText.setOnClickListener {
            val email = emailField.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Enter your email to reset password", Toast.LENGTH_SHORT).show()
            } else {
                sendPasswordReset(email)
            }
        }
    }

    private fun performLogin(email: String, password: String) {
        showLoading(true)
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    lifecycleScope.launch {
                        when (val result = ClinicianRepository.getClinician(email)) {
                            is RepositoryResult.Success -> {
                                if (result.data == null) {
                                    ClinicianRepository.saveClinician(
                                        Clinician(
                                            email = email,
                                            name = "Dr. Unknown",
                                            licenseNumber = null,
                                            fitzpatrickType = null
                                        )
                                    )
                                }
                                navigateToHome()
                            }
                            is RepositoryResult.Error -> {
                                navigateToHome()
                            }

                        }
                    }
                } else {
                    Toast.makeText(
                        this,
                        task.exception?.localizedMessage ?: "Authentication failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun sendPasswordReset(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                val message = if (task.isSuccessful) {
                    "Password reset email sent"
                } else {
                    task.exception?.localizedMessage ?: "Failed to send password reset email"
                }
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun showLoading(isLoading: Boolean) {
        loginButton.isEnabled = !isLoading
        emailField.isEnabled = !isLoading
        passwordField.isEnabled = !isLoading
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun navigateToHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
