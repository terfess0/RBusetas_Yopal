package com.terfess.rutasbusetasyopal

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class RutasAdapter(var listado: List<DatosRuta>) : RecyclerView.Adapter<RutasHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RutasHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return RutasHolder(layoutInflater.inflate(R.layout.activity_seccion, parent, false))
    }

    override fun getItemCount(): Int {
        return listado.size
    }

    override fun onBindViewHolder(holder: RutasHolder, position: Int) {
        val buseta = listado[position]
        holder.mostrar(buseta)
    }

    //funcion para filtro de Recycler view
    fun updateLista(listado: List<DatosRuta>) {
        this.listado = listado
        notifyDataSetChanged()
    }
}
