package com.terfess.busetasyopal.actividades.reports.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.actividades.reports.view.AdapterResponsesUser
import com.terfess.busetasyopal.actividades.reports.viewmodel.ViewModelReports
import com.terfess.busetasyopal.modelos_dato.reports_system.DatoReport
import com.terfess.busetasyopal.databinding.FormatRecyclerReportsUserBinding
import com.terfess.busetasyopal.modelos_dato.reports_system.ResponseReportDato

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
        holder.showReports(reports[position])
    }

    fun notyChange(data: List<DatoReport>) {
        reports = data
        notifyDataSetChanged()
    }

    inner class HolderReportsUser(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = FormatRecyclerReportsUserBinding.bind(view)
        private var isObserving = false

        fun showReports(currentReport: DatoReport) {
            // Cancelar observaciones previas por seguridad
            clearObservers()

            // Seteamos datos básicos
            binding.tvFechaReportUser.text = currentReport.dateReport
            binding.tvTextReportUser.text = currentReport.situationReport
            binding.tvTaskReportUser.text = currentReport.currentTask
            binding.tvTimeReportUser.text = currentReport.timeReport
            binding.tvLocationReportUser.visibility = View.GONE

            // Configuramos adapter vacío inicialmente
            val adapter = AdapterResponsesUser(emptyList())
            binding.listRespuestas.layoutManager = LinearLayoutManager(binding.root.context)
            binding.listRespuestas.adapter = adapter

            // Configurar botón toggle
            binding.btnToggleRespuestas.setOnClickListener {
                toggleRespuestas(currentReport.id)
            }

            // Botón para eliminar (o consultar) reporte
            binding.btnDeleteReportUser.setOnClickListener {
                viewModel.getResponsesByReportId(currentReport.id)
            }

            // Observamos respuestas específicas del reporte
            observeResponses(currentReport.id, adapter)
        }

        private fun toggleRespuestas(reportId: String) {
            val isHidden = binding.layoutRespuestas.visibility == View.GONE
            binding.layoutRespuestas.visibility = if (isHidden) View.VISIBLE else View.GONE
            binding.textToggle.text = if (isHidden) "Ocultar respuestas ▴" else "Ver respuestas ▾"

            if (isHidden) {
                val count = viewModel.countEarringsNotis.value ?: 0
                if (count > 0) {
                    viewModel.byCycleChangeStatusViewNotis()
                }
            }
        }

        private fun observeResponses(reportId: String, adapter: AdapterResponsesUser) {
            if (isObserving) return
            isObserving = true

            val responsesObserver = Observer<List<ResponseReportDato>> { allResponses ->
                val filtered = allResponses.filter { it.idReport == reportId }

                if (filtered.isEmpty()) {
                    binding.btnToggleRespuestas.visibility = View.GONE
                    binding.textNoResponses.visibility = View.VISIBLE
                } else {
                    binding.textNoResponses.visibility = View.GONE
                    binding.btnToggleRespuestas.visibility = View.VISIBLE

                    val unseenCount = filtered.count { !it.statusCheckedSeenNoti }
                    viewModel.countEarringsNotis.postValue(unseenCount)
                }

                adapter.updateData(filtered)
            }

            // Observamos sólo las respuestas necesarias
            viewModel.myResponses.observe(lifecycleOwner, responsesObserver)

            // Guardamos referencia para limpiar después
            observers.add(responsesObserver)
        }

        private val observers = mutableListOf<Observer<*>>()

        private fun clearObservers() {
            observers.forEach { observer ->
                if (observer is Observer<*>) {
                    viewModel.myResponses.removeObserver(observer as Observer<in List<ResponseReportDato>>)
                }
            }
            observers.clear()
            isObserving = false
        }
    }
}