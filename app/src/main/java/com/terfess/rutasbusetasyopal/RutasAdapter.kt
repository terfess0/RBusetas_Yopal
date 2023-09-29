package com.terfess.rutasbusetasyopal

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class RutasAdapter(private var listado: List<DatosRuta>) : RecyclerView.Adapter<RutasHolder>() {
    var color: String = "#524e4e"
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RutasHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return RutasHolder(layoutInflater.inflate(R.layout.activity_seccion, parent, false))
    }

    override fun getItemCount(): Int {
        return listado.size
    }

    override fun onBindViewHolder(holder: RutasHolder, position: Int) {
        val buseta = listado[position]
        holder.mostrar(buseta, color)
    }

    //funcion para filtro de Recycler view
    @SuppressLint("NotifyDataSetChanged")
    fun updateLista(listado: List<DatosRuta>, coloriltrando: String) {
        this.listado = listado
        this.color = coloriltrando
        notifyDataSetChanged()
    }
}
