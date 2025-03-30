package com.terfess.busetasyopal.admin.recycler.reports

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.admin.viewmodel.ReportViewModel
import com.terfess.busetasyopal.modelos_dato.reports_system.DatoReport


class AdapterReportsAdmin(private var reports: List<DatoReport>, private var viewModelRep: ReportViewModel) :
    RecyclerView.Adapter<HolderReportsAdmin>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderReportsAdmin {
        val layoutInflater = LayoutInflater.from(parent.context)

        return HolderReportsAdmin(
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

    override fun onBindViewHolder(holder: HolderReportsAdmin, position: Int) {
        holder.showReports(reports[position], position, viewModelRep)
    }

    fun notyChange(data: List<DatoReport>) {
        reports = data
        notifyDataSetChanged()
    }
}