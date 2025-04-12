package com.terfess.busetasyopal.admin.recycler.reports

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.admin.recycler.reports.response_recycler.AdapterResponsesAdmin
import com.terfess.busetasyopal.admin.viewmodel.ReportViewModel
import com.terfess.busetasyopal.clases_utiles.UtilidadesMenores
import com.terfess.busetasyopal.databinding.BtnSheetResponsesRepUserBinding
import com.terfess.busetasyopal.modelos_dato.reports_system.DatoReport
import com.terfess.busetasyopal.databinding.FormatRecyclerReportsAdmBinding

class HolderReportsAdmin(
    view: View,
    private val viewModel: ReportViewModel,
    private val lifecycleOwner: LifecycleOwner
) : RecyclerView.ViewHolder(view) {
    private var binding = FormatRecyclerReportsAdmBinding.bind(view)
    private val contexto = binding.root.context

    //set show report
    private val tvDate = binding.tvFechaReportAdmin
    private val tvText = binding.tvTextReportAdmin
    private val tvTime = binding.tvTimeReportAdmin
    private val tvTask = binding.tvTaskReportAdmin
    private val tvLocation = binding.tvLocationReportAdmin
    private val tvHasBeenResponded = binding.hasBeenRespondedAdmin
    private val btnDeleteReport = binding.btnDeleteReportAdmin
    private val btnReplyReport = binding.btnResponseReportAdmin
    private val btnSeeResponses = binding.btnSeeResponsesRepAdmin

    private val listViewsStateBtnReply = mutableListOf<Int>()

    fun showReports(
        currentReport: DatoReport,
        idView: Int,
        viewModelRep: ReportViewModel,
        position: Int
    ) {
        tvDate.text = currentReport.dateReport
        tvText.text = currentReport.situationReport
        tvTask.text = currentReport.currentTask
        tvLocation.text = currentReport.location
        tvTime.text = currentReport.timeReport

        if (currentReport.hasResponse) {
            tvHasBeenResponded.text = contexto.getString(R.string.respondido)
            btnSeeResponses.visibility = View.VISIBLE
        } else {
            tvHasBeenResponded.text = contexto.getString(R.string.no_respondido)
            btnSeeResponses.visibility = View.GONE
        }

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
                    viewModelRep.requestDeleteReport(currentReport.id, currentReport.hasResponse)
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

        btnSeeResponses.setOnClickListener {
            alertDialogResponse(currentReport.id, position)
        }
    }

    private fun alertDialogResponse(id: String, position: Int) {
        // Inflar el layout del BottomSheet con LayoutInflater
        val inflater = LayoutInflater.from(binding.root.context)
        val btnSheetLayout = BtnSheetResponsesRepUserBinding.inflate(inflater)

        // Crear el BottomSheetDialog
        val btnSheetDia =
            BottomSheetDialog(binding.root.context, R.style.Theme_BottomSheetTheme)

        // Establecer el contenido del BottomSheet
        btnSheetDia.setContentView(btnSheetLayout.root)

        // Configurar el BottomSheet
        setupBottomSheet(btnSheetDia)

        // Cargar datos
        viewModel.getResponsesByReportId(id)

        // Crear y asignar adapter
        val adapter = AdapterResponsesAdmin(emptyList(), viewModel)
        setupRecyclerView(adapter, btnSheetLayout)

        // Observar respuestas del ViewModel
        observeResponses(adapter, btnSheetLayout, position)

        // Mostrar el BottomSheet
        btnSheetDia.show()
    }

    // Configura el BottomSheet y establece su altura
    private fun setupBottomSheet(btnSheetDia: BottomSheetDialog) {
        val bottomSheet =
            btnSheetDia.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            it.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            it.requestLayout()

            // Establecer altura máxima como 40% de la pantalla
            val height = UtilidadesMenores().getScreenPercentDp(binding.root.context, 0.45)
            BottomSheetBehavior.from(it).maxHeight = height
        }
    }

    // RecyclerView del BottomSheet Calculate route
    private fun setupRecyclerView(
        adapter: AdapterResponsesAdmin,
        btnSheetLayout: BtnSheetResponsesRepUserBinding
    ): RecyclerView {
        val recycler = btnSheetLayout.listRespuestas

        // Configuramos adapter vacío inicialmente
        recycler.layoutManager = LinearLayoutManager(binding.root.context)
        recycler.adapter = adapter

        return recycler
    }

    // Observa los resultados del cálculo de rutas
    private fun observeResponses(
        adapter: AdapterResponsesAdmin,
        btnSheetLayout: BtnSheetResponsesRepUserBinding,
        position: Int
    ) {
        viewModel.myResponses.observe(lifecycleOwner) { allResponses ->
            // Ocultar el progreso cuando hay resultados
            if (allResponses.isEmpty()) {
                btnSheetLayout.titleResponse.text = "No hay respuestas para este reporte."
            } else {
                btnSheetLayout.titleResponse.text =
                    "Viendo ${allResponses.size} respuestas del Reporte"
            }

            adapter.updateData(allResponses)
        }
    }


    private fun clearResponseField() {
        binding.edtResponseAdmin.setText("")
        binding.section2.visibility = View.GONE
    }

    private fun hideKeyboard() {
        binding.edtResponseAdmin.clearFocus() // Quita el foco del EditText
        val imm =
            binding.root.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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
