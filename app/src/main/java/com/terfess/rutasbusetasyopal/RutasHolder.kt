package com.terfess.rutasbusetasyopal

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.text.Html
import android.text.Html.FROM_HTML_MODE_LEGACY
import android.view.View
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.terfess.rutasbusetasyopal.databinding.ActivitySeccionBinding

class RutasHolder(vista: View) : RecyclerView.ViewHolder(vista) {
    private val binding = ActivitySeccionBinding.bind(vista)
    private val caja = vista.context as Activity


    fun mostrar(dato: DatosRuta) {
        val color = "#00AC19"
        val texto = "<font color='$color' >Lugares Relevantes:</font> ${dato.sitios}"
        binding.numRuta.text = "RUTA #"+dato.numRuta
        binding.sitios.text = Html.fromHtml(texto,FROM_HTML_MODE_LEGACY)
        binding.precio.text = "PRECIO:" + dato.precio


        binding.contenedor.setOnClickListener {
            val selector = dato.numRuta
            val intent = Intent(binding.contenedor.context, Mapa::class.java)
            intent.putExtra("selector", selector)
            caja.startActivity(intent)
        }
    }
}