package com.terfess.busetasyopal.admin.recycler.reports

import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.admin.model.DatoReport
import com.terfess.busetasyopal.admin.viewmodel.ReportViewModel
import com.terfess.busetasyopal.databinding.FormatRecyclerReportsBinding

class HolderReportsAdmin(view: View) : RecyclerView.ViewHolder(view) {
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
        tvLocation.text = currentReport.location
        tvTime.text = currentReport.timeReport

        //btn delete report
        binding.btnDeleteReport.setOnClickListener {
            // Contexto para el AlertDialog
            val context = binding.root.context

            val builder = AlertDialog.Builder(context, R.style.AlertDialogTheme)

            builder.setTitle(context.getString(R.string.alert_text_literal))
                .setIcon(R.drawable.ic_panel_admin)
                .setMessage("¿Quieres eliminar este reporte '${currentReport.situationReport}'?")
                .setPositiveButton(context.getString(R.string.confirm)) { _, _ ->
                    // Delete report at confirm
                    ReportViewModel().requestDeleteReport(currentReport.id)
                }
                .setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
                    // cancel
                    dialog.dismiss()
                }
            // Crear y mostrar el diálogo
            val dialog = builder.create()
            dialog.show()
        }
    }
}
