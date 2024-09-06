package com.terfess.busetasyopal.admin.recycler.records

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.admin.model.DatoRecord

class AdapterRecordsAdmin(private var records:List<DatoRecord>) : Adapter<HolderRecordsAdmin>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderRecordsAdmin {
        val layoutInflater = LayoutInflater.from(parent.context)
        return HolderRecordsAdmin(layoutInflater.inflate(R.layout.format_records_admin, parent, false))
    }

    override fun onBindViewHolder(holder: HolderRecordsAdmin, position: Int) {
        val regist = records[position]
        holder.showInfo(regist)
    }

    override fun getItemCount(): Int {
        return records.size
    }

    fun pushData(data: List<DatoRecord>?) {
        records = data!!
        notifyDataSetChanged()
    }
}