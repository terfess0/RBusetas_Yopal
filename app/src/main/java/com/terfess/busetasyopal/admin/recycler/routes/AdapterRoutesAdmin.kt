package com.terfess.busetasyopal.admin.recycler.routes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.terfess.busetasyopal.R
import com.terfess.busetasyopal.admin.model.DatoRuta
import com.terfess.busetasyopal.admin.viewmodel.RoutesViewModel

class AdapterRoutesAdmin(private var listado: List<DatoRuta>, viewModel: RoutesViewModel) : RecyclerView.Adapter<HolderRoutesAdmin>() {

    private var viewModelInstance = viewModel
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderRoutesAdmin {
        val layoutInflater = LayoutInflater.from(parent.context)
        return HolderRoutesAdmin(layoutInflater.inflate(R.layout.format_recycler_routes_admin, parent, false))
    }

    override fun getItemCount(): Int {
        return listado.size
    }

    override fun onBindViewHolder(holder: HolderRoutesAdmin, position: Int) {
        val buseta = listado[position]
        holder.mostrar(buseta, viewModelInstance)
    }

    fun setNewData(newData: MutableList<DatoRuta>) {
        listado = newData
        notifyDataSetChanged()
    }
}
