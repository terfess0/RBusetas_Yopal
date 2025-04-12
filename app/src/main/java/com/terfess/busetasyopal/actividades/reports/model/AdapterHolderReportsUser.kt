package com.terfess.busetasyopal.actividades.reports.model

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.actividades.reports.view.AdapterResponsesUser
import com.terfess.busetasyopal.actividades.reports.viewmodel.ViewModelReports
import com.terfess.busetasyopal.clases_utiles.UtilidadesMenores
import com.terfess.busetasyopal.databinding.BtnSheetResponsesRepUserBinding
import com.terfess.busetasyopal.modelos_dato.reports_system.DatoReport
import com.terfess.busetasyopal.databinding.FormatRecyclerReportsUserBinding
import kotlin.concurrent.timerTask

class AdapterHolderReportsUser(
    private var reports: List<DatoReport>,
    val viewModel: ViewModelReports,
    private val lifecycleOwner: LifecycleOwner
) :
    RecyclerView.Adapter<AdapterHolderReportsUser.HolderReportsUser>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderReportsUser {
        val layoutInflater = LayoutInflater.from(parent.context)

        return HolderReportsUser(
            layoutInflater.inflate(
                R.layout.format_recycler_reports_user,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return reports.size
    }

    override fun onBindViewHolder(holder: HolderReportsUser, position: Int) {
        holder.showReports(reports[position], position)
    }

    fun notyChange(data: List<DatoReport>) {
        reports = data
        notifyDataSetChanged()
    }

    inner class HolderReportsUser(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = FormatRecyclerReportsUserBinding.bind(view)
        private var isObserving = false

        fun showReports(currentReport: DatoReport, position: Int) {
            // Cancelar observaciones previas por seguridad
//            clearObservers()

            // Seteamos datos básicos
            binding.tvFechaReportUser.text = currentReport.dateReport
            binding.tvTextReportUser.text = currentReport.situationReport

            binding.tvTimeReportUser.text = currentReport.timeReport
            binding.txtNumReport.text = "Reporte #${position + 1}"
            binding.tvLocationReportUser.visibility = View.GONE

            if (currentReport.hasResponse) binding.btnSeeResponsesRep.visibility =
                View.VISIBLE else binding.btnSeeResponsesRep.visibility = View.GONE

            binding.btnSeeResponsesRep.setOnClickListener {
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
            val adapter = AdapterResponsesUser(emptyList())
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
            adapter: AdapterResponsesUser,
            btnSheetLayout: BtnSheetResponsesRepUserBinding
        ): RecyclerView? {
            val recycler = btnSheetLayout.listRespuestas

            // Configuramos adapter vacío inicialmente
            recycler.layoutManager = LinearLayoutManager(binding.root.context)
            recycler.adapter = adapter

            return recycler
        }

        // Observa los resultados del cálculo de rutas
        private fun observeResponses(
            adapter: AdapterResponsesUser,
            btnSheetLayout: BtnSheetResponsesRepUserBinding,
            position: Int
        ) {
            viewModel.myResponses.observe(lifecycleOwner) { allResponses ->
                // Ocultar el progreso cuando hay resultados
                if (allResponses.isEmpty()) {
                    btnSheetLayout.titleResponse.text = "No hay respuestas para este reporte."
                } else {
                    btnSheetLayout.titleResponse.text =
                        "Viendo ${allResponses.size} respuestas de Reporte #${position + 1}"

                    // Update view state
                    Handler(Looper.getMainLooper()).postDelayed({
                        viewModel.byCycleChangeStatusViewNotis()
                    }, 3000)


                    val unseenCount = allResponses.count { !it.statusCheckedSeenNoti }
                    viewModel.countEarringsNotis.postValue(unseenCount)
                }

                adapter.updateData(allResponses)
            }
        }

//        private fun toggleRespuestas(reportId: String) {
//            val isHidden = binding.layoutRespuestas.visibility == View.GONE
//            binding.layoutRespuestas.visibility = if (isHidden) View.VISIBLE else View.GONE
//            binding.textToggle.text = if (isHidden) "Ocultar respuestas ▴" else "Ver respuestas ▾"
//
//            if (isHidden) {
//                val count = viewModel.countEarringsNotis.value ?: 0
//                if (count > 0) {
//                    viewModel.byCycleChangeStatusViewNotis()
//                }
//            }
//        }

//        private fun observeResponses(reportId: String, adapter: AdapterResponsesUser) {
//            if (isObserving) return
//            isObserving = true
//
//            // Observamos sólo las respuestas necesarias
//            viewModel.myResponses.observe(lifecycleOwner, responsesObserver)
//
//            // Guardamos referencia para limpiar después
//            observers.add(responsesObserver)
//        }

//        private val observers = mutableListOf<Observer<*>>()
//
//        private fun clearObservers() {
//            observers.forEach { observer ->
//                if (observer is Observer<*>) {
//                    viewModel.myResponses.removeObserver(observer as Observer<in List<ResponseReportDato>>)
//                }
//            }
//            observers.clear()
//            isObserving = false
//        }
    }
}