package com.terfess.busetasyopal.actividades.reports.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.modelos_dato.reports_system.DatoReport
import com.terfess.busetasyopal.databinding.FormatRecyclerReportsUserBinding

class AdapterHolderReportsUser(private var reports: List<DatoReport>) :
    RecyclerView.Adapter<AdapterHolderReportsUser.HolderReportsUser>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderReportsUser {
        val layoutInflater = LayoutInflater.from(parent.context)

        return HolderReportsUser(
            layoutInflater.inflate(
                R.layout.format_recycler_reports_adm,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return reports.size
    }

    override fun onBindViewHolder(holder: HolderReportsUser, position: Int) {
        holder.showReports(reports[position])
    }

    fun notyChange(data: List<DatoReport>) {
        reports = data
        notifyDataSetChanged()
    }

    inner class HolderReportsUser(view: View) : RecyclerView.ViewHolder(view) {
        private var binding = FormatRecyclerReportsUserBinding.bind(view)

        fun showReports(currentReport: DatoReport) {
            //set show report
            val tvDate = binding.tvFechaReportUser
            val tvText = binding.tvTextReportUser
            val tvTime = binding.tvTimeReportUser
            val tvTask = binding.tvTaskReportUser
            val tvLocation = binding.tvLocationReportUser

            tvDate.text = currentReport.dateReport
            tvText.text = currentReport.situationReport
            tvTask.text = currentReport.currentTask
            tvTime.text = currentReport.timeReport

            // Hide btn delete report (only available in admin)
            binding.btnDeleteReportUser.visibility = View.GONE

            tvLocation.visibility = View.GONE
        }
    }

}