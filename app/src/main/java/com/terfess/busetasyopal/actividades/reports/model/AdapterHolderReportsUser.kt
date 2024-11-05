package com.terfess.busetasyopal.actividades.reports.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.admin.model.DatoReport
import com.terfess.busetasyopal.admin.viewmodel.ReportViewModel
import com.terfess.busetasyopal.databinding.FormatRecyclerReportsBinding

class AdapterHolderReportsUser(private var reports: List<DatoReport>) :
    RecyclerView.Adapter<AdapterHolderReportsUser.HolderReportsUser>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderReportsUser {
        val layoutInflater = LayoutInflater.from(parent.context)

        return HolderReportsUser(
            layoutInflater.inflate(
                R.layout.format_recycler_reports,
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
        private var binding = FormatRecyclerReportsBinding.bind(view)

        fun showReports(currentReport: DatoReport) {
            //set show report
            val tvDate = binding.tvFechaReport
            val tvText = binding.tvTextReport
            val tvTime = binding.tvTimeReport
            val tvTask = binding.tvTaskReport
            val tvLocation = binding.tvLocationReport

            tvDate.text = currentReport.dateReport
            tvText.text = currentReport.situationReport
            tvTask.text = currentReport.currentTask
            tvTime.text = currentReport.timeReport

            // Hide btn delete report (only available in admin)
            binding.btnDeleteReport.visibility = View.GONE

            tvLocation.visibility = View.GONE
        }
    }

}