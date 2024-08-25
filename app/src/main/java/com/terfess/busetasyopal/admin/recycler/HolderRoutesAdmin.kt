package com.terfess.busetasyopal.admin.recycler


import android.app.Activity
import android.content.Intent
import android.text.Html
import android.text.Html.FROM_HTML_MODE_LEGACY
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.terfess.busetasyopal.actividades.Mapa
import com.terfess.busetasyopal.admin.model.DatoRuta
import com.terfess.busetasyopal.clases_utiles.RangoHorarios
import com.terfess.busetasyopal.clases_utiles.UtilidadesMenores
import com.terfess.busetasyopal.databinding.FormatRecyclerAdminBinding
import com.terfess.busetasyopal.modelos_dato.DatoFrecuencia
import com.terfess.busetasyopal.modelos_dato.DatoHorario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HolderRoutesAdmin(vista: View) : RecyclerView.ViewHolder(vista) {
    private val binding = FormatRecyclerAdminBinding.bind(vista)
    private val caja = vista.context as Activity
    private var colorSubTituloTema =
        UtilidadesMenores().colorSubTituloTema(vista.context) //color subtitulo tema
    private var rutaEnServicioLV = colorSubTituloTema //color subtitulo tema
    private var rutaEnServicioSab = colorSubTituloTema //color subtitulo tema
    private var rutaEnServicioDom = colorSubTituloTema //color subtitulo tema
    private var rutaEnDia = "#002fa7"
    private val colorBlack = colorSubTituloTema

    fun mostrar(dato: DatoRuta) {
        var colorLunVier = "#000000" //color titulo "Lunes a Viernes"
        var colorSab = "#000000" //color titulo "sabados"
        var colorDom = "#000000" //color titulo "domingos y festivos"

        val ruta = "Ruta\n" + dato.numRuta
        val contextoHolder = this //recuperar el contexto para usarlo en el scope coroutina
        val sitios =
            "<font color='$colorBlack' style='text-align:center'><b>Lugares Relevantes</b></font> <br> <font color='$colorSubTituloTema' >${dato.sitios}</font>"


        //se usa coroutinas para evitar congelamientos de la ui (xd es obvio)
        CoroutineScope(Dispatchers.Default).launch {//hilo default optimizado para operaciones cpu
            val horarioRuta = DatoHorario(
                dato.numRuta[0],
                dato.horarioLunVie[0],
                dato.horarioLunVie[1],
                dato.horarioSab[0],
                dato.horarioSab[1],
                dato.horarioDomFest[0],
                dato.horarioDomFest[1]
            )
            val frecuenciaRuta =
                DatoFrecuencia(
                    dato.numRuta[0],
                    dato.frecuenciaLunVie,
                    dato.frecuenciaSab,
                    dato.frecuenciaDomFest
                )
            val claseRango = RangoHorarios()

            //obtener rangos (valores) de horarios
            val horaInicioLV = horarioRuta.horaInicioLunesViernes
            val horaFinalLV = horarioRuta.horaFinalLunesViernes
            val horaInicioSab = horarioRuta.horaInicioSab
            val horaFinalSab = horarioRuta.horaFinalSab
            val horaInicioDom = horarioRuta.horaInicioDom
            val horaFinalDom = horarioRuta.horaFinalDom

            //obtener disponibilidad de rutas segun horarios
            val resultado1: Int = claseRango.busetaEnServicio(horaInicioLV, horaFinalLV)
            val resultado2: Int = claseRango.busetaEnServicio(horaInicioSab, horaFinalSab)
            val resultado3: Int = claseRango.busetaEnServicio(horaInicioDom, horaFinalDom)

            //dar color disponibilidad
            fun darColorDisponibilidadLunVie() {
                when (resultado1) {
                    0 -> {
                        contextoHolder.rutaEnServicioLV = "#c4120c" //rojo intenso
                    }

                    1 -> {
                        contextoHolder.rutaEnServicioLV = "#10b028" //verde intenso
                    }
                }
            }

            fun darColorDisponibilidadSab() {
                when (resultado2) {
                    0 -> {
                        contextoHolder.rutaEnServicioSab = "#c4120c" //rojo intenso
                    }

                    1 -> {
                        contextoHolder.rutaEnServicioSab = "#10b028" //verde intenso
                    }
                }
            }

            fun darColorDisponibilidadDom() {
                when (resultado3) {
                    0 -> {
                        contextoHolder.rutaEnServicioDom = "#c4120c" //rojo intenso
                    }

                    1 -> {
                        contextoHolder.rutaEnServicioDom = "#10b028" //verde intenso
                    }
                }
            }
            //obtener dia actual y actuar en consecuencia
            when (RangoHorarios().busetaEnDia()) {
                //aqui se cambia el color del texto seccion horarios segun el dia de la semana, lunes a viernes, sabados, domingos
                2, 3, 4, 5, 6 -> { //entre semana
                    colorLunVier = rutaEnDia
                    colorSab = colorBlack
                    colorDom = colorBlack
                    darColorDisponibilidadLunVie()
                }

                7 -> { //sabado
                    colorLunVier = colorBlack
                    colorSab = rutaEnDia
                    colorDom = colorBlack
                    darColorDisponibilidadSab()
                }

                1 -> { //domingo
                    colorLunVier = colorBlack
                    colorSab = colorBlack
                    colorDom = rutaEnDia
                    darColorDisponibilidadDom()
                }

                else -> {
                    println("error holder")
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
                    binding.horarioLVAdmin.text = Html.fromHtml(horLunVie, FROM_HTML_MODE_LEGACY)
                }
                if (horarioRuta.horaInicioSab.isNotBlank() && frecuenciaRuta.frecSab.isNotBlank()) {
                    binding.horarioSAdmin.text = Html.fromHtml(horSab, FROM_HTML_MODE_LEGACY)
                }
                if (horarioRuta.horaInicioDom.isNotBlank() && frecuenciaRuta.frecDomFest.isNotBlank()) {
                    binding.horarioDFAdmin.text = Html.fromHtml(horDom, FROM_HTML_MODE_LEGACY)
                }
            }
        }


        //demas acciones recyclerview
        if (colorBlack == "#2196F3") {
            binding.contenedorHorAdmin.visibility = View.GONE
            binding.guideline2.setGuidelinePercent(1.0f) //se establece porcentage de 100% para que se adapte bien al contenido

        } else {
            binding.contenedorHorAdmin.visibility = View.VISIBLE
            binding.guideline2.setGuidelinePercent(0.56f) //se establece porcentage por defecto 56% para que siga adaptandose

        }

        binding.rutaIdAdmin.text = ruta
        binding.sitiosAdmin.text = Html.fromHtml(sitios, FROM_HTML_MODE_LEGACY)


        //el usuario selecciona una de las rutas
        binding.contenedorAdmin.setOnClickListener {
            CoroutineScope(Dispatchers.Default).launch {
                val selector = dato.numRuta[0]
                val intent = Intent(binding.contenedorAdmin.context, Mapa::class.java)
                intent.putExtra("selector", selector)
                caja.startActivity(intent)
            }

            //mensaje "cargando mapa" importante en primera vez usando la aplicacion
            UtilidadesMenores().crearToast(binding.contenedorAdmin.context, "Cargando Mapa")
        }

    }


}