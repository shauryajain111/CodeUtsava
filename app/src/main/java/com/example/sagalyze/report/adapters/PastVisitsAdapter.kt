package com.example.sagalyze.report.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sagalyze.R
import com.example.sagalyze.report.models.Visit
import com.google.android.material.button.MaterialButton



class PastVisitsAdapter(
    private val visits: List<Visit>,
    private val onReportClick: (Visit) -> Unit
) : RecyclerView.Adapter<PastVisitsAdapter.VisitViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_past_visit, parent, false)
        return VisitViewHolder(view)
    }

    override fun onBindViewHolder(holder: VisitViewHolder, position: Int) {
        holder.bind(visits[position])
    }

    override fun getItemCount(): Int = visits.size

    inner class VisitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvVisitDate: TextView = itemView.findViewById(R.id.tvVisitDate)
        private val tvDiagnosis: TextView = itemView.findViewById(R.id.tvDiagnosis)
        private val btnFullReport: MaterialButton = itemView.findViewById(R.id.btnFullReport)

        fun bind(visit: Visit) {
            tvVisitDate.text = visit.date
            tvDiagnosis.text = visit.diagnosis

            btnFullReport.setOnClickListener {
                onReportClick(visit)
            }

            itemView.setOnClickListener {
                onReportClick(visit)
            }
        }
    }
}
