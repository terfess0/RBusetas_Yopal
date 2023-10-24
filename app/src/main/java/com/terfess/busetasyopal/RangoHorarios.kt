package com.terfess.busetasyopal

import java.util.*
import java.time.LocalTime

class RangoHorarios {
    fun busetaEnServicio(horaInicio: String, horaFinal: String): Int {
        //obtener hora actual
        val horaActual = LocalTime.now()
        val horaInicioTipoLT = LocalTime.parse(horaInicio)
        val horaFinalTipoLT = LocalTime.parse(horaFinal)

        //comprueba si la hora actual está dentro del rango
        // LT es LocalTime
        return if (horaActual.isAfter(horaInicioTipoLT) && horaActual.isBefore(
                horaFinalTipoLT
            )
        ) {
            1
        } else {
            0
        }
    }

    fun busetaEnDia(): Int {
        val calendario = Calendar.getInstance()
        return calendario.get(Calendar.DAY_OF_WEEK)
    }
}