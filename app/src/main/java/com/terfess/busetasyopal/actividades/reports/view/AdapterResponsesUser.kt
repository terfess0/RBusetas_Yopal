package com.terfess.busetasyopal.actividades.reports.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.recyclerview.widget.RecyclerView
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.databinding.FormatResponseItemBinding
import com.terfess.busetasyopal.modelos_dato.reports_system.ResponseReportDato

class AdapterResponsesUser (
    private var respuestas: List<ResponseReportDato>
) : RecyclerView.Adapter<AdapterResponsesUser.HolderResponse>() {
    class HolderResponse(view:View): RecyclerView.ViewHolder(view) {
        private val binding = FormatResponseItemBinding.bind(view)
        private val author = binding.tvAuthorResponseUser
        private val responseText = binding.txtResponseUser
        private val fecha = binding.tvDateReponseUser
        private val badgeIndicateNew = binding.badgeIsNewnoti

        fun mostrar(repuesta: ResponseReportDato) {
            author.text = repuesta.authorResponse
            responseText.text = repuesta.textResponse
            fecha.text = "Respondió el ${repuesta.dateResponse} a las ${repuesta.timeResponse}"

            if (repuesta.statusCheckedSeenNoti){
                badgeIndicateNew.visibility = View.GONE
            }else{
                badgeIndicateNew.visibility = View.VISIBLE
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
        return HolderResponse(view)
    }

    override fun getItemCount(): Int {
        return respuestas.size
    }

    override fun onBindViewHolder(holder: HolderResponse, position: Int) {
        holder.mostrar(respuestas[position])
    }
}
