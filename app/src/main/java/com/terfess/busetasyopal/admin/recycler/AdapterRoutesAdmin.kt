package com.terfess.busetasyopal.admin.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.admin.model.DatoRuta

class AdapterRoutesAdmin(private var listado: List<DatoRuta>) : RecyclerView.Adapter<HolderRoutesAdmin>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderRoutesAdmin {
        val layoutInflater = LayoutInflater.from(parent.context)
        return HolderRoutesAdmin(layoutInflater.inflate(R.layout.format_recycler_admin, parent, false))
    }

    override fun getItemCount(): Int {
        return listado.size
    }

    override fun onBindViewHolder(holder: HolderRoutesAdmin, position: Int) {
        val buseta = listado[position]
        holder.mostrar(buseta)
    }

    fun setNewData(newData:MutableList<DatoRuta>) {
        listado = newData
        notifyDataSetChanged()
    }
}
