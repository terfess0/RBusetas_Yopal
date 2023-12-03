package com.terfess.busetasyopal


import android.app.Activity
import android.content.Intent
import android.text.Html
import android.text.Html.FROM_HTML_MODE_LEGACY
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.terfess.busetasyopal.actividades.Mapa
import com.terfess.busetasyopal.clases_utiles.RangoHorarios
import com.terfess.busetasyopal.databinding.FormatoRecyclerBinding
import com.terfess.busetasyopal.listas_datos.ListaHorarios
import com.terfess.busetasyopal.modelos_dato.DatoHorario
import com.terfess.busetasyopal.modelos_dato.DatosPrimariosRuta

class RutasHolder(vista: View) : RecyclerView.ViewHolder(vista) {
    private val binding = FormatoRecyclerBinding.bind(vista)
    private val caja = vista.context as Activity
    private var rutaEnServicioLV = "#000000" //negro
    private var rutaEnServicioSab = "#000000" //negro
    private var rutaEnServicioDom = "#000000" //negro
    private var rutaEnDia = "#221785"

    fun mostrar(dato: DatosPrimariosRuta, colorDia: String) {
        var colorLunVier = "#524e4e"
        var colorSab = "#524e4e"
        var colorDom = "#524e4e"

        val ruta = "Ruta\n" + dato.numRuta
        val sitios =
            "<font color='$colorDia' style='text-align:center'><b>Lugares Relevantes</b></font> <br> ${dato.sitios}"
        val datos2: DatoHorario = ListaHorarios.busetaHorario[dato.numRuta - 1] // se resta 1 para ir acorde a las posiciones del arreglo busetaHorario
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
        fun darColorDisponibilidadLunVie(){
            when (resultado1) {
                0 -> {
                    this.rutaEnServicioLV = "#8a0322" //rojo oscuro
                }

                1 -> {
                    this.rutaEnServicioLV = "#006b15" //verde oscuro
                }
            }
        }
        fun darColorDisponibilidadSab(){
            when (resultado2) {
                0 -> {
                    this.rutaEnServicioSab = "#8a0322" //rojo oscuro
                }

                1 -> {
                    this.rutaEnServicioSab = "#006b15" //verde oscuro
                }
            }
        }
        fun darColorDisponibilidadDom(){
            when (resultado3) {
                0 -> {
                    this.rutaEnServicioDom = "#8a0322" //rojo oscuro
                }

                1 -> {
                    this.rutaEnServicioDom = "#006b15" //verde oscuro
                }
            }
        }
        //obtener dia actual y actuar en consecuencia
        when (RangoHorarios().busetaEnDia()) {
            //aqui se cambia el color del texto seccion horarios segun el dia de la semana, lunes a viernes, sabados, domingos
            2, 3, 4, 5, 6 -> { //entre semana
                colorLunVier = rutaEnDia
                colorSab = colorDia
                colorDom = colorDia
                darColorDisponibilidadLunVie()
            }

            7 -> { //sabado
                colorLunVier = colorDia
                colorSab = rutaEnDia
                colorDom = colorDia
                darColorDisponibilidadSab()
            }

            1 -> { //domingo
                colorLunVier = colorDia
                colorSab = colorDia
                colorDom = rutaEnDia
                darColorDisponibilidadDom()
            }
        }

        val horLunVie =
            "<font color='$colorLunVier'><b>Lunes a Viernes</b></font> <br> <font color='$rutaEnServicioLV'>${dato.horLunVie}<br>${dato.frecLunVie}</font>"
        val horSab =
            "<font color='$colorSab'><b>Sabados</b></font> <br> <font color='$rutaEnServicioSab'>${dato.horSab}<br>${dato.frecSab}</font>"
        val horDom =
            "<font color='$colorDom'><b>Domingos y Festivos</b></font> <br> <font color='$rutaEnServicioDom'>${dato.horDomFest}<br>${dato.frecDomFest}</font>"

        if (colorDia == "#2196F3"){
            binding.contenedorHor.visibility = View.GONE
            binding.guideline2.setGuidelinePercent(1.0f)//se establece porcentage de 100% para que se adapte bien al contenido

        }else{
            binding.contenedorHor.visibility = View.VISIBLE
            binding.guideline2.setGuidelinePercent(0.56f)//se establece porcentage por defecto 56% para que siga adaptandose

        }

        binding.numRuta.text = ruta
        binding.sitios.text = Html.fromHtml(sitios, FROM_HTML_MODE_LEGACY)
        binding.horarioLV.text = Html.fromHtml(horLunVie, FROM_HTML_MODE_LEGACY)
        binding.horarioS.text = Html.fromHtml(horSab, FROM_HTML_MODE_LEGACY)
        binding.horarioDF.text = Html.fromHtml(horDom, FROM_HTML_MODE_LEGACY)

        //el usuario selecciona una de las rutas
        binding.contenedor.setOnClickListener {
            val selector = dato.numRuta
            val intent = Intent(binding.contenedor.context, Mapa::class.java)
            intent.putExtra("selector", selector)
            caja.startActivity(intent)
        }
    }
}