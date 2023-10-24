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
    private var rutaEnServicioLV = "#000000" //negro
    private var rutaEnServicioSab = "#000000" //negro
    private var rutaEnServicioDom = "#000000" //negro
    private var rutaEnDia = "#191163"

    fun mostrar(dato: DatosRuta, colorDia: String) {
        val color = colorDia
        var colorLunVier = "#524e4e"
        var colorSab = "#524e4e"
        var colorDom = "#524e4e"

        val ruta = "Ruta\n" + dato.numRuta
        val sitios =
            "<font color='$color' style='text-align:center'><b>Lugares Relevantes</b></font> <br> ${dato.sitios}"
        val datos2: DatoHorario = ListaHorarios.busetaHorario[dato.numRuta]
        val claseRango = RangoHorarios()
        val horaInicioLV = datos2.horaInicioLunesViernes
        val horaFinalLV = datos2.horaFinalLunesViernes
        val horaInicioSab = datos2.horaInicioSab
        val horaFinalSab = datos2.horaFinalSab
        val horaInicioDom = datos2.horaInicioDom
        val horaFinalDom = datos2.horaFinalDom
        val resultado1 = claseRango.busetaEnServicio(horaInicioLV, horaFinalLV)
        val resultado2 = claseRango.busetaEnServicio(horaInicioSab, horaFinalSab)
        val resultado3 = claseRango.busetaEnServicio(horaInicioDom, horaFinalDom)

        //obtener dia actual y actuar en consecuencia
        when (RangoHorarios().busetaEnDia()) {
            //aqui se cambia el color del texto seccion horarios segun el dia de la semana, lunes a viernes, sabados, domingos
            2, 3, 4, 5, 6 -> { //entre semana
                colorLunVier = rutaEnDia
                colorSab = color
                colorDom = color
            }

            7 -> { //sabado
                colorLunVier = color
                colorSab = rutaEnDia
                colorDom = color
            }

            1 -> { //domingo
                colorLunVier = color
                colorSab = color
                colorDom = rutaEnDia
            }
        }

        when (resultado1) {
            0 -> {
                this.rutaEnServicioLV = "#8a0322" //rojo oscuro
            }

            1 -> {
                this.rutaEnServicioLV = "#006b15" //verde oscuro
            }
        }
        when (resultado2) {
            0 -> {
                this.rutaEnServicioSab = "#8a0322" //rojo oscuro
            }

            1 -> {
                this.rutaEnServicioSab = "#006b15" //verde oscuro
            }
        }
        when (resultado3) {
            0 -> {
                this.rutaEnServicioDom = "#8a0322" //rojo oscuro
            }

            1 -> {
                this.rutaEnServicioDom = "#006b15" //verde oscuro
            }
        }

        val horLunVie =
            "<font color='$colorLunVier'><b>Lunes a Viernes</b></font> <br> <font color='$rutaEnServicioLV'>${dato.horLunVie}<br>${dato.frecLunVie}</font>"
        val horSab =
            "<font color='$colorSab'><b>Sabados</b></font> <br> <font color='$rutaEnServicioSab'>${dato.horSab}<br>${dato.frecSab}</font>"
        val horDom =
            "<font color='$colorDom'><b>Domingos y Festivos</b></font> <br> <font color='$rutaEnServicioDom'>${dato.horDomFest}<br>${dato.frecDomFest}</font>"

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