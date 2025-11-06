package com.example.sagalyze.report.utils

import android.content.Context
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import android.view.View
import com.example.sagalyze.R

object ToastHelper {

    fun showSuccess(context: Context, message: String) {
        Toast.makeText(context, "✓ $message", Toast.LENGTH_SHORT).show()
    }

    fun showError(context: Context, message: String) {
        Toast.makeText(context, "✗ $message", Toast.LENGTH_SHORT).show()
    }

    fun showInfo(context: Context, message: String) {
        Toast.makeText(context, "ℹ $message", Toast.LENGTH_SHORT).show()
    }

    fun showLong(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    // Snackbar versions (modern UI)
    fun showSuccessSnackbar(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(
                ContextCompat.getColor(view.context, R.color.status_improved)
            )
            .show()
    }

    fun showErrorSnackbar(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(
                ContextCompat.getColor(view.context, R.color.status_declined)
            )
            .show()
    }

    fun showInfoSnackbar(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(
                ContextCompat.getColor(view.context, R.color.primary_green)
            )
            .show()
    }
}
