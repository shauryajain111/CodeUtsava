package com.example.sagalyze.report.models

data class Visit(
    val date: String,
    val diagnosis: String
) {
    companion object {
        fun getSampleVisits() = listOf(
            Visit(
                date = "28/10/2025",
                diagnosis = "Psoriasis\nElbow Region"
            ),
            Visit(
                date = "12/09/2025",
                diagnosis = "Psoriasis\nElbow Region"
            ),
            Visit(
                date = "03/08/2025",
                diagnosis = "Psoriasis\nInitial Assessment"
            ),
            Visit(
                date = "15/06/2025",
                diagnosis = "Eczema\nForearm"
            )
        )
    }
}
