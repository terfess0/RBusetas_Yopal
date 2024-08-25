package com.terfess.busetasyopal.admin.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.admin.model.DatoReport


class AdapterReportsAdmin(private var reports: List<DatoReport>) :
    RecyclerView.Adapter<HolderReportsAdmin>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderReportsAdmin {
        val layoutInflater = LayoutInflater.from(parent.context)

        return HolderReportsAdmin(layoutInflater.inflate(R.layout.format_recycler_reports, parent, false))
    }

    override fun getItemCount(): Int {
        return reports.size
    }

    override fun onBindViewHolder(holder: HolderReportsAdmin, position: Int) {
        holder.showReports(reports[position])
    }

    fun notyChange(data: List<DatoReport>) {
        reports = data
        notifyDataSetChanged()
    }
}