package com.terfess.busetasyopal


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.Html
import android.text.Html.FROM_HTML_MODE_LEGACY
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.terfess.busetasyopal.actividades.mapa.view.Mapa
import com.terfess.busetasyopal.clases_utiles.RangoHorarios
import com.terfess.busetasyopal.clases_utiles.UtilidadesMenores
import com.terfess.busetasyopal.databinding.FormatoRecyclerPrincBinding
import com.terfess.busetasyopal.enums.MapRouteOption
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
    private var rutaEnDia = "#1b4fd1"
    private val baseData = AppDatabase.getDatabase(binding.root.context)

    private var colorRojo = "#d60000"
    private var colorVerde = "#10b028"


    fun mostrar(dato: Int, colorDia: String) {

        var colorLunVier = "#000000" //color titulo "Lunes a Viernes"
        var colorSab = "#000000" //color titulo "sabados"
        var colorDom = "#000000" //color titulo "domingos y festivos"

        val ruta = "Ruta\n$dato"
        val contextoHolder = this //recuperar el contexto para usarlo en el scope coroutina

        var sitios: String
        var sites: String

        binding.messageDirectionRoute.visibility = View.GONE
        binding.messageDirectionRoute.isSelected = false

        //se usa coroutinas para evitar congelamientos de la ui (xd es obvio)

        CoroutineScope(Dispatchers.Default).launch {//hilo default optimizado para operaciones cpu
            //get sites
            sites = if (colorDia == "#2196F3") {
                baseData.routeDao().getSiteExtendedRoute(dato)
            } else {
                baseData.routeDao().getSimpleSitesRoute(dato)
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

            // Definir una función genérica para manejar el color de disponibilidad
            fun darColorDisponibilidad(dia: Int, resultado: Int) {
                val colorEnServicio: String
                val horaFinal: String

                // Determina el color y horaFinal basados en el día
                when (dia) {
                    2, 3, 4, 5, 6 -> { // Entre semana
                        colorEnServicio = if (resultado == 0) colorRojo else colorVerde
                        horaFinal = horaFinalLV
                    }
                    7 -> { // Sábado
                        colorEnServicio = if (resultado == 0) colorRojo else colorVerde
                        horaFinal = horaFinalSab
                    }
                    1 -> { // Domingo
                        colorEnServicio = if (resultado == 0) colorRojo else colorVerde
                        horaFinal = horaFinalDom
                    }
                    else -> return // Si el día no es válido, sal de la función
                }

                // Actualiza el color de disponibilidad según el día
                when (dia) {
                    2, 3, 4, 5, 6 -> contextoHolder.rutaEnServicioLV = colorEnServicio
                    7 -> contextoHolder.rutaEnServicioSab = colorEnServicio
                    1 -> contextoHolder.rutaEnServicioDom = colorEnServicio
                }

                //
                if(claseRango.afterEndHour(horaFinal)) {

                    directionalText(
                        this@HolderPrincipal.binding,
                        horaFinal,
                        colorDia,
                        contextoHolder.binding.root.context
                    )
                }
            }

            // Obtener el día actual y actuar en consecuencia
            when (val diaActual = RangoHorarios().busetaEnDia()) {
                2, 3, 4, 5, 6 -> {
                    colorLunVier = rutaEnDia
                    colorSab = colorDia
                    colorDom = colorDia
                    darColorDisponibilidad(diaActual, resultado1)
                }
                7 -> {
                    colorLunVier = colorDia
                    colorSab = rutaEnDia
                    colorDom = colorDia
                    darColorDisponibilidad(diaActual, resultado2)
                }
                1 -> {
                    colorLunVier = colorDia
                    colorSab = colorDia
                    colorDom = rutaEnDia
                    darColorDisponibilidad(diaActual, resultado3)
                }
            }

            //comenzar a prepararas textos horarios con y sin color disponibilidad
            val horLunVie =
                "<font color='$colorLunVier'><b>Lunes a Viernes</b></font><br><font color='$rutaEnServicioLV'>$horaInicioLV am - $horaFinalLV pm<br>${frecuenciaRuta.frec_mon_fri}</font></center>"
            val horSab =
                "<font color='$colorSab'><b>Sabados</b></font> <br> <font color='$rutaEnServicioSab'>$horaInicioSab am - $horaFinalSab pm<br>${frecuenciaRuta.frec_sat}</font>"
            val horDom =
                "<font color='$colorDom'><b>Domingos y Festivos</b></font> <br> <font color='$rutaEnServicioDom'>$horaInicioDom am - $horaFinalDom pm<br>${frecuenciaRuta.frec_sun_holi}</font>"

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
            binding.barrieHor.visibility = View.GONE
//            binding.guideline2.setGuidelinePercent(1.0f) //se establece porcentage de 100% para que se adapte bien al contenido

        } else {
            binding.contenedorHor.visibility = View.VISIBLE
            binding.barrieHor.visibility = View.VISIBLE
//            binding.guideline2.setGuidelinePercent(0.56f) //se establece porcentage por defecto 56% para que siga adaptandose

        }

        binding.numRuta.text = ruta

        // On user selected a item route
        binding.contenedor.setOnClickListener {
            CoroutineScope(Dispatchers.Default).launch {
                val intent = Intent(binding.contenedor.context, Mapa::class.java)
                val typeMapOption = MapRouteOption.SIMPLE_ROUTE.toString()

                intent.putExtra("type_option", typeMapOption)
                intent.putExtra("num_route", dato)

                caja.startActivity(intent)
            }
        }
    }

    fun showContainHorFrec(value:Boolean){
        binding.contenedorHor.visibility = if (value) View.VISIBLE else View.GONE
    }

    fun showContainSites(value:Boolean){
        binding.sitios.visibility = if (value) View.VISIBLE else View.GONE
    }

    private fun directionalText(
        binding: FormatoRecyclerPrincBinding,
        hora: String,
        colorDia: String,
        context: Context
    ) {
        val isAfterStart = RangoHorarios().afterStartHour(hora)

        CoroutineScope(Dispatchers.Main).launch {
            if (isAfterStart && colorDia != "#2196F3") { //if the bus is after start hour and not if filtering

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


}