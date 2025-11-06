package com.example.sagalyze.report.models

data class PatientInfo(
    val name: String,
    val age: Int,
    val gender: String,
    val contact: String,
    val previousVisit: String,
    val patientId: String,
    val bloodGroup: String? = null
) {
    companion object {
        // Sample patient data
        fun getSamplePatient() = PatientInfo(
            name = "Sarah Johnson",
            age = 32,
            gender = "Female",
            contact = "+1 (555) 234-5678",
            previousVisit = "October 28, 2025",
            patientId = "PT-2025-0432",
            bloodGroup = "O+"
        )
    }
}
