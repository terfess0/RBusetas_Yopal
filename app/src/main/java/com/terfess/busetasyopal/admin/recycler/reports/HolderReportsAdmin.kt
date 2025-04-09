package com.terfess.busetasyopal.admin.recycler.reports

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.admin.viewmodel.ReportViewModel
import com.terfess.busetasyopal.modelos_dato.reports_system.DatoReport
import com.terfess.busetasyopal.databinding.FormatRecyclerReportsAdmBinding

class HolderReportsAdmin(view: View) : RecyclerView.ViewHolder(view) {
    private var binding = FormatRecyclerReportsAdmBinding.bind(view)

    //set show report
    private val tvDate = binding.tvFechaReportAdmin
    private val tvText = binding.tvTextReportAdmin
    private val tvTime = binding.tvTimeReportAdmin
    private val tvTask = binding.tvTaskReportAdmin
    private val tvLocation = binding.tvLocationReportAdmin
    private val btnDeleteReport = binding.btnDeleteReportAdmin
    private val btnReplyReport = binding.btnResponseReportAdmin

    private val listViewsStateBtnReply = mutableListOf<Int>()

    fun showReports(currentReport: DatoReport, idView: Int, viewModelRep: ReportViewModel) {
        tvDate.text = currentReport.dateReport
        tvText.text = currentReport.situationReport
        tvTask.text = currentReport.currentTask
        tvLocation.text = currentReport.location
        tvTime.text = currentReport.timeReport


        //btn delete report
        btnDeleteReport.setOnClickListener {
            // Contexto para el AlertDialog
            val context = binding.root.context

            val builder = AlertDialog.Builder(context, R.style.AlertDialogTheme)

            builder.setTitle(context.getString(R.string.alert_text_literal))
                .setIcon(R.drawable.ic_panel_admin)
                .setMessage("¿Quieres eliminar este reporte '${currentReport.situationReport}'?")
                .setPositiveButton(context.getString(R.string.confirm)) { _, _ ->
                    // Delete report at confirm
                    viewModelRep.requestDeleteReport(currentReport.id)
                }
                .setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
                    // cancel
                    dialog.dismiss()
                }
            // Crear y mostrar el diálogo
            val dialog = builder.create()
            dialog.show()
        }

        btnReplyReport.setOnClickListener {
            viewAction(idView)
            binding.sendReplyAdm.setOnClickListener {
                if (binding.edtResponseAdmin.text.isNullOrBlank()) {
                    viewModelRep.setAlert("Debes ingresar un mensaje")
                } else {
                    viewModelRep.requestSendReply(
                        currentReport.id,
                        binding.edtResponseAdmin.text.toString(),
                        currentReport.idUser
                    )
                    hideKeyboard()
                }
                clearResponseField()
            }
        }
    }

    private fun clearResponseField() {
        binding.edtResponseAdmin.setText("")
        binding.section2.visibility = View.GONE
    }

    private fun hideKeyboard() {
        binding.edtResponseAdmin.clearFocus() // Quita el foco del EditText
        val imm = binding.root.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0) // Oculta el teclado
    }

    private fun viewAction(idPos: Int) {
        val elemSection2 = binding.section2

        if (elemSection2.visibility == View.VISIBLE) {
            elemSection2.visibility = View.GONE

            listViewsStateBtnReply.remove(idPos)
        } else {
            elemSection2.visibility = View.VISIBLE

            listViewsStateBtnReply.add(idPos)
        }
    }
}
