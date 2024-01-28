package com.terfess.busetasyopal


import android.app.Activity
import android.content.Intent
import android.text.Html
import android.text.Html.FROM_HTML_MODE_LEGACY
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.terfess.busetasyopal.actividades.Mapa
import com.terfess.busetasyopal.clases_utiles.DatosASqliteLocal
import com.terfess.busetasyopal.clases_utiles.RangoHorarios
import com.terfess.busetasyopal.clases_utiles.UtilidadesMenores
import com.terfess.busetasyopal.databinding.FormatoRecyclerPrincBinding
import com.terfess.busetasyopal.modelos_dato.DatosPrimariosRuta
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HolderPrincipal(vista: View) : RecyclerView.ViewHolder(vista) {
    private val binding = FormatoRecyclerPrincBinding.bind(vista)
    private val caja = vista.context as Activity
    private var rutaEnServicioLV = "#000000" //negro
    private var rutaEnServicioSab = "#000000" //negro
    private var rutaEnServicioDom = "#000000" //negro
    private var rutaEnDia = "#221785"
    private val baseSql = DatosASqliteLocal(vista.context)

    fun mostrar(dato: DatosPrimariosRuta, colorDia: String) {
        var colorLunVier = "#524e4e"
        var colorSab = "#524e4e"
        var colorDom = "#524e4e"

        val ruta = "Ruta\n" + dato.numRuta
        val contextoHolder = this //recuperar el contexto para usarlo en el scope coroutina
        val sitios =
            "<font color='$colorDia' style='text-align:center'><b>Lugares Relevantes</b></font> <br> <font color='attr?/colorPrimary' >${dato.sitios}</font>"


        //se usa coroutinas para evitar congelamientos de la ui (xd es obvio)
        CoroutineScope(Dispatchers.Default).launch {//hilo default optimizado para operaciones cpu
            val horarioRuta =
                baseSql.obtenerHorarioRuta(dato.numRuta) //ListaHorarios.busetaHorario[dato.numRuta - 1] se resta 1 para ir acorde a las posiciones del arreglo busetaHorario
            val frecuenciaRuta = baseSql.obtenerFrecuenciaRuta(dato.numRuta)
            val claseRango = RangoHorarios()

            //obtener rangos (valores) de horarios
            val horaInicioLV = horarioRuta.horaInicioLunesViernes
            val horaFinalLV = horarioRuta.horaFinalLunesViernes
            val horaInicioSab = horarioRuta.horaInicioSab
            val horaFinalSab = horarioRuta.horaFinalSab
            val horaInicioDom = horarioRuta.horaInicioDom
            val horaFinalDom = horarioRuta.horaFinalDom

            //obtener disponibilidad de rutas segun horarios
            var resultado1: Int = claseRango.busetaEnServicio(horaInicioLV, horaFinalLV)
            var resultado2: Int = claseRango.busetaEnServicio(horaInicioSab, horaFinalSab)
            var resultado3: Int = claseRango.busetaEnServicio(horaInicioDom, horaFinalDom)

            //dar color disponibilidad
            fun darColorDisponibilidadLunVie() {
                when (resultado1) {
                    0 -> {
                        contextoHolder.rutaEnServicioLV = "#a63e3a" //rojo oscuro
                    }

                    1 -> {
                        contextoHolder.rutaEnServicioLV = "#119400" //verde oscuro
                    }
                }
            }

            fun darColorDisponibilidadSab() {
                when (resultado2) {
                    0 -> {
                        contextoHolder.rutaEnServicioSab = "#a63e3a" //rojo oscuro
                    }

                    1 -> {
                        contextoHolder.rutaEnServicioSab = "#119400" //verde oscuro
                    }
                }
            }

            fun darColorDisponibilidadDom() {
                when (resultado3) {
                    0 -> {
                        contextoHolder.rutaEnServicioDom = "#a63e3a" //rojo oscuro
                    }

                    1 -> {
                        contextoHolder.rutaEnServicioDom = "#119400" //verde oscuro
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

            //comenzar a prepararas textos horarios con y sin color disponibilidad
            val horLunVie =
                "<font color='$colorLunVier'><b>Lunes a Viernes</b></font><br><font color='$rutaEnServicioLV'>${horarioRuta.horaInicioLunesViernes} am - ${horarioRuta.horaFinalLunesViernes} pm<br>${frecuenciaRuta.frecLunVie}</font></center>"
            val horSab =
                "<font color='$colorSab'><b>Sabados</b></font> <br> <font color='$rutaEnServicioSab'>${horarioRuta.horaInicioSab} am -${horarioRuta.horaFinalSab} pm<br>${frecuenciaRuta.frecSab}</font>"
            val horDom =
                "<font color='$colorDom'><b>Domingos y Festivos</b></font> <br> <font color='$rutaEnServicioDom'>${horarioRuta.horaInicioDom} am -${horarioRuta.horaFinalDom} pm<br>${frecuenciaRuta.frecDomFest}</font>"

            withContext(Dispatchers.Main) {

                //aplicar textos a recyclerview hilo prinipal junto a color disponibilidad en ellos
                if (horarioRuta.horaInicioLunesViernes.isNotBlank() && frecuenciaRuta.frecLunVie.isNotBlank()) {
                    binding.horarioLV.text = Html.fromHtml(horLunVie, FROM_HTML_MODE_LEGACY)
                }
                if (horarioRuta.horaInicioSab.isNotBlank() && frecuenciaRuta.frecSab.isNotBlank()) {
                    binding.horarioS.text = Html.fromHtml(horSab, FROM_HTML_MODE_LEGACY)
                }
                if (horarioRuta.horaInicioDom.isNotBlank() && frecuenciaRuta.frecDomFest.isNotBlank()) {
                    binding.horarioDF.text = Html.fromHtml(horDom, FROM_HTML_MODE_LEGACY)
                }
            }
        }


        //demas acciones recyclerview
        if (colorDia == "#2196F3") {
            binding.contenedorHor.visibility = View.GONE
            binding.guideline2.setGuidelinePercent(1.0f) //se establece porcentage de 100% para que se adapte bien al contenido

        } else {
            binding.contenedorHor.visibility = View.VISIBLE
            binding.guideline2.setGuidelinePercent(0.56f) //se establece porcentage por defecto 56% para que siga adaptandose

        }

        binding.numRuta.text = ruta
        binding.sitios.text = Html.fromHtml(sitios, FROM_HTML_MODE_LEGACY)


        //el usuario selecciona una de las rutas
        binding.contenedor.setOnClickListener {
            val selector = dato.numRuta
            val intent = Intent(binding.contenedor.context, Mapa::class.java)
            intent.putExtra("selector", selector)
            caja.startActivity(intent)

            //mensaje "cargando mapa" importante en primera vez usando la aplicacion
            UtilidadesMenores().crearToast(binding.contenedor.context, "Cargando Mapa")
        }
    }


}