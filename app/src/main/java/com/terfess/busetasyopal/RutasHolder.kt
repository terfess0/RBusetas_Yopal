package com.terfess.busetasyopal


import android.app.Activity
import android.content.Intent
import android.text.Html
import android.text.Html.FROM_HTML_MODE_LEGACY
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.terfess.busetasyopal.databinding.ActivitySeccionBinding

class RutasHolder(vista: View) : RecyclerView.ViewHolder(vista) {
    private val binding = ActivitySeccionBinding.bind(vista)
    private val caja = vista.context as Activity
    private var rutaEnServicioLV = "#000000"
    private var rutaEnServicioSab = "#000000"
    private var rutaEnServicioDom = "#000000"

    fun mostrar(dato: DatosRuta, color: String) {

        val ruta = "Ruta\n"+dato.numRuta
        val sitios = "<font color='$color' style='text-align:center'><b>Lugares Relevantes</b></font> <br> ${dato.sitios}"

        val datos2 :DatoHorario = ListaHorarios.busetaHorario[dato.numRuta]
        val claseRango = RangoHorarios()
        val horaInicioLV = datos2.horaInicioLunesViernes
        val horaFinalLV = datos2.horaFinalLunesViernes
        val horaInicioSab = datos2.horaInicioSab
        val horaFinalSab = datos2.horaFinalSab
        val horaInicioDom = datos2.horaInicioDom
        val horaFinalDom = datos2.horaFinalDom
        val resultado1 = claseRango.BusetaEnServicio(horaInicioLV, horaFinalLV)
        val resultado2 = claseRango.BusetaEnServicio(horaInicioSab, horaFinalSab)
        val resultado3 = claseRango.BusetaEnServicio(horaInicioDom, horaFinalDom)

        when (resultado1){
            0 -> {
                this.rutaEnServicioLV = "#8a0322" //rojo oscuro
            }
            1 -> {
                this.rutaEnServicioLV = "#006b15" //verde oscuro
            }
        }
        when (resultado2){
            0 -> {
                this.rutaEnServicioSab = "#8a0322" //rojo oscuro
            }
            1 -> {
                this.rutaEnServicioSab = "#006b15" //verde oscuro
            }
        }
        when (resultado3){
            0 -> {
                this.rutaEnServicioDom = "#8a0322" //rojo oscuro
            }
            1 -> {
                this.rutaEnServicioDom = "#006b15" //verde oscuro
            }
        }

        val horLunVie = "<font color='$color'><b>Lunes a Viernes</b></font> <br> <font color='$rutaEnServicioLV'>${dato.horLunVie}<br>${dato.frecLunVie}</font>"
        val horSab = "<font color='$color'><b>Sabados</b></font> <br> <font color='$rutaEnServicioSab'>${dato.horSab}<br>${dato.frecSab}</font>"
        val horDom = "<font color='$color'><b>Domingos y Festivos</b></font> <br> <font color='$rutaEnServicioDom'>${dato.horDomFest}<br>${dato.frecDomFest}</font>"

        binding.numRuta.text = ruta
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