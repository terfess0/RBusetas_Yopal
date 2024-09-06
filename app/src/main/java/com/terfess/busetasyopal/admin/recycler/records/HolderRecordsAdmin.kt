package com.terfess.busetasyopal.admin.recycler.records

import android.view.View
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.terfess.busetasyopal.admin.model.DatoRecord
import com.terfess.busetasyopal.databinding.FormatRecordsAdminBinding

class HolderRecordsAdmin(view: View):ViewHolder(view) {
    private var binding = FormatRecordsAdminBinding.bind(view)

    fun showInfo(regist: DatoRecord) {
        binding.tvTimeRecord.text = regist.hora
        binding.tvFechaRecord.text = regist.fecha
        binding.tvRecordText.text = regist.registro
    }
}