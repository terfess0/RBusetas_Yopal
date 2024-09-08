package com.terfess.busetasyopal

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.terfess.busetasyopal.modelos_dato.DatosPrimariosRuta

class AdapterPrincipal(private var listado: List<Int>, colorTema:String) : RecyclerView.Adapter<HolderPrincipal>() {
    var color: String = colorTema
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPrincipal {
        val layoutInflater = LayoutInflater.from(parent.context)
        return HolderPrincipal(layoutInflater.inflate(R.layout.formato_recycler_princ, parent, false))
    }

    override fun getItemCount(): Int {
        return listado.size
    }

    override fun onBindViewHolder(holder: HolderPrincipal, position: Int) {
        val idBuseta = listado[position]
        holder.mostrar(idBuseta, color)
    }

    //funcion para filtro de Recycler view
    @SuppressLint("NotifyDataSetChanged")
    fun updateLista(listado: List<Int>, coloriltrando: String) {
        this.listado = listado
        this.color = coloriltrando
        notifyDataSetChanged()
    }

}
