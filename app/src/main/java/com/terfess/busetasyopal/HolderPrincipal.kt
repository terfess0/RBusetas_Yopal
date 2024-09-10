package com.terfess.busetasyopal


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.Html
import android.text.Html.FROM_HTML_MODE_LEGACY
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.terfess.busetasyopal.actividades.Mapa
import com.terfess.busetasyopal.clases_utiles.RangoHorarios
import com.terfess.busetasyopal.clases_utiles.UtilidadesMenores
import com.terfess.busetasyopal.databinding.FormatoRecyclerPrincBinding
import com.terfess.busetasyopal.enums.RoomPeriod
import com.terfess.busetasyopal.room.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HolderPrincipal(vista: View) : RecyclerView.ViewHolder(vista) {
    private val binding = FormatoRecyclerPrincBinding.bind(vista)
    private val caja = vista.context as Activity
    private var colorSubTituloTema =
        UtilidadesMenores().colorSubTituloTema(vista.context) //color subtitulo tema
    private var rutaEnServicioLV = colorSubTituloTema //color subtitulo tema
    private var rutaEnServicioSab = colorSubTituloTema //color subtitulo tema
    private var rutaEnServicioDom = colorSubTituloTema //color subtitulo tema
    private var rutaEnDia = "#002fa7"
    private val baseData = AppDatabase.getDatabase(binding.root.context)


    fun mostrar(dato: Int, colorDia: String) {

        var colorLunVier = "#000000" //color titulo "Lunes a Viernes"
        var colorSab = "#000000" //color titulo "sabados"
        var colorDom = "#000000" //color titulo "domingos y festivos"

        val ruta = "Ruta\n$dato"
        val contextoHolder = this //recuperar el contexto para usarlo en el scope coroutina

        var sitios = "asd"
        var sites = ""

        binding.messageDirectionRoute.visibility = View.GONE

        //se usa coroutinas para evitar congelamientos de la ui (xd es obvio)

        CoroutineScope(Dispatchers.Default).launch {//hilo default optimizado para operaciones cpu
            //get sites
            if (colorDia == "#2196F3") {
                sites = baseData.routeDao().getSiteExtendedRoute(dato)
            } else {
                sites = baseData.routeDao().getSimpleSitesRoute(dato)
            }

            //..
            //get schedule
            val horarioLV =
                baseData.scheduleDao().getSchedule(
                    dato,
                    RoomPeriod.LUN_VIE.toString()
                )
            val horarioS =
                baseData.scheduleDao().getSchedule(
                    dato,
                    RoomPeriod.SAB.toString()
                )
            val horarioDF =
                baseData.scheduleDao().getSchedule(
                    dato,
                    RoomPeriod.DOM_FEST.toString()
                )

            //..
            //get frequencie
            val frecuenciaRuta = baseData.routeDao().getFrequencieRoute(dato)

            //..
            val claseRango = RangoHorarios()

            //obtener rangos (valores) de horarios
            val horaInicioLV = horarioLV.sche_start
            val horaFinalLV = horarioLV.sche_end
            val horaInicioSab = horarioS.sche_start
            val horaFinalSab = horarioS.sche_end
            val horaInicioDom = horarioDF.sche_start
            val horaFinalDom = horarioDF.sche_end

            //obtener disponibilidad de rutas segun horarios
            val resultado1: Int = claseRango.busetaEnServicio(horaInicioLV, horaFinalLV)
            val resultado2: Int = claseRango.busetaEnServicio(horaInicioSab, horaFinalSab)
            val resultado3: Int = claseRango.busetaEnServicio(horaInicioDom, horaFinalDom)

            //dar color disponibilidad
            fun darColorDisponibilidadLunVie() {
                when (resultado1) {
                    0 -> {
                        contextoHolder.rutaEnServicioLV = "#c4120c" //rojo intenso

                        directionalText(horaFinalLV, colorDia, contextoHolder.binding.root.context)
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

                        directionalText(horaFinalSab, colorDia, contextoHolder.binding.root.context)
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

                        directionalText(horaFinalDom, colorDia, contextoHolder.binding.root.context)
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
                "<font color='$colorLunVier'><b>Lunes a Viernes</b></font><br><font color='$rutaEnServicioLV'>${horaInicioLV} am - ${horaFinalLV} pm<br>${frecuenciaRuta.frec_mon_fri}</font></center>"
            val horSab =
                "<font color='$colorSab'><b>Sabados</b></font> <br> <font color='$rutaEnServicioSab'>${horaInicioSab} am -${horaFinalSab} pm<br>${frecuenciaRuta.frec_sat}</font>"
            val horDom =
                "<font color='$colorDom'><b>Domingos y Festivos</b></font> <br> <font color='$rutaEnServicioDom'>${horaInicioDom} am -${horaFinalDom} pm<br>${frecuenciaRuta.frec_sun_holi}</font>"

            withContext(Dispatchers.Main) {


                //aplicar textos a recyclerview hilo prinipal junto a color disponibilidad en ellos
                if (horarioLV.sche_start.isNotBlank() && frecuenciaRuta.frec_mon_fri.isNotBlank()) {
                    binding.horarioLV.text = Html.fromHtml(horLunVie, FROM_HTML_MODE_LEGACY)
                }
                if (horarioS.sche_start.isNotBlank() && frecuenciaRuta.frec_sat.isNotBlank()) {
                    binding.horarioS.text = Html.fromHtml(horSab, FROM_HTML_MODE_LEGACY)
                }
                if (horarioDF.sche_start.isNotBlank() && frecuenciaRuta.frec_sun_holi.isNotBlank()) {
                    binding.horarioDF.text = Html.fromHtml(horDom, FROM_HTML_MODE_LEGACY)
                }

                sitios =
                    "<font color='$colorDia' style='text-align:center'><b>Lugares Relevantes</b></font> <br> <font color='$colorSubTituloTema' >${sites}</font>"
                binding.sitios.text = Html.fromHtml(sitios, FROM_HTML_MODE_LEGACY)
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


        //el usuario selecciona una de las rutas
        binding.contenedor.setOnClickListener {
            CoroutineScope(Dispatchers.Default).launch {
                val intent = Intent(binding.contenedor.context, Mapa::class.java)
                intent.putExtra("selector", dato)
                caja.startActivity(intent)
            }

            //mensaje "cargando mapa" importante en primera vez usando la aplicacion
            UtilidadesMenores().crearToast(binding.contenedor.context, "Cargando Mapa")
        }
    }

    fun directionalText(hora: String, colorDia: String, context: Context) {
        if (colorDia != "#2196F3") {
            val textDirectional = if (hora != "00:00") {
                context.getString(R.string.ultima_ruta_salio, hora)
            } else {
                context.getString(R.string.sin_servicio_hoy)
            }

            binding.messageDirectionRoute.apply {
                text = textDirectional
                visibility = View.VISIBLE
            }
            binding.messageDirectionRoute.isSelected = true

        } else {
            binding.messageDirectionRoute.visibility = View.GONE
        }
    }


}