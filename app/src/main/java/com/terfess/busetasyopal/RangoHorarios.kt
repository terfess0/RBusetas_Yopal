package com.terfess.busetasyopal

import java.text.SimpleDateFormat
import java.util.*

class RangoHorarios {
    fun BusetaEnServicio(horaInicio: String, horaFinal: String): Int {
        val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())
        val horaActual = Date()

        val horaInicioTipoDate = formatoHora.parse(horaInicio)
        val horaFinalTipoDate = formatoHora.parse(horaFinal)

        if (horaInicioTipoDate != null && horaFinalTipoDate != null) {
            //obtiene la hora actual
            val currentCalendar = Calendar.getInstance()
            currentCalendar.time = horaActual

            //establece las horas de inicio y final
            val calendarHoraInicio = Calendar.getInstance()
            calendarHoraInicio.time = horaInicioTipoDate

            val calendarHoraFinal = Calendar.getInstance()
            calendarHoraFinal.time = horaFinalTipoDate

            //comprueba si la hora actual está dentro del rango
            return if (currentCalendar.after(calendarHoraInicio) && currentCalendar.before(
                    calendarHoraFinal)){
                1
            } else {
                0
            }
        }
        return 2 //significa que tuvo horaios nulos
    }
}