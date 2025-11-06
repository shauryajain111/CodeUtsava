package com.sagalyze.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sagalyze.R
import com.example.sagalyze.app.Patient

class PatientAdapter(
    private val patients: MutableList<Patient>, // ✅ changed from List<Patient> to MutableList<Patient>
    private val onPatientClick: (Patient) -> Unit
) : RecyclerView.Adapter<PatientAdapter.PatientViewHolder>() {

    inner class PatientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPatientName: TextView = itemView.findViewById(R.id.tvPatientName)
        val tvPatientInfo: TextView = itemView.findViewById(R.id.tvPatientInfo)
        val ivAvatar: ImageView = itemView.findViewById(R.id.ivAvatar)
        val ivChevron: ImageView = itemView.findViewById(R.id.ivChevron)

        fun bind(patient: Patient) {
            tvPatientName.text = patient.name
            tvPatientInfo.text = "${patient.age} • ${patient.gender} • ${patient.condition}"

            itemView.setOnClickListener {
                onPatientClick(patient)
            }
        }
    }

    fun updateList(newList: List<Patient>) {
        patients.clear()           // ✅ works now because patients is MutableList
        patients.addAll(newList)   // ✅ works now because patients is MutableList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_patient, parent, false)
        return PatientViewHolder(view)
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        holder.bind(patients[position])
    }

    override fun getItemCount(): Int = patients.size
}
