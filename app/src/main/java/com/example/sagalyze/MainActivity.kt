package com.sagalyze.app

import android.animation.ValueAnimator
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sagalyze.R
import com.example.sagalyze.app.Patient
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.search.SearchBar
import com.google.android.material.textfield.TextInputEditText

//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.tooling.preview.Preview
//import com.example.sagalyze.ui.theme.SAGAlyzeTheme
//
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContent {
//            SAGAlyzeTheme {
//                Scaffold(modifier=Modifier.fillMaxSize()) { innerPadding ->
//                    Greeting(
//                        name="Android",
//                        modifier=Modifier.padding(innerPadding)
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun Greeting(name: String, modifier: Modifier=Modifier) {
//    Text(
//        text="Hello $name!",
//        modifier=modifier
//    )
//}
//
//@Preview(showBackground=true)
//@Composable
//fun GreetingPreview() {
//    SAGAlyzeTheme {
//        Greeting("Android")
//    }
//}



class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var patientAdapter: PatientAdapter
    private lateinit var searchInput: TextInputEditText
    private lateinit var chipAge: Chip
    private lateinit var chipGender: Chip
    private lateinit var chipCondition: Chip
    private lateinit var chipSort: Chip
    private lateinit var fab: FloatingActionButton
    private lateinit var backgroundView: View

    private val allPatients = listOf(
        Patient(1, "Sarah Mitchell", 34, "Female", "Psoriasis"),
        Patient(2, "James Anderson", 42, "Male", "Eczema"),
        Patient(3, "Emily Rodriguez", 28, "Female", "Acne Vulgaris"),
        Patient(4, "Michael Chen", 55, "Male", "Melanoma Screening"),
        Patient(5, "Lisa Thompson", 39, "Female", "Rosacea"),
        Patient(6, "David Park", 47, "Male", "Dermatitis")
    )

    private var filteredPatients = allPatients.toMutableList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupRecyclerView()
        setupSearchBar()
        setupFilterChips()
        setupFAB()
        startBackgroundAnimation()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewPatients)
        searchInput = findViewById(R.id.searchInput)
        chipAge = findViewById(R.id.chipAge)
        chipGender = findViewById(R.id.chipGender)
        chipCondition = findViewById(R.id.chipCondition)
        chipSort = findViewById(R.id.chipSort)
        fab = findViewById(R.id.fabAddPatient)
        backgroundView = findViewById(R.id.animatedBackground)
    }

    private fun setupRecyclerView() {
        patientAdapter = PatientAdapter(filteredPatients) { patient ->
            // Handle patient click
            Toast.makeText(
                this,
                "Opening details for ${patient.name}",
                Toast.LENGTH_SHORT
            ).show()
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = patientAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupSearchBar() {
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                filterPatients(s.toString())
            }
        })
    }

    private fun setupFilterChips() {
        chipAge.setOnClickListener {
            chipAge.isChecked = !chipAge.isChecked
            Toast.makeText(this, "Age filter toggled", Toast.LENGTH_SHORT).show()
        }

        chipGender.setOnClickListener {
            chipGender.isChecked = !chipGender.isChecked
            Toast.makeText(this, "Gender filter toggled", Toast.LENGTH_SHORT).show()
        }

        chipCondition.setOnClickListener {
            chipCondition.isChecked = !chipCondition.isChecked
            Toast.makeText(this, "Condition filter toggled", Toast.LENGTH_SHORT).show()
        }

        chipSort.setOnClickListener {
            chipSort.isChecked = !chipSort.isChecked
            Toast.makeText(this, "Sort filter toggled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupFAB() {
        fab.setOnClickListener {
            Toast.makeText(this, "Add New Patient", Toast.LENGTH_SHORT).show()
        }
    }

    private fun filterPatients(query: String) {
        filteredPatients.clear()
        if (query.isEmpty()) {
            filteredPatients.addAll(allPatients)
        } else {
            filteredPatients.addAll(
                allPatients.filter {
                    it.name.contains(query, ignoreCase = true)
                }
            )
        }
        patientAdapter.notifyDataSetChanged()
    }

    private fun startBackgroundAnimation() {
        // Animate gradient background with parallax effect
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = 8000
        animator.repeatCount = ValueAnimator.INFINITE
        animator.repeatMode = ValueAnimator.REVERSE
        animator.interpolator = AccelerateDecelerateInterpolator()

        animator.addUpdateListener { animation ->
            val progress = animation.animatedValue as Float
            backgroundView.alpha = 0.3f + (progress * 0.3f)
        }

        animator.start()
    }
}