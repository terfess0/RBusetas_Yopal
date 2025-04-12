package com.terfess.busetasyopal.admin.recycler.reports.response_recycler

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.admin.viewmodel.ReportViewModel
import com.terfess.busetasyopal.databinding.FormatResponseItemBinding
import com.terfess.busetasyopal.modelos_dato.reports_system.ResponseReportDato

class AdapterResponsesAdmin(
    private var respuestas: List<ResponseReportDato>,
    private val viewModel: ReportViewModel
) : RecyclerView.Adapter<AdapterResponsesAdmin.HolderResponse>() {
    class HolderResponse(view: View, private val viewModel: ReportViewModel) :
        RecyclerView.ViewHolder(view) {
        private val binding = FormatResponseItemBinding.bind(view)
        private val author = binding.tvAuthorResponseUser
        private val responseText = binding.txtResponseUser
        private val fecha = binding.tvDateReponseUser
        private val badgeIndicateNew = binding.badgeIsNewnoti

        fun mostrar(respuesta: ResponseReportDato) {
            author.text = respuesta.authorResponse
            responseText.text = respuesta.textResponse
            fecha.text = "Respondido el ${respuesta.dateResponse} a las ${respuesta.timeResponse}"

            if (respuesta.statusCheckedSeenNoti) {
                badgeIndicateNew.visibility = View.GONE
            } else {
                badgeIndicateNew.visibility = View.VISIBLE
            }

            binding.btnDeleteResponseAdmin.visibility = View.VISIBLE

            binding.btnDeleteResponseAdmin.setOnClickListener {
                // Contexto para el AlertDialog
                val context = binding.root.context

                val builder = AlertDialog.Builder(context, R.style.AlertDialogTheme)

                builder.setTitle(context.getString(R.string.alert_text_literal))
                    .setIcon(R.drawable.ic_panel_admin)
                    .setMessage("¿Quieres eliminar esta respuesta '${respuesta.textResponse}'?")
                    .setPositiveButton(context.getString(R.string.confirm)) { _, _ ->
                        // Delete response at confirm
                        viewModel.requestDeleteResponse(respuesta.idResponse, respuesta.textResponse)
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

    fun updateData(nuevasRespuestas: List<ResponseReportDato>) {
        respuestas = nuevasRespuestas
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderResponse {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.format_response_item, parent, false)
        return HolderResponse(view, viewModel)
    }

    override fun getItemCount(): Int {
        return respuestas.size
    }

    override fun onBindViewHolder(holder: HolderResponse, position: Int) {
        holder.mostrar(respuestas[position])
    }
}
