package com.example.sagalyze.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.sagalyze.R
import com.sagalyze.ui.AddPatientFragment


class PatientAddActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_patient)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AddPatientFragment())
                .commit()
        }
    }
}
