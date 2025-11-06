package com.example.sagalyze.report

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sagalyze.R
import com.example.sagalyze.report.models.Visit
import com.example.sagalyze.report.utils.ToastHelper
import com.google.android.material.navigation.NavigationView
import com.example.sagalyze.report.adapters.PastVisitsAdapter
import com.example.sagalyze.report.models.PatientInfo
import java.util.*

class ReportActivity : AppCompatActivity() {

    // UI Components
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var recyclerPastVisits: RecyclerView

    // Patient Data
    private lateinit var currentPatient: PatientInfo
    private var nextVisitDate: Date? = null
    private var uploadedFileUri: Uri? = null

    // Adapters
    private lateinit var pastVisitsAdapter: PastVisitsAdapter

    // File picker launcher
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleFileSelection(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        // ✅ fetch intent extras INSIDE onCreate (cannot be accessed at property level)
        val patientName = intent.getStringExtra("PATIENT_NAME")
        val condition = intent.getStringExtra("PATIENT_CONDITION")

        // Initialize patient data
        currentPatient = PatientInfo.getSamplePatient()

        // Setup UI
        setupToolbar()
        setupNavigationDrawer()
        setupPatientHeader()
        setupBackButton()
        setupPastVisits()
        setupNewReportCard()
        setupActionButtons()

        // ✅ Handle back press dispatcher (replacement for deprecated onBackPressed)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun setupToolbar() {
        findViewById<View>(R.id.ivMenuButton).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        findViewById<View>(R.id.ivLogoutButton).setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun setupNavigationDrawer() {
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_patients -> {
                    ToastHelper.showInfo(this, "Navigating to Patient Records...")
                }
                R.id.nav_dashboard -> {
                    ToastHelper.showInfo(this, "Navigating to Dashboard...")
                }
                R.id.nav_settings -> {
                    ToastHelper.showInfo(this, "Opening Settings...")
                }
                R.id.nav_logout -> {
                    showLogoutDialog()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setupPatientHeader() {
        findViewById<TextView>(R.id.tvPatientName).text = currentPatient.name
        findViewById<TextView>(R.id.tvPatientId).text =
            getString(R.string.patient_id_label) + " " + currentPatient.patientId
    }

    private fun setupBackButton() {
        findViewById<View>(R.id.btnBackToRecords).setOnClickListener {
            ToastHelper.showInfo(this, getString(R.string.toast_navigation))
        }
    }

    private fun setupPastVisits() {
        val pastVisits = Visit.getSampleVisits()
        pastVisitsAdapter = PastVisitsAdapter(pastVisits) { visit ->
            handleViewReport(visit)
        }

        recyclerPastVisits = findViewById(R.id.rvPastVisits)
        recyclerPastVisits.layoutManager = LinearLayoutManager(this)
        recyclerPastVisits.adapter = pastVisitsAdapter
    }

    private fun setupNewReportCard() {
        val uploadArea = findViewById<View>(R.id.uploadArea)
        val btnBrowseFiles = findViewById<View>(R.id.btnBrowseFiles)
        val btnRemoveFile = findViewById<View>(R.id.btnRemoveFile)
        val uploadPrompt = findViewById<View>(R.id.uploadPrompt)
        val fileSelectedView = findViewById<View>(R.id.fileSelectedView)

        btnBrowseFiles.setOnClickListener {
            openFilePicker()
        }

        uploadArea.setOnClickListener {
            if (uploadedFileUri == null) {
                openFilePicker()
            }
        }

        btnRemoveFile.setOnClickListener {
            uploadedFileUri = null
            uploadPrompt.visibility = View.VISIBLE
            fileSelectedView.visibility = View.GONE
            ToastHelper.showInfo(this, "File removed")
        }

        findViewById<View>(R.id.btnGenerateReport).setOnClickListener {
            handleGenerateReport()
        }

        findViewById<View>(R.id.btnDownloadPdf).setOnClickListener {
            ToastHelper.showInfo(this, getString(R.string.toast_downloading_report))
        }

        findViewById<View>(R.id.btnSendToPatient).setOnClickListener {
            ToastHelper.showSuccess(this, getString(R.string.toast_report_sent))
        }
    }

    private fun setupActionButtons() {
        findViewById<View>(R.id.btnSaveInfo).setOnClickListener {
            ToastHelper.showSuccess(
                this,
                getString(R.string.toast_saved, currentPatient.name)
            )
        }

        findViewById<View>(R.id.btnClearAll).setOnClickListener {
            handleClearAll()
        }
    }

    private fun openFilePicker() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
            return
        }

        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
        }
        filePickerLauncher.launch(intent)
    }

    private fun handleFileSelection(uri: Uri) {
        uploadedFileUri = uri

        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(MediaStore.Images.Media.SIZE)

            if (cursor.moveToFirst()) {
                val fileName = cursor.getString(nameIndex)
                val fileSize = cursor.getLong(sizeIndex)

                findViewById<View>(R.id.uploadPrompt).visibility = View.GONE
                findViewById<View>(R.id.fileSelectedView).visibility = View.VISIBLE
                findViewById<TextView>(R.id.tvFileName).text = fileName
                findViewById<TextView>(R.id.tvFileSize).text =
                    String.format("%.2f MB", fileSize / (1024.0 * 1024.0))
            }
        }
    }

    private fun handleGenerateReport() {
        val reportTitle = findViewById<EditText>(R.id.etReportTitle).text.toString()

        if (reportTitle.isBlank()) {
            ToastHelper.showError(this, getString(R.string.toast_report_title_missing))
            return
        }

        if (uploadedFileUri == null) {
            ToastHelper.showError(this, getString(R.string.toast_photo_missing))
            return
        }

        ToastHelper.showSuccess(this, getString(R.string.toast_report_generated))
    }

    private fun handleClearAll() {
        nextVisitDate = null
        uploadedFileUri = null
        findViewById<EditText>(R.id.etReportTitle).setText("")
        findViewById<EditText>(R.id.etClinicalFindings).setText("")
        findViewById<EditText>(R.id.etTreatmentRecommendations).setText("")

        findViewById<View>(R.id.uploadPrompt).visibility = View.VISIBLE
        findViewById<View>(R.id.fileSelectedView).visibility = View.GONE

        ToastHelper.showInfo(this, getString(R.string.toast_cleared))
    }

    private fun handleViewReport(visit: Visit) {
        ToastHelper.showInfo(
            this,
            getString(R.string.toast_opening_report, visit.date)
        )
    }

    private fun showLogoutDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                ToastHelper.showInfo(this, "Logging out...")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }
}
