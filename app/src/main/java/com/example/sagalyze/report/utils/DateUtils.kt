package com.example.sagalyze.report.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    private const val DATE_FORMAT_DISPLAY = "dd/MM/yyyy"
    private const val DATE_FORMAT_FULL = "MMMM dd, yyyy"

    fun formatDate(date: Date, pattern: String = DATE_FORMAT_DISPLAY): String {
        val formatter = SimpleDateFormat(pattern, Locale.getDefault())
        return formatter.format(date)
    }

    fun formatDateFull(date: Date): String {
        return formatDate(date, DATE_FORMAT_FULL)
    }

    fun parseDate(dateString: String, pattern: String = DATE_FORMAT_DISPLAY): Date? {
        return try {
            val formatter = SimpleDateFormat(pattern, Locale.getDefault())
            formatter.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    fun getCurrentDate(): Date {
        return Date()
    }

    fun getCurrentDateString(): String {
        return formatDate(getCurrentDate())
    }
}
