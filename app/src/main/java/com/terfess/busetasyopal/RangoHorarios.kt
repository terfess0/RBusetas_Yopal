package com.terfess.busetasyopal

import java.text.SimpleDateFormat
import java.util.*
import java.time.LocalTime

class RangoHorarios {
    fun BusetaEnServicio(horaInicio: String, horaFinal: String): Int {
        //obtener hora actual
        val horaActual = LocalTime.now()

        val horaInicioTipoLT = LocalTime.parse(horaInicio)
        val horaFinalTipoLT = LocalTime.parse(horaFinal)

        //comprueba si la hora actual está dentro del rango
        return if (horaActual.isAfter(horaInicioTipoLT) && horaActual.isBefore(
                horaFinalTipoLT
            )
        ) {
            1
        } else {
            0
        }
    }
}