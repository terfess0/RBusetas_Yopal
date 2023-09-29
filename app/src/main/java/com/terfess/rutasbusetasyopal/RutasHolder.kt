package com.terfess.rutasbusetasyopal


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.text.Html
import android.text.Html.FROM_HTML_MODE_LEGACY
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.terfess.rutasbusetasyopal.databinding.ActivitySeccionBinding

class RutasHolder(vista: View) : RecyclerView.ViewHolder(vista) {
    private val binding = ActivitySeccionBinding.bind(vista)
    private val caja = vista.context as Activity


    fun mostrar(dato: DatosRuta, color: String) {
        val sitios = "<font color='$color' style='text-align:center'><b>Lugares Relevantes</b></font> <br> ${dato.sitios}"
        val horLunVie = "<font color='$color'><b>Lunes a Viernes</b></font> <br> ${dato.horLunVie}<br>${dato.frecLunVie}"
        val horSab = "<font color='$color'><b>Sabados</b></font> <br> ${dato.horSab}<br>${dato.frecSab}"
        val horDom = "<font color='$color'><b>Domingos y Festivos</b></font> <br> ${dato.horDomFest}<br>${dato.frecDomFest}"

        binding.numRuta.text = "RUTA #" + dato.numRuta
        binding.sitios.text = Html.fromHtml(sitios, FROM_HTML_MODE_LEGACY)
        binding.horarioLV.text = Html.fromHtml(horLunVie, FROM_HTML_MODE_LEGACY)
        binding.horarioS.text = Html.fromHtml(horSab, FROM_HTML_MODE_LEGACY)
        binding.horarioDF.text = Html.fromHtml(horDom, FROM_HTML_MODE_LEGACY)


        binding.contenedor.setOnClickListener {
            val selector = dato.numRuta
            val intent = Intent(binding.contenedor.context, Mapa::class.java)
            intent.putExtra("selector", selector)
            caja.startActivity(intent)
        }
    }
}